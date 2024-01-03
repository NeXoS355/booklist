package data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

public class Updater {

	public static void checkUpdate(Connection con) {

		String currentVersion = checkVersion(con);
		switch (currentVersion) {
		case "2.2.0":
			try {
				String sql = "ALTER TABLE bücher ADD description clob (64 M)";
				PreparedStatement st;
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.0'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.0 aktualisiert!");
		case "2.4.0":
			try {
				String sql = "ALTER TABLE bücher ADD isbn varchar(13)";
				PreparedStatement st;
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				sql = "UPDATE versions set version='2.4.4'";
				st = con.prepareStatement(sql);
				st.execute();
				st.close();
				JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.4 aktualisiert!");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Fehler bei der Datenbank Aktualisierung!");
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		case "2.4.4":
			int success = 0;
			String sql = "";
			PreparedStatement st;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
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
				JOptionPane.showMessageDialog(null, "Aufgrund eines Fehlers wurde die version nicht erhöht");

			JOptionPane.showMessageDialog(null, "Datenbank auf Version 2.4.5 aktualisiert!");
			String version_new = checkVersion(con);

			if (!version_new.equals("2.4.5")) {
				JOptionPane.showMessageDialog(null, "Datenbank nicht aktuell! Bitte Prozess wiederholen!");
				System.exit(1);
			}
		case "2.4.5":
			// all good
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version;
	}

}
