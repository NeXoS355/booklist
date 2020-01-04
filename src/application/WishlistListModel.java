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

public class WishlistListModel extends AbstractListModel<Book> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ArrayList<Book> b�cher = new ArrayList<Book>();
	public static ArrayList<String> autoren = new ArrayList<String>();

	public WishlistListModel() {
		Database.createConnection();
		ResultSet rs = Database.readDbWishlist();
		try {
			while (rs.next()) {
				try {
					String autor = rs.getString("autor").trim();
					String titel = rs.getString("titel").trim();
					String bemerkung = rs.getString("bemerkung").trim();
					String serie = rs.getString("serie").trim();
					String seriePart = rs.getString("seriePart");
					boolean ausgeliehen = false;
					Timestamp datum = rs.getTimestamp("date");
					Blob picture = rs.getBlob("pic");
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					if (rs.getString(3).equals("an")) {
						ausgeliehen = true;
						String ausgeliehen_an = rs.getString("name").trim();
						b�cher.add(new Book(autor, titel, ausgeliehen, ausgeliehen_an, "", bemerkung, serie, seriePart,
								buf_pic, datum, false));
					} else if (rs.getString(3).equals("von")) {
						ausgeliehen = true;
						String ausgeliehen_von = rs.getString("name").trim();
						b�cher.add(new Book(autor, titel, ausgeliehen, "", ausgeliehen_von, bemerkung, serie, seriePart,
								buf_pic, datum, false));
					} else {
						b�cher.add(new Book(autor, titel, bemerkung, serie, seriePart, buf_pic, ausgeliehen, datum,
								false));
					}
				} catch (DateTimeParseException ex1) {
					System.err.println("Datum falsch w�hrend DB auslesen");
				}

			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void autorenPr�fen() {
		autoren.clear();
		for (int i = 0; i < b�cher.size(); i++) {
			if (!autoren.contains(b�cher.get(i).getAutor()))
				autoren.add(b�cher.get(i).getAutor());
		}
		Mainframe.updateNode();
	}

	public void add(Book buch) {
		b�cher.add(buch);
		fireIntervalAdded(this, 0, b�cher.size());
		System.out.println("Buch hinzugef�gt: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(Book buch) {
		b�cher.remove(buch);
		fireIntervalRemoved(this, 0, b�cher.size());
		System.out.println("Buch gel�scht: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(int index) {
		Database.delete(b�cher.get(index).getAutor(), b�cher.get(index).getTitel());
		b�cher.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public static String[] getSerienVonAutor(String autor) {
		ArrayList<String> serien = new ArrayList<String>();
		
		for (int i = 0; i < b�cher.size(); i++) {
			Book buch = b�cher.get(i);
			if (buch.getAutor().contains(autor)) {
				if (!buch.getSerie().trim().equals("")) {
					boolean newSerie = true;
					for(int j=0;j < serien.size();j++) {
						if(serien.get(j).equals(buch.getSerie())) newSerie=false;
					}
					if (newSerie) serien.add(buch.getSerie());
					
				}
			}

		}
		String[] returnArr = new String[serien.size()];
		for (int i = 0; i < serien.size(); i++) {
			returnArr[i] = serien.get(i);
		}
		return returnArr;
	}

	public static boolean hatAutorSerie(String autor) {
		for (int i = 0; i < b�cher.size(); i++) {
			Book buch = b�cher.get(i);
			if (buch.getAutor().contains(autor)) {
				if (!buch.getSerie().trim().equals("")) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Book getElementAt(int arg0) {
		return b�cher.get(arg0);
	}

	public int getIndexOf(String searchAutor, String searchTitel) {
		for (int i = 0; i < b�cher.size(); i++) {
			Book eintrag = b�cher.get(i);
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
		return b�cher.size();
	}

	public int indexOf(Book buch) {
		return b�cher.indexOf(buch);
	}

}
