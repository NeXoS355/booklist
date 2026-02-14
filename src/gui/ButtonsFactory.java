package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

import javax.swing.*;

public class ButtonsFactory {

	public static JButton createButton() {
		JButton button = new JButton() {
			@Serial
			private static final long serialVersionUID = 1L;
			private boolean hovered = false;

			{
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
					@Override
					public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
				});
			}

			@Override
			protected void paintComponent(Graphics g) {
				if (hovered) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g2.setColor(new Color(128, 128, 128, 50));
					g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
					g2.dispose();
				}
				super.paintComponent(g);
			}
		};
		button.putClientProperty("JButton.buttonType", "none");
		button.setContentAreaFilled(false);
		button.setOpaque(false);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setFont(Mainframe.defaultFont);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		return button;
	}

	public static JButton createButton(String text) {
		JButton button = createButton();
		button.setText(text);
		return button;
	}

	public static JButton createButton(ImageIcon icon) {
		JButton button = createButton();
		button.setIcon(icon);
		return button;
	}

}
