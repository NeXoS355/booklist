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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import data.Database;
import gui.HandleConfig;
import gui.Mainframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HandleWebInfo {

	public static boolean DownloadWebPage(Book_Booklist eintrag, int maxResults, boolean retry) {
		boolean ret = false;
		try {
			StringBuilder str = new StringBuilder();
			if (HandleConfig.searchParam.equals("at")) {
				str.append(sanitizeString(eintrag.getAutor()) + "+");
			}
			str.append(sanitizeString(eintrag.getTitel()));

			// Die URL der REST-API
			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + str.toString() + "&maxResults="
					+ maxResults + "&printType=books";

			System.out.println(apiUrl);

			// HttpURLConnection erstellen
			URL url = new URL(apiUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();

			// GET-Methode festlegen
			connection.setRequestMethod("GET");

			// Verbindung öffnen und Response-Code überprüfen
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// InputStream lesen und in einen String umwandeln
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				StringBuilder response = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				reader.close();

				// JSON-Antwort in ein JsonObject umwandeln
				JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
				analyseApiRequst(jsonObject, eintrag);
			}
			// Verbindung schließen
			connection.disconnect();

			if (retry) {
				if (HandleConfig.searchParam.equals("t")) {
					HandleConfig.searchParam = "at";
					System.out.println("changed searchParam to " + HandleConfig.searchParam);
				} else if (HandleConfig.searchParam.equals("at")) {
					HandleConfig.searchParam = "t";
					System.out.println("changed searchParam to " + HandleConfig.searchParam);
				}
			}

			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ret;

	}

	private static void analyseApiRequst(JsonObject jsonObject, Book_Booklist eintrag) {
		// Auf den Titel zugreifen
		int i = 0;
		int cCover = 0;
		int cDesc = 0;
		int cIsbn = 0;
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
//								System.out.println("Link: " + link);
								// Downloading Image
								savePic(link, eintrag);
								cCover = 1;
							} else {
//								System.out.println("smallThumbnail nicht gefunden");
							}
						} else {
//							System.out.println("ImageLink nicht gefunden.");
						}
						if (volumeInfo.has("industryIdentifiers") && cIsbn == 0) {
							var isbnidentifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
							for (int j = 0; j < isbnidentifiers.size(); j++) {
								var isbnidentifiers13 = isbnidentifiers.get(j).getAsJsonObject();
								if (isbnidentifiers13.has("identifier")) {
									String type = isbnidentifiers13.get("type").getAsString();
									if (type.equals("ISBN_13")) {
										String isbn = isbnidentifiers13.get("identifier").getAsString();
										eintrag.setIsbn(isbn);
										cIsbn = 1;
										Mainframe.executor.submit(() -> {
											Database.addIsbn(eintrag.getBid(), isbn);
										});
									} else {
//										System.out.println("industryIdentifiers nicht gefunden");
									}
								}
							}

						}

						if (volumeInfo.has("description") && cDesc == 0) {
							String description = volumeInfo.get("description").getAsString();
							eintrag.setDesc(description);
							cDesc = 1;
							Mainframe.executor.submit(() -> {
								Database.addDesc(eintrag.getBid(), description);
							});
						} else {
//							System.out.println("Description nicht gefunden.");
						}
					} else {
//						System.out.println("Feld 'volumeInfo' nicht gefunden.");
					}
				} else {
//					System.out.println("Keine Elemente in 'items' gefunden.");
				}
			} else {
//				System.out.println("Feld 'items' nicht gefunden.");
			}
			i++;
		}

	}

	public static boolean checkDownload() {
		int antwort = JOptionPane.showConfirmDialog(null, "Ist das Buch korrekt?", "Warnung",
				JOptionPane.YES_NO_OPTION);
		boolean ret = (antwort == JOptionPane.NO_OPTION);
		if (ret) {
			if (HandleConfig.searchParam.equals("t")) {
				HandleConfig.searchParam = "at";
				System.out.println("changed searchParam to " + HandleConfig.searchParam);
			} else if (HandleConfig.searchParam.equals("at")) {
				HandleConfig.searchParam = "t";
				System.out.println("changed searchParam to " + HandleConfig.searchParam);
			}
		}
		return ret;
	}

	public static boolean deletePic(int bid) {
		return Database.delPic(bid);
	}

	public static boolean savePic(String weblink, Book_Booklist eintrag) {
		BufferedInputStream in;
		try {
			URL url = new URL(weblink);
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
			eintrag.setPic(img);

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
					Database.addPic(eintrag.getBid(), stream);
					stream.close();
					Path file = Paths.get(path);
					Files.delete(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			weblink = JOptionPane.showInputDialog(null, "Kein Bild gefunden. Bitte manuell einf�gen");
			if (weblink != "") {
				DownloadWebPage(eintrag, 2, false);
			}

		}
		return true;
	}
	
	private static String sanitizeString(String input) {
	    String newString = input.replace("\u00fc", "ue")
	            .replace("\u00f6", "oe")
	            .replace("\u00e4", "ae")
	            .replace("\u00df", "ss")
	            .replaceAll("\u00dc(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ue")
	            .replaceAll("\u00d6(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Oe")
	            .replaceAll("\u00c4(?=[a-z\u00e4\u00f6\u00fc\u00df ])", "Ae")
	            .replace("\u00dc", "UE")
	            .replace("\u00d6", "OE")
	            .replace("\u00c4", "AE")
	            .replace(" ", "+");
	    return newString;
	}

}
