package application;

import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.AbstractListModel;

import data.Database;
import gui.Mainframe;

public class WishlistListModel extends AbstractListModel<Book_Wishlist> {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final ArrayList<Book_Wishlist> books = new ArrayList<>();

	public WishlistListModel() {
		books.clear();
		Database.createConnection();
		ResultSet rs = Database.readDbWishlist();
		try {
			while (rs.next()) {
				try {
					String author = rs.getString("author").trim();
					String title = rs.getString("title").trim();
					String note = rs.getString("note").trim();
					String series = rs.getString("series").trim();
					int seriesVolInt = rs.getInt("series_vol");
					String seriesVol = rs.wasNull() ? "" : String.valueOf(seriesVolInt);
					Timestamp date = rs.getTimestamp("added_date");
					int wid = rs.getInt("wid");
					Book_Wishlist book = new Book_Wishlist(author, title, note, series,
							seriesVol, date, false);
					book.setWid(wid);
					books.add(book);

				} catch (DateTimeParseException ex1) {
					System.err.println("Datum falsch waehrend DB auslesen");
				}

			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
		}
	}

	public void add(Book_Wishlist book) {
		books.add(book);
		fireIntervalAdded(this, 0, books.size());
		System.out.println("Wishlist Buch hinzugefuegt: " + book.getAuthor() + "," + book.getTitle());
	}

	public void delete(Book_Wishlist book) {
		books.remove(book);
		fireIntervalRemoved(this, 0, books.size());
		System.out.println("Wischlist Buch geloescht: " + book.getAuthor() + "," + book.getTitle());
	}

	public void delete(int index) {
		Database.deleteFromWishlist(books.get(index).getWid());
		books.remove(index);
		fireIntervalRemoved(this, index, index);
	}

	@Override
	public Book_Wishlist getElementAt(int arg0) {
		return books.get(arg0);
	}

	public int getIndexOf(String searchAuthor, String searchTitle) {
		for (int i = 0; i < books.size(); i++) {
			Book_Wishlist entry = books.get(i);
			String author = entry.getAuthor().toUpperCase();
			String title = entry.getTitle().toUpperCase();
			if (author.equals(searchAuthor.toUpperCase()) && title.equals(searchTitle.toUpperCase())) {
				return i;
			}
		}
		return 0;

	}

	@Override
	public int getSize() {
		return books.size();
	}

	public int indexOf(Book_Wishlist book) {
		return books.indexOf(book);
	}

}
