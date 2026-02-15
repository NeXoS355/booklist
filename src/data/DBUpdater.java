package data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import gui.Mainframe;

/**
 * checks the connected SQLite Database version and updates if necessary
 */
public class DBUpdater {

	/**
	 * checks current Version of Booklist Table and runs the update if necessary
	 *
	 * @param con - Connection to Database
	 */
	public static void checkUpdate(Connection con) {
		String currentVersion = checkVersion(con);
		switch (currentVersion) {
		case "4.0.0":
			// current version, all good
			break;
		default:
			Mainframe.logger.warn("Unbekannte DB Version: {}", currentVersion);
			break;
		}
	}

	/**
	 * reads version in Table "versions"
	 *
	 * @param con - Connection to Database
	 */
	public static String checkVersion(Connection con) {
		ResultSet rs = null;
		String version = "";
		try {
			Statement st = con.createStatement();
			rs = st.executeQuery("SELECT version from versions");
			while (rs.next()) {
				version = rs.getString("version").trim();
			}
		} catch (SQLException e) {
			Mainframe.logger.error("Version konnte nicht aus der Datenbank ausgelesen werden!");
		} finally {
			Database.closeResultSet(rs);
		}
		return version;
	}

}
