package application;

import java.sql.SQLException;
import java.sql.Timestamp;

import data.Database;

public class Book_Wishlist {

	private String autor;
	private String titel;
	private String bemerkung;
	private String serie;
	private String seriePart;
	private Timestamp datum;

	public Book_Wishlist(String autor, String titel, String bemerkung, String serie,String seriePart, Timestamp datum, boolean db) throws SQLException {
		super();
		this.autor = autor;
		this.titel = titel;
		this.bemerkung = bemerkung;
		this.serie = serie;
		this.seriePart = seriePart;
		this.datum = datum;
		if (db) {
				Database.addToWishlist(autor, titel, bemerkung, serie, seriePart, datum.toString());
		}
	}

	@Override
	public String toString() {
		return autor + "    |    " + titel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((autor == null) ? 0 : autor.hashCode());
		result = prime * result + ((titel == null) ? 0 : titel.hashCode());
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
		if (autor == null) {
			if (other.autor != null)
				return false;
		} else if (!autor.equals(other.autor))
			return false;
		if (titel == null) {
			if (other.titel != null)
				return false;
		} else if (!titel.equals(other.titel))
			return false;
		return true;
	}

	public String getAutor() {
		return autor;
	}

	public void setAutor(String autor) {
		this.autor = autor;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public Timestamp getDatum() {
		return datum;
	}

	public void setDatum(Timestamp datum) {
		this.datum = datum;
	}

	public String getSeriePart() {
		return seriePart;
	}

	public void setSeriePart(String seriePart) {
		this.seriePart = seriePart;
	}
	
	

}
