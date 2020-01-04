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

	public SimpleTableModel(BookListModel b�cher) {

		super();
		this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < b�cher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = b�cher.getElementAt(i).getAutor();
				} else if (j == 1) {
					rowData[j] = b�cher.getElementAt(i).getTitel();
				} else if (j == 2) {
					rowData[j] = b�cher.getElementAt(i).getSerie() + " - " + b�cher.getElementAt(i).getSeriePart();
				} else if (j == 3) {
					rowData[j] = b�cher.getElementAt(i).getBemerkung();
				}
			}
			this.addRow(rowData);
		}

	}
	
	public SimpleTableModel(DefaultListModel<Book> b�cher) {

		super();
		this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < b�cher.getSize(); i++) {
			for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = b�cher.getElementAt(i).getAutor();
				} else if (j == 1) {
					rowData[j] = b�cher.getElementAt(i).getTitel();
				} else if (j == 2) {
					rowData[j] = b�cher.getElementAt(i).getSerie() + " - " + b�cher.getElementAt(i).getSeriePart();
				} else if (j == 3) {
					rowData[j] = b�cher.getElementAt(i).getBemerkung();
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
