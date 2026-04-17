package application;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import data.Database;
import gui.Mainframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetBookInfosFromWeb {

	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 10000;

	/**
	 * request Book Infos from Google API to save Covers, description and ISBN No.
	 *
	 * @param entry      - Book entry to search for
	 * @param maxResults - how many Results should be requested from Google Books API
	 * @param retry      - change Setting if first request was not sufficient
	 */
	public static int getBookInfoFromGoogleApiWebRequest(Book_Booklist entry, int maxResults, boolean retry) {
		int compareReturn = 0;
		try {
			String localSearchParam = HandleConfig.searchParam;
			if (retry) {
				localSearchParam = localSearchParam.equals("at") ? "t" : "at";
				Mainframe.logger.info("Retry with searchParam: {}", localSearchParam);
			}

			StringBuilder str = new StringBuilder();
			if (entry.getIsbn() != null && !entry.getIsbn().isEmpty()) {
				str.append("isbn:").append(sanitizeString(entry.getIsbn()));
			} else {
				if (localSearchParam.equals("at")) {
					str.append(sanitizeString(entry.getAuthor())).append("+");
				}
				str.append(sanitizeString(entry.getTitle()));
			}

			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + str + "&maxResults="
					+ maxResults + "&printType=books";

			Mainframe.logger.info("Search API: {}", entry);
			Mainframe.logger.info("Search API URL: {}", apiUrl);

			String response = executeApiRequest(apiUrl);
			if (response != null) {
				JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
				compareReturn = analyseApiRequestBookInfo(jsonObject, entry, maxResults);
			}

		} catch (IOException | URISyntaxException e) {
			Mainframe.logger.error("API request failed: {}", e.getMessage());
			Mainframe.showNotification("API Fehler: " + e.getMessage());
		}
		Mainframe.logger.info("checkWebInfo Retry: {}", retry);
		Mainframe.logger.info(
				"checkWebInfo Overall Score: {}-{}:{}", entry.getAuthor(), entry.getTitle(), compareReturn);
		return compareReturn;
	}

	/**
	 * request Series Infos from Google API to save new Books from that series.
	 * First tries with langRestrict=de, falls back to unrestricted if no results.
	 *
	 * @param series     - name of the series
	 * @param author     - author of the series
	 * @param volNumber  - missing volume number to search for
	 * @param maxResults - how many Results should be requested from Google Books API
	 */
	public static String[][] getSeriesInfoFromGoogleApiWebRequest(String series, String author, int volNumber, int maxResults) {
		String[][] result = doSeriesApiRequest(series, author, volNumber, maxResults, true);
		boolean hasResults = false;
		for (String[] entry : result) {
			if (entry[0] != null || entry[1] != null) { hasResults = true; break; }
		}
		if (!hasResults) {
			Mainframe.logger.info("Serie: keine Treffer mit langRestrict=de, Wiederholung ohne Sprachfilter");
			result = doSeriesApiRequest(series, author, volNumber, maxResults, false);
		}
		return result;
	}

	private static String[][] doSeriesApiRequest(String series, String author, int volNumber, int maxResults, boolean langRestrict) {
		String[][] compareReturn = new String[maxResults][3];
		try {
			String query = "intitle:" + sanitizeString(series)
					+ "+intitle:" + sanitizeString(String.valueOf(volNumber))
					+ "+inauthor:" + sanitizeString(author);

			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + query
					+ "&maxResults=" + maxResults
					+ "&printType=books"
					+ (langRestrict ? "&langRestrict=de" : "");

			Mainframe.logger.info("Search API series query: {}", query);
			Mainframe.logger.info("Search API URL: {}", apiUrl);

			String response = executeApiRequest(apiUrl);
			if (response != null) {
				JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
				compareReturn = analyseApiRequestSeries(jsonObject, maxResults, volNumber);
			}

		} catch (IOException | URISyntaxException e) {
			Mainframe.logger.error("API request failed: {}", e.getMessage());
			Mainframe.showNotification("API Fehler: " + e.getMessage());
		}
		return compareReturn;
	}

	/**
	 * Execute an API request with timeouts and rate-limit handling
	 */
	private static String executeApiRequest(String apiUrl) throws IOException, URISyntaxException {
		return executeApiRequest(apiUrl, 0);
	}

	private static String executeApiRequest(String apiUrl, int attempt) throws IOException, URISyntaxException {
		URL url = new URI(apiUrl).toURL();
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(CONNECT_TIMEOUT);
		connection.setReadTimeout(READ_TIMEOUT);

		int responseCode = connection.getResponseCode();
		Mainframe.logger.info("Search API response: {}", responseCode);

		if ((responseCode == 429 || responseCode == 503) && attempt < 3) {
			connection.disconnect();
			// Exponential backoff: 3s, 6s, 12s
			int waitMs = responseCode == 429 ? 1000 : (3000 * (1 << attempt));
			Mainframe.logger.warn("Google Books API HTTP {} (Versuch {}) – warte {}ms ...", responseCode, attempt + 1, waitMs);
			try {
				Thread.sleep(waitMs);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
			return executeApiRequest(apiUrl, attempt + 1);
		}

		try {
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					return response.toString();
				}
			} else {
				Mainframe.logger.warn("API returned HTTP {}", responseCode);
			}
		} finally {
			connection.disconnect();
		}
		return null;
	}

	/**
	 * Analyze the JSON response from Google Books API.
	 * Prueft bis zu maxResults Ergebnisse und waehlt den besten Treffer.
	 */
	private static int analyseApiRequestBookInfo(JsonObject jsonObject, Book_Booklist entry, int maxResults) {
		if (!jsonObject.has("items")) {
			Mainframe.logger.info("WebInfo Download: 'items' not found!");
			return 0;
		}

		var itemsArray = jsonObject.getAsJsonArray("items");
		if (itemsArray.isEmpty()) {
			Mainframe.logger.info("WebInfo Download: no elements found in 'items'!");
			return 0;
		}

		int bestScore = 0;
		int bestIndex = 0;
		int itemCount = Math.min(itemsArray.size(), maxResults);

		// Besten Treffer finden (Author+Title Vergleich)
		for (int i = 0; i < itemCount; i++) {
			var item = itemsArray.get(i).getAsJsonObject();
			if (!item.has("volumeInfo")) continue;
			var volumeInfo = item.getAsJsonObject("volumeInfo");

			int scoreAuthor = 0;
			int scoreTitle = 0;

			if (volumeInfo.has("title")) {
				scoreTitle = compareString(volumeInfo.get("title").getAsString(), entry.getTitle());
			}
			if (volumeInfo.has("authors") && !volumeInfo.getAsJsonArray("authors").isEmpty()) {
				scoreAuthor = compareString(
						volumeInfo.getAsJsonArray("authors").get(0).getAsString(), entry.getAuthor());
			}

			int score = (scoreAuthor + scoreTitle) / 2;
			if (score > bestScore) {
				bestScore = score;
				bestIndex = i;
			}
		}

		// Daten vom besten Treffer und ggf. Fallbacks extrahieren
		boolean hasCover = false;
		boolean hasDesc = false;
		boolean hasIsbn = false;

		for (int i = 0; i < itemCount && !(hasCover && hasDesc && hasIsbn); i++) {
			// Besten Treffer zuerst, dann die anderen
			int idx = (i == 0) ? bestIndex : (i <= bestIndex ? i - 1 : i);
			var item = itemsArray.get(idx).getAsJsonObject();
			if (!item.has("volumeInfo")) continue;
			var volumeInfo = item.getAsJsonObject("volumeInfo");

			if (!hasCover && volumeInfo.has("imageLinks")) {
				var imageLinks = volumeInfo.getAsJsonObject("imageLinks");
				String link = null;
				if (imageLinks.has("thumbnail")) {
					link = imageLinks.get("thumbnail").getAsString();
				} else if (imageLinks.has("smallThumbnail")) {
					link = imageLinks.get("smallThumbnail").getAsString();
				}
				if (link != null) {
					savePic(link, entry);
					hasCover = true;
				}
			}

			if (!hasIsbn && volumeInfo.has("industryIdentifiers")) {
				var identifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
				for (int j = 0; j < identifiers.size(); j++) {
					var identifier = identifiers.get(j).getAsJsonObject();
					if (identifier.has("type") && identifier.get("type").getAsString().equals("ISBN_13")) {
						String isbn = identifier.get("identifier").getAsString();
						hasIsbn = true;
						Mainframe.executor.submit(() -> entry.setIsbn(isbn, true));
						break;
					}
				}
			}

			if (!hasDesc && volumeInfo.has("description")) {
				String description = volumeInfo.get("description").getAsString();
				hasDesc = true;
				Mainframe.executor.submit(() -> entry.setDesc(description, true));
			}
		}

		return bestScore;
	}

	/**
	 * Analyze the JSON response from Google Books API.
	 * Ergebnisse mit volNumber im Titel werden bevorzugt (an Index 0 gestellt).
	 */
	private static String[][] analyseApiRequestSeries(JsonObject jsonObject, int maxResults, int volNumber) {
		String[][] returnArray = new String[maxResults][3];
		if (!jsonObject.has("items")) return returnArray;

		var itemsArray = jsonObject.getAsJsonArray("items");
		int itemCount = Math.min(itemsArray.size(), maxResults);

		int fillIndex = 0;
		int firstVolMatch = -1;

		for (int i = 0; i < itemCount; i++) {
			var item = itemsArray.get(i).getAsJsonObject();
			if (!item.has("volumeInfo")) continue;
			var volumeInfo = item.getAsJsonObject("volumeInfo");

			String[] entry = new String[3];
			if (volumeInfo.has("title")) {
				entry[1] = volumeInfo.get("title").getAsString();
			}
			if (volumeInfo.has("authors") && !volumeInfo.getAsJsonArray("authors").isEmpty()) {
				entry[0] = volumeInfo.getAsJsonArray("authors").get(0).getAsString();
			}
			if (volumeInfo.has("industryIdentifiers")) {
				var identifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
				for (int j = 0; j < identifiers.size(); j++) {
					var identifier = identifiers.get(j).getAsJsonObject();
					if (identifier.has("type") && identifier.get("type").getAsString().equals("ISBN_13")) {
						entry[2] = identifier.get("identifier").getAsString();
					}
				}
			}

			if (fillIndex < maxResults) {
				returnArray[fillIndex] = entry;
				if (firstVolMatch < 0 && entry[1] != null && titleContainsVolume(entry[1], volNumber)) {
					firstVolMatch = fillIndex;
				}
				fillIndex++;
			}
		}

		// Bestes Ergebnis (Bandnummer im Titel) an Index 0 schieben
		if (firstVolMatch > 0) {
			String[] best = returnArray[firstVolMatch];
			returnArray[firstVolMatch] = returnArray[0];
			returnArray[0] = best;
		}

		return returnArray;
	}

	/**
	 * Prueft ob ein Titel die Bandnummer enthaelt — unterstuetzt gaengige Formate
	 * wie "Band 3", "Bd. 3", "Teil 3", "Vol. 3" sowie blosse Zahlen als eigenes Wort.
	 */
	static boolean titleContainsVolume(String title, int volNumber) {
		String volStr = String.valueOf(volNumber);
		String titleLower = title.toLowerCase();
		String[] prefixes = {"band ", "bd. ", "bd ", "teil ", "vol. ", "vol ", "tome ", "book ", "part "};
		for (String prefix : prefixes) {
			if (titleLower.contains(prefix + volStr)) return true;
		}
		// Zahl als eigenes Wort (keine Ziffern direkt davor/dahinter)
		return title.matches(".*(?<!\\d)" + volStr + "(?!\\d).*");
	}

	/**
	 * Compare two Strings to rate the quality of the found Information
	 *
	 * @return int - returns a percentage Number of the comparison
	 */
	public static int compareString(String str1, String str2) {
		int hit = 0;
		int counterBig = 0;
		int counterSmall = 0;
		boolean newWord = true;
		String small;
		String big;

		if (str1.length() > str2.length()) {
			big = str1;
			small = str2;
		} else {
			big = str2;
			small = str1;
		}

		int anzahl = big.length();
		while (counterSmall < small.length() && counterBig < big.length()) {
			char smallChar = small.charAt(counterSmall);
			char bigChar = big.charAt(counterBig);
			while (smallChar == ' ' && bigChar != ' ' && counterBig < big.length() - 1) {
				counterBig++;
				bigChar = big.charAt(counterBig);
				newWord = true;
			}
			while (smallChar != ' ' && bigChar == ' ' && counterBig < big.length() - 1) {
				counterBig++;
				bigChar = big.charAt(counterBig);
				newWord = true;
			}

			if (newWord && smallChar != bigChar) {
				counterBig++;
			} else if (smallChar == bigChar) {
				counterBig++;
				counterSmall++;
				hit++;
				newWord = false;
			} else {
				counterBig++;
				counterSmall++;
			}
		}
		int equalPercentage = hit * 100 / anzahl;
		Mainframe.logger.info("checkWebInfo {}-{}:{}", str1, str2, equalPercentage);
		return equalPercentage;
	}

	/**
	 * Delete a pic from a Book entry
	 */
	public static boolean deletePic(int bid) {
		return Database.delPic(bid);
	}

	private static final int MAX_COVER_HEIGHT = 512;
	private static final float JPEG_QUALITY = 0.92f;

	/**
	 * Laedt ein Cover-Bild herunter und speichert es. Versucht zuerst zoom=0
	 * (groesseres Bild), faellt bei Fehler auf den Original-Link (zoom=1) zurueck.
	 */
	public static void savePic(String weblink, Book_Booklist entry) {
		String zoomedLink = weblink.replaceAll("&zoom=\\d+", "&zoom=0");
		if (!doSavePic(zoomedLink, entry)) {
			Mainframe.logger.warn("zoom=0 fehlgeschlagen, Fallback auf zoom=1: {}", weblink);
			doSavePic(weblink, entry);
		}
	}

	/**
	 * Fuehrt den eigentlichen Download und die Verarbeitung durch.
	 *
	 * @return true bei Erfolg, false bei Fehler
	 */
	private static boolean doSavePic(String weblink, Book_Booklist entry) {
		try (BufferedInputStream in = new BufferedInputStream(new URI(weblink).toURL().openStream())) {

			BufferedImage originalImage = ImageIO.read(in);
			if (originalImage == null) {
				Mainframe.logger.error("Bild konnte nicht gelesen werden: {}", weblink);
				return false;
			}

			// Bei Bedarf auf Maximalhoehe skalieren (Seitenverhaeltnis beibehalten)
			BufferedImage scaledImage;
			if (originalImage.getHeight() > MAX_COVER_HEIGHT) {
				int newWidth = (int) ((double) MAX_COVER_HEIGHT / originalImage.getHeight() * originalImage.getWidth());
				scaledImage = new BufferedImage(newWidth, MAX_COVER_HEIGHT, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2 = scaledImage.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(originalImage, 0, 0, newWidth, MAX_COVER_HEIGHT, null);
				g2.dispose();
			} else {
				scaledImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
						BufferedImage.TYPE_INT_RGB);
				Graphics2D g2Copy = scaledImage.createGraphics();
				g2Copy.drawImage(originalImage, 0, 0, null);
				g2Copy.dispose();
			}

			// JPEG mit guter Qualitaet speichern
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
			ImageWriter writer = writers.next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(JPEG_QUALITY);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
				writer.setOutput(ios);
				writer.write(null, new IIOImage(scaledImage, null, null), param);
			}

			byte[] imageData = baos.toByteArray();
			entry.setPic(scaledImage);
			entry.setPicSizeBytes(imageData.length);

			Mainframe.executor.submit(() -> {
				try (ByteArrayInputStream dbStream = new ByteArrayInputStream(imageData)) {
					Database.updatePic(entry.getBid(), dbStream);
				} catch (IOException e) {
					Mainframe.logger.error(e.getMessage());
				}
			});

			return true;

		} catch (URISyntaxException | IOException e) {
			Mainframe.logger.error(e.getMessage());
			return false;
		}
	}

	/**
	 * URL-encode a String for a Web request
	 */
	private static String sanitizeString(String input) {
		return URLEncoder.encode(input, StandardCharsets.UTF_8);
	}

}
