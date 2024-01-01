package application;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import data.Database;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HandleWebInfo {

	public static boolean DownloadWebPage(Book_Booklist eintrag) {
		boolean ret = false;
		try {
			String titel = eintrag.getTitel().replace(" ", "+");
//			String autor = eintrag.getAutor().replace(" ", "+");

			// Die URL der REST-API
			String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=" + titel + "&maxResults=2&printType=books";

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

				// Auf den Titel zugreifen
				int i = 0;
				while (i < 2) {
					if (jsonObject.has("items")) {
						var itemsArray = jsonObject.getAsJsonArray("items");
						if (itemsArray.size() > 0) {
							var firstItem = itemsArray.get(i).getAsJsonObject();
							if (firstItem.has("volumeInfo")) {
								var volumeInfo = firstItem.getAsJsonObject("volumeInfo");
								if (volumeInfo.has("imageLinks")) {
									i = 99;
									var imageLinks = volumeInfo.getAsJsonObject("imageLinks");
									if (imageLinks.has("smallThumbnail")) {
										String link = imageLinks.get("smallThumbnail").getAsString();
										System.out.println("Link: " + link);
										// Downloading Image
										savePic(link, eintrag);
									} else {
										System.out.println("smallThumbnail nicht gefunden");
									}
								} else {
									System.out.println("ImageLink nicht gefunden.");
									i++;
								}
								if (volumeInfo.has("industryIdentifiers")) {
									var isbnidentifiers = volumeInfo.getAsJsonArray("industryIdentifiers");
									var isbnidentifiers13 = isbnidentifiers.get(0).getAsJsonObject();
									if (isbnidentifiers13.has("identifier")) {
										String isbn = isbnidentifiers13.get("identifier").getAsString();
										saveIsbn(isbn, eintrag);
									}
								}
								if (volumeInfo.has("description")) {
									String description = volumeInfo.get("description").getAsString();
									eintrag.setDesc(description);
									Database.addDesc(eintrag.getAutor(), eintrag.getTitel(), description);
								} else {
									System.out.println("Description nicht gefunden.");
									i++;
								}
							} else {
								System.out.println("Feld 'volumeInfo' nicht gefunden.");
								i++;
							}
						} else {
							System.out.println("Keine Elemente in 'items' gefunden.");
							i++;
						}
					} else {
						System.out.println("Feld 'items' nicht gefunden.");
						i++;
					}
				}
			}
			// Verbindung schließen
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret = true;
		return ret;

	}

	public static boolean deletePic(String autor, String titel) {
		return Database.delPic(autor, titel);

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
			out.close();
			in.close();
			byte[] response = out.toByteArray();
			String path = "tmp.jpg";
			FileOutputStream fos = new FileOutputStream(path);
			fos.write(response);
			fos.close();
			BufferedInputStream photoStream = new BufferedInputStream(new FileInputStream(path));
			Database.addPic(eintrag.getAutor(), eintrag.getTitel(), photoStream);
			photoStream.close();
			out.close();
			in.close();
			File file = new File(path);
			if (file.exists()) {
				file.delete();
				System.out.println("Temp File deleted");
			}
			ResultSet rs = Database.getPic(eintrag.getAutor(), eintrag.getTitel());
			try {
				while (rs.next()) {
					Blob picture = rs.getBlob("pic");
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					eintrag.setPic(buf_pic);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			weblink = JOptionPane.showInputDialog(null, "Kein Bild gefunden. Bitte manuell einf�gen");
			if (weblink != "") {
				DownloadWebPage(eintrag);
			}

		}
		return true;
	}
	
	private static boolean saveIsbn(String isbn, Book_Booklist eintrag) {
		Database.addIsbn(eintrag.getAutor(), eintrag.getTitel(), isbn);
		eintrag.setIsbn(isbn);
		return true;
	}

}
