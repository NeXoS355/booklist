package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

import gui.Mainframe;

public class Updater {

	public static void checkUpdate(Connection con) {

		String sql = "";
		PreparedStatement st;
		String currentVersion = checkVersion(con);
		String version_new = "";

		switch (currentVersion) {
		case "2.2.0":
			try {
				sql = "ALTER TABLE b�cher ADD description clob (64 M)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.0'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
				Mainframe.logger.error("Fehler bei Aktualisierung auf 2.4.0");
			}
			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.0 aktualisiert!");
		case "2.4.0":
			try {
				sql = "ALTER TABLE b�cher ADD isbn varchar(13)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.4'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.4 aktualisiert!");
			} catch (SQLException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		case "2.4.4":
			int success = 0;
			try {
				sql = "ALTER TABLE b�cher ADD bid numeric(6,0) NOT NULL DEFAULT 0";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				int bid = 100000;
				ResultSet rs = null;
				rs = Database.readDbBooklist();
				while (rs.next()) {
					String autor = rs.getString("autor").trim();
					String titel = rs.getString("titel").trim();
					sql = "UPDATE b�cher set bid= ? WHERE autor = ? AND titel = ?";
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
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			try {
				sql = "ALTER TABLE b�cher DROP CONSTRAINT buecher_pk";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
			try {
				sql = "ALTER TABLE b�cher ADD CONSTRAINT buecher_pk PRIMARY KEY (bid)";
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
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die version nicht erh�ht");

			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.5 aktualisiert!");
			version_new = checkVersion(con);

			if (!version_new.equals("2.4.5")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "2.4.5":
			success = 0;
			try {
				sql = "ALTER TABLE b�cher ADD ebook NUMERIC(1,0)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				e.printStackTrace();
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
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die Version nicht erh�ht");

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
				JOptionPane.showMessageDialog(null, counter + " B�cher wurden als E-Book markiert");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		case "2.5.0":
			success = 0;
			try {
				sql = "ALTER TABLE b�cher ADD rating NUMERIC(2,0)";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				success++;
			} catch (SQLException e) {
				e.printStackTrace();
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
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die Version nicht erh�ht");

			version_new = checkVersion(con);
			if (!version_new.equals("2.6.0")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "2.6.0":
			//all good
		}

	}

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
			e.printStackTrace();
		}
		return version;
	}

}
