package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.Serial;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.formdev.flatlaf.util.UIScale;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

	@Serial
	private static final long serialVersionUID = 1L;
	int hoveredRow = -1;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public CustomTreeCellRenderer() {
		setFont(Mainframe.defaultFont);
		setBorderSelectionColor(null);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf,
			int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);

		setIcon(null);

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			String nodeText = node.getUserObject().toString();

			if (parent != null && parent.getParent() == null) {
				// Autoren-Ebene
				int bookCount = Mainframe.allEntries.getBookCountForAuthor(nodeText);
				setText("\u25CF " + nodeText + "  (" + bookCount + ")");
				setFont(Mainframe.defaultFont);
			} else if (parent != null && parent.getParent() != null && parent.getParent().getParent() == null) {
				// Serien-Ebene
				setText("  \u25B8 " + nodeText);
				setFont(Mainframe.defaultFont.deriveFont(Font.PLAIN, Mainframe.defaultFont.getSize() * 0.9f));
			} else {
				// Root-Node
				setFont(Mainframe.defaultFont.deriveFont(Font.BOLD));
			}
		}

		setBorder(BorderFactory.createEmptyBorder(UIScale.scale(1), 0, UIScale.scale(1), 0));

		Color textFg = UIManager.getColor("Tree.textForeground");
		Color textBg = UIManager.getColor("Tree.textBackground");

		if (isSelected) {
			Color selBg = UIManager.getColor("Tree.selectionBackground");
			Color selFg = UIManager.getColor("Tree.selectionForeground");
			if (selBg == null) selBg = new Color(60, 60, 60);
			if (selFg == null) selFg = Color.WHITE;
			setForeground(selFg);
			setBackground(selBg);
			setBackgroundSelectionColor(selBg);
			setTextSelectionColor(selFg);
		} else if (row == hoveredRow) {
			Color hoverBg = UIManager.getColor("Tree.selectionInactiveBackground");
			setForeground(textFg);
			setBackground(hoverBg != null ? hoverBg : UIManager.getColor("control"));
		} else {
			setForeground(textFg);
			setBackground(textBg);
		}

		setOpaque(true);
		return this;
	}

}
