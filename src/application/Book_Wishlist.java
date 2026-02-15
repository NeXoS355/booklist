package application;

import java.sql.Timestamp;

import data.Database;

public class Book_Wishlist {

	private int wid;
	private String author;
	private String title;
	private String note;
	private String series;
	private String seriesVol;
	private Timestamp date;

	public Book_Wishlist(String author, String title, String note, String series,String seriesVol, Timestamp date, boolean db) {
		super();
		this.author = author;
		this.title = title;
		this.note = note;
		this.series = series;
		this.seriesVol = seriesVol;
		this.date = date;
		if (db) {
				this.wid = Database.addToWishlist(author, title, note, series, seriesVol, date.toString());
		}
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
		Book_Wishlist other = (Book_Wishlist) obj;
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

	public int getWid() {
		return wid;
	}

	public void setWid(int wid) {
		this.wid = wid;
	}

}
