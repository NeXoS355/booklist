package application;

import gui.Localization;

import javax.swing.table.DefaultTableModel;
import java.io.Serial;

public class SimpleTableModel extends DefaultTableModel {

	@Serial
	private static final long serialVersionUID = 1L;

	// Internal column keys for identification (language-independent)
	public static final String KEY_EBOOK = "ebook";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SERIES = "series";
	public static final String KEY_RATING = "rating";

	// Column key order, persisted in config.conf as layoutSort
	public static final String[] columnKeys = { KEY_EBOOK, KEY_AUTHOR, KEY_TITLE, KEY_SERIES, KEY_RATING };

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

		int columnEbook = 0;
		int columnAuthor = 0;
		int columnTitle = 0;
		int columnSeries = 0;
		int columnRating = 0;

		for (int i = 0; i < columnKeys.length; i++) {
            switch (columnKeys[i]) {
                case KEY_EBOOK -> columnEbook = i;
                case KEY_AUTHOR -> columnAuthor = i;
                case KEY_TITLE -> columnTitle = i;
                case KEY_SERIES -> columnSeries = i;
                case KEY_RATING -> columnRating = i;
            }
		}

		for (int i = 0; i < books.getSize(); i++) {
            int cols = columnNames.length;
            String[] rowData = new String[cols];
            for (int j = 0; j < cols; j++) {
				if (j == columnEbook) {
					boolean isEbook = books.getElementAt(i).isEbook();
					if (isEbook) {
						rowData[j] = "●";
					} else {
						rowData[j] = "○";
					}
				} else if (j == columnAuthor) {
					rowData[j] = books.getElementAt(i).getAuthor();
				} else if (j == columnTitle) {
					rowData[j] = books.getElementAt(i).getTitle();
				} else if (j == columnSeries) {
					rowData[j] = books.getElementAt(i).getSeries() + " - " + books.getElementAt(i).getSeriesVol();
				} else if (j == columnRating) {
					if (books.getElementAt(i).getRating() > 0) {
						rowData[j] = Double.toString(books.getElementAt(i).getRating());
					} else {
						rowData[j] = "";
					}
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
