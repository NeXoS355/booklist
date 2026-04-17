package application;

import gui.Mainframe;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;

public class BackupService {

  /**
   * Erstellt ein vollständiges Datei-Backup von DB, Config und JAR.
   *
   * @return true bei Erfolg, false bei Fehler
   */
  public static boolean createBackup() {
    try {
      File jarFile = new File(BackupService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      String filename = jarFile.getName();
      File workingDir = new File(System.getProperty("user.dir"));
      if (filename.contains(".jar")) {
        Date dt = new Date();
        long LongTime = dt.getTime();
        String StrTime = Long.toString(LongTime).substring(0, Long.toString(LongTime).length() - 3);
        File backupDir = new File(workingDir, "Backup/" + StrTime);
        copyFileToDirectory(new File(workingDir, "booklist.db"), backupDir);
        copyFileToDirectory(new File(workingDir, "config.conf"), backupDir);
        copyFileToDirectory(jarFile, backupDir);
        Mainframe.logger.info("Backup created");
        return true;
      } else {
        Mainframe.logger.error("Error while creating Backup. Could not extract filename.");
        return false;
      }
    } catch (IOException e1) {
      Mainframe.logger.error("Error while creating Backup. IOException");
      Mainframe.logger.error(e1.toString());
      return false;
    } catch (URISyntaxException e1) {
      Mainframe.logger.error("Error while creating Backup. URISyntaxException");
      Mainframe.logger.error(e1.toString());
      return false;
    }
  }

  /**
   * Kopiert eine einzelne Datei in ein Zielverzeichnis.
   *
   * @param file - zu kopierende Datei
   * @param to   - Zielverzeichnis
   */
  private static void copyFileToDirectory(File file, File to) throws IOException {
    boolean success;
    if (!to.exists()) {
      success = to.mkdirs();
    } else {
      success = true;
    }
    if (success) {
      File n = new File(to.getAbsolutePath() + "/" + file.getName());
      Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * Kopiert mehrere Dateien rekursiv in ein Zielverzeichnis.
   *
   * @param from - Quellverzeichnis oder -datei
   * @param to   - Zielverzeichnis
   */
  public static void copyFilesInDirectory(File from, File to) {
    boolean success = false;
    if (!to.exists()) {
      success = to.mkdirs();
    }
    File[] files = from.listFiles();
    if (success && files != null) {
      for (File file : files) {
        File n = new File(to, file.getName());
        if (file.isDirectory()) {
          copyFilesInDirectory(file, n);
        } else {
          try {
            Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException e) {
            Mainframe.logger.error(e.getMessage());
          }
        }
      }
    }
  }
}
