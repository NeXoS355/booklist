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
import gui.Mainframe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HandleWebInfo {

	public static boolean DownloadWebPage(Book_Booklist eintrag, int maxResults) {
		boolean ret = false;
		try {
			String titel = eintrag.getTitel().replace(" ", "+");

			// Die URL der REST-API
			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + titel + "&maxResults=" + maxResults
					+ "&printType=books";

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
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret = true;
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
								cCover = 1;
								String link = imageLinks.get("smallThumbnail").getAsString();
								System.out.println("Link: " + link);
								// Downloading Image
								savePic(link, eintrag);

							} else {
								System.out.println("smallThumbnail nicht gefunden");
							}
						} else {
							System.out.println("ImageLink nicht gefunden.");
						}
						if (volumeInfo.has("industryIdentifiers") && cIsbn == 0) {
							var isbnidentifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
							for (int j = 0; j < isbnidentifiers.size(); j++) {
								var isbnidentifiers13 = isbnidentifiers.get(j).getAsJsonObject();
								if (isbnidentifiers13.has("identifier")) {
									String type = isbnidentifiers13.get("type").getAsString();
									if (type.equals("ISBN_13")) {
										cIsbn = 1;
										String isbn = isbnidentifiers13.get("identifier").getAsString();
										eintrag.setIsbn(isbn);
										Mainframe.executor.submit(() -> {
											Database.addIsbn(eintrag.getBid(), isbn);
										});
									} else {
										System.out.println("industryIdentifiers nicht gefunden");
									}
								}
							}

						}

						if (volumeInfo.has("description") && cDesc == 0) {
							cDesc = 1;
							String description = volumeInfo.get("description").getAsString();
							eintrag.setDesc(description);
							Mainframe.executor.submit(() -> {
								Database.addDesc(eintrag.getBid(), description);
							});
						} else {
							System.out.println("Description nicht gefunden.");
						}
					} else {
						System.out.println("Feld 'volumeInfo' nicht gefunden.");
					}
				} else {
					System.out.println("Keine Elemente in 'items' gefunden.");
				}
			} else {
				System.out.println("Feld 'items' nicht gefunden.");
			}
			i++;
		}
		if (cCover + cDesc + cIsbn < 3) {
			int antwort = JOptionPane.showConfirmDialog(null,
					"Es konnten nicht alle Informationen gefunden werden.\nKriterien erweitern?", "Warnung",
					JOptionPane.YES_NO_OPTION);
			if (antwort == JOptionPane.YES_OPTION) {
				DownloadWebPage(eintrag, 5);
			}
		}
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
				DownloadWebPage(eintrag, 2);
			}

		}
		return true;
	}

}
