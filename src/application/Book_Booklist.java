package application;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.sql.Timestamp;

import data.Database;

public class Book_Booklist {

	private String autor;
	private String titel;
	private boolean ausgeliehen;
	private String ausgeliehen_an;
	private String ausgeliehen_von;
	private String bemerkung;
	private String serie;
	private String seriePart;
	private Image pic;
	private String desc;
	private Timestamp datum;

	public Book_Booklist(String autor, String titel, boolean ausgeliehen, String ausgeliehen_an, String ausgeliehen_von,
			String bemerkung, String serie,String seriePart, Image pic, String desc,Timestamp datum, boolean db) throws SQLException {
		super();
		this.autor = autor;
		this.titel = titel;
		this.ausgeliehen = ausgeliehen;
		this.bemerkung = bemerkung;
		this.serie = serie;
		this.seriePart = seriePart;
		this.pic = pic;
		this.desc = desc;
		this.datum = datum;
		if (ausgeliehen) {
			this.ausgeliehen_an = ausgeliehen_an;
			this.ausgeliehen_von = ausgeliehen_von;
		} else {
			this.ausgeliehen_an = "";
			this.ausgeliehen_von = "";
		}

		if (db) {
			if (!ausgeliehen_an.isEmpty())
				Database.addToBooklist(autor, titel, "an", ausgeliehen_an, bemerkung, serie, seriePart, datum.toString());
			else if (!ausgeliehen_von.isEmpty())
				Database.addToBooklist(autor, titel, "von", ausgeliehen_von, bemerkung, serie, seriePart, datum.toString());
			else
				Database.addToBooklist(autor, titel, "nein", "", bemerkung, serie, seriePart, datum.toString());
		}
	}

	public Book_Booklist(String autor, String titel, String bemerkung, String serie,String seriePart, Image pic, String desc, boolean ausgeliehen,Timestamp datum, boolean db) throws SQLException {
		this(autor, titel, ausgeliehen, "", "", bemerkung, serie, seriePart, pic, desc, datum, db);
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
		Book_Booklist other = (Book_Booklist) obj;
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

	public boolean isAusgeliehen() {
		return ausgeliehen;
	}

	public void setAusgeliehen(boolean ausgeliehen) {
		this.ausgeliehen = ausgeliehen;
	}

	public String getAusgeliehen_an() {
		return ausgeliehen_an;
	}

	public void setAusgeliehen_an(String ausgeliehen_an) {
		this.ausgeliehen_an = ausgeliehen_an;
	}

	public String getAusgeliehen_von() {
		return ausgeliehen_von;
	}

	public void setAusgeliehen_von(String ausgeliehen_von) {
		this.ausgeliehen_von = ausgeliehen_von;
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

	public Image getPic() {
		return pic;
	}

	public void setPic(Image pic) {
		this.pic = pic;
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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	

}
