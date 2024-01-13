package application;

import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;

public class SimpleTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int cols = 4;

	private String[] rowData = new String[cols];
	private String[] columnNames = { "Autor", "Titel", "Serie", "Bemerkung" };

	public SimpleTableModel(BookListModel bücher) {

		super();
		this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < bücher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = bücher.getElementAt(i).getAuthor();
				} else if (j == 1) {
					rowData[j] = bücher.getElementAt(i).getTitle();
				} else if (j == 2) {
					rowData[j] = bücher.getElementAt(i).getSeries() + " - " + bücher.getElementAt(i).getSeriesVol();
				} else if (j == 3) {
					rowData[j] = bücher.getElementAt(i).getNote();
				}
			}
			this.addRow(rowData);
		}

	}
	
	public SimpleTableModel(DefaultListModel<Book_Booklist> bücher) {

		super();
		this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < bücher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = bücher.getElementAt(i).getAuthor();
				} else if (j == 1) {
					rowData[j] = bücher.getElementAt(i).getTitle();
				} else if (j == 2) {
					rowData[j] = bücher.getElementAt(i).getSeries() + " - " + bücher.getElementAt(i).getSeriesVol();
				} else if (j == 3) {
					rowData[j] = bücher.getElementAt(i).getNote();
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
