package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import application.HandleConfig;

import java.awt.*;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int hoveredRow = -2;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public void clearHoveredRow() {
		this.hoveredRow = -2;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBorder(null);

		// Hintergrundfarbe setzen: Selected, Hover, normal
		if (HandleConfig.darkmode == 1) {
			if (isSelected) {
				component.setForeground(Color.WHITE);
				component.setBackground(Color.GRAY);
			} else if (row == hoveredRow) {
				component.setForeground(Color.BLACK);
				component.setBackground(Color.LIGHT_GRAY);
			} else {
				component.setForeground(new Color(220,220,220));
				component.setBackground(Color.DARK_GRAY);
			}
		} else {
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

		// Table Header Settings
		if (row == -1) {
			setFont(new Font("Roboto", Font.BOLD, 16));
			if (HandleConfig.darkmode == 1)
				component.setForeground(Color.WHITE);
			else
				component.setForeground(Color.BLACK);

		}

		return component;
	}
}
