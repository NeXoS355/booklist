package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int hoveredRow = -1;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public void clearHoveredRow() {
		this.hoveredRow = -1;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBorder(null);

		// Hintergrundfarbe setzen: Selected, Hover, normal
		if (Mainframe.darkmode) {
			if (isSelected) {
				component.setForeground(Color.WHITE);
				component.setBackground(Color.GRAY);
			} else if (row == hoveredRow) {
				component.setForeground(Color.BLACK);
				component.setBackground(Color.LIGHT_GRAY);
			} else {
				component.setForeground(Color.LIGHT_GRAY);
				component.setBackground(Color.DARK_GRAY);
			}
		}

		else {
			if (isSelected) {
				component.setForeground(Color.WHITE);
				component.setBackground(Color.DARK_GRAY);
			} else if (row == hoveredRow) {
				component.setForeground(Color.BLACK);
				component.setBackground(Color.LIGHT_GRAY);
			} else {
				component.setForeground(Color.BLACK);
				component.setBackground(Color.WHITE);
			}
		}

		return component;
	}
}
