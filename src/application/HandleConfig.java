package application;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

import gui.Mainframe;

public class HandleConfig {

	public static int autoDownload = 0;
	public static int loadOnDemand = 1;
	public static String debug = "TRACE";
	public static String searchParam = "at";

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
						if (value.trim().toLowerCase().equals("true") ) 
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
					} else if (setting.contains("layoutWidth")) {
						String[] values = value.trim().split(",");
						for (int j = 0; j < values.length; j++) {
							Mainframe.prozEbook = Integer.parseInt(values[0]);
							Mainframe.prozAuthor = Integer.parseInt(values[1]);
							Mainframe.prozTitle = Integer.parseInt(values[2]);
							Mainframe.prozSeries = Integer.parseInt(values[3]);
							Mainframe.prozRating = Integer.parseInt(values[4]);
						}
					} else if (setting.contains("layoutSort")) {
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
		} else

		{
			try (PrintWriter out = new PrintWriter("config.conf")) {
				out.println("fontSize=" + Mainframe.defaultFont.getSize());
				out.println("descFontSize=" + Mainframe.descFont.getSize());
				out.println("autoDownload=" + autoDownload);
				out.println("loadOnDemand=" + loadOnDemand);
				out.println("searchParam=" + searchParam);
				out.println("debug=" + debug);
			} catch (FileNotFoundException e) {
				Mainframe.logger.error(e.getMessage());
			}
		}

	}

}
