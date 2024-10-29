package application;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import java.io.Serial;

public class SimpleTableModel extends DefaultTableModel {

	@Serial
	private static final long serialVersionUID = 1L;

	//default column Names, not used if already set in config.conf
	public static final String[] columnNames = { "E-Book", "Autor", "Titel", "Serie", "Rating" };


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

		for (int i = 0; i < columnNames.length; i++) {
            switch (columnNames[i]) {
                case "E-Book" -> columnEbook = i;
                case "Autor" -> columnAuthor = i;
                case "Titel" -> columnTitle = i;
                case "Serie" -> columnSeries = i;
                case "Rating" -> columnRating = i;
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
						rowData[j] = Integer.toString(books.getElementAt(i).getRating());
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
