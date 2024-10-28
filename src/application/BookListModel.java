package application;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.Serial;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;

import data.Database;
import gui.Mainframe;
import gui.wishlist;

/**
 * 
 */
public class BookListModel extends AbstractListModel<Book_Booklist> {

	@Serial
	private static final long serialVersionUID = 1L;
	private static final ArrayList<Book_Booklist> books = new ArrayList<>();
	public static final ArrayList<String> authors = new ArrayList<>();
	public static boolean useDB = false;

	/**
	 * Constructor 
	 * Manages the Booklist and Authorlist
	 */
	public BookListModel() {
		Database.createConnection();
		ResultSet rs;
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
					Book_Booklist book;
					String author = rs.getString("autor").trim();
					String title = rs.getString("titel").trim();
					String series = rs.getString("serie").trim();
					String seriesVolume = rs.getString("seriePart");
					int int_ebook = rs.getInt("ebook");
					boolean ebook = int_ebook == 1;
                    int rating = rs.getInt("rating");
					int bid = Integer.parseInt(rs.getString("bid"));

					// Empty Variables for LoadOnDemand
					String note = "";
					Blob picture = null;
					String desc = "";
					String isbn = "";
					Timestamp date = null;
					String borrowed;
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
					Mainframe.logger.info("Buch ausgelesen: {}-{}", book.getAuthor(), book.getTitle());
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
	 * This method loads the extended information which are not loaded on Startup
	 * if "loadOnDemand = 1".
	 * 
	 * @param book - load values of this Book Entry
	 * 
	 */
	public static void loadOnDemand(Book_Booklist book) {
		if (Objects.equals(book.getDesc(), "") && book.getPic() == null) {
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
					if (borrowed.equals("an")) {
						String borrowedTo = rs.getString("name").trim();
						book.setBorrowedTo(borrowedTo);
						book.setBorrowed(true);
					} else if (borrowed.equals("von")) {
						String borrowedFrom = rs.getString("name").trim();
						book.setBorrowedFrom(borrowedFrom);
						book.setBorrowed(true);
					}
					Mainframe.logger.info("loading Book info: {}-{}", book.getAuthor(), book.getTitle());
				}
			} catch (SQLException | IOException e) {
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

		if (useDB) {
			try {
				String[] columnName = { "autor" };
				ResultSet rs = Database.getColumnsFromBooklist(columnName);
				while (rs.next()) {
					authors.add(rs.getString(1));
				}
				Mainframe.logger.info("Updated Author List through DB");
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			}
		} else {
			for (int i = 0; i < getBooks().size(); i++) {
				if (!authors.contains(getBooks().get(i).getAuthor()))
					authors.add(getBooks().get(i).getAuthor());
			}
			Mainframe.logger.info("Updated Author List through Java Lists");
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
		Mainframe.logger.info("Booklist Buch hinzugefuegt: {},{}", book.getAuthor(), book.getTitle());
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
		Mainframe.logger.info("Booklist Buch geloescht: {}", book.getAuthor(), book.getTitle());
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
		ArrayList<String> seriesList = new ArrayList<>();

		if (useDB) {
			try {
				ResultSet rs = Database.getColumnWithWhere("serie", "autor", author);
				while (rs.next()) {
					String series = rs.getString(1);
					if (!series.isEmpty())
						seriesList.add(series);
				}
				Mainframe.logger.info("Got Series from Author through DB: {}", author);
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			}
		} else {
			for (int i = 0; i < getBooks().size(); i++) {
				Book_Booklist book = getBooks().get(i);
				if (book.getAuthor().contains(author)) {
					if (!book.getSeries().trim().isEmpty()) {
						boolean newSeries = true;
                        for (String s : seriesList) {
                            if (s.equals(book.getSeries())) {
                                newSeries = false;
                                break;
                            }
                        }
						if (newSeries) {
							seriesList.add(book.getSeries());
							Mainframe.logger.info("Got Series from Author through Lists: {}", author);
						}
					}
				}

			}
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
		if (useDB) {
			try {
				ResultSet rs = Database.getColumnWithWhere("serie", "autor", author);
				while (rs.next()) {
					String series = rs.getString(1);
					if (!series.isEmpty()) {
						return true;
					}
				}
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			}
		} else {
			for (int i = 0; i < getBooks().size(); i++) {
				Book_Booklist book = getBooks().get(i);
				if (book.getAuthor().contains(author)) {
					if (!book.getSeries().trim().isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * queries the Database for a specific column and gets the most Occurences
	 * 
	 * @return return a List with the value(s) with the most Occurences in the
	 *         specified column
	 */
	public static ArrayList<String> getMostOf(String getValue) {
		ArrayList<String> value = new ArrayList<>();
		ArrayList<Integer> valueCount = new ArrayList<>();
		ArrayList<String> mostOfValue = new ArrayList<>();
		int mostCount = 0;
		ResultSet rs = Database.getColumnCountsWithGroup(getValue);

		try {
			while (rs.next()) {
				int count = rs.getInt(1);
				String valueString = rs.getString(2);

				if (!valueString.isEmpty()) {
					valueCount.add(count);
					value.add(valueString);

					if (count > mostCount)
						mostCount = count;
				}
			}
			for (int i = 0; i < value.size(); i++) {
				if (valueCount.get(i) == mostCount)
					mostOfValue.add(value.get(i));
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return mostOfValue;
	}

	/**
	 * gets Author or Series with the best overall Rating
	 * 
	 * @return return a List with Authors or Series with the best overall Rating
	 */
	public static ArrayList<String> getBestRatingOf(String getValue) {
		ArrayList<String> value = new ArrayList<>();
		ArrayList<Double> valueRating = new ArrayList<>();
		ArrayList<String> BestOfRating = new ArrayList<>();
		double maxRating = 0;
		String[] columnNames = { getValue };
		ResultSet rs = Database.getAvgRating(columnNames);

		try {
			while (rs.next()) {
				double rating = rs.getDouble(1);
				String valueString = rs.getString(2);

				if (!valueString.isEmpty()) {
					valueRating.add(rating);
					value.add(valueString);

					if (rating > maxRating)
						maxRating = rating;
				}
			}
			for (int i = 0; i < value.size(); i++) {
				if (valueRating.get(i) == maxRating)
					BestOfRating.add(value.get(i));
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return BestOfRating;
	}

	/**
	 * gets
	 * 
	 * @return return a List with Authors or Series with the best overall Rating
	 */
	public static int getEbookCount(int ebook) {
		int noEbook = 0;
		int count = 0;

		ResultSet rs = Database.getColumnCountsWithGroup("ebook");

		try {
			while (rs.next()) {
				count = rs.getInt(1);
				String isEbook = rs.getString(2);

				if (ebook == 1 && isEbook.equals("1")) {
					return count;
				} else if (ebook == 0 && isEbook == null) {
					if (noEbook == 0) {
						noEbook += count;
					} else {
						noEbook += count;
						return noEbook;
					}
				} else if (ebook == 0 && isEbook.equals("0")) {
					if (noEbook == 0) {
						noEbook += count;
					} else {
						noEbook += count;
						return noEbook;
					}

				}

			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return count;
	}

	/**
	 * gets Author or Series with the best overall Rating
	 * 
	 * @return return a String with Authors or Series with the best overall Rating
	 */
	public static String getBooksPerYear() {
		StringBuilder result = new StringBuilder();
		result.append("<html>");
		ResultSet rs = Database.getColumnCountsWithGroup("YEAR(date)");

		try {
			while (rs.next()) {
				int yearCount = rs.getInt(1);
				String yearValue = rs.getString(2);

				result.append(yearValue).append(" - ").append(yearCount).append("<br>");
			}

		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
		result.append("</html>");
		return result.toString();
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
	 * analyzes one specified Bookseries
	 * 
	 * @param series - Name of Series
	 * @param author - Name of corresponding author
	 * 
	 * @return true if Book was found else false
	 */
	public static boolean analyzeSeries(String series, String author) {
		ArrayList<Integer> ownedBooksOfSeries = new ArrayList<>();
		int maxVol = 0;
		// get the last owned Book of the Series and store the number in maxVol
        for (Book_Booklist bookBooklist : books) {
            if (bookBooklist.getSeries().equals(series)) {
                ownedBooksOfSeries.add(Integer.parseInt(bookBooklist.getSeriesVol()));
                if (maxVol < Integer.parseInt(bookBooklist.getSeriesVol()))
                    maxVol = Integer.parseInt(bookBooklist.getSeriesVol());
            }
        }
		// create a list with the missing parts in the series up to maxVol
		ArrayList<Integer> missingBooksOfSeries = new ArrayList<>();
		boolean missing = true;
		for (int i = 1; i < maxVol; i++) {
            for (Integer ownedBooksOfSery : ownedBooksOfSeries) {
                if (ownedBooksOfSery == i) {
                    missing = false;
                    break;
                }
            }
			if (missing) {
				missingBooksOfSeries.add(i);
			}
			missing = true;
		}
		// how many Books should be requested from the Google Books API for every
		// missing Volume
		int returnCount = 3;
		// create a list with new Books which are not in the current list
		ArrayList<String[]> newBooksList = new ArrayList<>();
		// query the Google Book API for every missing Volume
        for (Integer missingBooksOfSery : missingBooksOfSeries) {
            String[][] returnArray = GetBookInfosFromWeb
                    .getSeriesInfoFromGoogleApiWebRequest(series + "+" + missingBooksOfSery, returnCount);
            int versuch = 0;
            // go through all returned Books to analyze them
            for (int j = 0; j < returnCount; j++) {
                String foundAuthor = returnArray[j][0];
                String foundTitle = returnArray[j][1];
                String foundIsbn = returnArray[j][2];
                // Abort attempt if Author or Title is null
                if (foundAuthor != null && foundTitle != null) {
                    boolean owned = false;
                    // check if found book is already in the main Booklist
                    for (Book_Booklist book : books) {
                        String listAuthor = book.getAuthor();
                        String listTitle = book.getTitle();
                        if (foundAuthor.equals(listAuthor) && foundTitle.equals(listTitle)) {
                            owned = true;
                            break;
                        }
                    }
                    // filter out some cases where the Title is too long or might be a collection
                    if (foundTitle.length() <= 50 && !foundTitle.contains(" / ")) {
                        // progress only if book is not already owned, author is the same as the
                        // requested one
                        if (!owned && foundAuthor.equals(author)) {
                            boolean added = false;
                            // check if Book was previously added
                            for (String[] strings : newBooksList) {
                                if (foundAuthor.contains(strings[0])
                                        && foundTitle.contains(strings[1])) {
                                    added = true;
                                    break;
                                }

                            }
                            // check if Book is already in wishlist
                            for (int k = 0; k < wishlist.wishlistEntries.getSize(); k++) {
                                if (foundAuthor.contains(wishlist.wishlistEntries.getElementAt(k).getAuthor()) && foundTitle.contains(wishlist.wishlistEntries.getElementAt(k).getTitle())) {
                                    added = true;
                                }

                            }
                            // add the Book to the list and create wishlist Entry
                            if (!added) {
                                newBooksList.add(returnArray[j]);
                                Mainframe.logger.info("AnalyseSeries: Versuch: {}", versuch);
                                Mainframe.logger.info("AnalyseSeries: Band: {}", missingBooksOfSery);
                                Mainframe.logger.info("AnalyseSeries: Autor: {}", foundAuthor);
                                Mainframe.logger.info("AnalyseSeries: Titel: {}", foundTitle);
                                Mainframe.logger.info("AnalyseSeries: ISBN: {}", foundIsbn);
                                wishlist.wishlistEntries
                                        .add(new Book_Wishlist(foundAuthor, foundTitle, "Automatisch hinzugefuegt",
                                                series, Integer.toString(missingBooksOfSery),
                                                new Timestamp(System.currentTimeMillis()), true));
                            }
                        }
                    }
                }
                versuch++;
            }
        }
        return !newBooksList.isEmpty();
	}

}
