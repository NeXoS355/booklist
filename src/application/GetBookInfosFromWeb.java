package application;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import data.Database;
import gui.Mainframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GetBookInfosFromWeb {
	
	/**
	 * request Book Infos from Google API to save Covers, description and ISBN No.
	 * 
	 * @param entry - Book entry to search for
	 * @param maxResults - how many Results should be requested from Google Books API
	 * @param retry - change Setting if first request was not sufficient
	 */
	public static int getBookInfoFromGoogleApiWebRequest(Book_Booklist entry, int maxResults, boolean retry) {
		int compareReturn = 0;
		try {

			if (retry) {
				if (HandleConfig.searchParam.equals("t")) {
					HandleConfig.searchParam = "at";
					Mainframe.logger.info("changed searchParam to " + HandleConfig.searchParam);
				} else if (HandleConfig.searchParam.equals("at")) {
					HandleConfig.searchParam = "t";
					Mainframe.logger.info("changed searchParam to " + HandleConfig.searchParam);
				}
			}

			StringBuilder str = new StringBuilder();
			if (HandleConfig.searchParam.equals("at")) {
				str.append(sanitizeString(entry.getAuthor()) + "+");
			}
			str.append(sanitizeString(entry.getTitle()));

			// URL of REST-API
			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + str.toString() + "&maxResults="
					+ maxResults + "&printType=books";

			Mainframe.logger.info("Search API: " + entry.toString().toString());
			Mainframe.logger.info("Search API URL: " + apiUrl);

			URL url = new URI(apiUrl).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// open connection and check response Code
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// read InputStream and create String
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();

				// create JSONObject from response
				JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
				compareReturn = analyseApiRequestBookInfo(jsonObject, entry);
			}
			connection.disconnect();

		} catch (Exception e) {
			Mainframe.logger.error(e.getMessage());
		}
		Mainframe.logger.info("checkWebInfo " + "Retry: " + retry);
		Mainframe.logger.info(
				"checkWebInfo " + "Overall Score: " + entry.getAuthor() + "-" + entry.getTitle() + ":" + compareReturn);
		return compareReturn;

	}
	
	/**
	 * request Series Infos from Google API to save new Books from that series.
	 * 
	 * @param str - search String for API request
	 * @param maxResults - how many Results should be requested from Google Books API
	 */
	public static String[][] getSeriesInfoFromGoogleApiWebRequest(String str, int maxResults) {
		String[][] compareReturn = new String[3][10];
		try {
			
			str = sanitizeString(str);
			
			// URL of REST-API
			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + str + "&maxResults="
					+ maxResults + "&printType=books";

			Mainframe.logger.trace("Search API: " + str);
			Mainframe.logger.trace("Search API URL: " + apiUrl);

			URL url = new URI(apiUrl).toURL();
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			// open connection and check response Code
			int responseCode = connection.getResponseCode();
			Mainframe.logger.trace("Search API response: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// read InputStream and create String
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();

				// create JSONObject from response
				JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
				compareReturn = analyseApiRequestSeries(jsonObject, maxResults);
			}
			connection.disconnect();

		} catch (Exception e) {
			Mainframe.logger.error(e.getMessage());
		}
		return compareReturn;

	}
	
	/**
	 * Analyze the JSON response from Google Books API
	 * 
	 * @param jsonObject - JSON response from the request
	 * @param entry - Book entry to save the Information
	 */
	private static int analyseApiRequestBookInfo(JsonObject jsonObject, Book_Booklist entry) {
		int i = 0;
		int cCover = 0;
		int cDesc = 0;
		int cIsbn = 0;
		int cCompAuthor = 0;
		int cCompTitle = 0;
		while (i < 2 && cCover + cDesc + cIsbn < 3) {
			if (jsonObject.has("items")) {
				var itemsArray = jsonObject.getAsJsonArray("items");
				if (itemsArray.size() > 0) {
					var firstItem = itemsArray.get(i).getAsJsonObject();
					if (firstItem.has("volumeInfo")) {
						var volumeInfo = firstItem.getAsJsonObject("volumeInfo");
						if (volumeInfo.has("imageLinks") && cCover == 0) {
							var imageLinks = volumeInfo.getAsJsonObject("imageLinks");
							if (imageLinks.has("smallThumbnail")) {
								String link = imageLinks.get("smallThumbnail").getAsString();
								// Downloading Image
								savePic(link, entry);
								cCover = 1;
							} else {
								Mainframe.logger.trace("WebInfo Download: 'smallThumbnail' not found!");
							}
						} else {
							Mainframe.logger.trace("WebInfo Download: 'ImageLink' not found!");
						}
						if (volumeInfo.has("industryIdentifiers") && cIsbn == 0) {
							var isbnidentifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
							for (int j = 0; j < isbnidentifiers.size(); j++) {
								var isbnidentifiers13 = isbnidentifiers.get(j).getAsJsonObject();
								if (isbnidentifiers13.has("identifier")) {
									String type = isbnidentifiers13.get("type").getAsString();
									if (type.equals("ISBN_13")) {
										String isbn = isbnidentifiers13.get("identifier").getAsString();
										cIsbn = 1;
										Mainframe.executor.submit(() -> {
											entry.setIsbn(isbn, true);
										});
									}
								}
							}

						} else {
							Mainframe.logger.trace("WebInfo Download: 'industryIdentifiers' not found!");
						}
						if (volumeInfo.has("title")) {
							String title = volumeInfo.get("title").getAsString();
							cCompTitle = compareString(title, entry.getTitle());
						}
						if (volumeInfo.has("authors")) {
							var authors = volumeInfo.getAsJsonArray("authors");
							String author = authors.get(0).getAsString();
							cCompAuthor = compareString(author, entry.getAuthor());
						}

						if (volumeInfo.has("description") && cDesc == 0) {
							String description = volumeInfo.get("description").getAsString();
							cDesc = 1;
							Mainframe.executor.submit(() -> {
								entry.setDesc(description, true);
							});
						} else {
							Mainframe.logger.trace("WebInfo Download: 'description' not found!");
						}
					} else {
						Mainframe.logger.trace("WebInfo Download: 'VolumeInfo' not found!");
					}
				} else {
					Mainframe.logger.trace("WebInfo Download: no elements found in 'items'!");
				}
			} else {
				Mainframe.logger.trace("WebInfo Download: 'items' not found!");
			}
			i++;
		}
		return (cCompAuthor + cCompTitle) / 2;
	}
	
	/**
	 * Analyze the JSON response from Google Books API
	 * 
	 * @param jsonObject - JSON response from the request
	 * @param maxResults - how many Results were requested from Google Books API
	 */
	private static String[][] analyseApiRequestSeries(JsonObject jsonObject, int maxResults) {
		int i = 0;
		String[][] returnArray = new String[maxResults][3];
		while (i < maxResults) {
			if (jsonObject.has("items")) {
				var itemsArray = jsonObject.getAsJsonArray("items");
				if (itemsArray.size() > 0) {
					var firstItem = itemsArray.get(i).getAsJsonObject();
					if (firstItem.has("volumeInfo")) {
						var volumeInfo = firstItem.getAsJsonObject("volumeInfo");
						if (volumeInfo.has("industryIdentifiers")) {
							
							var isbnidentifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
							for (int j = 0; j < isbnidentifiers.size(); j++) {
								var isbnidentifiers13 = isbnidentifiers.get(j).getAsJsonObject();
								if (isbnidentifiers13.has("identifier")) {
									String type = isbnidentifiers13.get("type").getAsString();
									if (type.equals("ISBN_13")) {
										returnArray[i][2] = isbnidentifiers13.get("identifier").getAsString();										
									}
								}
							}

						} else {
							Mainframe.logger.trace("WebInfo Download: 'industryIdentifiers' not found!");
						}
						if (volumeInfo.has("title")) {
							returnArray[i][1] = volumeInfo.get("title").getAsString();
//							cCompTitle = compareString(title[i], entry.getTitle());
						}
						if (volumeInfo.has("authors")) {
							var authors = volumeInfo.getAsJsonArray("authors");
							returnArray[i][0] = authors.get(0).getAsString();
//							cCompAuthor = compareString(author[i], entry.getAuthor());
						}
					} else {
						Mainframe.logger.trace("WebInfo Download: 'VolumeInfo' not found!");
					}
				} else {
					Mainframe.logger.trace("WebInfo Download: no elements found in 'items'!");
				}
			} else {
				Mainframe.logger.trace("WebInfo Download: 'items' not found!");
			}
			i++;
		}
		
		return returnArray;
	}

	/**
	 * Compare to Strings to rate the quality of the found Information
	 * 
	 * @param str1 - String to compare
	 * @param str2 - String to compare
	 * 
	 * @return int - returns a percentage Number of the comparison
	 */
	public static int compareString(String str1, String str2) {
		int equalPercentage = 0;
		int hit = 0;
		int counterBig = 0;
		int counterSmall = 0;
		boolean newWord = true;
		String small = "";
		String big = "";

		if (str1.length() > str2.length()) {
			big = str1;
			small = str2;
		} else {
			big = str2;
			small = str1;
		}

		int anzahl = big.length();
		for (; counterSmall < small.length();) {
			char smallChar = small.charAt(counterSmall);
			char bigChar = big.charAt(counterBig);
			while (smallChar == ' ' && bigChar != ' ') {
				counterBig++;
				bigChar = big.charAt(counterBig);
				newWord = true;
			}
			while (smallChar != ' ' && bigChar == ' ') {
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
				newWord = false;
			}
		}
		equalPercentage = hit * 100 / anzahl;
		Mainframe.logger.trace("checkWebInfo " + str1 + "-" + str2 + ": " + equalPercentage);

		return equalPercentage;
	}

	/**
	 * Delete a pic from an Book entry
	 * 
	 * @param bid - Bookid
	 * 
	 * @return boolean - returns boolean as success
	 */
	public static boolean deletePic(int bid) {
		return Database.delPic(bid);
	}

	/**
	 * Saves a pic from an Book entry
	 * 
	 * @param weblink - Link where to get the Bookcover
	 * @param entry - Bookentry to save the Cover
	 * 
	 * @return boolean - returns boolean as success
	 */
	public static boolean savePic(String weblink, Book_Booklist entry) {
		BufferedInputStream in;
		try {
			URL url = new URI(weblink).toURL();
			in = new BufferedInputStream(url.openStream());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int n = 0;
			while (-1 != (n = in.read(buf))) {
				out.write(buf, 0, n);
			}

			byte[] response = out.toByteArray();
			ByteArrayInputStream inStreambj = new ByteArrayInputStream(response);
			BufferedImage newImage = ImageIO.read(inStreambj);
			Image img = newImage;
			entry.setPic(img);

			BufferedInputStream photoStream = new BufferedInputStream(inStreambj);
			photoStream.close();
			out.close();
			in.close();

			String path = "tmp.jpg";
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(response);
			fos.close();
			BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path));

			Mainframe.executor.submit(() -> {
				try {
					Database.updatePic(entry.getBid(), stream);
					stream.close();
					Path file = Paths.get(path);
					Files.delete(file);
				} catch (IOException e) {
					Mainframe.logger.error(e.getMessage());
				}
			});

		} catch (MalformedURLException e) {
			Mainframe.logger.error(e.getMessage());
		} catch (IOException e) {
			Mainframe.logger.error(e.getMessage());
		} catch (URISyntaxException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return true;
	}

	/**
	 * Sanitize String for a Web request
	 * 
	 * @param input - String to sanitize
	 * 
	 * @return String - sanitized input String
	 */
	private static String sanitizeString(String input) {
		String newString = input.replace("\u00fc", "ue").replace("\u00f6", "oe").replace("\u00e4", "ae")
				.replace("\u00df", "ss").replaceAll("\u00dc(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ue")
				.replaceAll("\u00d6(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Oe")
				.replaceAll("\u00c4(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ae").replace("\u00dc", "UE")
				.replace("\u00d6", "OE").replace("\u00c4", "AE").replace(" ", "+");
		return newString;
	}

}
