package application;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

public class SimpleTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//default column Names, not used if already set in config.conf
	public static String[] columnNames = { "E-Book", "Autor", "Titel", "Serie", "Rating" };
	private int cols = columnNames.length;
	private String[] rowData = new String[cols];

	
	public SimpleTableModel(BookListModel b�cher) {

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
			if (columnNames[i].equals("E-Book"))
				columnEbook = i;
			else if (columnNames[i].equals("Autor"))
				columnAuthor = i;
			else if (columnNames[i].equals("Titel"))
				columnTitle = i;
			else if (columnNames[i].equals("Serie"))
				columnSeries = i;
			else if (columnNames[i].equals("Rating"))
				columnRating = i;
		}

		for (int i = 0; i < b�cher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == columnEbook) {
					boolean isEbook = b�cher.getElementAt(i).isEbook();
					if (isEbook) {
						rowData[j] = "X";
					} else {
						rowData[j] = "";
					}
				} else if (j == columnAuthor) {
					rowData[j] = b�cher.getElementAt(i).getAuthor();
				} else if (j == columnTitle) {
					rowData[j] = b�cher.getElementAt(i).getTitle();
				} else if (j == columnSeries) {
					rowData[j] = b�cher.getElementAt(i).getSeries() + " - " + b�cher.getElementAt(i).getSeriesVol();
				} else if (j == columnRating) {
					if (b�cher.getElementAt(i).getRating() > 0) {
						rowData[j] = Integer.toString(b�cher.getElementAt(i).getRating());
					} else {
						rowData[j] = "";
					}
				}
			}
			this.addRow(rowData);
		}

	}

	public SimpleTableModel(DefaultListModel<Book_Booklist> b�cher) {

		super();
		this.setColumnIdentifiers(columnNames);

		for (int i = 0; i < this.getRowCount(); i++) {
			this.removeRow(i);
		}

		for (int i = 0; i < b�cher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					boolean isEbook = b�cher.getElementAt(i).isEbook();
					if (isEbook) {
						rowData[j] = "X";
					} else {
						rowData[j] = "";
					}
				} else if (j == 1) {
					rowData[j] = b�cher.getElementAt(i).getAuthor();
				} else if (j == 2) {
					rowData[j] = b�cher.getElementAt(i).getTitle();
				} else if (j == 3) {
					rowData[j] = b�cher.getElementAt(i).getSeries() + " - " + b�cher.getElementAt(i).getSeriesVol();
				} else if (j == 4) {
					if (b�cher.getElementAt(i).getRating() > 0) {
						rowData[j] = Integer.toString(b�cher.getElementAt(i).getRating());
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
