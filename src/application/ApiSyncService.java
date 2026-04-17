package application;

import com.google.gson.*;
import data.Database;
import gui.Mainframe;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reiner HTTP-Client für die Web-App-Synchronisation.
 * Keine UI-Aufrufe, keine Mainframe-Referenz.
 * Orchestrierung (Reihenfolge, Notifications, Model-Updates) bleibt in Mainframe.
 */
public class ApiSyncService {

  public record PendingBook(String author, String title, String series,
                            String seriesPart, String note, boolean ebook) {}

  public record RatingUpdate(int bid, double rating) {}

  /**
   * Prüft die Verbindung zur Web-App via GET /api/get.php.
   *
   * @return true bei HTTP 200, sonst false
   */
  public boolean testConnection() {
    try {
      Mainframe.logger.info("Web API request: {}/api/get.php", HandleConfig.apiURL);
      URL getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
      HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
      con.setConnectTimeout(2000);
      con.setRequestMethod("GET");
      long startTime = System.currentTimeMillis();
      int responseCode = con.getResponseCode();
      long responseTime = System.currentTimeMillis() - startTime;
      Mainframe.logger.info("Web API request: responseCode: {}", responseCode);
      Mainframe.logger.info("Web API request: responseTime: {}ms", responseTime);
      con.disconnect();
      return responseCode == HttpURLConnection.HTTP_OK;
    } catch (URISyntaxException | IOException e) {
      Mainframe.logger.error("Verbindung zur API fehlgeschlagen: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Ruft ausstehende Bücher von der Web-App ab (GET /api/get.php).
   * Gibt Rohdaten zurück – Duplikatprüfung und Model-Updates macht Mainframe.
   *
   * @return Liste der ausstehenden Bücher (leer wenn keine vorhanden oder Fehler)
   */
  public List<PendingBook> fetchPendingBooks() throws URISyntaxException, IOException {
    List<PendingBook> result = new ArrayList<>();
    Mainframe.logger.info("Web API Download request: {}/api/get.php?token=****{}",
        HandleConfig.apiURL, HandleConfig.apiToken.substring(HandleConfig.apiToken.length() - 4));
    URL getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
    HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
    con.setRequestMethod("GET");
    con.setConnectTimeout(5000);
    int responseCode = con.getResponseCode();
    Mainframe.logger.info("Web API GET responseCode: {}", responseCode);
    if (responseCode != HttpURLConnection.HTTP_OK) {
      con.disconnect();
      return result;
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) response.append(inputLine);
    }
    con.disconnect();

    String jsonResponse = response.toString();
    Mainframe.logger.info("Web API GET response: {}", jsonResponse);
    JsonElement jsonElement = JsonParser.parseString(jsonResponse);
    if (jsonElement.isJsonArray()) {
      for (JsonElement element : jsonElement.getAsJsonArray()) {
        JsonObject obj = element.getAsJsonObject();
        String author = obj.get("author").getAsString();
        String title = obj.get("title").getAsString();
        String series = obj.has("series") ? obj.get("series").getAsString() : "";
        String seriesPart = obj.has("series_part") ? obj.get("series_part").getAsString() : "";
        String note = obj.has("note") ? obj.get("note").getAsString() : "";
        String ebook = obj.has("ebook") ? obj.get("ebook").getAsString() : null;
        boolean boolEbook = "1".equals(ebook);
        if ("0".equals(seriesPart)) seriesPart = "";
        result.add(new PendingBook(author, title, series, seriesPart, note, boolEbook));
      }
    }
    return result;
  }

  /**
   * Löscht importierte Bücher aus der Web-App (POST /api/delete.php).
   */
  public void clearPendingBooks() throws URISyntaxException, IOException {
    URL deleteUrl = new URI(HandleConfig.apiURL + "/api/delete.php").toURL();
    HttpURLConnection con = (HttpURLConnection) deleteUrl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    con.setDoOutput(true);
    try (OutputStream os = con.getOutputStream()) {
      os.write(("token=" + HandleConfig.apiToken).getBytes(StandardCharsets.UTF_8));
    }
    int responseCode = con.getResponseCode();
    Mainframe.logger.info("Web API DELETE books responseCode: {}", responseCode);
    con.disconnect();
  }

  /**
   * Ruft ausstehende Rating-Updates ab und löscht sie danach aus der Web-App.
   *
   * @return Liste der Rating-Updates (leer wenn keine vorhanden oder Fehler)
   */
  public List<RatingUpdate> fetchPendingRatingUpdates() throws URISyntaxException, IOException {
    List<RatingUpdate> result = new ArrayList<>();
    URL url = new URI(HandleConfig.apiURL + "/api/getRatingUpdates.php?token=" + HandleConfig.apiToken).toURL();
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setConnectTimeout(5000);
    con.setReadTimeout(10000);

    if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
      Mainframe.logger.warn("getRatingUpdates: HTTP {}", con.getResponseCode());
      con.disconnect();
      return result;
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
      String line;
      while ((line = in.readLine()) != null) response.append(line);
    }
    con.disconnect();

    JsonElement el = JsonParser.parseString(response.toString());
    if (!el.isJsonArray()) return result;

    JsonArray arr = el.getAsJsonArray();
    if (arr.isEmpty()) return result;

    for (JsonElement e : arr) {
      JsonObject obj = e.getAsJsonObject();
      int bid = obj.get("bid").getAsInt();
      double rating = obj.get("rating").getAsDouble();
      result.add(new RatingUpdate(bid, rating));
    }

    // Verarbeitete Updates aus Web-App löschen
    URL deleteUrl = new URI(HandleConfig.apiURL + "/api/deleteRatingUpdates.php").toURL();
    HttpURLConnection delCon = (HttpURLConnection) deleteUrl.openConnection();
    delCon.setRequestMethod("POST");
    delCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    delCon.setConnectTimeout(5000);
    delCon.setDoOutput(true);
    try (OutputStream os = delCon.getOutputStream()) {
      os.write(("token=" + HandleConfig.apiToken).getBytes(StandardCharsets.UTF_8));
    }
    Mainframe.logger.info("deleteRatingUpdates: HTTP {}", delCon.getResponseCode());
    delCon.disconnect();

    return result;
  }

  /**
   * Lädt alle Bücher in die Web-App hoch (POST /api/upload.php JSON).
   *
   * @param books - hochzuladende Bücher
   * @return true bei HTTP 200, sonst false
   */
  public boolean uploadBooks(List<Book_Booklist> books) throws URISyntaxException, IOException {
    HttpURLConnection con = buildUploadConnection(books);
    int responseCode = con.getResponseCode();
    con.disconnect();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      Mainframe.logger.info("Books successfully uploaded");
      return true;
    } else {
      Mainframe.logger.error("Error while uploading: {}", responseCode);
      return false;
    }
  }

  /**
   * Löscht die synchronisierten Bücher aus der Web-App (POST /api/deleteSynced.php).
   */
  public void clearSyncedBooks() throws URISyntaxException, IOException {
    URL deleteUrl = new URI(HandleConfig.apiURL + "/api/deleteSynced.php").toURL();
    HttpURLConnection con = (HttpURLConnection) deleteUrl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    con.setDoOutput(true);
    con.setConnectTimeout(5000);
    try (OutputStream os = con.getOutputStream()) {
      os.write(("token=" + HandleConfig.apiToken).getBytes(StandardCharsets.UTF_8));
    }
    int responseCode = con.getResponseCode();
    Mainframe.logger.info("Web API DELETE SyncedBooks responseCode: {}", responseCode);
    con.disconnect();
  }

  private HttpURLConnection buildUploadConnection(List<Book_Booklist> books) throws URISyntaxException, IOException {
    Mainframe.logger.info("Web API request: {}/api/upload.php?token=****{}",
        HandleConfig.apiURL, HandleConfig.apiToken.substring(HandleConfig.apiToken.length() - 4));
    URL postUrl = new URI(HandleConfig.apiURL + "/api/upload.php?token=" + HandleConfig.apiToken).toURL();

    HttpURLConnection con = (HttpURLConnection) postUrl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json; utf-8");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    con.setConnectTimeout(5000);
    con.setReadTimeout(15000);

    Gson gson = new Gson();
    JsonArray jsonArray = new JsonArray();
    for (Book_Booklist book : books) {
      JsonObject jsonBook = new JsonObject();
      jsonBook.addProperty("bid", book.getBid());
      jsonBook.addProperty("author", book.getAuthor());
      jsonBook.addProperty("title", book.getTitle());
      jsonBook.addProperty("series", book.getSeries());
      jsonBook.addProperty("series_part", book.getSeriesVol());
      jsonBook.addProperty("ebook", book.isEbook());
      jsonBook.addProperty("rating", book.getRating());
      jsonBook.addProperty("note", book.getNote() != null ? book.getNote() : "");
      if (HandleConfig.loadOnDemand == 1) {
        Database.loadIsbnAndDescForSync(book);
      }
      jsonBook.addProperty("isbn", book.getIsbn() != null ? book.getIsbn() : "");
      jsonBook.addProperty("description", book.getDesc() != null ? book.getDesc() : "");
      jsonBook.addProperty("date_added", book.getDate() != null ? book.getDate().toString() : "");
      if (!book.getBorrowedTo().isEmpty()) {
        jsonBook.addProperty("ausgeliehen", "an");
        jsonBook.addProperty("borrow_name", book.getBorrowedTo());
      } else if (!book.getBorrowedFrom().isEmpty()) {
        jsonBook.addProperty("ausgeliehen", "von");
        jsonBook.addProperty("borrow_name", book.getBorrowedFrom());
      } else {
        jsonBook.addProperty("ausgeliehen", "nein");
        jsonBook.addProperty("borrow_name", "");
      }
      jsonArray.add(jsonBook);
    }

    String jsonInputString = gson.toJson(jsonArray);
    try (OutputStream os = con.getOutputStream()) {
      os.write(jsonInputString.getBytes(StandardCharsets.UTF_8));
    }
    return con;
  }
}
