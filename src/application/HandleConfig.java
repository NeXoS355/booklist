package application;

import java.awt.Font;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Set;

import javax.swing.table.TableColumnModel;

import com.formdev.flatlaf.util.UIScale;
import gui.Mainframe;

import static gui.Mainframe.logger;

public class HandleConfig {

  private static final String CONFIG_FILE = "config.conf";

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

  public static void readConfig() {
    File f = new File(CONFIG_FILE);
    if (f.exists() && !f.isDirectory()) {
      Properties props = new Properties();
      try (FileReader reader = new FileReader(f)) {
        props.load(reader);
      } catch (IOException e) {
        logger.error(e.getMessage());
        return;
      }

      lang = props.getProperty("lang", lang);
      logger.info("lang: {}", lang);

      Mainframe.defaultFont = new Font("Roboto", Font.PLAIN, getInt(props, "fontSize", Mainframe.defaultFont.getSize()));
      logger.info("fontSize: {}", Mainframe.defaultFont.getSize());

      Mainframe.descFont = new Font("Roboto", Font.PLAIN, getInt(props, "descFontSize", Mainframe.descFont.getSize()));
      logger.info("descFontSize: {}", Mainframe.descFont.getSize());

      autoDownload = getInt(props, "autoDownload", autoDownload, 0, 1);
      logger.info("autoDownload: {}", autoDownload);

      loadOnDemand = getInt(props, "loadOnDemand", loadOnDemand, 0, 1);
      logger.info("loadOnDemand: {}", loadOnDemand);

      String useDBValue = props.getProperty("useDB");
      if (useDBValue != null) {
        BookListModel.useDB = !useDBValue.trim().equalsIgnoreCase("false");
        logger.info("useDB: {}", BookListModel.useDB);
      }

      String searchParamValue = props.getProperty("searchParam");
      if (searchParamValue != null) {
        String tmp = searchParamValue.trim();
        if (tmp.equals("a") || tmp.equals("at")) {
          searchParam = tmp;
        }
        logger.info("searchParam: {}", searchParam);
      }

      String debugValue = props.getProperty("debug");
      if (debugValue != null) {
        String tmp = debugValue.trim();
        if (tmp.equals("WARN") || tmp.equalsIgnoreCase("INFO")) {
          debug = tmp;
        }
        logger.info("debug: {}", debug);
      }

      backup = getInt(props, "backup", backup, 0, 2);
      logger.info("backup: {}", backup);

      String tokenValue = props.getProperty("apiToken");
      if (tokenValue != null && tokenValue.trim().length() > 60) {
        apiToken = tokenValue.trim();
      }

      String urlValue = props.getProperty("apiURL");
      if (urlValue != null && urlValue.trim().length() > 10) {
        apiURL = urlValue.trim();
      }

      darkmode = getInt(props, "darkmode", darkmode, 0, 1);
      tmpDarkmode = darkmode;
      logger.info("darkmode: {}", darkmode);

      String layoutWidth = props.getProperty("layoutWidth");
      if (layoutWidth != null) {
        try {
          String[] values = layoutWidth.trim().split(",");
          Mainframe.prozEbook = Integer.parseInt(values[0]);
          Mainframe.prozAuthor = Integer.parseInt(values[1]);
          Mainframe.prozTitle = Integer.parseInt(values[2]);
          Mainframe.prozSeries = Integer.parseInt(values[3]);
          Mainframe.prozRating = Integer.parseInt(values[4]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
          logger.error("Fehler beim Lesen von layoutWidth: {}", e.getMessage());
        }
      }

      String layoutSort = props.getProperty("layoutSort");
      if (layoutSort != null) {
        try {
          String[] values = layoutSort.trim().split(",");
          Set<String> validKeys = Set.of(
              SimpleTableModel.KEY_EBOOK, SimpleTableModel.KEY_AUTHOR,
              SimpleTableModel.KEY_TITLE, SimpleTableModel.KEY_SERIES,
              SimpleTableModel.KEY_RATING);
          if (values.length != SimpleTableModel.columnKeys.length) {
            throw new IllegalArgumentException("Erwartete " + SimpleTableModel.columnKeys.length + " Spalten, gefunden: " + values.length);
          }
          for (String key : values) {
            if (!validKeys.contains(key.trim())) {
              throw new IllegalArgumentException("Ungültiger Spaltenkey: " + key);
            }
          }
          System.arraycopy(values, 0, SimpleTableModel.columnKeys, 0, values.length);
        } catch (IllegalArgumentException e) {
          logger.warn("Fehler beim Lesen von layoutSort, verwende Defaults: {}", e.getMessage());
        }
      }

      Mainframe.startX = getInt(props, "MainframeX", Mainframe.startX);
      Mainframe.startY = getInt(props, "MainframeY", Mainframe.startY);
      Mainframe.defaultFrameWidth = getInt(props, "MainframeWidth", Mainframe.defaultFrameWidth);
      Mainframe.defaultFrameHeight = getInt(props, "MainframeHeight", Mainframe.defaultFrameHeight);

    } else {
      Mainframe.executor.submit(() -> writeDefaults());
    }
  }

  public static void writeSettings() {
    logger.info("Save Settings to File");

    Properties props = new Properties();
    props.setProperty("lang", lang);
    props.setProperty("fontSize", String.valueOf(UIScale.unscale(Mainframe.defaultFont.getSize())));
    props.setProperty("descFontSize", String.valueOf(UIScale.unscale(Mainframe.descFont.getSize())));
    props.setProperty("autoDownload", String.valueOf(autoDownload));
    props.setProperty("loadOnDemand", String.valueOf(loadOnDemand));
    props.setProperty("searchParam", searchParam);
    props.setProperty("debug", debug);
    props.setProperty("backup", String.valueOf(backup));
    props.setProperty("apiToken", apiToken);
    props.setProperty("apiURL", apiURL);
    props.setProperty("darkmode", String.valueOf(tmpDarkmode));
    props.setProperty("version", Mainframe.getVersion());

    TableColumnModel columnModel = Mainframe.table.getColumnModel();
    props.setProperty("layoutWidth",
        columnModel.getColumn(0).getWidth() + "," +
        columnModel.getColumn(1).getWidth() + "," +
        columnModel.getColumn(2).getWidth() + "," +
        columnModel.getColumn(3).getWidth() + "," +
        columnModel.getColumn(4).getWidth());

    props.setProperty("layoutSort", String.join(",", SimpleTableModel.columnKeys));
    props.setProperty("MainframeX", String.valueOf(Mainframe.getInstance().getX()));
    props.setProperty("MainframeY", String.valueOf(Mainframe.getInstance().getY()));
    props.setProperty("MainframeWidth", String.valueOf(Mainframe.getInstance().getWidth()));
    props.setProperty("MainframeHeight", String.valueOf(Mainframe.getInstance().getHeight()));

    try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
      props.store(writer, "Booklist Configuration");
      Mainframe.showNotification("Einstellungen gespeichert");
    } catch (IOException e) {
      logger.error("Fehler beim speichern der Einstellungen");
      logger.error(e.getMessage());
    }
  }

  private static void writeDefaults() {
    Properties props = new Properties();
    props.setProperty("fontSize", String.valueOf(UIScale.unscale(Mainframe.defaultFont.getSize())));
    props.setProperty("descFontSize", String.valueOf(UIScale.unscale(Mainframe.descFont.getSize())));
    props.setProperty("autoDownload", String.valueOf(autoDownload));
    props.setProperty("loadOnDemand", String.valueOf(loadOnDemand));
    props.setProperty("useDB", String.valueOf(BookListModel.useDB));
    props.setProperty("searchParam", searchParam);
    props.setProperty("debug", debug);
    props.setProperty("backup", String.valueOf(backup));
    apiToken = generateRandomToken(64);
    props.setProperty("apiToken", apiToken);
    props.setProperty("apiURL", apiURL);
    props.setProperty("darkmode", String.valueOf(darkmode));

    try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
      props.store(writer, "Booklist Configuration");
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  static int getInt(Properties props, String key, int defaultValue) {
    String value = props.getProperty(key);
    if (value == null) return defaultValue;
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      logger.error("Fehler in der config ({}): erwartet Integer, gefunden '{}'", key, value);
      return defaultValue;
    }
  }

  static int getInt(Properties props, String key, int defaultValue, int min, int max) {
    int value = getInt(props, key, defaultValue);
    if (value < min || value > max) {
      logger.error("Fehler in der config ({}): Wert {} nicht im Bereich {}-{}", key, value, min, max);
      return defaultValue;
    }
    return value;
  }

  public static String generateRandomToken(int length) {
    final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    SecureRandom random = new SecureRandom();
    StringBuilder token = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int index = random.nextInt(CHARACTERS.length());
      token.append(CHARACTERS.charAt(index));
    }
    return token.toString();
  }

}
