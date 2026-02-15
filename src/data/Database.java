package data;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.opencsv.CSVWriter;

import application.BookListModel;
import application.Book_Booklist;
import gui.Mainframe;

/**
 * Contains methods to handle SQLite Database Connection and Queries
 */
public class Database {

	private static Connection con = null;

	/**
	 * Create Connection to SQLite Database
	 */
	public static void createConnection() {
		try {
			if (con == null || con.isClosed()) {
				con = DriverManager.getConnection("jdbc:sqlite:booklist.db");
				try (Statement st = con.createStatement()) {
					st.execute("PRAGMA journal_mode=WAL");
					st.execute("PRAGMA foreign_keys=ON");
					st.execute("PRAGMA busy_timeout=5000");
				}
				createTable(con);
			}
			DBUpdater.checkUpdate(con);
		} catch (SQLException e) {
			System.err.println("Verbindung konnte nicht hergestellt werden");
			Mainframe.logger.error(e.getMessage());
		}
	}

	/**
	 * Close Connection to SQLite Database
	 */
	public static void closeConnection() {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
				Mainframe.logger.info("Database connection closed");
			}
		} catch (SQLException e) {
			Mainframe.logger.error("Error closing database connection: {}", e.getMessage());
		}
	}

	/**
	 * checks DB Tables and creates them if needed
	 *
	 * @param con - Connection Object from "createConnection" Function
	 */
	public static void createTable(Connection con) {
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

			// Insert initial version if versions table is empty
			try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM versions")) {
				if (rs.next() && rs.getInt(1) == 0) {
					Timestamp datum = new Timestamp(System.currentTimeMillis());
					try (PreparedStatement pst = con.prepareStatement(
							"INSERT INTO versions (version, added_date) VALUES (?, ?)")) {
						pst.setString(1, "4.0.0");
						pst.setString(2, datum.toString());
						pst.execute();
					}
					Mainframe.logger.info("Datenbanken erstellt");
				}
			}
		} catch (SQLException e) {
			printSQLException(e);
		}
	}

	/**
	 * Closes a ResultSet and its parent Statement to prevent resource leaks.
	 *
	 * @param rs - ResultSet to close (may be null)
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				Statement st = rs.getStatement();
				rs.close();
				if (st != null) st.close();
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			}
		}
	}

	/**
	 * prints SQL Exception to console and logger
	 *
	 * @param e - Exception handler
	 */
	private static void printSQLException(SQLException e) {
		System.err.println("\n----- SQLException -----");
		System.err.println("  SQL State:  " + e.getSQLState());
		System.err.println("  Error Code: " + e.getErrorCode());
		System.err.println("  Message:    " + e.getMessage());
		Mainframe.logger.error(e.getMessage());
	}

	/**
	 * reads whole table "books" from Database
	 *
	 * @return - ResultSet with all entries from Table "books"
	 */
	public static ResultSet readDbBooklist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM books ORDER BY author, series, series_vol");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads only mandatory columns of table "books"
	 *
	 * @return - ResultSet with only mandatory columns from Table "books"
	 */
	public static ResultSet readDbBooklistLite() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(
					"SELECT author,title,ebook,series,series_vol,rating,bid,added_date FROM books ORDER BY author, series, series_vol");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads single DISTINCT column from table "books" to get e.g. different
	 * authors or series info
	 *
	 * @param columnName - Array of column names to retrieve
	 *
	 * @return - ResultSet with only mandatory columns from Table "books"
	 */
	public static ResultSet getColumnsFromBooklist(String[] columnName) {
		ResultSet rs = null;
		StringBuilder str = new StringBuilder();

		str.append("SELECT DISTINCT ");
		for (int i = 0; i < columnName.length; i++) {
			str.append(columnName[i]);
			if (i != columnName.length-1)
				str.append(",");
		}
		str.append(" FROM books ORDER BY ");
		for (int i = 0; i < columnName.length; i++) {
			str.append(columnName[i]);
			if (i != columnName.length-1)
				str.append(",");
		}

		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(str.toString());
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * calculates the Median Rating  with specific group by Clause
	 *
	 * @param columnName - name of the column to retrieve
	 *
	 * @return - ResultSet with Average Rating over specific Group
	 */
	public static ResultSet getAvgRating(String[] columnName) {
		ResultSet rs = null;
		StringBuilder str = new StringBuilder();

		str.append("SELECT AVG(rating),");
		for (int i = 0; i < columnName.length; i++) {
			str.append(columnName[i]);
			if (i != columnName.length - 1)
				str.append(",");
		}
		str.append(" FROM books WHERE rating <> 0 GROUP BY ");
		for (int i = 0; i < columnName.length; i++) {
			str.append(columnName[i]);
			if (i != columnName.length - 1)
				str.append(",");
		}


		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(str.toString());
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads single DISTINCT column from table "books" to get e.g. different
	 * authors or series info
	 *
	 * @param columnName  - name of the column to retrieve
	 * @param whereColumn - name of column to filter
	 * @param whereValue  - value to search in column "whereColumn"
	 *
	 * @return - ResultSet with only mandatory columns from Table "books"
	 */
	public static ResultSet getColumnWithWhere(String columnName, String whereColumn, String whereValue) {
		ResultSet rs = null;
		try {
			PreparedStatement st = con.prepareStatement(
					"SELECT DISTINCT " + columnName + " FROM books WHERE " + whereColumn + "=?");
			st.setString(1, whereValue);
			rs = st.executeQuery();
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * counts columns with GROUP BY Clause
	 *
	 * @param groupByColumn - name of column to group
	 *
	 * @return - Returns ResultSet of Query with GrouBy Clause. [1] Count, [2] groupByColumn
	 */
	public static ResultSet getColumnCountsWithGroup(String groupByColumn) {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT COUNT(*)," + groupByColumn + " FROM books GROUP BY " + groupByColumn);
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads one specific book from table "books"
	 *
	 * @param bid - Book ID of needed entry
	 *
	 * @return - ResultSet with one entry from Table "books"
	 */
	public static ResultSet selectFromBooklist(int bid) {
		String sql = "SELECT * FROM books WHERE bid=" + bid;
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(sql);
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * exports all entries from booklist to .csv file (books.csv) in the same
	 * Directory
	 *
	 * @return - success value
	 */
	public static boolean CSVExport() {
		boolean success = false;
		Mainframe.logger.info("CSV Export");
		String fileName = "books.csv";
		try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
			// Spaltenüberschriften hinzufügen
			String[] header = { "Autor", "Titel", "ausgeliehen an", "ausgeliehen von", "Bemerkung", "Serie",
					"Serienteil", "E-Book", "ISBN", "Datum" };
			writer.writeNext(header);
			ArrayList<Book_Booklist> list = Mainframe.allEntries.getBooks();
            // bücher in die Tabelle einfügen
            for (Book_Booklist bookBooklist : list) {
                if (bookBooklist.getDate() == null) {
                    BookListModel.loadOnDemand(bookBooklist);
                }
                String autor = bookBooklist.getAuthor();
                String titel = bookBooklist.getTitle();
                String ausgeliehen_an = bookBooklist.getBorrowedTo();
                String ausgeliehen_von = bookBooklist.getBorrowedFrom();
                String bemerkung = bookBooklist.getNote();
                String datum = bookBooklist.getDate().toString();
                String serie = bookBooklist.getSeries();
                String seriePart = bookBooklist.getSeriesVol();
                String ebook = Boolean.toString(bookBooklist.isEbook());
                String isbn = bookBooklist.getIsbn();
                String[] data = {autor, titel, ausgeliehen_an, ausgeliehen_von, bemerkung, serie, seriePart, ebook,
                        isbn, datum};
                writer.writeNext(data);
            }
			success = true;

		} catch (IOException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return success;
	}

	/**
	 * read the current DB Layout from table "versions"
	 *
	 * @return - DB version String with last modified date
	 */
	public static String readCurrentLayoutVersion() {
		ResultSet rs = null;
		String version = "";
		String sql = "SELECT * FROM versions";
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				version = rs.getString("version").trim();
			}
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen der DB Version");
		} finally {
			closeResultSet(rs);
		}
		return version;
	}

	/**
	 * read the current version of SQLite
	 *
	 * @return - SQLite version String
	 */
	public static String readCurrentDBVersion() {
		String version = "";
		try (Statement st = con.createStatement();
			 ResultSet rs = st.executeQuery("SELECT sqlite_version()")) {
			if (rs.next()) {
				version = rs.getString(1);
			}
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen der SQLite Version");
		}
		return version;
	}

	/**
	 * deletes one specific book from table "books"
	 *
	 * @param bid - Book ID of entry
	 */
	public static void deleteFromBooklist(int bid) {
		try {
			String sql = "DELETE FROM books WHERE bid = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.executeUpdate();
			st.close();
			Mainframe.logger.info("Booklist Datenbank Eintrag geloescht - {}", bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Buchs (Booklist)");
		}
	}

	/**
	 * saves a new entry to booklist
	 *
	 * @param author    - full name of the Author
	 * @param title     - title of the Book
	 * @param borrowed  - set if borrowed From or To
	 * @param name      - set to name of which one borrowed a book from or to
	 * @param note      - free String input about Book or Author
	 * @param series    - series of book
	 * @param seriesVol - set which volume is the book in the specified series
	 * @param ebook     - boolean variable if Book is an ebook
	 * @param date      - date when the entry was added
	 *
	 * @return assigned bid of the newly added book
	 */
	public static int addToBooklist(String author, String title, String borrowed, String name, String note,
			String series, String seriesVol, boolean ebook, String date) throws SQLException {
		String sql = "INSERT INTO books(author,title,borrow_status,borrower,note,series,series_vol,ebook,added_date) VALUES(?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		int int_ebook = ebook ? 1 : 0;

		st.setString(1, author);
		st.setString(2, title);
		st.setString(3, borrowed);
		st.setString(4, name);
		st.setString(5, note);
		st.setString(6, series);
		if (seriesVol != null && !seriesVol.trim().isEmpty()) {
			try {
				st.setInt(7, Integer.parseInt(seriesVol.trim()));
			} catch (NumberFormatException e) {
				st.setNull(7, java.sql.Types.INTEGER);
			}
		} else {
			st.setNull(7, java.sql.Types.INTEGER);
		}
		st.setInt(8, int_ebook);
		st.setString(9, date);
		st.executeUpdate();

		int generatedBid;
		try (ResultSet keys = st.getGeneratedKeys()) {
			keys.next();
			generatedBid = keys.getInt(1);
		}
		st.close();

		Mainframe.logger
				.info("Booklist Datenbank Eintrag erstellt: {},{},{},{},{},{},{},{},{}", author, title, borrowed, name, note,series,seriesVol,date,int_ebook);
		return generatedBid;
	}

	/**
	 * updates a specified column with provided value and bid
	 *
	 * @param bid     - book id
	 * @param colName - set value of this column
	 * @param value   - set column to this value
	 */
	public static void updateBooklistEntry(int bid, String colName, String value) {
		String sql = "update books set " + colName + "=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, value);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Table updated - {}-{}={}", bid, colName, value);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim aktualisieren des Buchs: {}-{}={}",bid, colName, value);
		}
	}

	/**
	 * updates the "pic" column of specific Book entry
	 *
	 * @param bid   - book id
	 * @param photo - photo data
	 */
	public static void updatePic(int bid, InputStream photo) {
		String sql = "update books set pic=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setBytes(1, photo.readAllBytes());
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Cover gespeichert: {}", bid);
		} catch (SQLException | IOException e) {
			Mainframe.logger.error("Fehler beim speichern des Covers: {}", bid);
		}

	}

	/**
	 * empties the "pic" column of specific Book
	 *
	 * @param bid - book id
	 *
	 * @return success value
	 */
	public static boolean delPic(int bid) {
		String sql = "update books set pic=null where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Cover geloescht: {}", bid);
			return true;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Covers: {}", bid);
			return false;
		}

	}

	/**
	 * updates the "description" column of specific Book entry
	 *
	 * @param bid  - book id
	 * @param desc - description String
	 */
	public static void updateDesc(int bid, String desc) {
		String sql = "update books set description=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, desc);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Description gespeichert: {}", bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern der Beschreibung: {}", bid);
		}

	}

	/**
	 * empties the "description" column of specific Book
	 *
	 * @param bid - book id
	 *
	 * @return success value
	 */
	public static boolean delDesc(int bid) {
		String sql = "update books set description=null where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Description geloescht: {}", bid);
			return true;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen der Beschreibung: {}", bid);
			return false;
		}

	}

	/**
	 * updates the "isbn" column of specific Book entry
	 *
	 * @param bid  - book id
	 * @param isbn - isbn Number as String
	 */
	public static void updateIsbn(int bid, String isbn) {
		String sql = "update books set isbn=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, isbn);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("ISBN gespeichert: {}", bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern der ISBN: {}", bid);
		}

	}

	/**
	 * updates the "added_date" column of specific Book entry
	 *
	 * @param bid  - book id
	 * @param date - new Date as String
	 */
	public static void updateDate(int bid, String date) {
		String sql = "update books set added_date=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, date);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Neues Datum gespeichert: {}", bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des neuen Datums: {}", bid);
		}

	}

	/**
	 * updates the "rating" column of specific Book entry
	 *
	 * @param bid    - book id
	 * @param rating - rating as double (0.0 - 5.0)
	 */
	public static void updateRating(int bid, double rating) {
		String sql = "update books set rating=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setDouble(1, rating);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Rating gespeichert: {}", bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Ratings: {}", bid);
		}

	}

	/**
	 * reads all columns of table "wishlist" from Database
	 *
	 * @return - ResultSet with all columns from Table "wishlist"
	 */
	public static ResultSet readDbWishlist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM wishlist ORDER BY author");
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen der Wunschliste");
		}
		return rs;
	}

	/**
	 * saves a new entry to wishlist
	 *
	 * @param author    - full name of the Author
	 * @param title     - title of the Book
	 * @param note      - free String input about Book or Author
	 * @param series    - series of book
	 * @param seriesVol - set which volume is the book in the specified series
	 * @param date      - date when the entry was added
	 *
	 * @return assigned wid of the newly added wishlist entry
	 */
	public static int addToWishlist(String author, String title, String note, String series, String seriesVol,
			String date) {
		try {
			String sql = "INSERT INTO wishlist(author,title,note,series,series_vol,added_date) VALUES(?,?,?,?,?,?)";
			PreparedStatement st = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			st.setString(1, author);
			st.setString(2, title);
			st.setString(3, note);
			st.setString(4, series);
			if (seriesVol != null && !seriesVol.trim().isEmpty()) {
				try {
					st.setInt(5, Integer.parseInt(seriesVol.trim()));
				} catch (NumberFormatException e) {
					st.setNull(5, java.sql.Types.INTEGER);
				}
			} else {
				st.setNull(5, java.sql.Types.INTEGER);
			}
			st.setString(6, date);
			st.executeUpdate();

			int generatedWid;
			try (ResultSet keys = st.getGeneratedKeys()) {
				keys.next();
				generatedWid = keys.getInt(1);
			}
			st.close();
			Mainframe.logger.info("Wishlist Datenbank Eintrag erstellt: {},{},{},{},{},{}", author ,title, note,series, seriesVol, date);
			return generatedWid;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Buchs (Wunschliste): {}-{}",author,title);
			return -1;
		}
	}

	/**
	 * deletes one specific book from table "wishlist"
	 *
	 * @param wid - wishlist entry id
	 */
	public static void deleteFromWishlist(int wid) {
		try {
			String sql = "DELETE FROM wishlist WHERE wid = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, wid);
			st.executeUpdate();
			st.close();
			Mainframe.logger.info("Wishlist Datenbank Eintrag geloescht: {}", wid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Buchs (Wunschliste): {}", wid);
		}
	}

}
