package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.io.Serial;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {
	@Serial
	private static final long serialVersionUID = 1L;
	private int hoveredRow = -2;

	private final String parent;

	public CustomTableCellRenderer(String parent) {
		this.parent = parent;
	}

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
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")));

	    // Ausrichtung für erste Spalte zentriert, rest bleibt linksbündig
	    if (parent.equals("Booklist") && (column == 0 || column == 4)) {
	        setHorizontalAlignment(SwingConstants.CENTER);
	    } else {
	        setHorizontalAlignment(SwingConstants.LEFT);
	    }

		if (isSelected) {
			component.setForeground(UIManager.getColor("Table.selectionForeground"));
			component.setBackground(UIManager.getColor("Table.selectionBackground"));
		} else if (row == hoveredRow) {
			component.setForeground(UIManager.getColor("Table.foreground"));
			component.setBackground(UIManager.getColor("Table.selectionInactiveBackground"));
		} else {
			component.setForeground(UIManager.getColor("Table.foreground"));
			component.setBackground(UIManager.getColor("Table.background"));
		}
		return component;
	}
}
