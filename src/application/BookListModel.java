package application;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serial;
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
	private final ArrayList<Book_Booklist> books = new ArrayList<>();
	public final ArrayList<String> authors = new ArrayList<>();
	public static boolean useDB = false;

	/**
	 * Constructor
	 * Manages the Booklist and Authorlist
	 */
	public BookListModel(boolean fill) {
		if(fill){
			readDbAndFill();
		}

	}

	private void readDbAndFill() {
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
					String author = rs.getString("author").trim();
					String title = rs.getString("title").trim();
					String series = rs.getString("series").trim();
					int seriesVolInt = rs.getInt("series_vol");
					String seriesVolume = rs.wasNull() ? "" : String.valueOf(seriesVolInt);
					int int_ebook = rs.getInt("ebook");
					boolean ebook = int_ebook == 1;
					double rating = rs.getDouble("rating");
					int bid = rs.getInt("bid");
					Timestamp date = rs.getTimestamp("added_date");

					// Empty Variables for LoadOnDemand
					String note = "";
					byte[] picBytes = null;
					String desc = "";
					String isbn = "";
					String borrowed;
					String borrowedTo = "";
					String borrowedFrom = "";
					boolean boolBorrowed = false;

					if (HandleConfig.loadOnDemand == 0) {
						note = rs.getString("note");
						picBytes = rs.getBytes("pic");
						desc = rs.getString("description");
						isbn = rs.getString("isbn");
						borrowed = rs.getString("borrow_status");
						BorrowStatus borrowStatus = BorrowStatus.fromDbValue(borrowed);
						if (borrowStatus == BorrowStatus.LENT_TO) {
							boolBorrowed = true;
							borrowedTo = rs.getString("borrower").trim();
						} else if (borrowStatus == BorrowStatus.BORROWED_FROM) {
							boolBorrowed = true;
							borrowedFrom = rs.getString("borrower").trim();
						}
					}
					Image buf_pic = null;
					if (picBytes != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(new ByteArrayInputStream(picBytes));
						buf_pic = ImageIO.read(bis_pic);
					}
					book = new Book_Booklist(author, title, boolBorrowed, borrowedTo, borrowedFrom, note, series,
							seriesVolume, ebook, rating, buf_pic, desc, isbn, date, false);
					book.setBid(bid);
					if (picBytes != null) book.setPicSizeBytes(picBytes.length);
					book.setExtendedDataLoaded(true);
					getBooks().add(book);
					Mainframe.logger.info("Buch ausgelesen: {}-{}", book.getAuthor(), book.getTitle());
				} catch (DateTimeParseException e) {
					Mainframe.logger.error(e);
				}
			}
		} catch (SQLException | IOException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
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
		if (!book.isExtendedDataLoaded()) {
			ResultSet rs = null;
			try {
				rs = Database.selectFromBooklist(book.getBid());
				while (rs.next()) {
					String note = rs.getString("note");
					byte[] picBytes = rs.getBytes("pic");
					String desc = rs.getString("description");
					Timestamp datum = rs.getTimestamp("added_date");
					String isbn = rs.getString("isbn");
					double rating = rs.getDouble("rating");

					Image buf_pic = null;
					if (picBytes != null) {
						BufferedInputStream bis_pic = new BufferedInputStream(new ByteArrayInputStream(picBytes));
						buf_pic = ImageIO.read(bis_pic);
						book.setPicSizeBytes(picBytes.length);
					}

					book.setPic(buf_pic);
					book.setDesc(desc, false);
					book.setNote(note);
					book.setDate(datum, false);
					book.setIsbn(isbn, false);
					book.setRating(rating, false);

					String borrowed = rs.getString("borrow_status");
					BorrowStatus borrowStatus = BorrowStatus.fromDbValue(borrowed);
					if (borrowStatus == BorrowStatus.LENT_TO) {
						String borrowedTo = rs.getString("borrower").trim();
						book.setBorrowedTo(borrowedTo);
						book.setBorrowed(true);
					} else if (borrowStatus == BorrowStatus.BORROWED_FROM) {
						String borrowedFrom = rs.getString("borrower").trim();
						book.setBorrowedFrom(borrowedFrom);
						book.setBorrowed(true);
					}
					Mainframe.logger.info("loading Book info: {}-{}", book.getAuthor(), book.getTitle());
				}
			} catch (SQLException | IOException e) {
				Mainframe.logger.error(e.getMessage());
			} finally {
				Database.closeResultSet(rs);
			}
			book.setExtendedDataLoaded(true);
        }
	}

	/**
	 * updates the author list and updates the displayed Tree
	 *
	 */
	public void checkAuthors() {
		authors.clear();

		if (useDB) {
			ResultSet rs = null;
			try {
				String[] columnName = { "author" };
				rs = Database.getColumnsFromBooklist(columnName);
				while (rs.next()) {
					authors.add(rs.getString(1));
				}
				Mainframe.logger.info("Updated Author List through DB");
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			} finally {
				Database.closeResultSet(rs);
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
	public String[] getSeriesFromAuthor(String author) {
		ArrayList<String> seriesList = new ArrayList<>();

		if (useDB) {
			ResultSet rs = null;
			try {
				rs = Database.getColumnWithWhere("series", "author", author);
				while (rs.next()) {
					String series = rs.getString(1);
					if (!series.isEmpty())
						seriesList.add(series);
				}
				Mainframe.logger.info("Got Series from Author through DB: {}", author);
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			} finally {
				Database.closeResultSet(rs);
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
			ResultSet rs = null;
			try {
				rs = Database.getColumnWithWhere("series", "author", author);
				while (rs.next()) {
					String series = rs.getString(1);
					if (!series.isEmpty()) {
						return true;
					}
				}
			} catch (SQLException e) {
				Mainframe.logger.error(e.getMessage());
			} finally {
				Database.closeResultSet(rs);
			}
		} else {
			for (int i = 0; i < Mainframe.allEntries.getBooks().size(); i++) {
				Book_Booklist book = Mainframe.allEntries.getBooks().get(i);
				if (book.getAuthor().contains(author)) {
					if (!book.getSeries().trim().isEmpty()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public int getBookCountForAuthor(String author) {
		int count = 0;
		for (Book_Booklist book : getBooks()) {
			if (book.getAuthor().equals(author)) {
				count++;
			}
		}
		return count;
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
		} finally {
			Database.closeResultSet(rs);
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
		} finally {
			Database.closeResultSet(rs);
		}
		return BestOfRating;
	}

	/**
	 * Counts ebooks (ebook=1) or physical books (ebook=0 or NULL)
	 *
	 * @param ebook - 1 for ebooks, 0 for physical books
	 * @return count of matching books
	 */
	public static int getEbookCount(int ebook) {
		int ebookCount = 0;
		int physicalCount = 0;

		ResultSet rs = Database.getColumnCountsWithGroup("ebook");
		try {
			while (rs.next()) {
				int count = rs.getInt(1);
				String isEbook = rs.getString(2);

				if ("1".equals(isEbook)) {
					ebookCount += count;
				} else {
					// ebook=0 → physisches Buch
					physicalCount += count;
				}
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
		}
		return ebook == 1 ? ebookCount : physicalCount;
	}

	/**
	 * gets books per year statistics
	 *
	 * @return return a String with book counts per year
	 */
	public static String getBooksPerYear() {
		StringBuilder result = new StringBuilder();
		result.append("<html>");
		ResultSet rs = Database.getColumnCountsWithGroup("strftime('%Y', added_date)");

		try {
			while (rs.next()) {
				int yearCount = rs.getInt(1);
				String yearValue = rs.getString(2);

				result.append(yearValue).append(" - ").append(yearCount).append("<br>");
			}

		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
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
		return -1;

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
	 * get whole Booklist
	 *
	 * @return ArrayList with all Books
	 */
	public ArrayList<Book_Booklist> getBooks() {
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
	public boolean analyzeSeries(String series, String author) {
		ArrayList<Integer> ownedBooksOfSeries = new ArrayList<>();
		int maxVol = 0;
		// get the last owned Book of the Series and store the number in maxVol
        for (Book_Booklist book : books) {
            if (book.getSeries().equals(series)) {
                String vol = book.getSeriesVol();
                if (vol == null || vol.trim().isEmpty()) continue;
                try {
                    int volInt = Integer.parseInt(vol.trim());
                    ownedBooksOfSeries.add(volInt);
                    if (maxVol < volInt)
                        maxVol = volInt;
                } catch (NumberFormatException e) {
                    Mainframe.logger.warn("Ungueltige Bandnummer fuer {}: '{}'", book.getTitle(), vol);
                }
            }
        }
		// create a list with the missing parts in the series up to maxVol
		ArrayList<Integer> missingBooksOfSeries = new ArrayList<>();
		boolean missing = true;
		for (int i = 1; i < maxVol; i++) {
            for (Integer ownedBookOfSeries : ownedBooksOfSeries) {
                if (ownedBookOfSeries == i) {
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
        for (Integer missingBookOfSeries : missingBooksOfSeries) {
            String[][] returnArray = GetBookInfosFromWeb
                    .getSeriesInfoFromGoogleApiWebRequest(series + "+" + missingBookOfSeries, returnCount);
            int tryCounter = 0;
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
                                Mainframe.logger.info("AnalyseSeries: Versuch: {}", tryCounter);
                                Mainframe.logger.info("AnalyseSeries: Band: {}", missingBookOfSeries);
                                Mainframe.logger.info("AnalyseSeries: Autor: {}", foundAuthor);
                                Mainframe.logger.info("AnalyseSeries: Titel: {}", foundTitle);
                                Mainframe.logger.info("AnalyseSeries: ISBN: {}", foundIsbn);
                                wishlist.wishlistEntries
                                        .add(new Book_Wishlist(foundAuthor, foundTitle, "Automatisch hinzugefuegt",
                                                series, Integer.toString(missingBookOfSeries),
                                                new Timestamp(System.currentTimeMillis()), true));
                            }
                        }
                    }
                }
                tryCounter++;
            }
        }
        return !newBooksList.isEmpty();
	}

	public void addElement(Book_Booklist book) {
		books.add(book);
	}
}
