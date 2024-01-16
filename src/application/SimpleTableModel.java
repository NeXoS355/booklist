package application;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

public class SimpleTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String[] columnNames = { "E-Book", "Autor", "Titel", "Serie", "Rating" };
	private int cols = columnNames.length;
	private String[] rowData = new String[cols];

	public SimpleTableModel(BookListModel b�cher) {

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
