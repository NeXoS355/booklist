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
	public static int debug_timings = 0;
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
							Mainframe.schrift = new Font("Roboto", Font.PLAIN, size);
							System.out.println("fontSize: " + size);
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (fontSize): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("descFontSize")) {
						try {
							size = Integer.parseInt(value.trim());
							Mainframe.descSchrift = new Font("Roboto", Font.PLAIN, size);
							System.out.println("descFontSize: " + size);
						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (descFontSize): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("autoDownload")) {
						try {
							int tmp = Integer.parseInt(value.trim());
							if (tmp >= 0 && tmp < 2) {
								autoDownload = tmp;
								System.out.println("autoDownload: " + autoDownload);
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
								System.out.println("loadOnDemand: " + loadOnDemand);
							} else 
								JOptionPane.showMessageDialog(null,
										"Fehler in der config (loadOnDemand): Falscher Wert - erwartet 1 oder 0");
							

						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (loadOnDemand): Falsches Format - erwartet integer");
						}
					} else if (setting.equals("searchParam")) {
						String tmp = value.trim();
						if (tmp.equals("a") || tmp.equals("at")) {
							searchParam = tmp;
							System.out.println("searchParam: " + searchParam);
						} else 
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (searchParam): Falscher Wert - erwartet 't' oder 'at'");
						

					} else if (setting.equals("debug_timings")) {
						
						try {
							int tmp = Integer.parseInt(value.trim());
							if (tmp >= 0 && tmp < 2) {
								debug_timings = tmp;
								System.out.println("debug_timings: " + debug_timings);
							} else 
								JOptionPane.showMessageDialog(null,
										"Fehler in der config (debug_timings): Falscher Wert - erwartet 1 oder 0");
							

						} catch (NumberFormatException e) {
							JOptionPane.showMessageDialog(null,
									"Fehler in der config (debug_timings): Falsches Format - erwartet integer");
						}
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (PrintWriter out = new PrintWriter("config.conf")) {
				out.println("fontSize=" + Mainframe.schrift.getSize());
				out.println("descFontSize=" + Mainframe.descSchrift.getSize());
				out.println("autoDownload=" + autoDownload);
				out.println("loadOnDemand=" + loadOnDemand);
				out.println("searchParam=" + searchParam);
				out.println("debug_timings=" + debug_timings);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

}
