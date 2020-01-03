package datenhaltung;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

	private static Connection con = null;

	public static Connection createConnection() {
		try {
			if (con == null || con.isClosed()) {
				con = DriverManager.getConnection("jdbc:derby:MyDB;create=true");
				createTable(con);
			}

		} catch (SQLException e) {
			System.err.println("Verbindung konnte nicht hergestellt werden");
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
		Statement createTable = null;
		try {
			createTable = con.createStatement();
			createTable.execute(
					"CREATE TABLE bücher (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, ausgeliehen VARCHAR(4), name VARCHAR(50),bemerkung VARCHAR(100),serie VARCHAR(50),pic blob,date timestamp, CONSTRAINT buecher_pk PRIMARY KEY (autor,titel))");
		} catch (SQLException e) {
			if ("X0Y32".equals(e.getSQLState())) {
				System.out.println("Tabelle existiert schon.");
			} else {
				printSQLException(e);
			}
		} finally {
			if (createTable != null) {
				// createTable.close();
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

	public static ResultSet readDB() {
		ResultSet rs = null;
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM bücher ORDER BY autor");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static void delete(String autor, String titel) {
		try {
			String sql = "DELETE FROM bücher WHERE autor = ? AND titel = ?";
			PreparedStatement st = con.prepareStatement(sql);
			st.setString(1, autor);
			st.setString(2, titel);
			st.executeUpdate();
			st.close();
			System.out.println("Datenbank Eintrag gelöscht: " + autor + "," + titel);
		} catch (

		SQLException ex) {
			System.err.println("Buch wurde nicht gelöscht");
		}
	}

	public static void add(String autor, String titel, String ausgeliehen, String name, String bemerkung, String serie, String datum) throws SQLException {
		String sql = "INSERT INTO bücher(autor,titel,ausgeliehen,name,bemerkung,serie,date) VALUES(?,?,?,?,?,?,?)";
		PreparedStatement st = con.prepareStatement(sql);
		st.setString(1, autor);
		st.setString(2, titel);
		st.setString(3, ausgeliehen);
		st.setString(4, name);
		st.setString(5, bemerkung);
		st.setString(6, serie);
		st.setString(7, datum);
		st.executeUpdate();
		st.close();
		System.out.println("Datenbank Eintrag erstellt: " + autor + "," + titel + "," + ausgeliehen + "," + name + "," + bemerkung + "," + serie + "," + datum);
	}
	
	public static void select(String autor, String titel) throws SQLException {
		String sql = "SELECT * FROM bücher WHERE autor=? and titel=?";
		PreparedStatement st = con.prepareStatement(sql);
		st.setString(1, autor);
		st.setString(2, titel);
		st.executeUpdate();
		st.close();
		System.out.println("Datenbank durchsucht: " + autor + "," + titel);
	}
	
	public static void addPic(String autor, String titel, InputStream photo) {
		String sql = "update bücher set pic=? where autor=? and titel=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setBinaryStream(1, photo);
			st.setString(2, autor);
			st.setString(3, titel);
			st.execute();
			st.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static boolean delPic(String autor, String titel) {
		String sql = "update bücher set pic=null where autor=? and titel=?";
		PreparedStatement st;
		try {
			st = con.prepareStatement(sql);
			st.setString(1, autor);
			st.setString(2, titel);
			st.execute();
			st.close();
			System.out.println("Bild gelöscht " + autor + " - " + titel);
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	
	public static void alterTableAdd(String spaltenName) throws SQLException {
		String sql = "ALTER TABLE bücher ADD ? VARCHAR(100)";
		PreparedStatement st = con.prepareStatement(sql);
		st.setString(1, spaltenName);
		st.execute();
		st.close();
	}

}
