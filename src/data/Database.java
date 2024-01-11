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
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.opencsv.CSVWriter;

import application.BookListModel;
import application.Book_Booklist;
import gui.Mainframe;

public class Database {

	private static Connection con = null;
	public static int highestBid = 100000;

	public static Connection createConnection() {
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		try {
			if (con == null || con.isClosed()) {
				Class.forName(driver);
				con = DriverManager.getConnection("jdbc:derby:BooklistDB;create=true;upgrade=true");
				createTable(con);
			}
			Updater.checkUpdate(con);
		} catch (SQLException e) {
			System.err.println("Verbindung konnte nicht hergestellt werden");
			Mainframe.logger.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Verbindung konnte nicht hergestellt werden. Class not found");
			Mainframe.logger.error(e.getMessage());
		}
		return con;

	}

	public static void closeConnection() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (((e.getErrorCode() == 50000) && ("XJ015".equals(e.getSQLState())))) {
				Mainframe.logger.info("Derby shut down normally");
			} else {
				System.err.println("Derby did not shut down normally");
				Mainframe.logger.error("Derby did not shut down normally");
				printSQLException(e);
			}
		}

	}

	public static void createTable(Connection con) {
		Statement createBooklist = null;
		Statement createWishlist = null;
		Statement createVersions = null;
		PreparedStatement pst;
		Timestamp datum = new Timestamp(System.currentTimeMillis());
		try {
			createBooklist = con.createStatement();
			createBooklist.execute(
					"CREATE TABLE bücher (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, ausgeliehen VARCHAR(4), name VARCHAR(50),bemerkung VARCHAR(100),serie VARCHAR(50),seriePart VARCHAR(2),ebook NUMERIC(1,0),rating NUMERIC(2,0),pic blob,description clob (64 M), isbn varchar(13),date timestamp,bid NUMERIC(6,0) NOT NULL, CONSTRAINT buecher_pk PRIMARY KEY (bid))");
			createWishlist = con.createStatement();
			createWishlist.execute(
					"CREATE TABLE wishlist (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, bemerkung VARCHAR(100),serie VARCHAR(50),seriePart VARCHAR(2), date timestamp, CONSTRAINT wishlist_pk PRIMARY KEY (autor,titel))");
			createVersions = con.createStatement();
			createVersions.execute("CREATE TABLE versions (version VARCHAR(10) NOT NULL, date timestamp NOT NULL)");
			String sql = "INSERT INTO versions (version ,date) VALUES ('2.6.0','" + datum + "')";
			pst = con.prepareStatement(sql);
			pst.execute();
			Mainframe.logger.info("Datenbanken erstellt");
			pst.close();
		} catch (SQLException e) {
			if ("X0Y32".equals(e.getSQLState())) {
				Mainframe.logger.info("Datenbank existiert bereits");
			} else {
				printSQLException(e);
			}
		} finally {
			if (createBooklist != null && createWishlist != null) {
				try {
					createBooklist.close();
					createWishlist.close();
					createVersions.close();
					Mainframe.logger.info("DB closed");
				} catch (SQLException e) {
					Mainframe.logger.error(e.getMessage());
				}
			}
		}
	}

	private static void printSQLException(SQLException e) {
		System.err.println("\n----- SQLException -----");
		System.err.println("  SQL State:  " + e.getSQLState());
		System.err.println("  Error Code: " + e.getErrorCode());
		System.err.println("  Message:    " + e.getMessage());
		// for stack traces, refer to derby.log or uncomment this:
		Mainframe.logger.error(e.getMessage());
	}

	public static ResultSet readDbBooklist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM bücher ORDER BY autor, serie, seriePart");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	public static ResultSet readDbBooklistLite() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(
					"SELECT autor,titel, bemerkung,serie,seriePart,bid FROM bücher ORDER BY autor, serie, seriePart");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	public static ResultSet selectFromBooklist(int bid) {
		String sql = "SELECT * FROM bücher WHERE bid=" + bid;
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(sql);
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	public static int CSVExport() {
		int returnValue = 2;
		System.out.println("CSV Export");
		String fileName = "books.csv";
		try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
			// Spaltenüberschriften hinzufügen
			String[] header = { "Autor", "Titel", "Datum", "Serie", "Serienteil", "Bemerkung" };
			writer.writeNext(header);
			ArrayList<Book_Booklist> list = BookListModel.getBücher();
			int größe = list.size();
			// Bücher in die Tabelle einfügen
			for (int i = 0; i < größe; i++) {
				String autor = list.get(i).getAutor();
				String titel = list.get(i).getTitel();
				Timestamp datum = list.get(i).getDatum();
				String serie = list.get(i).getSerie();
				String seriePart = list.get(i).getSeriePart();
				String bemerkung = list.get(i).getBemerkung();
				String[] data = { autor, titel, datum.toString(), serie, seriePart, bemerkung };
				writer.writeNext(data);
			}
			;
			returnValue = 1;

		} catch (IOException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return returnValue;
	}

	public static String readCurrentLayoutVersion() {
		ResultSet rs = null;
		String version = "";
		Timestamp date = null;
		String sql = "SELECT * FROM versions";
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				version = rs.getString("version").trim();
				date = rs.getTimestamp("date");
			}
			st.close();
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen der DB Version");
		}
		return "DB Layout Version:" + version + "   -   " + new SimpleDateFormat("dd.MM.yyyy").format(date);
	}

	public static void deleteFromBooklist(int bid) {
		try {
			String sql = "DELETE FROM bücher WHERE bid = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.executeUpdate();
			st.close();
			System.out.println("Booklist Datenbank Eintrag gelöscht - " + bid);
			Mainframe.logger.info("Booklist Datenbank Eintrag gelöscht - " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Buchs (Booklist)");
		}
	}

	public static int addToBooklist(String autor, String titel, String ausgeliehen, String name, String bemerkung,
			String serie, String seriePart, boolean ebook, String datum) throws SQLException {
		String sql = "INSERT INTO bücher(autor,titel,ausgeliehen,name,bemerkung,serie,seriePart,ebook,date,bid) VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sql);
		highestBid++;
		int int_ebook = 0;
		if (ebook)
			int_ebook = 1;

		st.setString(1, autor);
		st.setString(2, titel);
		st.setString(3, ausgeliehen);
		st.setString(4, name);
		st.setString(5, bemerkung);
		st.setString(6, serie);
		st.setString(7, seriePart);
		st.setInt(8, int_ebook);
		st.setString(9, datum);
		st.setInt(10, highestBid);
		st.executeUpdate();
		st.close();

		Mainframe.logger.info("Booklist Datenbank Eintrag erstellt: " + autor + "," + titel + "," + ausgeliehen + ","
				+ name + "," + bemerkung + "," + serie + "," + seriePart + "," + datum + "," + int_ebook + ","
				+ (highestBid));
		return highestBid;
	}

	public static void updateBooklistEntry(int bid, String colName, String value) {
		String sql = "update bücher set " + colName + "=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, value);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Table updated - " + bid + " - " + colName + "=" + value);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim aktualisieren des Buchs: " + bid + " - " + colName + "=" + value);
		}
	}
	
	public static ResultSet analyzeAuthor(String author) {
		ResultSet rs = null;
		String sql = "SELECT serie, seriePart FROM bücher WHERE autor=? ORDER BY serie";
		try {
			PreparedStatement pst = con.prepareStatement(sql);
			pst.setString(1, author);
			rs = pst.executeQuery();
			Mainframe.logger.info("Autor analysiert:" + author);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim analysieren: " + author);
		}
		return rs;
	}

	public static void updatePic(int bid, InputStream photo) {
		String sql = "update bücher set pic=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setBinaryStream(1, photo);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Cover gespeichert: " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Covers: " + bid);
		}

	}

	public static boolean delPic(int bid) {
		String sql = "update bücher set pic=null where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Cover gelöscht: " + bid);
			return true;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Covers: " + bid);
			return false;
		}

	}

	public static ResultSet getPic(int bid) {
		ResultSet rs = null;
		String sql = "SELECT * FROM bücher WHERE bid=?";
		try {
			PreparedStatement pst = con.prepareStatement(sql);
			pst.setInt(1, bid);
			rs = pst.executeQuery();
			Mainframe.logger.info("Cover ausgelesen: " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen des Covers: " + bid);
		}
		return rs;
	}

	public static void updateDesc(int bid, String desc) {
		String sql = "update bücher set description=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, desc);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Description gespeichert: " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern der Beschreibung: " + bid);
		}

	}

	public static boolean delDesc(int bid) {
		String sql = "update bücher set description=null where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Description gelöscht: " + bid);
			return true;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen der Beschreibung: " + bid);
			return false;
		}

	}

	public static void updateIsbn(int bid, String isbn) {
		String sql = "update bücher set isbn=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, isbn);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("ISBN gespeichert: " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern der ISBN: " + bid);
		}

	}

	public static boolean delIsbn(int bid) {
		String sql = "update bücher set isbn='' where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("ISBN gelöscht: " + bid);
			return true;
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim löschen der ISBN: " + bid);
			return false;
		}

	}
	
	public static void updateRating(int bid, int rating) {
		String sql = "update bücher set rating=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setInt(1, rating);
			st.setInt(2, bid);
			st.execute();
			st.close();
			Mainframe.logger.info("Rating gespeichert: " + bid);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Ratings: " + bid);
		}

	}

	public static ResultSet readDbWishlist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM wishlist ORDER BY autor");
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim auslesen der Wunschliste");
		}
		return rs;
	}

	public static void addToWishlist(String autor, String titel, String bemerkung, String serie, String seriePart,
			String datum) {
		try {
			String sql = "INSERT INTO wishlist(autor,titel,bemerkung,serie,seriePart,date) VALUES(?,?,?,?,?,?)";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, autor);
			st.setString(2, titel);
			st.setString(3, bemerkung);
			st.setString(4, serie);
			st.setString(5, seriePart);
			st.setString(6, datum);
			st.executeUpdate();
			st.close();
			System.out.println("Wishlist Datenbank Eintrag erstellt: " + autor + "," + titel + "," + bemerkung + ","
					+ serie + "," + seriePart + "," + datum);
			Mainframe.logger.info("Wishlist Datenbank Eintrag erstellt: " + autor + "," + titel + "," + bemerkung + ","
					+ serie + "," + seriePart + "," + datum);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Buchs (Wunschliste): " + autor + "-" + titel);
		}
	}

	public static void deleteFromWishlist(String autor, String titel) {
		try {
			String sql = "DELETE FROM wishlist WHERE autor = ? AND titel = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, autor);
			st.setString(2, titel);
			st.executeUpdate();
			st.close();
			System.out.println("Wishlist Datenbank Eintrag gelöscht: " + autor + "," + titel);
		} catch (

		SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Buchs (Wunschliste): " + autor + "-" + titel);
		}
	}

}
