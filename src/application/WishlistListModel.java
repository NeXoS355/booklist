package application;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.AbstractListModel;

import data.Database;

public class WishlistListModel extends AbstractListModel<Book_Wishlist> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ArrayList<Book_Wishlist> b�cher = new ArrayList<Book_Wishlist>();
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
					Timestamp datum = rs.getTimestamp("date");
					b�cher.add(new Book_Wishlist(autor, titel, bemerkung, serie,
							seriePart, datum, false));

				} catch (DateTimeParseException ex1) {
					System.err.println("Datum falsch w�hrend DB auslesen");
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void add(Book_Wishlist buch) {
		b�cher.add(buch);
		fireIntervalAdded(this, 0, b�cher.size());
		System.out.println("Buch hinzugef�gt: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(Book_Wishlist buch) {
		b�cher.remove(buch);
		fireIntervalRemoved(this, 0, b�cher.size());
		System.out.println("Buch gel�scht: " + buch.getAutor() + "," + buch.getTitel());
	}

	public void delete(int index) {
		Database.deleteFromBooklist(b�cher.get(index).getAutor(), b�cher.get(index).getTitel());
		b�cher.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public Book_Wishlist getElementAt(int arg0) {
		return b�cher.get(arg0);
	}

	public int getIndexOf(String searchAutor, String searchTitel) {
		for (int i = 0; i < b�cher.size(); i++) {
			Book_Wishlist eintrag = b�cher.get(i);
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

	public int indexOf(Book_Wishlist buch) {
		return b�cher.indexOf(buch);
	}

}
