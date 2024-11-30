package gui;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import application.HandleConfig;
import application.SimpleTableModel;

import java.awt.*;
import java.io.Serial;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CustomTableHeaderRenderer extends DefaultTableCellRenderer {
	@Serial
	private static final long serialVersionUID = 1L;
	private static ImageIcon scaledEbookIcon = null;
	private static ImageIcon scaledStarIcon = null;
	private final Map<Integer, Icon> columnIcons = new HashMap<>();

    public CustomTableHeaderRenderer() {
		if (HandleConfig.darkmode == 1) {
			URL ebookIconURL = getClass().getResource("/resources/ebook_inv.png");
			assert ebookIconURL != null;
			ImageIcon ebookIcon = new ImageIcon(ebookIconURL);
			scaledEbookIcon = new ImageIcon(
					ebookIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
			);
		} else {
			URL ebookIconURL = getClass().getResource("/resources/ebook.png");
			assert ebookIconURL != null;
			ImageIcon ebookIcon = new ImageIcon(ebookIconURL);
			scaledEbookIcon = new ImageIcon(
					ebookIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
			);
		}
		URL starIconURL = getClass().getResource("/resources/star.png");
		assert starIconURL != null;
		ImageIcon starIcon = new ImageIcon(starIconURL);
		scaledStarIcon = new ImageIcon(
				starIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)
		);
    }

	// Methode zum Setzen eines Icons für eine bestimmte Spalte
	public void setColumnIcon(int column, Icon icon) {
		columnIcons.put(column, icon);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
		setupHeaderCell(component, column, value);

		if (table.getColumnName(column).equals("Rating"))
			setColumnIcon(column, scaledStarIcon);
		else if (table.getColumnName(column).equals("E-Book"))
			setColumnIcon(column, scaledEbookIcon);
		else
			setColumnIcon(column, null);

		table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
			@Override
			public void columnAdded(TableColumnModelEvent e) { }

			@Override
			public void columnRemoved(TableColumnModelEvent e) { }

			@Override
			public void columnMoved(TableColumnModelEvent e) {
				int fromIndex = e.getFromIndex();
				int toIndex = e.getToIndex();
				TableColumnModel columnModel = table.getColumnModel();
				String fromColumnName = columnModel.getColumn(fromIndex).getHeaderValue().toString();
				if(fromIndex != toIndex && !SimpleTableModel.columnNames[fromIndex].equals(fromColumnName)) {
					Mainframe.logger.info("Spalte verschoben von Index {} zu Index {}", fromIndex, toIndex);
					for (int i = 0; i < SimpleTableModel.columnNames.length; i++) {
						SimpleTableModel.columnNames[i] = columnModel.getColumn(i).getHeaderValue().toString();
					}
				}

			}

			@Override
			public void columnMarginChanged(ChangeEvent e) { }

			@Override
			public void columnSelectionChanged(ListSelectionEvent e) { }
		});


		return component;
	}

	private void setupHeaderCell(Component component, int column, Object value) {
		setFont(new Font("Roboto", Font.BOLD, 16));

		if (component instanceof JLabel) {
			JLabel label = (JLabel) component;

			// Prüfe, ob für diese Spalte ein Icon definiert ist
			Icon icon = columnIcons.get(column);
			if (icon != null) {
				label.setIcon(icon);
				label.setHorizontalTextPosition(JLabel.RIGHT);
				label.setIconTextGap(5); // Abstand zwischen Icon und Text
			} else {
				label.setIcon(null);
			}

			if (label.getIcon() != null)
				label.setText("");
			else
				label.setText(value != null ? value.toString() : "");

		}

		// Header-Farben setzen
		if (HandleConfig.darkmode == 1) {
			component.setBackground(Mainframe.darkmodeAccentColor);
			component.setForeground(Color.WHITE);
		} else {
			component.setForeground(Color.BLACK);
			component.setBackground(new Color(240, 240, 240));
		}
	}
}
