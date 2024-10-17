package application;

import javax.swing.table.DefaultTableModel;

public class WishlistTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int cols = 4;

	private String[] rowData = new String[cols];
	private String[] columnNames = { "Autor", "Titel", "Serie", "Bemerkung" };

	public WishlistTableModel(WishlistListModel books) {

		super();
		this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < books.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = books.getElementAt(i).getAuthor();
				} else if (j == 1) {
					rowData[j] = books.getElementAt(i).getTitle();
				} else if (j == 2) {
					rowData[j] = books.getElementAt(i).getSeries() + " - " + books.getElementAt(i).getSeriesVol();
				} else if (j == 3) {
					rowData[j] = books.getElementAt(i).getNote();
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
