package data;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import gui.Mainframe;

/**
 * Handles automatic migration from the old Derby database (BooklistDB/) to the new SQLite database (booklist.db).
 * Reads data using old Derby column names and writes with new SQLite column names and converted types.
 */
public class DerbyMigrator {

	/**
	 * Checks if a Derby database exists and no SQLite database exists yet.
	 * If so, migrates all data from Derby to SQLite.
	 */
	public static void migrateIfNeeded() {
		File derbyDir = new File("BooklistDB");
		File sqliteFile = new File("booklist.db");

		if (!derbyDir.exists() || !derbyDir.isDirectory()) {
			return; // No Derby DB to migrate
		}
		if (sqliteFile.exists()) {
			return; // SQLite DB already exists
		}

		Mainframe.logger.info("Derby-Datenbank gefunden, starte Migration nach SQLite...");

		try {
			migrate();
			Mainframe.logger.info("Migration von Derby nach SQLite erfolgreich abgeschlossen");
		} catch (Exception e) {
			Mainframe.logger.error("Migration fehlgeschlagen: {}", e.getMessage());
			// Delete incomplete SQLite DB
			File incompleteDb = new File("booklist.db");
			if (incompleteDb.exists()) {
				incompleteDb.delete();
			}
			throw new RuntimeException("Derby-zu-SQLite Migration fehlgeschlagen. " +
					"BooklistDB/ wurde nicht verändert.", e);
		}
	}

	private static void migrate() throws Exception {
		// Load Derby driver
		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

		// Connect to Derby and upgrade schema to 3.1.6 if needed
		Connection derbyCon = DriverManager.getConnection("jdbc:derby:BooklistDB;upgrade=true");
		upgradeDerbySchema(derbyCon);

		// Create SQLite DB and tables
		Connection sqliteCon = DriverManager.getConnection("jdbc:sqlite:booklist.db");
		try (Statement st = sqliteCon.createStatement()) {
			st.execute("PRAGMA journal_mode=WAL");
			st.execute("PRAGMA foreign_keys=ON");
		}
		createSqliteTables(sqliteCon);

		// Migrate data
		migrateBooks(derbyCon, sqliteCon);
		migrateWishlist(derbyCon, sqliteCon);
		migrateVersions(derbyCon, sqliteCon);

		// Close connections
		sqliteCon.close();
		derbyCon.close();

		// Shutdown Derby
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			// Derby always throws an exception on shutdown (error code 50000, state XJ015)
			if (e.getErrorCode() != 50000 || !"XJ015".equals(e.getSQLState())) {
				Mainframe.logger.warn("Unerwarteter Derby-Shutdown-Fehler: {}", e.getMessage());
			}
		}
	}

	/**
	 * Runs the old Derby schema migrations (2.2.0 → 3.1.6) if needed.
	 */
	private static void upgradeDerbySchema(Connection con) throws SQLException {
		String version = readDerbyVersion(con);
		if ("3.1.6".equals(version)) {
			return; // Already up to date
		}

		Mainframe.logger.info("Derby-Schema auf Version {} gefunden, upgrade auf 3.1.6...", version);

		// Determine actual table name (bücher or books)
		String tableName = getDerbyTableName(con);

		switch (version) {
		case "2.2.0":
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD description clob (64 M)");
			executeIgnoreError(con, "UPDATE versions set version='2.4.0'");
		case "2.4.0":
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD isbn varchar(13)");
			executeIgnoreError(con, "UPDATE versions set version='2.4.4'");
		case "2.4.4":
			// Add bid column and assign sequential IDs
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD bid numeric(6,0) NOT NULL DEFAULT 0");
			assignBids(con, tableName);
			executeIgnoreError(con, "ALTER TABLE " + tableName + " DROP CONSTRAINT buecher_pk");
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD CONSTRAINT buecher_pk PRIMARY KEY (bid)");
			executeIgnoreError(con, "UPDATE versions set version='2.4.5'");
		case "2.4.5":
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD ebook NUMERIC(1,0)");
			// Migrate ebook data from bemerkung field
			migrateEbookFromBemerkung(con, tableName);
			executeIgnoreError(con, "UPDATE versions set version='2.5.0'");
		case "2.5.0":
			executeIgnoreError(con, "ALTER TABLE " + tableName + " ADD rating NUMERIC(2,0)");
			executeIgnoreError(con, "UPDATE versions set version='2.6.0'");
		case "2.6.0":
			// Rename table bücher to books if needed
			if (!"BOOKS".equalsIgnoreCase(tableName)) {
				executeIgnoreError(con, "RENAME TABLE " + tableName + " TO books");
			}
			executeIgnoreError(con, "UPDATE versions set version='3.1.6'");
		case "3.1.6":
			break;
		default:
			throw new SQLException("Unbekannte Derby-Version: " + version);
		}
	}

	private static String getDerbyTableName(Connection con) throws SQLException {
		DatabaseMetaData meta = con.getMetaData();
		try (ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"})) {
			while (rs.next()) {
				String name = rs.getString("TABLE_NAME");
				if ("BOOKS".equalsIgnoreCase(name) || name.toLowerCase().contains("cher")) {
					return name;
				}
			}
		}
		return "books"; // default fallback
	}

	private static void assignBids(Connection con, String tableName) throws SQLException {
		int bid = 100000;
		try (Statement st = con.createStatement();
			 ResultSet rs = st.executeQuery("SELECT autor, titel FROM " + tableName)) {
			while (rs.next()) {
				String autor = rs.getString("autor").trim();
				String titel = rs.getString("titel").trim();
				try (PreparedStatement pst = con.prepareStatement(
						"UPDATE " + tableName + " SET bid=? WHERE autor=? AND titel=?")) {
					pst.setInt(1, bid);
					pst.setString(2, autor);
					pst.setString(3, titel);
					pst.execute();
				}
				bid++;
			}
		}
	}

	private static void migrateEbookFromBemerkung(Connection con, String tableName) throws SQLException {
		try (Statement st = con.createStatement();
			 ResultSet rs = st.executeQuery("SELECT bid, bemerkung FROM " + tableName)) {
			while (rs.next()) {
				String bemerkung = rs.getString("bemerkung");
				if (bemerkung != null) {
					String normalized = bemerkung.trim().toLowerCase().replace("-", "").replace(" ", "").replace("_", "");
					if (normalized.contains("ebook")) {
						int bid = rs.getInt("bid");
						try (PreparedStatement pst = con.prepareStatement(
								"UPDATE " + tableName + " SET ebook=1 WHERE bid=?")) {
							pst.setInt(1, bid);
							pst.execute();
						}
					}
				}
			}
		}
	}

	private static void executeIgnoreError(Connection con, String sql) throws SQLException {
		try (PreparedStatement st = con.prepareStatement(sql)) {
			st.execute();
		} catch (SQLException e) {
			Mainframe.logger.warn("Derby migration SQL ignored error: {} - {}", sql, e.getMessage());
		}
	}

	private static String readDerbyVersion(Connection con) throws SQLException {
		try (Statement st = con.createStatement();
			 ResultSet rs = st.executeQuery("SELECT version FROM versions")) {
			if (rs.next()) {
				return rs.getString("version").trim();
			}
		}
		return "2.2.0"; // Oldest known version
	}

	private static void createSqliteTables(Connection con) throws SQLException {
		try (Statement st = con.createStatement()) {
			st.execute("CREATE TABLE IF NOT EXISTS books ("
					+ "bid INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "author TEXT NOT NULL, "
					+ "title TEXT NOT NULL, "
					+ "borrow_status TEXT DEFAULT 'none', "
					+ "borrower TEXT DEFAULT '', "
					+ "note TEXT DEFAULT '', "
					+ "series TEXT DEFAULT '', "
					+ "series_vol INTEGER, "
					+ "ebook INTEGER DEFAULT 0, "
					+ "rating REAL DEFAULT 0.0, "
					+ "pic BLOB, "
					+ "description TEXT, "
					+ "isbn TEXT, "
					+ "added_date TEXT)");
			st.execute("CREATE TABLE IF NOT EXISTS wishlist ("
					+ "wid INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ "author TEXT NOT NULL, "
					+ "title TEXT NOT NULL, "
					+ "note TEXT DEFAULT '', "
					+ "series TEXT DEFAULT '', "
					+ "series_vol INTEGER, "
					+ "added_date TEXT)");
			st.execute("CREATE TABLE IF NOT EXISTS versions ("
					+ "version TEXT NOT NULL, "
					+ "added_date TEXT NOT NULL)");
		}
	}

	private static void migrateBooks(Connection derbyCon, Connection sqliteCon) throws SQLException {
		String insertSql = "INSERT INTO books(bid, author, title, borrow_status, borrower, note, series, series_vol, ebook, rating, pic, description, isbn, added_date) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		int count = 0;
		try (Statement st = derbyCon.createStatement();
			 ResultSet rs = st.executeQuery("SELECT * FROM books ORDER BY bid");
			 PreparedStatement pst = sqliteCon.prepareStatement(insertSql)) {

			sqliteCon.setAutoCommit(false);

			while (rs.next()) {
				// bid - keep original value
				int bid = Integer.parseInt(rs.getString("bid"));
				pst.setInt(1, bid);

				// author, title - trim Derby padding
				pst.setString(2, trimSafe(rs.getString("autor")));
				pst.setString(3, trimSafe(rs.getString("titel")));

				// borrow_status - convert German to English values
				String ausgeliehen = rs.getString("ausgeliehen");
				pst.setString(4, convertBorrowStatus(ausgeliehen));

				// borrower - trim
				pst.setString(5, trimSafe(rs.getString("name")));

				// note
				String bemerkung = rs.getString("bemerkung");
				pst.setString(6, bemerkung != null ? bemerkung : "");

				// series - trim
				pst.setString(7, trimSafe(rs.getString("serie")));

				// series_vol - convert String to Integer
				String seriePart = rs.getString("seriePart");
				if (seriePart != null && !seriePart.trim().isEmpty()) {
					try {
						pst.setInt(8, Integer.parseInt(seriePart.trim()));
					} catch (NumberFormatException e) {
						pst.setNull(8, java.sql.Types.INTEGER);
					}
				} else {
					pst.setNull(8, java.sql.Types.INTEGER);
				}

				// ebook - NULL becomes 0
				int ebook = rs.getInt("ebook"); // getInt returns 0 for NULL
				pst.setInt(9, ebook);

				// rating - convert int (0-10) to double (0.0-5.0)
				int derbyRating = rs.getInt("rating");
				pst.setDouble(10, (double) derbyRating / 2);

				// pic - BLOB as bytes
				byte[] pic = rs.getBytes("pic");
				if (pic != null) {
					pst.setBytes(11, pic);
				} else {
					pst.setNull(11, java.sql.Types.BLOB);
				}

				// description
				String desc = rs.getString("description");
				pst.setString(12, desc);

				// isbn
				pst.setString(13, rs.getString("isbn"));

				// added_date - Timestamp.toString() for format compatibility
				Timestamp date = rs.getTimestamp("date");
				if (date != null) {
					pst.setString(14, date.toString());
				} else {
					pst.setNull(14, java.sql.Types.VARCHAR);
				}

				pst.executeUpdate();
				count++;
			}

			sqliteCon.commit();
			sqliteCon.setAutoCommit(true);
		}

		Mainframe.logger.info("{} Bücher von Derby nach SQLite migriert", count);
	}

	private static void migrateWishlist(Connection derbyCon, Connection sqliteCon) throws SQLException {
		String insertSql = "INSERT INTO wishlist(author, title, note, series, series_vol, added_date) VALUES(?,?,?,?,?,?)";

		int count = 0;
		try (Statement st = derbyCon.createStatement();
			 ResultSet rs = st.executeQuery("SELECT * FROM wishlist ORDER BY autor");
			 PreparedStatement pst = sqliteCon.prepareStatement(insertSql)) {

			sqliteCon.setAutoCommit(false);

			while (rs.next()) {
				pst.setString(1, trimSafe(rs.getString("autor")));
				pst.setString(2, trimSafe(rs.getString("titel")));

				String bemerkung = rs.getString("bemerkung");
				pst.setString(3, bemerkung != null ? bemerkung.trim() : "");

				pst.setString(4, trimSafe(rs.getString("serie")));

				String seriePart = rs.getString("seriePart");
				if (seriePart != null && !seriePart.trim().isEmpty()) {
					try {
						pst.setInt(5, Integer.parseInt(seriePart.trim()));
					} catch (NumberFormatException e) {
						pst.setNull(5, java.sql.Types.INTEGER);
					}
				} else {
					pst.setNull(5, java.sql.Types.INTEGER);
				}

				Timestamp date = rs.getTimestamp("date");
				if (date != null) {
					pst.setString(6, date.toString());
				} else {
					pst.setNull(6, java.sql.Types.VARCHAR);
				}

				pst.executeUpdate();
				count++;
			}

			sqliteCon.commit();
			sqliteCon.setAutoCommit(true);
		}

		Mainframe.logger.info("{} Wunschlisten-Einträge von Derby nach SQLite migriert", count);
	}

	private static void migrateVersions(Connection derbyCon, Connection sqliteCon) throws SQLException {
		Timestamp now = new Timestamp(System.currentTimeMillis());
		try (PreparedStatement pst = sqliteCon.prepareStatement(
				"INSERT INTO versions(version, added_date) VALUES(?,?)")) {
			pst.setString(1, "4.0.0");
			pst.setString(2, now.toString());
			pst.executeUpdate();
		}
	}

	private static String convertBorrowStatus(String derbyValue) {
		if (derbyValue == null) return "none";
		return switch (derbyValue.trim()) {
			case "an" -> "lent";
			case "von" -> "borrowed";
			default -> "none";
		};
	}

	private static String trimSafe(String value) {
		return value != null ? value.trim() : "";
	}

}
