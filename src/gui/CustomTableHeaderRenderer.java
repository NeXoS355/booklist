package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import application.HandleConfig;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CustomTableHeaderRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<Integer, Icon> columnIcons = new HashMap<>();

	// Methode zum Setzen eines Icons für eine bestimmte Spalte
	public void setColumnIcon(int column, Icon icon) {
		columnIcons.put(column, icon);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//		this.setBorder(null);
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));
		setupHeaderCell(component, column, value);
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
			component.setForeground(Color.WHITE);
		} else {
			component.setForeground(Color.BLACK);
			component.setBackground(new Color(240, 240, 240));
		}
	}
}
