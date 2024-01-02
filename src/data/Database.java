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
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("Verbindung konnte nicht hergestellt werden. Class not found");
			e.printStackTrace();
		}
		return con;
	}

	public static void closeConnection() {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException se) {
			if (((se.getErrorCode() == 50000) && ("XJ015".equals(se.getSQLState())))) {
				System.out.println("Derby shut down normally");
			} else {
				System.err.println("Derby did not shut down normally");
				printSQLException(se);
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
					"CREATE TABLE bücher (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, ausgeliehen VARCHAR(4), name VARCHAR(50),bemerkung VARCHAR(100),serie VARCHAR(50),seriePart VARCHAR(2),pic blob,description clob (64 M), isbn varchar(13),date timestamp,bid numeric(6,0) NOT NULL, CONSTRAINT buecher_pk PRIMARY KEY (bid))");
			createWishlist = con.createStatement();
			createWishlist.execute(
					"CREATE TABLE wishlist (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, bemerkung VARCHAR(100),serie VARCHAR(50),seriePart VARCHAR(2), date timestamp, CONSTRAINT wishlist_pk PRIMARY KEY (autor,titel))");
			createVersions = con.createStatement();
			createVersions.execute("CREATE TABLE versions (version VARCHAR(10) NOT NULL, date timestamp NOT NULL)");
			String sql = "INSERT INTO versions (version ,date) VALUES ('2.4.5','" + datum + "')";
			pst = con.prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			if ("X0Y32".equals(e.getSQLState())) {
				System.out.println("Tabelle existiert schon.");
//				e.printStackTrace();
			} else {
				printSQLException(e);
			}
		} finally {
			if (createBooklist != null && createWishlist != null) {
				try {
					createBooklist.close();
					createWishlist.close();
					createVersions.close();
					System.out.println("DB closed");
				} catch (SQLException e) {
					e.printStackTrace();
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
		e.printStackTrace(System.err);
	}

	public static ResultSet readDbBooklist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM bücher ORDER BY autor");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static ResultSet readDbBooklistLite() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(
					"SELECT autor,titel,ausgeliehen,name, bemerkung,serie,seriePart,date,isbn,bid FROM bücher ORDER BY autor");
		} catch (SQLException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return returnValue;
	}

	public static ResultSet readDbWishlist() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM wishlist ORDER BY autor");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
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
			System.out.println("Fehler beim auslesen der DB Version");
			e.printStackTrace();
		}
		return "DB Layout Version:" + version + "   -   " + new SimpleDateFormat("dd.MM.yyyy").format(date);
	}

	public static void deleteFromBooklist(String autor, String titel) {
		try {
			String sql = "DELETE FROM bücher WHERE autor = ? AND titel = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, autor);
			st.setString(2, titel);
			st.executeUpdate();
			st.close();
			System.out.println("Booklist Datenbank Eintrag gelöscht: " + autor + "," + titel);
		} catch (

		SQLException ex) {
			System.err.println("Buch wurde nicht gelöscht");
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

		SQLException ex) {
			System.err.println("Buch wurde nicht gelöscht");
		}
	}

	public static int addToBooklist(String autor, String titel, String ausgeliehen, String name, String bemerkung,
			String serie, String seriePart, String datum) throws SQLException {
		String sql = "INSERT INTO bücher(autor,titel,ausgeliehen,name,bemerkung,serie,seriePart,date,bid) VALUES(?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sql);
		highestBid++;
		st.setString(1, autor);
		st.setString(2, titel);
		st.setString(3, ausgeliehen);
		st.setString(4, name);
		st.setString(5, bemerkung);
		st.setString(6, serie);
		st.setString(7, seriePart);
		st.setString(8, datum);
		st.setInt(9, highestBid);
		st.executeUpdate();
		st.close();

		System.out.println("Booklist Datenbank Eintrag erstellt: " + autor + "," + titel + "," + ausgeliehen + ","
				+ name + "," + bemerkung + "," + serie + "," + seriePart + "," + datum + "," + (highestBid - 1));
		return highestBid;
	}

	public static void addToWishlist(String autor, String titel, String bemerkung, String serie, String seriePart,
			String datum) throws SQLException {
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
		System.out.println("Wishlist Datenbank Eintrag erstellt: " + autor + "," + titel + "," + bemerkung + "," + serie
				+ "," + seriePart + "," + datum);
	}

	public static void addPic(int bid, InputStream photo) {
		String sql = "update bücher set pic=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setBinaryStream(1, photo);
			st.setInt(2, bid);
			st.execute();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (SQLException e) {
			System.out.println("Fehler beim auslesen des Bildes");
			e.printStackTrace();
		}
		return rs;
	}

	public static void addDesc(int bid, String desc) {
		String sql = "update bücher set description=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, desc);
			st.setInt(2, bid);
			st.execute();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public static void addIsbn(int bid, String isbn) {
		String sql = "update bücher set isbn=? where bid=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, isbn);
			st.setInt(2, bid);
			st.execute();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

}
