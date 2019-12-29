package anwendung;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.sql.Timestamp;

import datenhaltung.Datenbank;

public class Buch {

	private String autor;
	private String titel;
	private boolean ausgeliehen;
	private String ausgeliehen_an;
	private String ausgeliehen_von;
	private String bemerkung;
	private String serie;
	private Image pic;
	private Timestamp datum;

	public Buch(String autor, String titel, boolean ausgeliehen, String ausgeliehen_an, String ausgeliehen_von,
			String bemerkung, String serie, Image pic,Timestamp datum, boolean db) throws SQLException {
		super();
		this.autor = autor;
		this.titel = titel;
		this.ausgeliehen = ausgeliehen;
		this.bemerkung = bemerkung;
		this.serie = serie;
		this.pic = pic;
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
				Datenbank.add(autor, titel, "an", ausgeliehen_an, bemerkung, serie, datum.toString());
			else if (!ausgeliehen_von.isEmpty())
				Datenbank.add(autor, titel, "von", ausgeliehen_von, bemerkung, serie, datum.toString());
			else
				Datenbank.add(autor, titel, "nein", "", bemerkung, serie, datum.toString());
		}
	}

	public Buch(String autor, String titel, String bemerkung, String serie, Image pic, boolean ausgeliehen,Timestamp datum, boolean db) throws SQLException {
		this(autor, titel, ausgeliehen, "", "", bemerkung, serie,pic, datum, db);
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
		Buch other = (Buch) obj;
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

	public void setPic(BufferedImage pic) {
		this.pic = pic;
	}

	public Timestamp getDatum() {
		return datum;
	}

	public void setDatum(Timestamp datum) {
		this.datum = datum;
	}
	
	

}
