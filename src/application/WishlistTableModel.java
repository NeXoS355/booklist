package application;

import gui.Localization;

import javax.swing.table.DefaultTableModel;
import java.io.Serial;

public class WishlistTableModel extends DefaultTableModel {

	@Serial
	private static final long serialVersionUID = 1L;

    public WishlistTableModel(WishlistListModel books) {

		super();
        String[] columnNames = {
            Localization.get("label.author"),
            Localization.get("label.title"),
            Localization.get("label.series"),
            Localization.get("label.note")
        };
        this.setColumnIdentifiers(columnNames);
		
		for(int i = 0;i<this.getRowCount();i++) {
			this.removeRow(i);
		}
		
		for (int i = 0; i < books.getSize(); i++) {
            int cols = 4;
            String[] rowData = new String[cols];
            for (int j = 0; j < cols; j++) {
				if (j == 0) {
					rowData[j] = books.getElementAt(i).getAuthor();
				} else if (j == 1) {
					rowData[j] = books.getElementAt(i).getTitle();
				} else if (j == 2) {
					rowData[j] = books.getElementAt(i).getSeries() + " - " + books.getElementAt(i).getSeriesVol();
				} else {
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
