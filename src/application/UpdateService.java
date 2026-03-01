package application;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.IntConsumer;

/**
 * GitHub-API-Abfragen, JAR-Download, SHA-256-Prüfung und JAR-Austausch.
 */
public class UpdateService {

  private static final Logger logger = LogManager.getLogger(UpdateService.class);

  public record UpdateInfo(boolean available, String latestTag,
                           String downloadUrl, String expectedDigest) {}

  /**
   * Prüft über die GitHub API ob eine neue Version verfügbar ist.
   *
   * @param currentVersion - aktuell installierte Version (z.B. "3.2.9")
   * @return UpdateInfo mit available=true und Download-URL wenn Update vorhanden
   */
  public UpdateInfo checkForUpdate(String currentVersion) throws IOException, URISyntaxException {
    URL apiUrl = new URI("https://api.github.com/repos/NeXoS355/booklist/releases/latest").toURL();
    HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
    apiConn.setRequestProperty("Accept", "application/vnd.github+json");
    apiConn.setRequestMethod("GET");

    if (apiConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      logger.error("Update - GitHub API returned: {}", apiConn.getResponseCode());
      throw new IOException("GitHub API returned: " + apiConn.getResponseCode());
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(apiConn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) response.append(line);
    }
    apiConn.disconnect();

    JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
    String latestTag = release.get("tag_name").getAsString().replaceAll("^v", "");
    logger.info("Update - current version: {}, latest version: {}", currentVersion, latestTag);

    int currentVer = parseVersion(currentVersion);
    int latestVer = parseVersion(latestTag);

    if (latestVer <= currentVer) {
      logger.info("Update - no update available");
      return new UpdateInfo(false, latestTag, null, null);
    }

    // Update verfügbar – Download-URL und SHA-256 Digest aus den Release Assets lesen
    String downloadUrl = "https://github.com/NeXoS355/booklist/releases/latest/download/Booklist.jar";
    String expectedDigest = null;
    if (release.has("assets")) {
      for (var asset : release.getAsJsonArray("assets")) {
        JsonObject assetObj = asset.getAsJsonObject();
        String name = assetObj.get("name").getAsString();
        if (name.endsWith(".jar")) {
          downloadUrl = assetObj.get("browser_download_url").getAsString();
          if (assetObj.has("digest") && !assetObj.get("digest").isJsonNull()) {
            String digest = assetObj.get("digest").getAsString();
            if (digest.startsWith("sha256:")) {
              expectedDigest = digest.substring(7);
            }
          }
          break;
        }
      }
    }

    return new UpdateInfo(true, latestTag, downloadUrl, expectedDigest);
  }

  /**
   * Lädt eine JAR-Datei herunter und meldet den Fortschritt über den Callback.
   *
   * @param url              - Download-URL
   * @param target           - Zieldatei
   * @param progressCallback - wird mit dem Prozentsatz (0-100) aufgerufen; darf null sein
   */
  public void downloadJar(String url, File target, IntConsumer progressCallback)
      throws IOException, URISyntaxException {
    URL jarUrl = new URI(url).toURL();
    HttpURLConnection httpConn = (HttpURLConnection) jarUrl.openConnection();
    int fileSize = httpConn.getContentLength();
    logger.info("Update - downloading JAR ({} KB)", fileSize > 0 ? fileSize / 1024 : "unknown");

    try (BufferedInputStream in = new BufferedInputStream(jarUrl.openStream());
         FileOutputStream fos = new FileOutputStream(target)) {
      byte[] buffer = new byte[8192];
      int bytesRead;
      int downloaded = 0;
      int oldProgress = 0;
      while ((bytesRead = in.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
        downloaded += bytesRead;
        if (fileSize > 0 && progressCallback != null) {
          int progress = (int) ((double) downloaded / fileSize * 100);
          if (progress > oldProgress) {
            progressCallback.accept(progress);
            oldProgress = progress;
          }
        }
      }
    }
    logger.info("Update - download complete");
  }

  /**
   * Prüft SHA-256-Checksumme einer Datei gegen den erwarteten Hex-String.
   *
   * @return true wenn Prüfsumme übereinstimmt
   */
  public static boolean verifyChecksum(File file, String expectedHex)
      throws IOException, NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    try (InputStream fis = new FileInputStream(file)) {
      byte[] buf = new byte[8192];
      int n;
      while ((n = fis.read(buf)) != -1) {
        md.update(buf, 0, n);
      }
    }
    StringBuilder sb = new StringBuilder();
    for (byte b : md.digest()) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString().equals(expectedHex);
  }

  /**
   * Kopiert latest.jar über die aktuelle JAR und startet die App neu.
   * Wird als separater Prozess ausgeführt (java -jar app.jar update).
   */
  public static void applyUpdate() {
    try {
      File jarFile = new File(UpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      File baseDir = jarFile.getParentFile();
      try (PrintWriter log = new PrintWriter(new File(baseDir, "update.log"))) {
        Thread.sleep(2000);
        File source = new File(baseDir, "latest.jar");
        log.println("UPDATER: initialize");
        log.println("UPDATER: detected fileName: " + jarFile.getAbsolutePath());
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(jarFile)) {
          byte[] buffer = new byte[8192];
          int length;
          log.println("UPDATER: overwriting file");
          while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
          }
          log.println("UPDATER: writing complete");
          ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
          pb.directory(baseDir);
          log.println("UPDATER: starting " + pb.command());
          pb.start();
          log.println("UPDATER: SUCCESS");
        } catch (IOException e) {
          log.println("UPDATER: ERROR - " + e.getMessage());
        }
      }
    } catch (FileNotFoundException | InterruptedException | URISyntaxException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Wandelt einen Versionsstring (z.B. "3.3.0") in einen int um (3003000).
   */
  public static int parseVersion(String ver) {
    String[] parts = ver.split("[.]");
    int result = 0;
    for (String part : parts) {
      result = result * 1000 + Integer.parseInt(part);
    }
    return result;
  }

  /**
   * Ruft ein GitHub Release anhand eines Tags ab.
   */
  public static JsonObject fetchReleaseByTag(String tag) throws IOException, URISyntaxException {
    URL apiUrl = new URI("https://api.github.com/repos/NeXoS355/booklist/releases/tags/" + tag).toURL();
    HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
    apiConn.setRequestProperty("Accept", "application/vnd.github+json");
    apiConn.setRequestMethod("GET");

    if (apiConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException("GitHub API returned: " + apiConn.getResponseCode());
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(apiConn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) response.append(line);
    }
    apiConn.disconnect();
    return JsonParser.parseString(response.toString()).getAsJsonObject();
  }
}
