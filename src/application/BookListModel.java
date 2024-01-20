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

/**
 * Manages the Booklist and Authorlist
 */
public class BookListModel extends AbstractListModel<Book_Booklist> {

	private static final long serialVersionUID = 1L;
	private static ArrayList<Book_Booklist> books = new ArrayList<Book_Booklist>();
	public static ArrayList<String> authors = new ArrayList<String>();

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
					// necessary Variables which cannot be loaded onDemand
					Book_Booklist book = null;
					String author = rs.getString("autor").trim();
					String title = rs.getString("titel").trim();
					String series = rs.getString("serie").trim();
					String seriesVolume = rs.getString("seriePart");
					int int_ebook = rs.getInt("ebook");
					boolean ebook = false;
					if (int_ebook == 1)
						ebook = true;
					int rating = rs.getInt("rating");
					int bid = Integer.parseInt(rs.getString("bid"));

					// Empty Variables for LoadOnDemand
					String note = "";
					Blob picture = null;
					String desc = "";
					String isbn = "";
					Timestamp date = null;
					String borrowed = "";
					String borrowedTo = "";
					String borrowedFrom = "";
					boolean boolBorrowed = false;

					if (HandleConfig.loadOnDemand == 0) {
						note = rs.getString("bemerkung");
						picture = rs.getBlob("pic");
						desc = rs.getString("description");
						date = rs.getTimestamp("date");
						isbn = rs.getString("isbn");
						borrowed = rs.getString("ausgeliehen");
						boolBorrowed = false;
						if (borrowed.equals("an")) {
							boolBorrowed = true;
							borrowedTo = rs.getString("name").trim();
						} else if (borrowed.equals("von")) {
							boolBorrowed = true;
							borrowedFrom = rs.getString("name").trim();
						}
					}
					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}
					book = new Book_Booklist(author, title, boolBorrowed, borrowedTo, borrowedFrom, note, series,
							seriesVolume, ebook, rating, buf_pic, desc, isbn, date, false);
					book.setBid(bid);
					getBooks().add(book);
					Mainframe.logger.trace("Buch ausgelesen: " + book.getAuthor() + "-" + book.getTitle());
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

	/**
	 * This method loads the extended informations which are not loaded on Startup
	 * if "loadOnDemand = 1".
	 * 
	 * @param book - load values of this Book Entry
	 * 
	 */
	public static void loadOnDemand(Book_Booklist book) {
		if (book.getDesc() == "" && book.getPic() == null) {
			try {
				ResultSet rs = Database.selectFromBooklist(book.getBid());
				while (rs.next()) {
					String note = rs.getString("bemerkung");
					Blob picture = rs.getBlob("pic");
					String desc = rs.getString("description");
					Timestamp datum = rs.getTimestamp("date");
					String isbn = rs.getString("isbn");
					int rating = rs.getInt("rating");

					Image buf_pic = null;
					if (picture != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(picture.getBinaryStream());
						buf_pic = ImageIO.read(bis_pic).getScaledInstance(200, 300, Image.SCALE_FAST);
					}

					book.setPic(buf_pic);
					book.setDesc(desc, false);
					book.setNote(note);
					book.setDate(datum);
					book.setIsbn(isbn, false);
					book.setRating(rating, false);

					String borrowed = rs.getString("ausgeliehen");
					boolean boolBorrowed = false;
					if (borrowed.equals("an")) {
						boolBorrowed = true;
						String borrowedTo = rs.getString("name").trim();
						book.setBorrowedTo(borrowedTo);
						book.setBorrowed(boolBorrowed);
					} else if (borrowed.equals("von")) {
						boolBorrowed = true;
						String borrowedFrom = rs.getString("name").trim();
						book.setBorrowedFrom(borrowedFrom);
						book.setBorrowed(boolBorrowed);
					}
					Mainframe.logger.info("loading Book info: " + book.getAuthor() + "-" + book.getTitle());
				}
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			} catch (IOException e) {
				Mainframe.logger.error(e.getMessage());
			}
		}
	}

	/**
	 * updates the author list and updates the displayed Tree
	 * 
	 */
	public static void checkAuthors() {
		authors.clear();
		try {
			ResultSet rs = Database.getColumnFromBooklist("autor");
			while (rs.next()) {
				String author = rs.getString(1);
				authors.add(author);
			}
			Mainframe.logger.trace("Updated Author List");
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		Mainframe.updateNode();
	}

	/**
	 * Adds book to Booklist
	 * 
	 * @param book - Book Object
	 * 
	 */
	public void add(Book_Booklist book) {
		books.add(book);
		fireIntervalAdded(this, 0, books.size());
		Mainframe.logger.info("Booklist Buch hinzugefügt: " + book.getAuthor() + "," + book.getTitle());
	}

	/**
	 * Deletes book from Booklist
	 * 
	 * @param book - Book Object
	 * 
	 */
	public void delete(Book_Booklist book) {
		getBooks().remove(book);
		fireIntervalRemoved(this, 0, getBooks().size());
		Mainframe.logger.info("Booklist Buch gelöscht: " + book.getAuthor() + "," + book.getTitle());
	}

	/**
	 * Deletes book from Booklist
	 * 
	 * @param index - index of Book in List
	 * 
	 */
	public void delete(int index) {
		Database.deleteFromBooklist(getBooks().get(index).getBid());
		getBooks().remove(index);
		fireIntervalRemoved(this, index, index);
	}

	/**
	 * Gets all distinct series from a specific author
	 * 
	 * @param author - Full name of Author
	 * 
	 * @return String Array with all distinct series of the specified author
	 */
	public static String[] getSeriesFromAuthor(String author) {

		ArrayList<String> seriesList = new ArrayList<String>();
		try {
			ResultSet rs = Database.getColumnFromBooklist("serie", "autor", author);
			while (rs.next()) {
				String series = rs.getString(1);
				if (!series.isEmpty())
					seriesList.add(series);
			}
			Mainframe.logger.trace("Got Series from Author: " + author);
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}

		String[] returnArr = new String[seriesList.size()];
		for (int i = 0; i < seriesList.size(); i++) {
			returnArr[i] = seriesList.get(i);
		}
		return returnArr;
	}

	/**
	 * checks if an author has a series
	 * 
	 * @param author - Full name of Author
	 * 
	 * @return "true" of has series else "false"
	 */
	public static boolean authorHasSeries(String author) {
		try {
			ResultSet rs = Database.getColumnFromBooklist("serie", "autor", author);
			while (rs.next()) {
				String series = rs.getString(1);
				if (!series.isEmpty()) {
					Mainframe.logger.trace("Author '" + author + "' has minimum 1 Series");
					return true;
				}
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return false;
	}

	/**
	 * get Element at specific index
	 * 
	 * @param arg0 - index which to get
	 * 
	 * @return Book Object at specified index
	 */
	@Override
	public Book_Booklist getElementAt(int arg0) {
		return getBooks().get(arg0);
	}

	public int getIndexOf(String searchAuthor, String searchTitle) {
		for (int i = 0; i < getBooks().size(); i++) {
			Book_Booklist entry = getBooks().get(i);
			String author = entry.getAuthor().toUpperCase();
			String title = entry.getTitle().toUpperCase();
			if (author.equals(searchAuthor.toUpperCase()) && title.equals(searchTitle.toUpperCase())) {
				return i;
			}
		}
		return 0;

	}

	/**
	 * get size of Booklist
	 * 
	 * @return size of Booklist
	 */
	@Override
	public int getSize() {
		return getBooks().size();
	}

	/**
	 * get index of specific Book
	 * 
	 * @return index of Book in list
	 */
	public int indexOf(Book_Booklist book) {
		return getBooks().indexOf(book);
	}

	/**
	 * get whole Booklist
	 * 
	 * @return ArrayList with all Books
	 */
	public static ArrayList<Book_Booklist> getBooks() {
		return books;
	}

	/**
	 * check all series from author and add missing entries to wishlist
	 * 
	 * @param author - Full name of author
	 */
	public static void analyzeAuthor(String author) {
		ResultSet rs = Database.getSeriesInfo(author);
		String[] series = new String[10];
		int[] maxPart = new int[30];
		String oldSeries = "";
		int newSeries = 1;
		int i = 0;
		int rowCount = 0;

		try {
			while (rs.next()) {
				String realSeries = rs.getString("serie").trim();
				int seriesVolume = rs.getInt("seriePart");

				if (!realSeries.equals(oldSeries) && rowCount != 0) {
					i++;
					newSeries = 1;
				}

				series[i] = realSeries;
				if (seriesVolume > maxPart[i] || newSeries == 1) {
					maxPart[i] = seriesVolume;

				}

				newSeries = 0;
				oldSeries = realSeries;
				rowCount++;
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}

		boolean found = false;
		for (i = 0; i < series.length; i++) {
			if (series[i] != null) {
				for (int j = 1; j < maxPart[i]; j++) {
					for (int k = 0; k < books.size(); k++) {
						Book_Booklist book = books.get(k);
						if (book.getSeries().equals(series[i]) && Integer.parseInt(book.getSeriesVol()) == j) {
							found = true;
						}

					}
					if (!found) {
						try {
							System.out.println("Serie: " + series[i] + " fehlender Part: " + j);
							wishlist.wishlistEntries.add(new Book_Wishlist(author, Integer.toString(j), "", series[i],
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
