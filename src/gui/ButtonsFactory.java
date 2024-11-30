package gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.*;

import application.HandleConfig;

public class ButtonsFactory {

	static BufferedImage imageActive = null;
	static BufferedImage imageInActive = null;

	public static JButton createButton() {
		JButton button = new JButton();
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		button.setFont(Mainframe.defaultFont);

		if (HandleConfig.darkmode == 0) {
			button.setForeground(new Color(75, 75, 75));

			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					button.setBackground(new Color(240, 240, 240));
					button.setForeground(new Color(50, 50, 50));
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					button.setContentAreaFilled(true);
					button.setBackground(new Color(220, 220, 220));
					button.setForeground(Color.BLACK);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					Mainframe.executor.submit(() -> {
						try {
							button.setBackground(new Color(200, 200, 200));
							Thread.sleep(200);
							button.setBackground(new Color(220, 220, 220));
						} catch (InterruptedException e1) {
							Mainframe.logger.error(e1.getMessage());
						}

					});
				}
			});
		} else {
			button.setBackground(Color.DARK_GRAY);
			button.setForeground(Color.LIGHT_GRAY);

			button.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseExited(MouseEvent e) {
					button.setBackground(Color.DARK_GRAY);
					button.setForeground(Color.LIGHT_GRAY);
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					button.setBackground(new Color(75, 75, 75));
					button.setForeground(Color.WHITE);
				}

				@Override
				public void mousePressed(MouseEvent e) {
					Mainframe.executor.submit(() -> {
						try {
							button.setBackground(new Color(85, 85, 85));
							Thread.sleep(200);
							button.setBackground(new Color(75, 75, 75));
						} catch (InterruptedException e1) {
							Mainframe.logger.error(e1.getMessage());
						}

					});
				}

			});
		}

		return button;
	}

	public static JButton createButton(String text) {
		JButton button = createButton();
		button.setText(text);

		if (text.equals("suchen")) {
			try {
				imageActive = ImageIO.read(Objects.requireNonNull(Mainframe.class.getResource("/resources/lupe.png")));
				imageInActive = ImageIO.read(Objects.requireNonNull(Mainframe.class.getResource("/resources/lupe_inactive.png")));
				button.setIcon(new ImageIcon(imageInActive));
			} catch (IOException e1) {
				Mainframe.logger.error(e1.getMessage());
			}
		}

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				if (text.equals("suchen")) {
					button.setIcon(new ImageIcon(imageInActive));
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (text.equals("suchen")) {
					button.setIcon(new ImageIcon(imageActive));
				}

			}
		});
		return button;
	}

	public static JButton createButton(ImageIcon icon) {
		JButton button = createButton();
		button.setIcon(icon);
		return button;
	}

}
