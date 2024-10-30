package application;

import java.awt.Image;
import java.sql.SQLException;
import java.sql.Timestamp;

import data.Database;
import gui.Mainframe;

public class Book_Booklist {

	private String author;
	private String title;
	private boolean borrowed;
	private String borrowedTo;
	private String borrowedFrom;
	private String note;
	private String series;
	private String seriesVol;
	private boolean ebook;
	private Image pic;
	private String desc;
	private String isbn;
	private Timestamp date;
	private int bid;
	private int rating;

	/**
	 * Booklist Entry Constructor
	 * 
	 * @param author       - Full author Name
	 * @param title        - Book title
	 * @param borrowed     - boolean variable to declare Books as borrowed From or
	 *                     To
	 * @param borrowedTo   - Name of the Person the Book is borrowed to
	 * @param borrowedFrom - Name of the Person the Book has been borrowed from
	 * @param note         - free String variable as note
	 * @param series       - series name of the Book
	 * @param seriesVol     - volume number of the Book in the series
	 * @param ebook        - sets the book as ebook
	 * @param rating       - Book User Rating
	 * @param pic          - Book Cover
	 * @param desc         - Book Description mostly a short summary
	 * @param isbn         - International Standard Book Number (ISBN13)
	 * @param date         - date at which the book has been added to the Database
	 * @param db           - true if book should be added to db or just temporary to
	 *                     the list
	 */
	public Book_Booklist(String author, String title, boolean borrowed, String borrowedTo, String borrowedFrom,
			String note, String series, String seriesVol, boolean ebook, int rating,Image pic, String desc, String isbn,
			Timestamp date, boolean db) {
		super();
		this.author = author;
		this.title = title;
		this.borrowed = borrowed;
		this.note = note;
		this.series = series;
		this.seriesVol = seriesVol;
		this.ebook = ebook;
		this.pic = pic;
		this.desc = desc;
		this.isbn = isbn;
		this.date = date;
		this.rating = rating;
		if (borrowed) {
			this.borrowedTo = borrowedTo;
			this.borrowedFrom = borrowedFrom;
		} else {
			this.borrowedTo = "";
			this.borrowedFrom = "";
		}
		try {
			if (db) {
				if (!borrowedTo.isEmpty())

					bid = Database.addToBooklist(author, title, "an", borrowedTo, note, series, seriesVol, ebook,
							date.toString());

				else if (!borrowedFrom.isEmpty())
					bid = Database.addToBooklist(author, title, "von", borrowedFrom, note, series, seriesVol, ebook,
							date.toString());
				else
					bid = Database.addToBooklist(author, title, "nein", "", note, series, seriesVol, ebook,
							date.toString());
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
	}

	/**
	 * shorter Booklist Entry Constructor
	 * 
	 * @param author   - Full author Name
	 * @param title    - Book title
	 * @param note     - free String variable as note
	 * @param series   - series name of the Book
	 * @param seriesVol - volume number of the Book in the series
	 * @param ebook    - sets the book as ebook
	 * @param rating   - Book User Rating
	 * @param pic      - Book Cover
	 * @param desc     - Book Description mostly a short summary
	 * @param isbn     - International Standard Book Number (ISBN13)
	 * @param date     - date at which the book has been added to the Database
	 * @param db       - true if book should be added to db or just temporary to the
	 *                 list
	 */
	public Book_Booklist(String author, String title, String note, String series, String seriesVol, boolean ebook, int rating,
			Image pic, String desc, String isbn, Timestamp date, boolean db) {
		this(author, title, false, "", "", note, series, seriesVol, ebook, rating, pic, desc, isbn, date, db);
	}

	/**
	 * toString Method author + title
	 *
	 * @return author + title in String format
	 */
	@Override
	public String toString() {
		return author + "    |    " + title;
	}

	/**
	 * hashCode with autor + title
	 *
	 * @return hashCode with author + title
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/**
	 * equals check with author and title
	 *
	 * @param obj - object to compare
	 *
	 * @return "true" if equal else "false"
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book_Booklist other = (Book_Booklist) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (title == null) {
            return other.title == null;
		} else return title.equals(other.title);
    }

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isBorrowed() {
		return borrowed;
	}

	public void setBorrowed(boolean borrowed) {
		this.borrowed = borrowed;
	}

	public String getBorrowedTo() {
		return borrowedTo;
	}

	public void setBorrowedTo(String borrowedTo) {
		this.borrowedTo = borrowedTo;
	}

	public String getBorrowedFrom() {
		return borrowedFrom;
	}

	public void setBorrowedFrom(String borrowedFrom) {
		this.borrowedFrom = borrowedFrom;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public Image getPic() {
		return pic;
	}

	public void setPic(Image pic) {
		this.pic = pic;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date, boolean db) {
		this.date = date;
		if (db)
			Database.updateDate(this.bid, date.toString());
	}

	public String getSeriesVol() {
		return seriesVol;
	}

	public void setSeriesVol(String seriesVol) {
		this.seriesVol = seriesVol;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * setter for desc
	 *
	 * @param desc - description to set
	 * @param db   - boolean value if value should be written to db
	 *
	 */
	public void setDesc(String desc, boolean db) {
		this.desc = desc;
		if (db)
			Database.updateDesc(this.getBid(), desc);
	}

	public String getIsbn() {
		return isbn;
	}

	/**
	 * setter for isbn
	 *
	 * @param isbn - isbn to set
	 * @param db   - boolean value if value should be written to db
	 *
	 */
	public boolean setIsbn(String isbn, boolean db) {
		if (isbn.matches("[0-9]{13}")) {
			this.isbn = isbn;
			if (db)
				Database.updateIsbn(this.getBid(), isbn);
		} else
			return false;

        return true;
    }

	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public boolean isEbook() {
		return ebook;
	}

	public void setEbook(boolean ebook) {
		this.ebook = ebook;
	}

	public int getRating() {
		return rating;
	}

	/**
	 * setter for rating
	 *
	 * @param rating - rating to set
	 * @param db     - boolean value if value should be written to db
	 *
	 */
	public void setRating(int rating, boolean db) {
		this.rating = rating;
		if (db)
			Database.updateRating(this.getBid(), rating);
	}

}
