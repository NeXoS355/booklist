package gui;

import java.awt.*;
import java.io.Serial;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.util.UIScale;
import application.BookListModel;
import data.Database;

public class Dialog_info extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;

	public Dialog_info(Frame owner) {
		setTitle(Localization.get("t.info"));
		setModal(true);
		setLocationRelativeTo(owner);

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.setBorder(new EmptyBorder(UIScale.scale(20), UIScale.scale(24), UIScale.scale(20), UIScale.scale(24)));

		// --- Header ---
		JLabel header = new JLabel(Localization.get("t.info"));
		header.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD, Mainframe.defaultFont.getSize() * 1.4f));
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(header);
		content.add(Box.createVerticalStrut(UIScale.scale(4)));

		int totalBooks = Mainframe.allEntries.getSize();
		int ebooks = BookListModel.getEbookCount(1);
		int physical = BookListModel.getEbookCount(0);
		JLabel subtitle = new JLabel(totalBooks + " " + Localization.get("info.totalBooks") + "  ·  "
				+ physical + " " + Localization.get("info.physical") + "  ·  "
				+ ebooks + " E-Books");
		subtitle.setFont(Mainframe.defaultFont.deriveFont(Font.PLAIN, Mainframe.defaultFont.getSize() * 0.85f));
		subtitle.setForeground(UIManager.getColor("Label.disabledForeground"));
		subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(subtitle);
		content.add(Box.createVerticalStrut(UIScale.scale(20)));

		// --- Highlights ---
		content.add(createSectionLabel(Localization.get("info.highlights")));
		content.add(Box.createVerticalStrut(UIScale.scale(8)));

		JPanel highlights = new JPanel(new GridLayout(2, 2, UIScale.scale(16), UIScale.scale(8)));
		highlights.setAlignmentX(Component.LEFT_ALIGNMENT);
		highlights.setOpaque(false);

		String mostAuthor = BookListModel.getMostOf("autor").toString().replace("[", "").replace("]", "");
		String mostSeries = BookListModel.getMostOf("serie").toString().replace("[", "").replace("]", "");
		String bestAuthor = BookListModel.getBestRatingOf("autor").toString().replace("[", "").replace("]", "");
		String bestSeries = BookListModel.getBestRatingOf("serie").toString().replace("[", "").replace("]", "");

		highlights.add(createStatCard(Localization.get("info.mostAuthor"), mostAuthor.isEmpty() ? "—" : mostAuthor));
		highlights.add(createStatCard(Localization.get("info.bestAuthor"), bestAuthor.isEmpty() ? "—" : bestAuthor));
		highlights.add(createStatCard(Localization.get("info.mostSeries"), mostSeries.isEmpty() ? "—" : mostSeries));
		highlights.add(createStatCard(Localization.get("info.bestSeries"), bestSeries.isEmpty() ? "—" : bestSeries));

		content.add(highlights);
		content.add(Box.createVerticalStrut(UIScale.scale(20)));

		// --- Bücher pro Jahr (Balkendiagramm) ---
		Map<String, Integer> booksPerYear = getBooksPerYearData();
		if (!booksPerYear.isEmpty()) {
			content.add(createSectionLabel(Localization.get("info.countBooksPerYear")));
			content.add(Box.createVerticalStrut(UIScale.scale(8)));
			BarChartPanel yearChart = new BarChartPanel(booksPerYear);
			yearChart.setAlignmentX(Component.LEFT_ALIGNMENT);
			content.add(yearChart);
			content.add(Box.createVerticalStrut(UIScale.scale(20)));
		}

		// --- Top Autoren (Balkendiagramm) ---
		Map<String, Integer> topAuthors = getTopAuthorsData(7);
		if (!topAuthors.isEmpty()) {
			content.add(createSectionLabel(Localization.get("info.topAuthors")));
			content.add(Box.createVerticalStrut(UIScale.scale(8)));
			BarChartPanel authorChart = new BarChartPanel(topAuthors);
			authorChart.setAlignmentX(Component.LEFT_ALIGNMENT);
			content.add(authorChart);
			content.add(Box.createVerticalStrut(UIScale.scale(20)));
		}

		// --- Rating-Verteilung ---
		Map<String, Integer> ratingDist = getRatingDistribution();
		if (!ratingDist.isEmpty()) {
			content.add(createSectionLabel(Localization.get("info.ratingDistribution")));
			content.add(Box.createVerticalStrut(UIScale.scale(8)));
			BarChartPanel ratingChart = new BarChartPanel(ratingDist);
			ratingChart.setAlignmentX(Component.LEFT_ALIGNMENT);
			content.add(ratingChart);
		}

		JScrollPane scrollPane = new JScrollPane(content);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(UIScale.scale(16));
		setContentPane(scrollPane);

		setSize(UIScale.scale(520), UIScale.scale(620));
		setVisible(true);
	}

	private JLabel createSectionLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD, Mainframe.defaultFont.getSize() * 0.95f));
		label.setForeground(UIManager.getColor("Label.disabledForeground"));
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JPanel createStatCard(String title, String value) {
		JPanel card = new JPanel();
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1),
				new EmptyBorder(UIScale.scale(8), UIScale.scale(12), UIScale.scale(8), UIScale.scale(12))
		));
		card.setBackground(UIManager.getColor("Panel.background"));

		// Titel ohne Doppelpunkt
		String cleanTitle = title.endsWith(":") ? title.substring(0, title.length() - 1) : title;
		JLabel titleLabel = new JLabel(cleanTitle);
		titleLabel.setFont(Mainframe.defaultFont.deriveFont(Font.PLAIN, Mainframe.defaultFont.getSize() * 0.8f));
		titleLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JLabel valueLabel = new JLabel(value);
		valueLabel.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD));
		valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		card.add(titleLabel);
		card.add(Box.createVerticalStrut(UIScale.scale(2)));
		card.add(valueLabel);

		return card;
	}

	private Map<String, Integer> getBooksPerYearData() {
		Map<String, Integer> data = new LinkedHashMap<>();
		ResultSet rs = Database.getColumnCountsWithGroup("YEAR(date)");
		try {
			while (rs != null && rs.next()) {
				int count = rs.getInt(1);
				String year = rs.getString(2);
				if (year != null && !year.isEmpty()) {
					data.put(year, count);
				}
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
		}
		return data;
	}

	private Map<String, Integer> getTopAuthorsData(int limit) {
		Map<String, Integer> all = new LinkedHashMap<>();
		ResultSet rs = Database.getColumnCountsWithGroup("autor");
		try {
			while (rs != null && rs.next()) {
				int count = rs.getInt(1);
				String author = rs.getString(2);
				if (author != null && !author.isEmpty()) {
					all.put(author, count);
				}
			}
		} catch (SQLException e) {
			Mainframe.logger.error(e.getMessage());
		} finally {
			Database.closeResultSet(rs);
		}
		// Nach Anzahl sortieren, Top N
		Map<String, Integer> sorted = new LinkedHashMap<>();
		all.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.limit(limit)
				.forEach(e -> sorted.put(e.getKey(), e.getValue()));
		return sorted;
	}

	private Map<String, Integer> getRatingDistribution() {
		Map<String, Integer> dist = new LinkedHashMap<>();
		// getRating() gibt 0.5-5.0 zurück (halbe Sterne)
		// Auf ganze Sterne runden für die Verteilung
		int[] buckets = new int[6]; // Index 1-5 für 1★-5★
		for (int i = 0; i < Mainframe.allEntries.getSize(); i++) {
			double rating = Mainframe.allEntries.getElementAt(i).getRating();
			if (rating > 0) {
				int stars = Math.min(5, Math.max(1, (int) Math.round(rating)));
				buckets[stars]++;
			}
		}
		for (int i = 5; i >= 1; i--) {
			if (buckets[i] > 0) {
				dist.put(i + " \u2605", buckets[i]);
			}
		}
		return dist;
	}

	/**
	 * Horizontales Balkendiagramm per Custom Painting
	 */
	private static class BarChartPanel extends JPanel {
		private final Map<String, Integer> data;
		private final int maxValue;
		private static final int BAR_HEIGHT = 18;
		private static final int BAR_GAP = 6;
		private static final int LABEL_WIDTH = 120;

		BarChartPanel(Map<String, Integer> data) {
			this.data = data;
			this.maxValue = data.values().stream().mapToInt(Integer::intValue).max().orElse(1);
			setOpaque(false);
			int h = data.size() * UIScale.scale(BAR_HEIGHT + BAR_GAP) + UIScale.scale(4);
			setPreferredSize(new Dimension(0, h));
			setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			Font labelFont = Mainframe.defaultFont.deriveFont(Font.PLAIN, Mainframe.defaultFont.getSize() * 0.82f);
			Font valueFont = Mainframe.defaultFont.deriveFont(Font.BOLD, Mainframe.defaultFont.getSize() * 0.78f);
			FontMetrics fm = g2.getFontMetrics(labelFont);

			Color accent = UIManager.getColor("Component.accentColor");
			if (accent == null) accent = new Color(0, 120, 212);
			Color barBg = UIManager.getColor("Separator.foreground");
			if (barBg == null) barBg = new Color(200, 200, 200);
			Color textColor = UIManager.getColor("Label.foreground");

			int labelW = UIScale.scale(LABEL_WIDTH);
			int barH = UIScale.scale(BAR_HEIGHT);
			int gap = UIScale.scale(BAR_GAP);
			int barArea = getWidth() - labelW - UIScale.scale(40);
			int arc = UIScale.scale(4);
			int y = UIScale.scale(2);

			for (Map.Entry<String, Integer> entry : data.entrySet()) {
				// Label
				g2.setFont(labelFont);
				g2.setColor(textColor);
				String label = entry.getKey();
				// Kuerzen falls zu lang
				while (fm.stringWidth(label) > labelW - UIScale.scale(8) && label.length() > 3) {
					label = label.substring(0, label.length() - 2) + "…";
				}
				int textY = y + (barH + fm.getAscent()) / 2 - UIScale.scale(2);
				g2.drawString(label, 0, textY);

				// Bar background
				g2.setColor(barBg);
				g2.fillRoundRect(labelW, y, barArea, barH, arc, arc);

				// Bar fill
				int barW = Math.max(arc, (int) ((double) entry.getValue() / maxValue * barArea));
				g2.setColor(accent);
				g2.fillRoundRect(labelW, y, barW, barH, arc, arc);

				// Value text
				g2.setFont(valueFont);
				g2.setColor(textColor);
				String val = String.valueOf(entry.getValue());
				g2.drawString(val, labelW + barW + UIScale.scale(6), textY);

				y += barH + gap;
			}
			g2.dispose();
		}
	}
}
