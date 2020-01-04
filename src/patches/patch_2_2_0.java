package patches;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.JOptionPane;

import data.Database;

//Patch from 2.0.2 -> 2.1.0
public class patch_2_2_0 {
	
	private static Connection con = null;

	public static void main(String[] args) {
		try {
			con = createConnection();
			Database.createTable(con);
			String sql = "CREATE TABLE wishlist (autor VARCHAR(50) NOT NULL, titel VARCHAR(50) NOT NULL, ausgeliehen VARCHAR(4), name VARCHAR(50),bemerkung VARCHAR(100),serie VARCHAR(50),seriePart VARCHAR(2), pic blob,date timestamp, CONSTRAINT wishlist_pk PRIMARY KEY (autor,titel))";
			PreparedStatement st;
			st = con.prepareStatement(sql);
			st.execute();
			st.close();
			closeConnection();
			JOptionPane.showMessageDialog(null, "Datenbank erfolgreich aktualisiert!");
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Fehler!");
		}
	}
	
	
	public static Connection createConnection() {
		try {
			if (con == null || con.isClosed()) {
				con = DriverManager.getConnection("jdbc:derby:MyDB;create=true");
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
	
	private static void printSQLException(SQLException e) {
		System.err.println("\n----- SQLException -----");
		System.err.println("  SQL State:  " + e.getSQLState());
		System.err.println("  Error Code: " + e.getErrorCode());
		System.err.println("  Message:    " + e.getMessage());
		// for stack traces, refer to derby.log or uncomment this:
		e.printStackTrace(System.err);
	}

}


