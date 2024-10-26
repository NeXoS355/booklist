package data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import gui.Mainframe;

/**
 * checks the connected Database version and updates if necessary
 *
 */
public class DBUpdater {

	/**
	 * checks current Version of Booklist Table and runs the update if necessary
	 *
	 * @param con - Connection to Database
	 */
	public static void checkUpdate(Connection con) {

		String sql = "";
		PreparedStatement st;
		String currentVersion = checkVersion(con);
		String version_new = "";

		switch (currentVersion) {
		case "2.2.0":
			try {
				sql = "ALTER TABLE bücher ADD description clob (64 M)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.0'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
			} catch (SQLException e) {
				Mainframe.logger.error("Fehler bei Aktualisierung auf 2.4.0");
			}
			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.0 aktualisiert!");
		case "2.4.0":
			try {
				sql = "ALTER TABLE bücher ADD isbn varchar(13)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.4'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.4 aktualisiert!");
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		case "2.4.4":
			int success = 0;
			try {
				sql = "ALTER TABLE bücher ADD bid numeric(6,0) NOT NULL DEFAULT 0";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				int bid = 100000;
				ResultSet rs = null;
				rs = Database.readDbBooklist();
				while (rs.next()) {
					String autor = rs.getString("autor").trim();
					String titel = rs.getString("titel").trim();
					sql = "UPDATE bücher set bid= ? WHERE autor = ? AND titel = ?";
					st = con.prepareStatement(sql);
					st.setInt(1, bid);
					st.setString(2, autor);
					st.setString(3, titel);
					st.execute();
					st.close();
					bid++;
					System.out.println("BID set " + autor + " - " + titel + ", " + bid);
				}
				rs.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			try {
				sql = "ALTER TABLE bücher DROP CONSTRAINT buecher_pk";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			try {
				sql = "ALTER TABLE bücher ADD CONSTRAINT buecher_pk PRIMARY KEY (bid)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}

			if (success == 3) {
				try {
					sql = "UPDATE versions set version='2.4.5'";
					st = con.prepareStatement(sql);
					st.execute();
					st.close();
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			} else
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die version nicht erhoeht");

			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.5 aktualisiert!");
			version_new = checkVersion(con);

			if (!version_new.equals("2.4.5")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "2.4.5":
			success = 0;
			try {
				sql = "ALTER TABLE bücher ADD ebook NUMERIC(1,0)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}

			if (success == 1) {
				try {
					sql = "UPDATE versions set version='2.5.0'";
					st = con.prepareStatement(sql);
					st.execute();
					st.close();
					JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.5.0 aktualisiert!");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			} else
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die Version nicht erhoeht");

			version_new = checkVersion(con);
			if (!version_new.equals("2.5.0")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}

			// Start Migration of E-Book Data
			ResultSet rs = null;
			rs = Database.readDbBooklist();

			try {
				int counter = 0;
				String bemerkung = "";
				while (rs.next()) {
					bemerkung = rs.getString("bemerkung").trim();
					int bid = Integer.parseInt(rs.getString("bid"));

					bemerkung = bemerkung.toLowerCase().replace("-", "").replace(" ", "").replace("_", "");

					if (bemerkung.contains("ebook")) {
						Database.updateBooklistEntry(bid, "ebook", "1");
						counter++;
					}
				}
				rs.close();
				JOptionPane.showMessageDialog(null, counter + " Buecher wurden als E-Book markiert");
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Migration der Bücher ist fehlgeschlagen!");
			}

		case "2.5.0":
			success = 0;
			try {
				sql = "ALTER TABLE bücher ADD rating NUMERIC(2,0)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}

			if (success == 1) {
				try {
					sql = "UPDATE versions set version='2.6.0'";
					st = con.prepareStatement(sql);
					st.execute();
					st.close();
					JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.6.0 aktualisiert!");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			} else
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die Version nicht erhoeht");

			version_new = checkVersion(con);
			if (!version_new.equals("2.6.0")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "2.6.0":
			success = 0;

			try {
				sql = "RENAME TABLE bücher TO books";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			
			DatabaseMetaData metaDataForDatabaseConnection;
			try {
				metaDataForDatabaseConnection = con.getMetaData();
			    ResultSet resultSetForTableNames = metaDataForDatabaseConnection.getTables(null, null, null, new String[]{"TABLE"});


			    while (resultSetForTableNames.next()) {
			    	if(resultSetForTableNames.getString(3).equals("BOOKS"))
			    		success++;
			        
			    }
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
				success = 0;
			}
			System.out.println(success);
			if (success == 2) {
				try {

				    
					sql = "UPDATE versions set version='3.1.6'";
					st = con.prepareStatement(sql);
					st.execute();
					st.close();
					JOptionPane.showMessageDialog(null, "Datenbank auf Version 3.1.6 aktualisiert!");
				} catch (SQLException e) {
					JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			} else
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die Version nicht erhoeht");

			version_new = checkVersion(con);
			if (!version_new.equals("3.1.6")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "3.1.6":
			// all good
		}

	}

	/**
	 * reads version in Table "versions"
	 *
	 * @param con - Connection to Database
	 */
	public static String checkVersion(Connection con) {
		Statement st = null;
		ResultSet rs = null;
		String version = "";
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT version from versions");
			while (rs.next()) {
				version = rs.getString("version").trim();

			}
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "version konnte nicht aus der Datenbank ausgelesen werden!");
		}
		return version;
	}

}
