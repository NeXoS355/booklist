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
		textNonSelectionColor = Color.BLACK;
		textSelectionColor = Color.WHITE;
		backgroundNonSelectionColor = Color.WHITE;
		backgroundSelectionColor = Color.DARK_GRAY;
		borderSelectionColor = Color.BLACK;

//        backgroundNonSelectionColor=new Color(46,46,46);
//        borderSelectionColor=Color.BLACK; 

		setFont(new Font("Roboto", Font.PLAIN, 16));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {
		
		Component component = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		// Wenn die Maus über dieser Zeile schwebt, Hintergrundfarbe ändern
		if (sel) {
			component.setBackground(getBackgroundSelectionColor());
		} else if (row == hoveredRow) {
			component.setBackground(Color.LIGHT_GRAY);
			
		} else {
			component.setBackground(getBackgroundNonSelectionColor());
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
