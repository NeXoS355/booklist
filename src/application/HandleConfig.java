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
import javax.swing.table.TableColumnModel;

import gui.Mainframe;

import static gui.Mainframe.logger;

public class HandleConfig {

    public static String lang = "GERMAN";
	public static int autoDownload = 0;
	public static int loadOnDemand = 1;
	public static String debug = "WARN";
	public static String searchParam = "at";
	public static int backup = 0;
	public static String apiToken = generateRandomToken(64);
	public static String apiURL = "";
	public static int darkmode = 0;
    public static int tmpDarkmode = 0;

	/**
	 * Constructor 
	 * reads the config.conf file and sets the found Parameters
	 */
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
				String value;
				String setting;
				int size;

                for (String s : settings) {
                    String[] row = s.split("=");
                    setting = row[0];
                    value = row[1];
                    if (row.length == 2) {
                        switch (setting) {
                            case "lang" -> {
                                lang = value.trim();
                                logger.info("lang: {}",lang);
                            }
                            case "fontSize" -> {
                                try {
                                    size = Integer.parseInt(value.trim());
                                    Mainframe.defaultFont = new Font("Roboto", Font.PLAIN, size);
                                    logger.info("fontSize: {}", size);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (fontSize): Falsches Format - erwartet integer");
                                }
                            }
                            case "descFontSize" -> {
                                try {
                                    size = Integer.parseInt(value.trim());
                                    Mainframe.descFont = new Font("Roboto", Font.PLAIN, size);
                                    logger.info("descFontSize: {}", size);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (descFontSize): Falsches Format - erwartet integer");
                                }
                            }
                            case "autoDownload" -> {
                                try {
                                    int tmp = Integer.parseInt(value.trim());
                                    if (tmp >= 0 && tmp < 2) {
                                        autoDownload = tmp;
                                        logger.info("autoDownload: {}", autoDownload);
                                    } else
                                        JOptionPane.showMessageDialog(null,
                                                "Fehler in der config (autoDownload): Falscher Wert - erwartet 1 oder 0");

                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (autoDownload): Falsches Format - erwartet integer");
                                }
                            }
                            case "loadOnDemand" -> {
                                try {
                                    int tmp = Integer.parseInt(value.trim());
                                    if (tmp >= 0 && tmp < 2) {
                                        loadOnDemand = tmp;
                                        logger.info("loadOnDemand: {}", loadOnDemand);
                                    } else
                                        JOptionPane.showMessageDialog(null,
                                                "Fehler in der config (loadOnDemand): Falscher Wert - erwartet 0 oder 1");

                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (loadOnDemand): Falsches Format - erwartet integer");
                                }
                            }
                            case "useDB" -> {
                                boolean tmp = true;
                                if (value.trim().equalsIgnoreCase("false"))
                                    tmp = false;
                                else
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (useDB): Falscher Wert - erwartet true oder false");
                                BookListModel.useDB = tmp;
                                logger.info("useDB: {}", BookListModel.useDB);

                            }
                            case "searchParam" -> {
                                String tmp = value.trim();
                                if (tmp.equals("a") || tmp.equals("at")) {
                                    searchParam = tmp;
                                    logger.info("searchParam: {}", searchParam);
                                } else
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (searchParam): Falscher Wert - erwartet 't' oder 'at'");

                            }
                            case "debug" -> {
                                String tmp = value.trim();
                                if (tmp.equals("WARN") || tmp.equals("INFO") || tmp.equals("info")) {
                                    debug = tmp;
                                    logger.info("debug: {}", debug);
                                } else
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (debug): Falscher Wert - erwartet WARN, INFO oder info");
                            }
                            case "backup" -> {
                                try {
                                    int tmp = Integer.parseInt(value.trim());
                                    if (tmp >= 0 && tmp <= 2)
                                        backup = tmp;
                                    else
                                        JOptionPane.showMessageDialog(null,
                                                "Fehler in der config (backup): Falscher Wert - erwartet 0,1 oder 2");
                                    logger.info("backup: {}", backup);
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (backup): Falscher Wert - Integer erwartet");
                                    logger.info("ERROR backup NumberFormatException");
                                }
                            }
                            case "apiToken" -> {
                                if (value.length() > 60)
                                    apiToken = value.trim();
                                else
                                    apiToken = generateRandomToken(64);
                            }
                            case "apiURL" -> {
                                if (value.length() > 10)
                                    apiURL = value.trim();
                            }
                            case "darkmode" -> {
                                try {
                                    int tmp = Integer.parseInt(value.trim());
                                    if (tmp >= 0 && tmp < 2) {
                                        darkmode = tmp;
                                        tmpDarkmode = tmp;
                                        logger.info("darkmode: {}", darkmode);
                                    } else
                                        JOptionPane.showMessageDialog(null,
                                                "Fehler in der config (darkmode): Falscher Wert - erwartet 1 oder 0");

                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (darkmode): Falsches Format - erwartet integer");
                                }
                            }
                            case "layoutWidth" -> {
                                String[] values = value.trim().split(",");
                                for (int j = 0; j < values.length; j++) {
                                    Mainframe.prozEbook = Integer.parseInt(values[0]);
                                    Mainframe.prozAuthor = Integer.parseInt(values[1]);
                                    Mainframe.prozTitle = Integer.parseInt(values[2]);
                                    Mainframe.prozSeries = Integer.parseInt(values[3]);
                                    Mainframe.prozRating = Integer.parseInt(values[4]);
                                }
                            }
                            case "layoutSort" -> {
                                String[] values = value.trim().split(",");
                                System.arraycopy(values, 0, SimpleTableModel.columnNames, 0, values.length);
                            }
                            case "MainframeX" -> {
                                try {
                                    Mainframe.startX = Integer.parseInt(value.trim());
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (MainframeX): Falsches Format - erwartet integer");
                                }
                            }
                            case "MainframeY" -> {
                                try {
                                    Mainframe.startY = Integer.parseInt(value.trim());
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (MainframeY): Falsches Format - erwartet integer");
                                }
                            }
                            case "MainframeWidth" -> {
                                try {
                                    Mainframe.defaultFrameWidth = Integer.parseInt(value.trim());
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (MainframeWidth): Falsches Format - erwartet integer");
                                }
                            }
                            case "MainframeHeight" -> {
                                try {
                                    Mainframe.defaultFrameHeight = Integer.parseInt(value.trim());
                                } catch (NumberFormatException e) {
                                    JOptionPane.showMessageDialog(null,
                                            "Fehler in der config (MainframeHeight): Falsches Format - erwartet integer");
                                }
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null,
                                "Fehler in der config - Falsches Format. ");
                    }
                }
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		} else {
			Mainframe.executor.submit(() -> {
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
					out.println("darkmode=" + darkmode);

				} catch (FileNotFoundException e) {
					logger.error(e.getMessage());
				}
			});
		}

	}

    public static void writeSettings() {
        logger.info("Save Settings to File");

        try (PrintWriter out = new PrintWriter("config.conf")) {
            out.println("lang=" + HandleConfig.lang);
            out.println("fontSize=" + Mainframe.defaultFont.getSize());
            out.println("descFontSize=" + Mainframe.descFont.getSize());
            out.println("autoDownload=" + autoDownload);
            out.println("loadOnDemand=" + loadOnDemand);
            out.println("searchParam=" + searchParam);
            out.println("debug=" + debug);
            out.println("backup=" + backup);
            out.println("apiToken=" + apiToken);
            out.println("apiURL=" + apiURL);
            out.println("darkmode=" + tmpDarkmode);

            TableColumnModel columnModel = Mainframe.table.getColumnModel();

            String strWidth = "layoutWidth=" +
                    columnModel.getColumn(0).getWidth() +
                    "," +
                    columnModel.getColumn(1).getWidth() +
                    "," +
                    columnModel.getColumn(2).getWidth() +
                    "," +
                    columnModel.getColumn(3).getWidth() +
                    "," +
                    columnModel.getColumn(4).getWidth();

            out.println(strWidth);

            String strColumnTitle = "layoutSort=" +
                    columnModel.getColumn(0).getHeaderValue() +
                    "," +
                    columnModel.getColumn(1).getHeaderValue() +
                    "," +
                    columnModel.getColumn(2).getHeaderValue() +
                    "," +
                    columnModel.getColumn(3).getHeaderValue() +
                    "," +
                    columnModel.getColumn(4).getHeaderValue();
            out.println(strColumnTitle);

            out.println("MainframeX=" + Mainframe.getInstance().getX());
            out.println("MainframeY=" + Mainframe.getInstance().getY());
            out.println("MainframeWidth=" + Mainframe.getInstance().getWidth());
            out.println("MainframeHeigth=" + Mainframe.getInstance().getHeight());
            Mainframe.showNotification("Einstellungen gespeichert");
        } catch (FileNotFoundException e1) {
            logger.error("Fehler beim speichern der Einstellungen");
            logger.error(e1.getMessage());
        }
    }

	/**
	 * Method to generate a random token with 64 characters
	 */
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
