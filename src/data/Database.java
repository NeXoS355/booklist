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

import org.apache.derby.tools.sysinfo;

import com.opencsv.CSVWriter;

import application.BookListModel;
import application.Book_Booklist;
import gui.Mainframe;

/**
 * Contains methods to handle Derby Database Connection and Queries
 */
public class Database {

	private static Connection con = null;
	public static int highestBid = 100000;

	/**
	 * Create Connection to Derby Database
	 * 
	 * @return - Connection object to derby Database
	 */
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

	/**
	 * Close Connection to Derby Database
	 * 
	 */
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

	/**
	 * checks Derby DB Tables and creates them if needed
	 * 
	 * @param con - Connection Object from "createConnection" Function
	 */
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
		// for stack traces, refer to derby.log or uncomment this:
		Mainframe.logger.error(e.getMessage());
	}

	/**
	 * reads whole table "bücher" from Database
	 * 
	 * @return - ResultSet with all entries from Table "bücher"
	 */
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

	/**
	 * reads only mandatory columns of table "bücher"
	 * 
	 * @return - ResultSet with only mandatory columns from Table "bücher"
	 */
	public static ResultSet readDbBooklistLite() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(
					"SELECT autor,titel, ebook,serie,seriePart,rating,bid FROM bücher ORDER BY autor, serie, seriePart");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads single DISTINCT column from table "bücher" to get e.g. different
	 * authors or series info
	 * 
	 * @param columnName - Array of column names to retrieve
	 * 
	 * @return - ResultSet with only mandatory columns from Table "bücher"
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
		str.append(" FROM bücher ORDER BY ");
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
		str.append(" FROM bücher WHERE rating <> 0 GROUP BY ");
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
	 * reads single DISTINCT column from table "bücher" to get e.g. different
	 * authors or series info
	 * 
	 * @param columnName  - name of the column to retrieve
	 * @param whereColumn - name of column to filter
	 * @param whereValue  - value to search in column "whereColumn"
	 * 
	 * @return - ResultSet with only mandatory columns from Table "bücher"
	 */
	public static ResultSet getColumnWithWhere(String columnName, String whereColumn, String whereValue) {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery(
					"SELECT DISTINCT " + columnName + " FROM bücher WHERE " + whereColumn + "='" + whereValue + "'");
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
			rs = st.executeQuery("SELECT COUNT(*)," + groupByColumn + " FROM bücher GROUP BY " + groupByColumn);
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return rs;
	}

	/**
	 * reads one specific book from table "bücher"
	 * 
	 * @param bid - Book ID of needed entry
	 * 
	 * @return - ResultSet with one entry from Table "bücher"
	 */
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

	/**
	 * exports all entries from booklist to .csv file (books.csv) in the same
	 * Directory
	 * 
	 * @return - success value
	 */
	public static boolean CSVExport() {
		boolean success = false;
		System.out.println("CSV Export");
		String fileName = "books.csv";
		try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
			// Spaltenüberschriften hinzufügen
			String[] header = { "Autor", "Titel", "ausgeliehen an", "ausgeliehen von", "Bemerkung", "Serie",
					"Serienteil", "E-Book", "ISBN", "Datum" };
			writer.writeNext(header);
			ArrayList<Book_Booklist> list = BookListModel.getBooks();
			int größe = list.size();
			// Bücher in die Tabelle einfügen
			for (int i = 0; i < größe; i++) {
				if (list.get(i).getDate() == null) {
					BookListModel.loadOnDemand(list.get(i));
				}
				String autor = list.get(i).getAuthor();
				String titel = list.get(i).getTitle();
				String ausgeliehen_an = list.get(i).getBorrowedTo();
				String ausgeliehen_von = list.get(i).getBorrowedTo();
				String bemerkung = list.get(i).getNote();
				String datum = list.get(i).getDate().toString();
				String serie = list.get(i).getSeries();
				String seriePart = list.get(i).getSeriesVol();
				String ebook = Boolean.toString(list.get(i).isEbook());
				String isbn = list.get(i).getIsbn();
				String[] data = { autor, titel, ausgeliehen_an, ausgeliehen_von, bemerkung, serie, seriePart, ebook,
						isbn, datum };
				writer.writeNext(data);
			}
			;
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
		return version + "   -   " + new SimpleDateFormat("dd.MM.yyyy").format(date);
	}
	
	/**
	 * read the current version of apache derby
	 * 
	 * @return - apache derby version String
	 */
	public static String readCurrentDBVersion() {
		String version = sysinfo.getVersionString();
		return version;
	}

	/**
	 * deletes one specific book from table "bücher"
	 * 
	 * @param bid - Book ID of entry
	 */
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
		String sql = "INSERT INTO bücher(autor,titel,ausgeliehen,name,bemerkung,serie,seriePart,ebook,date,bid) VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sql);
		highestBid++;
		int int_ebook = 0;
		if (ebook)
			int_ebook = 1;

		st.setString(1, author);
		st.setString(2, title);
		st.setString(3, borrowed);
		st.setString(4, name);
		st.setString(5, note);
		st.setString(6, series);
		st.setString(7, seriesVol);
		st.setInt(8, int_ebook);
		st.setString(9, date);
		st.setInt(10, highestBid);
		st.executeUpdate();
		st.close();

		Mainframe.logger
				.info("Booklist Datenbank Eintrag erstellt: " + author + "," + title + "," + borrowed + "," + name + ","
						+ note + "," + series + "," + seriesVol + "," + date + "," + int_ebook + "," + (highestBid));
		return highestBid;
	}

	/**
	 * updates a specified column with provided value and bid
	 * 
	 * @param bid     - book id
	 * @param colName - set value of this column
	 * @param value   - set column to this value
	 * 
	 */
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

	/**
	 * gets Information about all series of one specific author
	 * 
	 * @param author - name of author
	 * 
	 * @return ResultSet with series and volume info from one specific author
	 * 
	 */
	public static ResultSet getSeriesInfo(String author) {
		ResultSet rs = null;
		String sql = "SELECT serie, seriePart FROM bücher WHERE autor=? and serie!= '' ORDER BY serie";
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

	/**
	 * updates the "pic" column of specific Book entry
	 * 
	 * @param bid   - book id
	 * @param photo - photo data
	 * 
	 */
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

	/**
	 * empties the "pic" column of specific Book
	 * 
	 * @param bid - book id
	 * 
	 * @return success value
	 */
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

	/**
	 * gets the "pic" column of specific Book entry
	 * 
	 * @param bid - book id
	 * 
	 * @return ResultSet with pic value
	 */
	public static ResultSet getPic(int bid) {
		ResultSet rs = null;
		String sql = "SELECT pic FROM bücher WHERE bid=?";
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

	/**
	 * updates the "description" column of specific Book entry
	 * 
	 * @param bid  - book id
	 * @param desc - description String
	 * 
	 */
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

	/**
	 * empties the "description" column of specific Book
	 * 
	 * @param bid - book id
	 * 
	 * @return success value
	 */
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

	/**
	 * updates the "isbn" column of specific Book entry
	 * 
	 * @param bid  - book id
	 * @param isbn - isbn Number as String
	 * 
	 */
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

	/**
	 * empties the "isbn" column of specific Book
	 * 
	 * @param bid - book id
	 * 
	 * @return success value
	 */
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

	/**
	 * updates the "rating" column of specific Book entry
	 * 
	 * @param bid    - book id
	 * @param rating - rating Number
	 * 
	 */
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

	/**
	 * reads all columns of table "wishlist" from Database
	 * 
	 * @return - ResultSet with all columns from Table "wishlist"
	 */
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
	 */
	public static void addToWishlist(String author, String title, String note, String series, String seriesVol,
			String date) {
		try {
			String sql = "INSERT INTO wishlist(autor,titel,bemerkung,serie,seriePart,date) VALUES(?,?,?,?,?,?)";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, author);
			st.setString(2, title);
			st.setString(3, note);
			st.setString(4, series);
			st.setString(5, seriesVol);
			st.setString(6, date);
			st.executeUpdate();
			st.close();
			Mainframe.logger.info("Wishlist Datenbank Eintrag erstellt: " + author + "," + title + "," + note + ","
					+ series + "," + seriesVol + "," + date);
		} catch (SQLException e) {
			Mainframe.logger.error("Fehler beim speichern des Buchs (Wunschliste): " + author + "-" + title);
		}
	}

	/**
	 * deletes one specific book from table "wishlist"
	 * 
	 * @param author - name of author
	 * @param title  - title of the book
	 */
	public static void deleteFromWishlist(String author, String title) {
		try {
			String sql = "DELETE FROM wishlist WHERE autor = ? AND titel = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, author);
			st.setString(2, title);
			st.executeUpdate();
			st.close();
			Mainframe.logger.info("Wishlist Datenbank Eintrag gelöscht: " + author + "," + title);
		} catch (

		SQLException e) {
			Mainframe.logger.error("Fehler beim löschen des Buchs (Wunschliste): " + author + "-" + title);
		}
	}

}
