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
					// necessary Variables cannot be loaded onDemand
					Book_Booklist book = null;
					String author = rs.getString("autor").trim();
					String title = rs.getString("titel").trim();
					String note = rs.getString("bemerkung");
					String series = rs.getString("serie").trim();
					String seriesVolume = rs.getString("seriePart");
					int bid = Integer.parseInt(rs.getString("bid"));

					// Variables for LoadOnDemand
					Blob picture = null;
					String desc = "";
					String isbn = "";
					Timestamp date = null;
					boolean ebook = false;
					String borrowed = "";
					String borrowedTo = "";
					String borrowedFrom = "";
					boolean boolBorrowed = false;

					if (HandleConfig.loadOnDemand == 0) {
						picture = rs.getBlob("pic");
						desc = rs.getString("description");
						int int_ebook = rs.getInt("ebook");
						ebook = false;
						if (int_ebook == 1)
							ebook = true;
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
					book = new Book_Booklist(author, title, boolBorrowed, borrowedTo, borrowedFrom, note,
							series, seriesVolume, ebook, buf_pic, desc, isbn, date, false);
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
					book.setDate(datum);
					book.setIsbn(isbn);
					book.setRating(rating);

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

	public static void checkAuthors() {
		authors.clear();
		for (int i = 0; i < getBooks().size(); i++) {
			if (!authors.contains(getBooks().get(i).getAuthor()))
				authors.add(getBooks().get(i).getAuthor());
		}
		Mainframe.updateNode();
	}

	public void add(Book_Booklist buch) {
		books.add(buch);
		fireIntervalAdded(this, 0, books.size());
		System.out.println("Booklist Buch hinzugefügt: " + buch.getAuthor() + "," + buch.getTitle());
	}

	public void delete(Book_Booklist buch) {
		getBooks().remove(buch);
		fireIntervalRemoved(this, 0, getBooks().size());
		System.out.println("Booklist Buch gelöscht: " + buch.getAuthor() + "," + buch.getTitle());
	}

	public void delete(int index) {
		Database.deleteFromBooklist(getBooks().get(index).getBid());
		getBooks().remove(index);
		fireIntervalRemoved(this, index, index);
	}

	public static String[] getSeriesFromAuthor(String author) {
		ArrayList<String> series = new ArrayList<String>();

		for (int i = 0; i < getBooks().size(); i++) {
			Book_Booklist book = getBooks().get(i);
			if (book.getAuthor().contains(author)) {
				if (!book.getSeries().trim().equals("")) {
					boolean newSeries = true;
					for (int j = 0; j < series.size(); j++) {
						if (series.get(j).equals(book.getSeries()))
							newSeries = false;
					}
					if (newSeries)
						series.add(book.getSeries());

				}
			}

		}
		String[] returnArr = new String[series.size()];
		for (int i = 0; i < series.size(); i++) {
			returnArr[i] = series.get(i);
		}
		return returnArr;
	}

	public static int[] getBooksFromAuthor(String author) {
		ArrayList<Integer> books = new ArrayList<Integer>();

		for (int i = 0; i < getBooks().size(); i++) {
			Book_Booklist book = getBooks().get(i);
			if (book.getAuthor().contains(author)) {
				books.add(book.getBid());
			}
		}
		int[] returnArr = new int[books.size()];
		for (int i = 0; i < books.size(); i++) {
			returnArr[i] = books.get(i);
		}
		return returnArr;

	}

	public static boolean authorHasSeries(String author) {
		for (int i = 0; i < getBooks().size(); i++) {
			Book_Booklist book = getBooks().get(i);
			if (book.getAuthor().contains(author)) {
				if (!book.getSeries().trim().equals("")) {
					return true;
				}
			}
		}
		return false;
	}

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

	@Override
	public int getSize() {
		return getBooks().size();
	}

	public int indexOf(Book_Booklist book) {
		return getBooks().indexOf(book);
	}

	public static ArrayList<Book_Booklist> getBooks() {
		return books;
	}

	public static void analyzeAuthor(String author) {
		ResultSet rs = Database.getSeriesInfo(author);
		String[] series = new String[10];
		int[] minPart = new int[30];
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
				if (seriesVolume < minPart[i] || newSeries == 1) {
					minPart[i] = seriesVolume;
				}

				newSeries = 0;
				oldSeries = realSeries;
				rowCount++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean found = false;
		for (i = 0; i < series.length; i++) {
			if (series[i] != null) {
				for (int j = minPart[i] + 1; j < maxPart[i]; j++) {
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
