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

	public SimpleTableModel(BookListModel bücher) {

		super();
		this.setColumnIdentifiers(columnNames);

		for (int i = 0; i < this.getRowCount(); i++) {
			this.removeRow(i);
		}

		for (int i = 0; i < bücher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					boolean isEbook = bücher.getElementAt(i).isEbook();
					if (isEbook) {
						rowData[j] = "X";
					} else {
						rowData[j] = "";
					}
				} else if (j == 1) {
					rowData[j] = bücher.getElementAt(i).getAuthor();
				} else if (j == 2) {
					rowData[j] = bücher.getElementAt(i).getTitle();
				} else if (j == 3) {
					rowData[j] = bücher.getElementAt(i).getSeries() + " - " + bücher.getElementAt(i).getSeriesVol();
				} else if (j == 4) {
					if (bücher.getElementAt(i).getRating() > 0) {
						rowData[j] = Integer.toString(bücher.getElementAt(i).getRating());
					} else {
						rowData[j] = "";
					}
				}
			}
			this.addRow(rowData);
		}

	}

	public SimpleTableModel(DefaultListModel<Book_Booklist> bücher) {

		super();
		this.setColumnIdentifiers(columnNames);

		for (int i = 0; i < this.getRowCount(); i++) {
			this.removeRow(i);
		}

		for (int i = 0; i < bücher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					boolean isEbook = bücher.getElementAt(i).isEbook();
					if (isEbook) {
						rowData[j] = "X";
					} else {
						rowData[j] = "";
					}
				} else if (j == 1) {
					rowData[j] = bücher.getElementAt(i).getAuthor();
				} else if (j == 2) {
					rowData[j] = bücher.getElementAt(i).getTitle();
				} else if (j == 3) {
					rowData[j] = bücher.getElementAt(i).getSeries() + " - " + bücher.getElementAt(i).getSeriesVol();
				} else if (j == 4) {
					if (bücher.getElementAt(i).getRating() > 0) {
						rowData[j] = Integer.toString(bücher.getElementAt(i).getRating());
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
