package gui;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;

public class HandleConfig {
	
	public static int autoDownload = 0;
	public static int loadOnDemand = 1;
	public static int debug_timings = 0;
	public static String searchParam= "at";

	static void readConfig() {
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
						size = Integer.parseInt(value.trim());
						Mainframe.schrift = new Font("Roboto", Font.PLAIN, size);
						System.out.println("fontSize: " + size);
					} else if (setting.equals("descFontSize")) {
						size = Integer.parseInt(value.trim());
						Mainframe.descSchrift = new Font("Roboto", Font.PLAIN, size);
						System.out.println("descFontSize: " + size);
					}  else if (setting.equals("autoDownload")) {
						autoDownload = Integer.parseInt(value.trim());
						System.out.println("autoDownload: " + autoDownload);
					}  else if (setting.equals("loadOnDemand")) {
						loadOnDemand = Integer.parseInt(value.trim());
						System.out.println("loadOnDemand: " + loadOnDemand);
					} else if (setting.equals("searchParam")) {
						searchParam = value.trim();
						System.out.println("searchParam: " + searchParam);
					} else if (setting.equals("debug_timings")) {
						debug_timings = Integer.parseInt(value.trim());
						System.out.println("debug_timings: " + debug_timings);
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Fehler in der config: Falsches Format - erwartet integer");
			}
		}else

	{
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
