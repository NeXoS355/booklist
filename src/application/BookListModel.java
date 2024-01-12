package application;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;

import data.Database;
import gui.Mainframe;
import gui.wishlist;

public class BookListModel extends AbstractListModel<Book_Booklist> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ArrayList<Book_Booklist> bücher = new ArrayList<Book_Booklist>();
	public static ArrayList<String> autoren = new ArrayList<String>();

	public BookListModel() {
		Database.createConnection();
		ResultSet rs = null;
		if (HandleConfig.loadOnDemand == 1) {
			Mainframe.logger.info("Reading Database Lite");
			rs = Database.readDbBooklistLite();
		} else {
			Mainframe.logger.info("Reading Database Full");
			rs = Database.readDbBooklist();
		}
		try {
			while (rs.next()) {
				try {
					// necessary Variables cannot be loaded onDemand
					Book_Booklist book = null;
					String autor = rs.getString("autor").trim();
					String titel = rs.getString("titel").trim();
					String bemerkung = rs.getString("bemerkung");
					String serie = rs.getString("serie").trim();
					String seriePart = rs.getString("seriePart");
					int bid = Integer.parseInt(rs.getString("bid"));

					// Variables for LoadOnDemand
					Blob picture = null;
					String desc = "";
					String isbn = "";
					Timestamp datum = null;
					boolean ebook = false;
					String ausgeliehen = "";
					String ausgeliehen_an = "";
					String ausgeliehen_von = "";
					boolean boolAusgeliehen = false;

					if (HandleConfig.loadOnDemand == 0) {
						picture = rs.getBlob("pic");
						desc = rs.getString("description");
						int int_ebook = rs.getInt("ebook");
						ebook = false;
						if (int_ebook == 1)
							ebook = true;
						datum = rs.getTimestamp("date");
						isbn = rs.getString("isbn");
						ausgeliehen = rs.getString("ausgeliehen");
						boolAusgeliehen = false;
						if (ausgeliehen.equals("an")) {
							boolAusgeliehen = true;
							ausgeliehen_an = rs.getString("name").trim();
						} else if (ausgeliehen.equals("von")) {
							boolAusgeliehen = true;
							ausgeliehen_von = rs.getString("name").trim();
						}
					}
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					book = new Book_Booklist(autor, titel, boolAusgeliehen, ausgeliehen_an, ausgeliehen_von, bemerkung,
							serie, seriePart, ebook, buf_pic, desc, isbn, datum, false);
					book.setBid(bid);
					getBücher().add(book);
					Mainframe.logger.trace("Buch ausgelesen: " + book.getAutor() + "-" + book.getTitel());
					if (bid > Database.highestBid) {
						Database.highestBid = bid;
					}
				} catch (DateTimeParseException e) {
					Mainframe.logger.error(e);
				}
			}
		} catch (SQLException | IOException e) {
			Mainframe.logger.error(e.getMessage());
		}
	}

	public static void loadOnDemand(Book_Booklist book) {
		if (book.getDesc() == "" && book.getPic() == null) {
			try {
				ResultSet rs = Database.selectFromBooklist(book.getBid());
				while (rs.next()) {
					Blob picture = rs.getBlob("pic");
					String desc = rs.getString("description");
					int int_ebook = rs.getInt("ebook");
					boolean ebook = false;
					if (int_ebook == 1)
						ebook = true;
					Timestamp datum = rs.getTimestamp("date");
					String isbn = rs.getString("isbn");
					int rating = rs.getInt("rating");

					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}

					book.setPic(buf_pic);
					book.setDesc(desc);
					book.setEbook(ebook);
					book.setDatum(datum);
					book.setIsbn(isbn);
					book.setRating(rating);

					String ausgeliehen = rs.getString("ausgeliehen");
					boolean boolAusgeliehen = false;
					if (ausgeliehen.equals("an")) {
						boolAusgeliehen = true;
						String ausgeliehen_an = rs.getString("name").trim();
						book.setAusgeliehen_an(ausgeliehen_an);
						book.setAusgeliehen(boolAusgeliehen);
					} else if (ausgeliehen.equals("von")) {
						boolAusgeliehen = true;
						String ausgeliehen_von = rs.getString("name").trim();
						book.setAusgeliehen_von(ausgeliehen_von);
						book.setAusgeliehen(boolAusgeliehen);
					}
					Mainframe.logger.info("loading Book info: " + book.getAutor() + "-" + book.getTitel());
				}
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			} catch (IOException e) {
				Mainframe.logger.error(e.getMessage());
			}
		}
	}

	public static void autorenPrüfen() {
		autoren.clear();
		for (int i = 0; i < getBücher().size(); i++) {
			if (!autoren.contains(getBücher().get(i).getAutor()))
				autoren.add(getBücher().get(i).getAutor());
		}
		Mainframe.updateNode();
	}

	public void add(Book_Booklist buch) {
		getBücher().add(buch);
		fireIntervalAdded(this, 0, getBücher().size());
		System.out.println("Booklist Buch hinzugefügt: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(Book_Booklist buch) {
		getBücher().remove(buch);
		fireIntervalRemoved(this, 0, getBücher().size());
		System.out.println("Booklist Buch gelöscht: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(int index) {
		Database.deleteFromBooklist(getBücher().get(index).getBid());
		getBücher().remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public static String[] getSerienVonAutor(String autor) {
		ArrayList<String> serien = new ArrayList<String>();

		for (int i = 0; i < getBücher().size(); i++) {
			Book_Booklist buch = getBücher().get(i);
			if (buch.getAutor().contains(autor)) {
				if (!buch.getSerie().trim().equals("")) {
					boolean newSerie = true;
					for (int j = 0; j < serien.size(); j++) {
						if (serien.get(j).equals(buch.getSerie()))
							newSerie = false;
					}
					if (newSerie)
						serien.add(buch.getSerie());

				}
			}

		}
		String[] returnArr = new String[serien.size()];
		for (int i = 0; i < serien.size(); i++) {
			returnArr[i] = serien.get(i);
		}
		return returnArr;
	}

	public static int[] getBuecherVonAutor(String autor) {
		ArrayList<Integer> buecher = new ArrayList<Integer>();

		for (int i = 0; i < getBücher().size(); i++) {
			Book_Booklist buch = getBücher().get(i);
			if (buch.getAutor().contains(autor)) {
				buecher.add(buch.getBid());
			}
		}
		int[] returnArr = new int[buecher.size()];
		for (int i = 0; i < buecher.size(); i++) {
			returnArr[i] = buecher.get(i);
		}
		return returnArr;

	}

	public static boolean hatAutorSerie(String autor) {
		for (int i = 0; i < getBücher().size(); i++) {
			Book_Booklist buch = getBücher().get(i);
			if (buch.getAutor().contains(autor)) {
				if (!buch.getSerie().trim().equals("")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Book_Booklist getElementAt(int arg0) {
		return getBücher().get(arg0);
	}

	public int getIndexOf(String searchAutor, String searchTitel) {
		for (int i = 0; i < getBücher().size(); i++) {
			Book_Booklist eintrag = getBücher().get(i);
			String autor = eintrag.getAutor().toUpperCase();
			String titel = eintrag.getTitel().toUpperCase();
			if (autor.equals(searchAutor.toUpperCase()) && titel.equals(searchTitel.toUpperCase())) {
				return i;
			}
		}
		return 0;

	}

	@Override
	public int getSize() {
		return getBücher().size();
	}

	public int indexOf(Book_Booklist buch) {
		return getBücher().indexOf(buch);
	}

	public static ArrayList<Book_Booklist> getBücher() {
		return bücher;
	}

	public static void analyseAuthor(String author) {
		ResultSet rs = Database.analyzeAuthor(author);
		String[] serien = new String[10];
		int[] minPart = new int[30];
		int[] maxPart = new int[30];
		String oldSerie = "";
		int newSerie = 1;
		int i = 0;
		int rowCount = 0;

		try {
			while (rs.next()) {
				String serie = rs.getString("serie").trim();
				int seriePart = rs.getInt("seriePart");

				if (!serie.equals(oldSerie) && rowCount != 0) {
					i++;
					newSerie = 1;
				}

				serien[i] = serie;
				if (seriePart > maxPart[i] || newSerie == 1) {
					maxPart[i] = seriePart;

				}
				if (seriePart < minPart[i] || newSerie == 1) {
					minPart[i] = seriePart;
				}

				newSerie = 0;
				oldSerie = serie;
				rowCount++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean found = false;
		for (i = 0; i < serien.length; i++) {
			if (serien[i] != null) {
				for (int j = minPart[i] + 1; j < maxPart[i]; j++) {
					for (int k = 0; k < bücher.size(); k++) {
						Book_Booklist buch = bücher.get(k);
						if (buch.getSerie().equals(serien[i]) && Integer.parseInt(buch.getSeriePart()) == j) {
							found = true;
						}

					}
					if (!found) {
						try {
							System.out.println("Serie: " + serien[i] + " fehlender Part: " + j);
							wishlist.Wishlisteinträge.add(new Book_Wishlist(author, "Titel "+j, "", serien[i],
									String.valueOf(j), new Timestamp(System.currentTimeMillis()), true));
						} catch (SQLException e) {
							Mainframe.logger.info(e.getMessage());
						}
					}
					found = false;
				}

			}
		}
	}

}
