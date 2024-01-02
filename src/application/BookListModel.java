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
		if (Mainframe.loadOnDemand == 1) {
			rs = Database.readDbBooklistLite();
			System.out.println("loading on Demand");
		} else {
			rs = Database.readDbBooklist();
			System.out.println("load All");
		}
		try {
			while (rs.next()) {
				try {
					Book_Booklist book = null;
					String autor = rs.getString("autor").trim();
					String titel = rs.getString("titel").trim();
					String bemerkung = rs.getString("bemerkung");
					String serie = rs.getString("serie").trim();
					String seriePart = rs.getString("seriePart");
					boolean ausgeliehen = false;
					Timestamp datum = rs.getTimestamp("date");
					String isbn = rs.getString("isbn");
					int bid = Integer.parseInt(rs.getString("bid"));
					
					Blob picture = null;
					String desc = "";

					if (Mainframe.loadOnDemand == 0) {
						picture = rs.getBlob("pic");
						desc = rs.getString("description");
					}
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					if (rs.getString(3).equals("an")) {
						ausgeliehen = true;
						String ausgeliehen_an = rs.getString("name").trim();
						book = new Book_Booklist(autor, titel, ausgeliehen, ausgeliehen_an, "", bemerkung,
								serie, seriePart, buf_pic, desc, isbn, datum, false);
						book.setBid(bid);
						getBücher().add(book);
					} else if (rs.getString(3).equals("von")) {
						ausgeliehen = true;
						String ausgeliehen_von = rs.getString("name").trim();
						book = new Book_Booklist(autor, titel, ausgeliehen, "", ausgeliehen_von, bemerkung,
								serie, seriePart, buf_pic, desc, isbn, datum, false);
						book.setBid(bid);
						getBücher().add(book);
					} else {
						book = new Book_Booklist(autor, titel, bemerkung, serie, seriePart, buf_pic, desc,
								isbn, datum, false);
						book.setBid(bid);
						getBücher().add(book);
					}
					if (bid > Database.highestBid) {
						Database.highestBid = bid;
					}
				} catch (DateTimeParseException ex1) {
					System.err.println("Datum falsch während DB auslesen");
				}
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadOnDemand(Book_Booklist buch) {
		if (buch.getDesc() == "" && buch.getPic() == null) {
			System.out.println("loading Image and Description");
			try {
				ResultSet rs = Database.selectFromBooklist(buch.getBid());
				while (rs.next()) {
					Blob picture = rs.getBlob("pic");
					String desc = rs.getString("description");
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					buch.setPic(buf_pic);
					buch.setDesc(desc);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
		Database.deleteFromBooklist(getBücher().get(index).getAutor(), getBücher().get(index).getTitel());
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

}
