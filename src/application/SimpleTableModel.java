package application;

import gui.Localization;

import javax.swing.table.DefaultTableModel;
import java.io.Serial;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SimpleTableModel extends DefaultTableModel {

	@Serial
	private static final long serialVersionUID = 1L;

	// Internal column keys for identification (language-independent)
	public static final String KEY_EBOOK  = "ebook";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_TITLE  = "title";
	public static final String KEY_SERIES = "series"; // kept as constant, nicht mehr in columnKeys
	public static final String KEY_DATE   = "date";
	public static final String KEY_RATING = "rating";

	// Trennzeichen zum Einbetten der Serie in die Titel-Zelle (NUL-Zeichen)
	public static final String TITLE_SEP = "\u0000";

	// Column key order, persisted in config.conf as layoutSort
	public static final String[] columnKeys = { KEY_EBOOK, KEY_AUTHOR, KEY_TITLE, KEY_DATE, KEY_RATING };

	// Display names loaded from Localization
	public static final String[] columnNames = new String[columnKeys.length];

	public static void initColumnNames() {
		for (int i = 0; i < columnKeys.length; i++) {
			columnNames[i] = Localization.get("column." + columnKeys[i]);
		}
	}

    public SimpleTableModel(BookListModel books) {

		super();
		this.setColumnIdentifiers(columnNames);

		for (int i = 0; i < this.getRowCount(); i++) {
			this.removeRow(i);
		}

		int columnEbook  = 0;
		int columnAuthor = 0;
		int columnTitle  = 0;
		int columnDate   = 0;
		int columnRating = 0;

		for (int i = 0; i < columnKeys.length; i++) {
            switch (columnKeys[i]) {
                case KEY_EBOOK  -> columnEbook  = i;
                case KEY_AUTHOR -> columnAuthor = i;
                case KEY_TITLE  -> columnTitle  = i;
                case KEY_DATE   -> columnDate   = i;
                case KEY_RATING -> columnRating = i;
            }
		}

		// Datumsformat je nach Sprache
		Locale locale = HandleConfig.lang.equals("GERMAN") ? Locale.GERMAN : Locale.ENGLISH;
		SimpleDateFormat sdf = new SimpleDateFormat(
				HandleConfig.lang.equals("GERMAN") ? "dd. MMM. yyyy" : "MMM dd, yyyy", locale);

		for (int i = 0; i < books.getSize(); i++) {
            int cols = columnNames.length;
            String[] rowData = new String[cols];
            for (int j = 0; j < cols; j++) {
				if (j == columnEbook) {
					rowData[j] = books.getElementAt(i).isEbook() ? "●" : "○";
				} else if (j == columnAuthor) {
					rowData[j] = books.getElementAt(i).getAuthor();
				} else if (j == columnTitle) {
					// Titel + Serie + Band als kombinierter Wert (NUL-getrennt)
					String series = books.getElementAt(i).getSeries();
					String vol    = books.getElementAt(i).getSeriesVol();
					rowData[j] = books.getElementAt(i).getTitle()
							+ TITLE_SEP + series
							+ TITLE_SEP + vol;
				} else if (j == columnDate) {
					Timestamp ts = books.getElementAt(i).getDate();
					rowData[j] = ts != null ? sdf.format(ts) : "";
				} else if (j == columnRating) {
					double r = books.getElementAt(i).getRating();
					rowData[j] = r > 0 ? Double.toString(r) : "";
				}
			}
			this.addRow(rowData);
		}

	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}


}
