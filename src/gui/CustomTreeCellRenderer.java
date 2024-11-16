package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serial;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import application.HandleConfig;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

	@Serial
	private static final long serialVersionUID = 1L;
	int hoveredRow = -1;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
		
	}


	public CustomTreeCellRenderer() {
		setFont(new Font("Roboto", Font.PLAIN, 16));
	}
	
	

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		Component component = super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
		
		if (HandleConfig.darkmode == 1) {
			if (isSelected) {
				component.setForeground(Color.WHITE);
				component.setBackground(Color.GRAY);
			} else if (row == hoveredRow) {
				component.setForeground(Color.BLACK);
				component.setBackground(Color.LIGHT_GRAY);
			} else {
				component.setForeground(new Color(220,220,220));
				component.setBackground(Mainframe.darkmodeBackgroundColor);
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

		// Da die Hintergrundfarbe nicht automatisch gezeichnet wird, erzwinge das Malen
		((JComponent) component).setOpaque(true);
		return component;
	}


}
