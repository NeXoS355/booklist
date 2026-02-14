package gui;

import application.HandleConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.io.Serial;

/**
 * Programmatisch gezeichnetes Star-Rating-Widget.
 * Ersetzt die 11 PNG-Dateien (0Star.png bis 5Star.png) durch vektorbasierte Sterne.
 * Unterstuetzt halbe Sterne (0.5er Schritte), Hover-Preview und Rechtsklick-Kontextmenue.
 */
public class StarRatingPanel extends JPanel {

	@Serial
	private static final long serialVersionUID = 1L;

	private static final int STAR_COUNT = 5;
	private static final int STAR_SIZE = 24;
	private static final int STAR_GAP = 4;

	private double rating = 0;
	private double hoverRating = -1;

	private final Color filledColor = new Color(255, 193, 7);
	private final Color emptyColor;

	private RatingChangeListener ratingChangeListener;
	private Runnable rightClickListener;

	public interface RatingChangeListener {
		void onRatingChanged(double newRating);
	}

	public StarRatingPanel() {
		emptyColor = HandleConfig.darkmode == 1 ? new Color(80, 80, 80) : new Color(200, 200, 200);

		int width = STAR_COUNT * (STAR_SIZE + STAR_GAP) - STAR_GAP;
		setPreferredSize(new Dimension(width, STAR_SIZE));
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!isEnabled()) return;
				hoverRating = calculateRating(e.getX());
				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				hoverRating = -1;
				repaint();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled()) return;
				if (SwingUtilities.isLeftMouseButton(e)) {
					double clicked = calculateRating(e.getX());
					setRating(clicked);
					if (ratingChangeListener != null) {
						ratingChangeListener.onRatingChanged(clicked);
					}
				} else if (SwingUtilities.isRightMouseButton(e)) {
					if (rightClickListener != null) {
						rightClickListener.run();
					}
				}
			}
		});
	}

	/**
	 * Berechnet das Rating (0.5-5.0 in 0.5er Schritten) aus der Mausposition.
	 */
	private double calculateRating(int mouseX) {
		int totalWidth = STAR_COUNT * (STAR_SIZE + STAR_GAP) - STAR_GAP;
		if (mouseX <= 0) return 0.5;
		if (mouseX >= totalWidth) return 5.0;

		double perStar = (double) totalWidth / STAR_COUNT;
		double raw = mouseX / perStar;
		// Auf 0.5er runden (aufrunden)
		return Math.min(5.0, Math.max(0.5, Math.ceil(raw * 2) / 2.0));
	}

	public void setRating(double rating) {
		this.rating = rating;
		repaint();
	}

	public double getRating() {
		return rating;
	}

	public void setRatingChangeListener(RatingChangeListener listener) {
		this.ratingChangeListener = listener;
	}

	public void setRightClickListener(Runnable listener) {
		this.rightClickListener = listener;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		double displayRating = hoverRating >= 0 ? hoverRating : rating;

		for (int i = 0; i < STAR_COUNT; i++) {
			int x = i * (STAR_SIZE + STAR_GAP);
			double starValue = i + 1;

			Shape starShape = createStarShape(x, 0, STAR_SIZE);

			if (displayRating >= starValue) {
				// Voller Stern
				g2.setColor(filledColor);
				g2.fill(starShape);
			} else if (displayRating >= starValue - 0.5) {
				// Halber Stern: linke Haelfte gefuellt, rechte leer
				g2.setColor(emptyColor);
				g2.fill(starShape);

				Shape leftClip = new Rectangle(x, 0, STAR_SIZE / 2, STAR_SIZE);
				g2.setClip(leftClip);
				g2.setColor(filledColor);
				g2.fill(starShape);
				g2.setClip(null);
			} else {
				// Leerer Stern
				g2.setColor(emptyColor);
				g2.fill(starShape);
			}
		}

		g2.dispose();
	}

	/**
	 * Erzeugt eine 5-zackige Stern-Form.
	 */
	private static Shape createStarShape(double x, double y, double size) {
		Path2D path = new Path2D.Double();
		double centerX = x + size / 2;
		double centerY = y + size / 2;
		double outerRadius = size / 2;
		double innerRadius = outerRadius * 0.4;

		for (int i = 0; i < 10; i++) {
			double angle = Math.PI / 2 + i * Math.PI / 5;
			double radius = (i % 2 == 0) ? outerRadius : innerRadius;
			double px = centerX + radius * Math.cos(angle);
			double py = centerY - radius * Math.sin(angle);

			if (i == 0) {
				path.moveTo(px, py);
			} else {
				path.lineTo(px, py);
			}
		}
		path.closePath();
		return path;
	}
}
