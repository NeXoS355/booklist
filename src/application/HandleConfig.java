package application;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;

import javax.swing.JOptionPane;

import gui.Mainframe;

public class HandleConfig {

	public static int autoDownload = 0;
	public static int loadOnDemand = 1;
	public static String debug = "TRACE";
	public static String searchParam = "at";
	public static int backup = 2;
	public static String apiToken = generateRandomToken(64);
	public static String apiURL = "";

	public static void readConfig() {
		File f = new File("config.conf");
		if (f.exists() && !f.isDirectory()) {
			try (BufferedReader br = new BufferedReader(new FileReader("config.conf"))) {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				String everything = sb.toString();
				String[] settings = everything.split("\n");
				String value = "";
				String setting = "";
				int size = 14;

				for (int i = 0; i < settings.length; i++) {
					setting = settings[i].split("=")[0];
					value = settings[i].split("=")[1];

					if (setting.equals("fontSize")) {
						try {
							size = Integer.parseInt(value.trim());
							Mainframe.defaultFont = new Font("Roboto", Font.PLAIN, size);
							Mainframe.logger.info("fontSize: " + size);
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (fontSize): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("descFontSize")) {
						try {
							size = Integer.parseInt(value.trim());
							Mainframe.descFont = new Font("Roboto", Font.PLAIN, size);
							Mainframe.logger.info("descFontSize: " + size);
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (descFontSize): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("autoDownload")) {
						try {
							int tmp = Integer.parseInt(value.trim());
							if (tmp >= 0 && tmp < 2) {
								autoDownload = tmp;
								Mainframe.logger.info("autoDownload: " + autoDownload);
							} else
								JOptionPane.showMessageDialog(null,
										"Fehler in der config (autoDownload): Falscher Wert - erwartet 1 oder 0");

						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (autoDownload): Falsches Format - erwartet integer");
						}

					} else if (setting.equals("loadOnDemand")) {
						try {
							int tmp = Integer.parseInt(value.trim());
							if (tmp >= 0 && tmp < 2) {
								loadOnDemand = tmp;
								Mainframe.logger.info("loadOnDemand: " + loadOnDemand);
							} else
								JOptionPane.showMessageDialog(null,
										"Fehler in der config (loadOnDemand): Falscher Wert - erwartet 0 oder 1");

						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (loadOnDemand): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("useDB")) {
						boolean tmp = true;
						if (value.trim().toLowerCase().equals("true"))
							tmp = true;
						else if (value.trim().toLowerCase().equals("false"))
							tmp = false;
						else
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (useDB): Falscher Wert - erwartet true oder false");
						BookListModel.useDB = tmp;
						Mainframe.logger.info("useDB: " + BookListModel.useDB);

					} else if (setting.equals("searchParam")) {
						String tmp = value.trim();
						if (tmp.equals("a") || tmp.equals("at")) {
							searchParam = tmp;
							Mainframe.logger.info("searchParam: " + searchParam);
						} else
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (searchParam): Falscher Wert - erwartet 't' oder 'at'");

					} else if (setting.equals("debug")) {
						String tmp = value.trim();
						if (tmp.equals("WARN") || tmp.equals("INFO") || tmp.equals("TRACE")) {
							debug = tmp;
							Mainframe.logger.info("debug: " + debug);
						} else
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (debug): Falscher Wert - erwartet WARN, INFO oder TRACE");
					} else if (setting.equals("backup")) {
						try {
							int tmp = Integer.parseInt(value.trim());
							if (tmp >= 0 && tmp <= 2)
								backup = tmp;
							else
								JOptionPane.showMessageDialog(null,
										"Fehler in der config (backup): Falscher Wert - erwartet 0,1 oder 2");
							Mainframe.logger.info("backup: " + backup);
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (backup): Falscher Wert - Integer erwartet");
							Mainframe.logger.info("ERROR backup NumberFormatException");
						}

					} else if (setting.equals("apiToken")) {
						if (value.length() > 60)
							apiToken = value.trim();
						else
							apiToken = generateRandomToken(64);
					} else if (setting.equals("apiURL")) {
						if (value.length() > 10)
							apiURL = value.trim();
					} else if (setting.equals("layoutWidth")) {
						String[] values = value.trim().split(",");
						for (int j = 0; j < values.length; j++) {
							Mainframe.prozEbook = Integer.parseInt(values[0]);
							Mainframe.prozAuthor = Integer.parseInt(values[1]);
							Mainframe.prozTitle = Integer.parseInt(values[2]);
							Mainframe.prozSeries = Integer.parseInt(values[3]);
							Mainframe.prozRating = Integer.parseInt(values[4]);
						}
					} else if (setting.equals("layoutSort")) {
						String[] values = value.trim().split(",");
						for (int j = 0; j < values.length; j++) {
							SimpleTableModel.columnNames[0] = (values[0]);
							SimpleTableModel.columnNames[1] = (values[1]);
							SimpleTableModel.columnNames[2] = (values[2]);
							SimpleTableModel.columnNames[3] = (values[3]);
							SimpleTableModel.columnNames[4] = (values[4]);
						}
					}

				}
			} catch (FileNotFoundException e) {
				Mainframe.logger.error(e.getMessage());
			} catch (IOException e) {
				Mainframe.logger.error(e.getMessage());
			}
		} else {
			try (PrintWriter out = new PrintWriter("config.conf")) {
				out.println("fontSize=" + Mainframe.defaultFont.getSize());
				out.println("descFontSize=" + Mainframe.descFont.getSize());
				out.println("autoDownload=" + autoDownload);
				out.println("loadOnDemand=" + loadOnDemand);
				out.println("useDB=" + BookListModel.useDB);
				out.println("searchParam=" + searchParam);
				out.println("debug=" + debug);
				out.println("backup=" + backup);
				String token = generateRandomToken(64);
				apiToken = token;
				out.println("apiToken=" + token);
				out.println("apiURL=" + apiURL);

			} catch (FileNotFoundException e) {
				Mainframe.logger.error(e.getMessage());
			}
		}

	}

	// Method to generate a random token with 64 characters
	public static String generateRandomToken(int length) {

		final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		SecureRandom random = new SecureRandom();
		StringBuilder token = new StringBuilder(length);

		// Generiere das Token aus der Zeichenliste
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(CHARACTERS.length());
			token.append(CHARACTERS.charAt(index));
		}

		return token.toString();
	}

}
