package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int hoveredRow = -1;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public MyTreeCellRenderer() {
		
//		if(Mainframe.darkmode) {
//	        setBackgroundNonSelectionColor(Color.DARK_GRAY);
//	        setBackgroundSelectionColor(Color.GRAY);
//	        setForeground(Color.WHITE);
//	        setBackground(Color.DARK_GRAY);
//	        setTextSelectionColor(Color.WHITE);
//	        setTextNonSelectionColor(Color.LIGHT_GRAY);
//		} else {
//	        setBackgroundNonSelectionColor(Color.WHITE);
//	        setBackgroundSelectionColor(Color.DARK_GRAY);
//	        setForeground(Color.BLACK);
//	        setBackground(Color.WHITE);
//	        setTextSelectionColor(Color.WHITE);
//	        setTextNonSelectionColor(Color.BLACK);
//		}

//        backgroundNonSelectionColor=new Color(46,46,46);
//        borderSelectionColor=Color.BLACK; 

		setFont(new Font("Roboto", Font.PLAIN, 16));
	}
	
	

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		Component component = super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
		
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

		// Da die Hintergrundfarbe nicht automatisch gezeichnet wird, erzwinge das Malen
		((JComponent) component).setOpaque(true);
		return component;
	}


	protected ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = getClass().getClassLoader().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		}
		Mainframe.logger.error("JTree: " + path + "not found");
		return null;
	}

}
