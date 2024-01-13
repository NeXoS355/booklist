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

	public Book_Booklist(String author, String title, boolean borrowed, String borrowedTo, String borrowedFrom,
			String note, String series, String seriesVol, boolean ebook, Image pic, String desc, String isbn,
			Timestamp date, boolean db) {
		super();
		this.author = author;
		this.title = title;
		this.borrowed = borrowed;
		this.note = note;
		this.series = series;
		this.seriesVol = seriesVol;
		this.setEbook(ebook);
		this.pic = pic;
		this.desc = desc;
		this.isbn = isbn;
		this.date = date;
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
					bid = Database.addToBooklist(author, title, "von", borrowedFrom, note, series, seriesVol,
							ebook, date.toString());
				else
					bid = Database.addToBooklist(author, title, "nein", "", note, series, seriesVol, ebook,
							date.toString());
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		}
	}

	public Book_Booklist(String author, String title, String note, String series, String seriesVol, boolean ebook,
			Image pic, String desc, String isbn, Timestamp date, boolean db) throws SQLException {
		this(author, title, false, "", "", note, series, seriesVol, ebook, pic, desc, isbn, date, db);
	}

	@Override
	public String toString() {
		return author + "    |    " + title;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

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
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
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

	public void setDate(Timestamp date) {
		this.date = date;
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

	public void setDesc(String desc) {
		this.desc = desc;
		Database.updateDesc(this.getBid(), desc);
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
		Database.updateIsbn(this.getBid(), isbn);
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

	public void setRating(int rating) {
		this.rating = rating;
		Database.updateRating(this.getBid(), rating);
	}

}
