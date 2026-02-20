package gui;

import application.HandleConfig;
import application.SimpleTableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import java.awt.*;
import java.io.Serial;
import java.net.URL;

public class CustomTableCellRenderer extends DefaultTableCellRenderer {
	@Serial
	private static final long serialVersionUID = 1L;
	private int hoveredRow = -2;

	private final String parent;
	private static ImageIcon ebookCellIcon = null;

	public CustomTableCellRenderer(String parent) {
		this.parent = parent;
		String iconPath = HandleConfig.darkmode == 1 ? "/resources/ebook_inv.png" : "/resources/ebook.png";
		URL url = getClass().getResource(iconPath);
		if (url != null) {
			ImageIcon raw = new ImageIcon(url);
			ebookCellIcon = new ImageIcon(raw.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));
		}
	}

	public void setHoveredRow(int row) {
		this.hoveredRow = row;
	}

	public void clearHoveredRow() {
		this.hoveredRow = -2;
	}

	/** Baut HTML-Stern-String für einen Rating-Wert (0–5, halbe Schritte). */
	private String buildStarHtml(double rating) {
		int full = (int) rating;
		boolean half = (rating - full) >= 0.5;
		int empty = 5 - full - (half ? 1 : 0);
		String emptyColor = HandleConfig.darkmode == 1 ? "#4A4A4A" : "#C8C8C8";
		StringBuilder sb = new StringBuilder("<html><big><font color='#F5A623'>");
		sb.append("★".repeat(full));
		sb.append("</font>");
		if (half) sb.append("<font color='#F5A623'>½</font>");
		sb.append("<font color='").append(emptyColor).append("'>");
		sb.append("☆".repeat(empty));
		sb.append("</font></big></html>");
		return sb.toString();
	}

	/** Escapet HTML-Sonderzeichen in Seriennamen. */
	private String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
		setIcon(null);

		// Spaltentyp nur für SimpleTableModel auswerten — WishlistTableModel hat
		// eine andere Spaltenstruktur und würde sonst falsch interpretiert.
		boolean isBooklist  = table.getModel() instanceof SimpleTableModel;
		boolean isEbookCol  = false;
		boolean isRatingCol = false;
		if (isBooklist) {
			int modelCol = table.convertColumnIndexToModel(column);
			String colKey = modelCol < SimpleTableModel.columnKeys.length
					? SimpleTableModel.columnKeys[modelCol] : "";
			isEbookCol  = SimpleTableModel.KEY_EBOOK.equals(colKey);
			isRatingCol = SimpleTableModel.KEY_RATING.equals(colKey);
		}

		// Ausrichtung
		if (isEbookCol || isRatingCol) {
			setHorizontalAlignment(SwingConstants.CENTER);
		} else {
			setHorizontalAlignment(SwingConstants.LEFT);
		}

		// c) E-Book-Spalte: Icon für E-Books, leer für physische Bücher
		if (isEbookCol) {
			String val = value != null ? value.toString() : "";
			if ("●".equals(val)) {
				if (ebookCellIcon != null) {
					setIcon(ebookCellIcon);
					setText("");
				}
				// else: Fallback auf ● bleibt über super gesetzt
			} else {
				setText("");
			}
		}

		// b) Rating-Spalte: HTML-Sterne statt Zahlenwert
		if (isRatingCol) {
			String val = value != null ? value.toString() : "";
			if (!val.isEmpty()) {
				try {
					double r = Double.parseDouble(val);
					if (r > 0) setText(buildStarHtml(r));
				} catch (NumberFormatException ignored) { }
			}
		}

		// Titel-Spalte: Titel fett + Serie · Band darunter (Spotify-Stil, zweizeilig)
		if (isBooklist && SimpleTableModel.KEY_TITLE.equals(
				SimpleTableModel.columnKeys[table.convertColumnIndexToModel(column)])) {
			String val = value != null ? value.toString() : "";
			String[] parts = val.split(SimpleTableModel.TITLE_SEP, 3);
			String title  = parts.length > 0 ? parts[0] : val;
			String series = parts.length > 1 ? parts[1] : "";
			String vol    = parts.length > 2 ? parts[2] : "";
			String subColor = isSelected ? "#dddddd"
					: (HandleConfig.darkmode == 1 ? "#888888" : "#5a5a5a");
			// Schriftgröße der Untertitelzeile direkt aus der aktuellen Komponentenschrift
			// ableiten, damit sie mit der fontSize-Einstellung skaliert (statt festes size='-1').
			int subPt = Math.max(8, table.getFont().getSize() - 3);
			String subStyle = "color:" + subColor + ";font-size:" + subPt + "pt";
			if (!series.isEmpty() && !vol.isEmpty()) {
				setText("<html><b>" + escapeHtml(title) + "</b>"
						+ "<br><span style='" + subStyle + "'>"
						+ escapeHtml(series) + " · " + escapeHtml(vol)
						+ "</span></html>");
			} else if (!series.isEmpty()) {
				setText("<html><b>" + escapeHtml(title) + "</b>"
						+ "<br><span style='" + subStyle + "'>"
						+ escapeHtml(series)
						+ "</span></html>");
			} else {
				setText("<html><b>" + escapeHtml(title) + "</b></html>");
			}
		}

		// Hintergrundfarben
		boolean darkMode = HandleConfig.darkmode == 1;
		if (isSelected) {
			component.setForeground(UIManager.getColor("Table.selectionForeground"));
			component.setBackground(UIManager.getColor("Table.selectionBackground"));
		} else if (row == hoveredRow) {
			// f) Hover: Mischfarbe aus Base + Selection (sichtbarer als selectionInactiveBackground)
			component.setForeground(UIManager.getColor("Table.foreground"));
			Color base = UIManager.getColor("Table.background");
			Color sel  = UIManager.getColor("Table.selectionBackground");
			if (base != null && sel != null) {
				component.setBackground(new Color(
						(base.getRed()   + sel.getRed())   / 2,
						(base.getGreen() + sel.getGreen()) / 2,
						(base.getBlue()  + sel.getBlue())  / 2
				));
			} else {
				component.setBackground(UIManager.getColor("Table.selectionInactiveBackground"));
			}
		} else {
			// a) Zebra-Stripes: ungerade Zeilen leicht aufgehellt (dark) bzw. abgedunkelt (light)
			component.setForeground(UIManager.getColor("Table.foreground"));
			if (row % 2 == 0) {
				component.setBackground(UIManager.getColor("Table.background"));
			} else {
				Color base = UIManager.getColor("Table.background");
				if (base != null) {
					int dr = darkMode ?  7 : -8;
					int dg = darkMode ?  7 : -7;
					int db = darkMode ?  9 : -5;
					component.setBackground(new Color(
							Math.min(255, Math.max(0, base.getRed()   + dr)),
							Math.min(255, Math.max(0, base.getGreen() + dg)),
							Math.min(255, Math.max(0, base.getBlue()  + db))
					));
				} else {
					component.setBackground(darkMode ? new Color(35, 36, 42) : new Color(246, 246, 251));
				}
			}
		}

		return component;
	}
}
