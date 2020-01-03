package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import application.Book;
import application.BookListModel;
import application.HandleImage;
import data.Database;

public class Dialog_edit extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txt_author;
	private JTextField txt_title;
	private JCheckBox check_von;
	private JTextField txt_leihVon;
	private JCheckBox check_an;
	private JTextField txt_leihAn;
	private JTextField txt_merk;
	private JTextField txt_serie;
	private Font standardFont = new Font("standard", Font.BOLD, 14);
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70,130,180,125),2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70,130,180,200),4);

	public Dialog_edit(BookListModel einträge, int index, DefaultTreeModel treeModel,
			DefaultMutableTreeNode rootNode) {
		this.setTitle("Buch bearbeiten");
		this.setSize(new Dimension(700, 500));
//		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		this.setAlwaysOnTop(true);
		
		Book eintrag = einträge.getElementAt(index);

		URL iconURL = getClass().getResource("/resources/Liste.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_north = new JPanel();
		panel_north.setLayout(new GridLayout(1, 1));

		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 40, 40));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridLayout(4, 1, 40, 40));

		JPanel panel_east_border = new JPanel();
		panel_east_border.setLayout(new BorderLayout(10, 10));

		JPanel panel_east_grid = new JPanel();
		panel_east_grid.setLayout(new GridLayout(4, 1, 40, 40));
		panel_east_border.add(panel_east_grid, BorderLayout.WEST);

		JPanel panel_south = new JPanel();
		panel_south.setLayout(new GridLayout(3, 2, 10, 10));

		int höhe = 35;
		int breite = 100;

		BufferedImage image = null;
		JButton btn_browseAuthor = new JButton();
		try {
			image = ImageIO.read(getClass().getResource("/resources/amazon.png"));
			btn_browseAuthor.setIcon(new ImageIcon(image));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		btn_browseAuthor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String amazonUrl = "www.amazon.de/s?k=" + txt_author.getText().replaceAll(" ", "+");
					openWebpage(new URI(amazonUrl));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		panel_east_grid.add(btn_browseAuthor);

		// Bild anzeigen
		ImageIcon img = showImg(eintrag);
		if (img != null) {
			JLabel lbl_pic = new JLabel(img);
			lbl_pic.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						e.getPoint();
						showMenu(e);
					}
				}

				private void showMenu(MouseEvent e) {
					JPopupMenu menu = new JPopupMenu();
					JMenuItem itemDelPic = new JMenuItem("Bild löschen");
					JMenuItem itemChanPic = new JMenuItem("Bild bearbeiten");
					menu.add(itemChanPic);
					menu.add(itemDelPic);
					menu.show(lbl_pic, e.getX(), e.getY());
					itemChanPic.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							String webpage = JOptionPane.showInputDialog(null, "Bitte URL einfügen");
							if (webpage != null && webpage != "") {
								HandleImage.DownloadWebPage(webpage, eintrag);
							}
						}
					});
					itemDelPic.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							boolean state = HandleImage.deletePic(txt_author.getText(), txt_title.getText());
							if (state == true) {
								JOptionPane.showMessageDialog(null, "Bild erfolgreich gelöscht");
								eintrag.setPic(null);
								dispose(); 
							} else {
								JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten");
							}
						}

					});

				}
			});
			panel_east_border.add(lbl_pic, BorderLayout.CENTER);

		} else {
			JButton btn_downloadPic = new JButton("Download");
			btn_downloadPic.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					HandleImage.getImage(eintrag);
				}
			});
			panel_east_border.add(btn_downloadPic, BorderLayout.CENTER);
		}
		
		
		JLabel lbl_datum = new JLabel("Datum: " + new SimpleDateFormat("dd.MM.yyyy").format(eintrag.getDatum()));
		panel_north.add(lbl_datum);

		// empty Label amazon Button
		JLabel lbl_emptySearch1 = new JLabel("");
		panel_east_grid.add(lbl_emptySearch1);
		JLabel lbl_emptySearch2 = new JLabel("");
		panel_east_grid.add(lbl_emptySearch2);
		// End empty Label

		JButton btn_browseSeries = new JButton();
		btn_browseSeries.setIcon(new ImageIcon(image));
		btn_browseSeries.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String amazonUrl = "www.amazon.de/s?k=" + txt_serie.getText().replaceAll(" ", "+");
					openWebpage(new URI(amazonUrl));
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		panel_east_grid.add(btn_browseSeries);

		// Empty Panel top Gap
		JLabel lbl_empty1 = new JLabel("");
		lbl_empty1.setFont(standardFont);
		lbl_empty1.setPreferredSize(new Dimension(breite, 10));
		panel_north.add(lbl_empty1);
		// Ende topGap

		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(standardFont);
		lbl_author.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_author);

		txt_author = new JTextField(eintrag.getAutor());
		txt_author.setFont(standardFont);
		txt_author.setPreferredSize(new Dimension(50, höhe));
		txt_author.setBorder(standardBorder);
		txt_author.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					speichern(eintrag);
				else if (!e.isActionKey()) {
					txt_author.setBackground(Color.white);
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}
		});
		txt_author.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_author.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_author.setBorder(activeBorder);
				
			}
			
		});
		panel_center.add(txt_author, BorderLayout.NORTH);

		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(standardFont);
		lbl_title.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_title);

		txt_title = new JTextField(eintrag.getTitel());
		txt_title.setFont(standardFont);
		txt_title.setPreferredSize(new Dimension(50, höhe));
		txt_title.setBorder(standardBorder);
		txt_title.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					speichern(eintrag);
				} else if (!e.isActionKey()) {
					txt_title.setBackground(Color.white);
					txt_title.setForeground(Color.black);
					if (txt_title.getText().equals("Buch bereits vorhanden!")) {
						txt_title.setText("");
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txt_title.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_title.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_title.setBorder(activeBorder);
				
			}
			
		});
		panel_center.add(txt_title, BorderLayout.CENTER);

		JLabel lbl_merk = new JLabel("Bemerkung:");
		lbl_merk.setFont(standardFont);
		lbl_merk.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_merk);

		txt_merk = new JTextField(eintrag.getBemerkung());
		txt_merk.setFont(standardFont);
		txt_merk.setPreferredSize(new Dimension(50, höhe));
		txt_merk.setBorder(standardBorder);
		txt_merk.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					speichern(eintrag);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txt_merk.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_merk.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_merk.setBorder(activeBorder);
				
			}
			
		});
		panel_center.add(txt_merk, BorderLayout.SOUTH);

		JLabel lbl_serie = new JLabel("Serie:");
		lbl_serie.setFont(standardFont);
		lbl_serie.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_serie);

		txt_serie = new JTextField(eintrag.getSerie());
		txt_serie.setFont(standardFont);
		txt_serie.setPreferredSize(new Dimension(50, höhe));
		txt_serie.setBorder(standardBorder);
		txt_serie.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					speichern(eintrag);
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txt_serie.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_serie.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_serie.setBorder(activeBorder);
				
			}
			
		});
		panel_center.add(txt_serie);

		check_von = new JCheckBox("ausgeliehen von");
		check_von.setFont(standardFont);
		if (!eintrag.getAusgeliehen_von().isEmpty())
			check_von.setSelected(true);
		check_von.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (check_von.isSelected()) {
					check_an.setSelected(false);
					txt_leihAn.setVisible(false);
					txt_leihVon.setText("");
					txt_leihVon.setVisible(true);
				} else {
					txt_leihVon.setVisible(false);
				}

			}
		});
		panel_south.add(check_von);

		check_an = new JCheckBox("ausgeliehen an");
		check_an.setFont(standardFont);
		if (!eintrag.getAusgeliehen_an().isEmpty())
			check_an.setSelected(true);
		check_an.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (check_an.isSelected()) {
					check_von.setSelected(false);
					txt_leihVon.setVisible(false);
					txt_leihAn.setText("");
					txt_leihAn.setVisible(true);
				} else {
					txt_leihAn.setVisible(false);
				}
			}
		});
		panel_south.add(check_an);

		txt_leihVon = new JTextField(eintrag.getAusgeliehen_von());
		txt_leihVon.setFont(standardFont);
		txt_leihVon.setBorder(standardBorder);
		if (eintrag.getAusgeliehen_von().isEmpty())
			txt_leihVon.setVisible(false);
		txt_leihVon.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					speichern(eintrag);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txt_leihVon.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_leihVon.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_leihVon.setBorder(activeBorder);
				
			}
			
		});
		panel_south.add(txt_leihVon);

		txt_leihAn = new JTextField(eintrag.getAusgeliehen_an());
		txt_leihAn.setFont(standardFont);
		txt_leihAn.setBorder(standardBorder);
		if (eintrag.getAusgeliehen_an().isEmpty())
			txt_leihAn.setVisible(false);
		txt_leihAn.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					speichern(eintrag);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txt_leihAn.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				txt_leihAn.setBorder(standardBorder);
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				txt_leihAn.setBorder(activeBorder);
				
			}
			
		});
		panel_south.add(txt_leihAn);

		JButton btn_add = new JButton("Speichern");
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				speichern(eintrag);
//				Mainframe.search(Mainframe.getLastSearch());
			}
		});
		panel_south.add(btn_add);

		JButton btn_abort = new JButton("Abbrechen");
		btn_abort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		panel_south.add(btn_abort);

		this.add(panel_north, BorderLayout.NORTH);
		this.add(panel_west, BorderLayout.WEST);
		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_east_border, BorderLayout.EAST);
		this.add(panel_south, BorderLayout.SOUTH);

		this.setVisible(true);
		this.setResizable(false);

	}

	public void speichern(Book eintrag) {
		try {
			String oldAutor = eintrag.getAutor();
			String oldTitel = eintrag.getTitel();
			String newAutor = txt_author.getText().trim();
			String newTitel = txt_title.getText().trim();
			String newBemerkung = txt_merk.getText().trim();
			String newSerie = txt_serie.getText().trim();
			Timestamp datum = new Timestamp(System.currentTimeMillis());
			if (!txt_author.getText().isEmpty() && !txt_title.getText().isEmpty()
					&& txt_title.getForeground() != Color.white) {
				if (check_an.isSelected()) {
					eintrag.setAusgeliehen(true);
					eintrag.setAusgeliehen_an(txt_leihAn.getText().trim());
					eintrag.setAusgeliehen_von("");
					Database.delete(oldAutor, oldTitel);
					Database.add(newAutor, newTitel, "an", txt_leihAn.getText().trim(), txt_merk.getText().trim(),
							txt_serie.getText().trim(),datum.toString());
				} else if (check_von.isSelected()) {
					eintrag.setAusgeliehen(true);
					eintrag.setAusgeliehen_von(txt_leihVon.getText().trim());
					eintrag.setAusgeliehen_an("");
					Database.delete(oldAutor, oldTitel);
					Database.add(newAutor, newTitel, "von", txt_leihVon.getText().trim(), txt_merk.getText().trim(),
							txt_serie.getText().trim(),datum.toString());

				} else {
					eintrag.setAusgeliehen(false);
					eintrag.setAusgeliehen_an("");
					eintrag.setAusgeliehen_von("");
					Database.delete(oldAutor, oldTitel);
					Database.add(newAutor, newTitel, "nein", "", txt_merk.getText().trim(),
							txt_serie.getText().trim(),datum.toString());
				}
				eintrag.setAutor(newAutor);
				eintrag.setTitel(newTitel);
				eintrag.setBemerkung(newBemerkung);
				eintrag.setSerie(newSerie);
				eintrag.setDatum(datum);
				dispose();
			} else {
				if (txt_author.getText().isEmpty()) {
					txt_author.setBackground(new Color(255, 105, 105));
				}
				if (txt_title.getText().isEmpty()) {
					txt_title.setBackground(new Color(255, 105, 105));
				}
				txt_title.setText("");
			}
		} catch (SQLException ex) {
			txt_title.setForeground(Color.white);
			txt_title.setBackground(new Color(255, 105, 105));
			if (ex.getSQLState() == "23505") {
				txt_title.setText("Buch bereits vorhanden!");
			} else if (ex.getSQLState() == "22001") {
				txt_title.setText("Autor/Titel zu lang (max. 50 Zeichen)!");
			}
		}
		BookListModel.autorenPrüfen();
	}

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public ImageIcon showImg(Book eintrag) {
		Image img = null;
		try {
			img = eintrag.getPic();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (img != null) {
			return new ImageIcon(img);
		} else {
			return null;
		}

	}

}
