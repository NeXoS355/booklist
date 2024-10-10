package gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import application.HandleConfig;

public class ButtonsFactory {

	static BufferedImage imageActive = null;
	static BufferedImage imageInActive = null;

	public static JButton createButton(String text) {
		JButton button = new JButton(text);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setFocusPainted(false);

		if (HandleConfig.darkmode == 0) {
			button.setForeground(new Color(75, 75, 75));

			if (text.equals("suchen")) {

				try {
					imageActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe.png"));
					imageInActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inactive.png"));
					button.setIcon(new ImageIcon(imageInActive));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					button.setBackground(new Color(240, 240, 240));
					button.setForeground(new Color(50, 50, 50));

					if (text.equals("suchen")) {
						button.setIcon(new ImageIcon(imageInActive));
					}
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					button.setBackground(new Color(220, 220, 220));
					button.setForeground(Color.BLACK);

					if (text.equals("suchen")) {
						button.setIcon(new ImageIcon(imageActive));
					}

				}

				@Override
				public void mousePressed(MouseEvent e) {
					Mainframe.executor.submit(() -> {
						try {
							button.setBackground(new Color(200, 200, 200));
							Thread.sleep(200);
							button.setBackground(new Color(220, 220, 220));
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

					});
				}

			});

		} else {

			button.setBackground(Color.DARK_GRAY);
			button.setForeground(Color.LIGHT_GRAY);

			if (text.equals("suchen")) {

				try {
					imageActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inv.png"));
					imageInActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inactive.png"));
					button.setIcon(new ImageIcon(imageInActive));
				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}

			button.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseExited(MouseEvent e) {
					button.setBackground(Color.DARK_GRAY);
					button.setForeground(Color.LIGHT_GRAY);

					if (text.equals("suchen")) {
						button.setIcon(new ImageIcon(imageInActive));
					}

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					button.setBackground(new Color(75, 75, 75));
					button.setForeground(Color.WHITE);

					if (text.equals("suchen")) {
						button.setIcon(new ImageIcon(imageActive));
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					Mainframe.executor.submit(() -> {
						try {
							button.setBackground(new Color(85, 85, 85));
							Thread.sleep(200);
							button.setBackground(new Color(75, 75, 75));
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}

					});
				}

			});
		}

		return button;
	}

	public static JButton createButton(ImageIcon icon) {
		JButton button = new JButton(icon);
		button.setContentAreaFilled(false);
		button.setOpaque(true);
		button.setFocusPainted(false);

		if (HandleConfig.darkmode == 1) {
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
							e1.printStackTrace();
						}

					});
				}

			});
		} else {
			button.setForeground(new Color(75, 75, 75));
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseExited(MouseEvent e) {
					button.setBackground(new Color(240, 240, 240));
					button.setForeground(new Color(75, 75, 75));
				}

				@Override
				public void mouseEntered(MouseEvent e) {
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
							e1.printStackTrace();
						}

					});

				}

			});
		}

		return button;
	}

}
