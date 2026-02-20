package gui;

import application.HandleConfig;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.BorderFactory;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.Serial;

public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

	@Serial
	private static final long serialVersionUID = 1L;

	int hoveredRow = -1;

	// Pill-Zustand, pro Render-Aufruf gesetzt
	private String pillText = null;
	private Color pillBgColor;
	private Color pillFgColor;

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public CustomTreeCellRenderer() {
		setFont(Mainframe.defaultFont);
		setBorderSelectionColor(null);
	}

	private String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	/** Mischt zwei Farben mit gegebenem Anteil b (0=nur a, 1=nur b). */
	private static Color blend(Color a, Color b, float ratio) {
		return new Color(
				Math.min(255, Math.max(0, (int) (a.getRed()   * (1 - ratio) + b.getRed()   * ratio))),
				Math.min(255, Math.max(0, (int) (a.getGreen() * (1 - ratio) + b.getGreen() * ratio))),
				Math.min(255, Math.max(0, (int) (a.getBlue()  * (1 - ratio) + b.getBlue()  * ratio)))
		);
	}

	/** Erweitert die bevorzugte Breite um Platz für die Pill. */
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		if (pillText != null) {
			Font pFont = getFont().deriveFont(Font.BOLD, Math.max(9f, getFont().getSize() * 0.75f));
			FontMetrics pFm = getFontMetrics(pFont);
			int hPad = UIScale.scale(6);
			int extra = pFm.stringWidth(pillText) + hPad * 2 + UIScale.scale(12);
			d = new Dimension(d.width + extra, d.height);
		}
		return d;
	}

	/** Malt die Pill-Badge nach dem normalen Label-Inhalt. */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (pillText == null) return;

		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font pFont = getFont().deriveFont(Font.BOLD, Math.max(9f, getFont().getSize() * 0.75f));
		FontMetrics nameFm = getFontMetrics(getFont());
		FontMetrics pFm    = g2.getFontMetrics(pFont);

		int nameW = nameFm.stringWidth(getText());
		int hPad  = UIScale.scale(6);
		int vPad  = UIScale.scale(2);
		int pW    = pFm.stringWidth(pillText) + hPad * 2;
		int pH    = pFm.getAscent() + pFm.getDescent() + vPad * 2;
		int px    = getInsets().left + nameW + UIScale.scale(8);
		int py    = (getHeight() - pH) / 2;

		if (px + pW <= getWidth() - UIScale.scale(4)) {
			g2.setColor(pillBgColor);
			g2.fillRoundRect(px, py, pW, pH, pH, pH);
			g2.setFont(pFont);
			g2.setColor(pillFgColor);
			g2.drawString(pillText, px + hPad, py + vPad + pFm.getAscent() - 1);
		}
		g2.dispose();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, hasFocus);
		setIcon(null);
		pillText = null;

		boolean isSeriesLevel = false;
		boolean dark = HandleConfig.darkmode == 1;

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node   = (DefaultMutableTreeNode) value;
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			String nodeText = node.getUserObject().toString();

			if (parent != null && parent.getParent() == null) {
				// Autoren-Ebene: plain text + Pill
				int bookCount = Mainframe.allEntries.getBookCountForAuthor(nodeText);
				setText(nodeText);
				pillText = String.valueOf(bookCount);
				setFont(Mainframe.defaultFont);

			} else if (parent != null && parent.getParent() != null
					&& parent.getParent().getParent() == null) {
				// Serien-Ebene
				isSeriesLevel = true;
				setText(nodeText);
				setFont(Mainframe.defaultFont.deriveFont(Font.PLAIN,
						Mainframe.defaultFont.getSize() * 0.9f));
			} else {
				// Root-Node
				setFont(Mainframe.defaultFont.deriveFont(Font.BOLD));
			}
		}

		int vPad = UIScale.scale(2);
		int lPad = isSeriesLevel ? UIScale.scale(4) : UIScale.scale(6);
		setBorder(BorderFactory.createEmptyBorder(vPad, lPad, vPad, UIScale.scale(6)));

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
			// Pill auf Selection: etwas aufgehellt/abgedunkelt
			pillBgColor = dark ? blend(selBg, Color.WHITE, 0.18f) : blend(selBg, Color.BLACK, 0.14f);
			pillFgColor = selFg;

		} else if (row == hoveredRow) {
			// Hover-Farbe: 50/50-Blende aus Tree-Hintergrund + Selection
			Color treeBg = UIManager.getColor("Tree.background");
			if (treeBg == null) treeBg = UIManager.getColor("Panel.background");
			Color sel = UIManager.getColor("Tree.selectionBackground");
			Color hoverBg = (treeBg != null && sel != null)
					? blend(treeBg, sel, 0.5f)
					: (UIManager.getColor("Tree.selectionInactiveBackground"));
			setForeground(textFg);
			setBackground(hoverBg);
			pillBgColor = dark ? new Color(58, 58, 62) : new Color(200, 200, 207);
			pillFgColor = dark ? new Color(135, 135, 140) : new Color(60, 60, 68);

		} else {
			// Serien-Ebene: etwas gedimmt
			if (isSeriesLevel && textFg != null) {
				textFg = new Color(textFg.getRed(), textFg.getGreen(), textFg.getBlue(), 170);
			}
			setForeground(textFg);
			setBackground(textBg);
			pillBgColor = dark ? new Color(40, 40, 43) : new Color(228, 228, 233);
			pillFgColor = dark ? new Color(118, 118, 124) : new Color(72, 72, 80);
		}

		setOpaque(true);
		return this;
	}
}
