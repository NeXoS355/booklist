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
	
	private String parent = "";

	public CustomTableCellRenderer(String parent) {
		this.parent = parent;
		System.out.println(parent);
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
		setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY));

	    // Ausrichtung für erste Spalte zentriert, rest bleibt linksbündig
	    if (parent.equals("Booklist") && (column == 0 || column == 4)) {
	        setHorizontalAlignment(SwingConstants.CENTER);
	    } else {
	        setHorizontalAlignment(SwingConstants.LEFT);
	    }
		
		// Hintergrundfarbe setzen: Selected, Hover, normal
		if (HandleConfig.darkmode == 1) {
			setupDarkMode(component, isSelected, row);
		} else {
			setupLightMode(component, isSelected, row);
		}
		return component;
	}
	
    private void setupDarkMode(Component component, boolean isSelected, int row) {
        if (isSelected) {
            component.setForeground(Color.WHITE);
            component.setBackground(Color.GRAY);
        } else if (row == hoveredRow) {
            component.setForeground(Color.BLACK);
            component.setBackground(Color.LIGHT_GRAY);
        } else {
            component.setForeground(new Color(220, 220, 220));
            component.setBackground(Color.DARK_GRAY);
        }
    }
    
    private void setupLightMode(Component component, boolean isSelected, int row) {
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
}
