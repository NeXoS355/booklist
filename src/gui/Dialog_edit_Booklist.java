package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import application.Book_Booklist;
import application.HandleConfig;
import application.BookListModel;
import application.HandleWebInfo;
import data.Database;

public class Dialog_edit_Booklist extends JDialog {

	private static final long serialVersionUID = 1L;
	private RoundJTextField txt_author;
	private RoundJTextField txt_title;
	private JCheckBox check_von;
	private RoundJTextField txt_leihVon;
	private JCheckBox check_an;
	private RoundJTextField txt_leihAn;
	private RoundJTextField txt_merk;
	private RoundJTextField txt_serie;
	private RoundJTextField txt_seriePart;
	private JCheckBox check_ebook;
	private JLabel lbl_pic;
	private JLabel lbl_firstStar;
	private JLabel lbl_secondStar;
	private JLabel lbl_thirdStar;
	private JLabel lbl_fourthStar;
	private JLabel lbl_fifthStar;
	private JLabel lbl_ackRating;
	private ImageIcon fullStar;
	private ImageIcon halfStar;
	private ImageIcon noStar;
	private ImageIcon ackRating;
	private boolean ack = false;
	private JPanel panel_east_rating = new JPanel(new GridLayout(1, 6, -5, -5));
	private int currentShownRating = 0;
	private Book_Booklist eintrag;
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_edit_Booklist(BookListModel einträge, int index, DefaultTreeModel treeModel,
			DefaultMutableTreeNode rootNode) {

		this.setTitle("Buch bearbeiten");
		this.setSize(new Dimension(600, 645));
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 100);
		this.setAlwaysOnTop(false);

		eintrag = einträge.getElementAt(index);

		if (HandleConfig.loadOnDemand == 1) {
			BookListModel.loadOnDemand(eintrag);
		}

		URL fullStarIconURL = getClass().getResource("/resources/fullStar.png");
		fullStar = new ImageIcon(fullStarIconURL);
		URL halfStarIconURL = getClass().getResource("/resources/halfStar.png");
		halfStar = new ImageIcon(halfStarIconURL);
		URL noStarIconURL = getClass().getResource("/resources/noStar.png");
		noStar = new ImageIcon(noStarIconURL);
		URL ackRatingIconURL = getClass().getResource("/resources/ackRating.png");
		ackRating = new ImageIcon(ackRatingIconURL);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_north = new JPanel();
		panel_north.setLayout(new GridLayout(1, 2));

		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 10, 20));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridBagLayout());

		JPanel panel_east_border = new JPanel();
		panel_east_border.setLayout(new BorderLayout(10, 10));

		JPanel panel_south_border = new JPanel();
		panel_south_border.setLayout(new BorderLayout(10, 10));

		JPanel panel_south = new JPanel();
		panel_south.setLayout(new GridLayout(3, 2, 10, 10));

		int höhe = 60;
		int breite = 100;

		/*
		 * create and add components to Panel North
		 */
		JLabel lbl_datum = new JLabel(
				"hinzugefügt am: " + new SimpleDateFormat("dd.MM.yyyy").format(eintrag.getDatum()));

		JLabel lbl_isbn = new JLabel("ISBN: " + eintrag.getIsbn(), SwingConstants.RIGHT);
		lbl_isbn.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					e.getPoint();
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemCopy = new JMenuItem("kopieren");
				JMenuItem itemDel = new JMenuItem("löschen");
				menu.add(itemCopy);
				menu.add(itemDel);
				menu.show(lbl_isbn, e.getX(), e.getY());
				itemCopy.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
						StringSelection selection = new StringSelection(eintrag.getIsbn());
						cb.setContents(selection, null);
					}
				});
				itemDel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						eintrag.setIsbn(null);
						Database.delIsbn(eintrag.getBid());
						dispose();
						new Dialog_edit_Booklist(einträge, index, treeModel, rootNode);
					}
				});
			}
		});

		panel_north.add(lbl_datum);
		panel_north.add(lbl_isbn);
		panel_north.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		/*
		 * create and add components to Panel East
		 */
		ImageIcon imgIcn = showImg(eintrag);
		if (imgIcn != null) {
			Image img = imgIcn.getImage();
			Image newimg = img.getScaledInstance(128, 192, java.awt.Image.SCALE_SMOOTH);
			imgIcn = new ImageIcon(newimg);
			lbl_pic = new JLabel(imgIcn);
			lbl_pic.setPreferredSize(new Dimension(160, 280));
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
								HandleWebInfo.DownloadWebPage(eintrag, 2, false);
								lbl_pic = new JLabel(showImg(eintrag));
							}
						}
					});
					itemDelPic.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							boolean state = HandleWebInfo.deletePic(eintrag.getBid());
							if (state == true) {
								// JOptionPane.showMessageDialog(null, "Bild erfolgreich gelöscht");
								eintrag.setPic(null);
								dispose();
								new Dialog_edit_Booklist(einträge, index, treeModel, rootNode);
							} else {
								JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten");
							}
						}

					});

				}
			});
			panel_east_border.add(lbl_pic, BorderLayout.CENTER);

		} else {
			JButton btn_downloadInfo = new JButton("Download Info");
			btn_downloadInfo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int compResult1 = 0;
					int compResult2 = 0;

					compResult1 = HandleWebInfo.DownloadWebPage(eintrag, 2, false);
					if (compResult1 < 75) {
						compResult2 = HandleWebInfo.DownloadWebPage(eintrag, 2, true);
						if (compResult1 > compResult2) {
							HandleWebInfo.DownloadWebPage(eintrag, 2, false);
						}
					}

					dispose();
					new Dialog_edit_Booklist(einträge, index, treeModel, rootNode);
				}
			});
			panel_east_border.add(btn_downloadInfo, BorderLayout.CENTER);
		}

		lbl_firstStar = new JLabel(noStar);
		lbl_secondStar = new JLabel(noStar);
		lbl_thirdStar = new JLabel(noStar);
		lbl_fourthStar = new JLabel(noStar);
		lbl_fifthStar = new JLabel(noStar);
		lbl_ackRating = new JLabel(ackRating);
		lbl_ackRating.setVisible(false);

		setRatingGUI();

		lbl_firstStar.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int half = lbl_firstStar.getWidth() / 2;
					int mouse = e.getX();
					if (mouse > half && currentShownRating != 2) {
						lbl_secondStar.setIcon(noStar);
						lbl_thirdStar.setIcon(noStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_firstStar.setIcon(fullStar);
						currentShownRating = 2;
					} else if (mouse < half && currentShownRating != 1) {
						lbl_secondStar.setIcon(noStar);
						lbl_thirdStar.setIcon(noStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_firstStar.setIcon(halfStar);
						currentShownRating = 1;
					}
				}
			}
		});
		lbl_firstStar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingGUI();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setRating(e, 1);
			}

		});

		lbl_secondStar.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int half = lbl_firstStar.getWidth() / 2;
					int mouse = e.getX();
					if (mouse > half && currentShownRating != 4) {
						lbl_firstStar.setIcon(fullStar);
						lbl_thirdStar.setIcon(noStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_secondStar.setIcon(fullStar);
						currentShownRating = 4;
					} else if (mouse < half && currentShownRating != 3) {
						lbl_firstStar.setIcon(fullStar);
						lbl_thirdStar.setIcon(noStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_secondStar.setIcon(halfStar);
						currentShownRating = 3;
					}
				}
			}
		});
		lbl_secondStar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingGUI();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setRating(e, 2);
			}

		});

		lbl_thirdStar.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int half = lbl_firstStar.getWidth() / 2;
					int mouse = e.getX();
					if (mouse > half && currentShownRating != 6) {
						lbl_firstStar.setIcon(fullStar);
						lbl_secondStar.setIcon(fullStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_thirdStar.setIcon(fullStar);
						currentShownRating = 6;
					} else if (mouse < half && currentShownRating != 5) {
						lbl_firstStar.setIcon(fullStar);
						lbl_secondStar.setIcon(fullStar);
						lbl_fourthStar.setIcon(noStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_thirdStar.setIcon(halfStar);
						currentShownRating = 5;
					}
				}
			}
		});
		lbl_thirdStar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingGUI();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setRating(e, 3);
			}

		});

		lbl_fourthStar.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int half = lbl_firstStar.getWidth() / 2;
					int mouse = e.getX();
					if (mouse > half && currentShownRating != 8) {
						lbl_firstStar.setIcon(fullStar);
						lbl_secondStar.setIcon(fullStar);
						lbl_thirdStar.setIcon(fullStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_fourthStar.setIcon(fullStar);
						currentShownRating = 8;
					} else if (mouse < half && currentShownRating != 7) {
						lbl_firstStar.setIcon(fullStar);
						lbl_secondStar.setIcon(fullStar);
						lbl_thirdStar.setIcon(fullStar);
						lbl_fifthStar.setIcon(noStar);
						lbl_fourthStar.setIcon(halfStar);
						currentShownRating = 7;
					}
				}
			}
		});
		lbl_fourthStar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingGUI();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setRating(e, 4);
			}

		});
		lbl_fifthStar.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int half = lbl_firstStar.getWidth() / 2;
					int mouse = e.getX();
					int delay = 25;
					if (mouse > half && currentShownRating != 10) {
						Mainframe.executor.submit(() -> {
							try {
								lbl_firstStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_secondStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_thirdStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_fourthStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_fifthStar.setIcon(fullStar);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});
						currentShownRating = 10;
					} else if (mouse < half && currentShownRating != 9) {
						Mainframe.executor.submit(() -> {
							try {
								lbl_firstStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_secondStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_thirdStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_fourthStar.setIcon(fullStar);
								Thread.sleep(delay);
								lbl_fifthStar.setIcon(halfStar);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						});
						currentShownRating = 9;
					}
				}
			}
		});
		lbl_fifthStar.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingGUI();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				setRating(e, 5);
			}

		});

		panel_east_rating.add(lbl_firstStar);
		panel_east_rating.add(lbl_secondStar);
		panel_east_rating.add(lbl_thirdStar);
		panel_east_rating.add(lbl_fourthStar);
		panel_east_rating.add(lbl_fifthStar);
		panel_east_rating.add(lbl_ackRating);

		panel_east_rating.setAlignmentX(0);

		panel_east_border.add(panel_east_rating, BorderLayout.SOUTH);

		/*
		 * create and add components to Panel Center
		 */
		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(Mainframe.schrift);
		lbl_author.setPreferredSize(new Dimension(breite, höhe));

		txt_author = new RoundJTextField(eintrag.getAutor());
		txt_author.setFont(Mainframe.schrift);
		txt_author.setPreferredSize(new Dimension(50, höhe));
		txt_author.setBorder(standardBorder);

		txt_author.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txt_author.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txt_author.getText().substring(0, typed);

					if (!txt_author.getText().equals("")) {
						String[] autoren = autoCompletion(typedString, "autor");
						for (int i = 0; i < autoren.length && autoren[i] != null; i++) {
							int autorenLength = autoren[i].length();
							String setText = autoren[i].substring(typed, autorenLength);
							txt_author.setText(typedString + setText);
							txt_author.setCaretPosition(typed);
							txt_author.setSelectionStart(typed);
							txt_author.setSelectionEnd(autoren[i].length());

						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txt_author.getCaretPosition();
					txt_author.setText(txt_author.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txt_author.getCaretPosition();
					txt_author.setText(txt_author.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					speichern(eintrag);
				} else if (!e.isActionKey()) {
					txt_author.setBackground(Color.white);
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();

			}

			public void keyPressed(KeyEvent e) {

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

		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(Mainframe.schrift);
		lbl_title.setPreferredSize(new Dimension(breite, höhe));
		txt_title = new RoundJTextField(eintrag.getTitel());
		txt_title.setFont(Mainframe.schrift);
		txt_title.setPreferredSize(new Dimension(50, höhe));
		txt_title.setBorder(standardBorder);
		txt_title.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					speichern(eintrag);
				} else if (!e.isActionKey()) {
					if (txt_title.getText().equals("Buch bereits vorhanden!")) {
						txt_title.setText("");
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txt_title.getText().length() > 50) {
					txt_title.setEditable(false);
					txt_title.setText("Nicht mehr als 50 Zeichen!");
					txt_title.setBackground(new Color(255, 105, 105));
				}

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

			@Override
			public void mousePressed(MouseEvent e) {
				if (txt_title.getText().equals("Nicht mehr als 50 Zeichen!")) {
					txt_title.setEditable(true);
					txt_title.setForeground(Color.black);
					txt_title.setBackground(Color.white);
					txt_title.setText("");
				}
			}

		});

		JLabel lbl_merk = new JLabel("Bemerkung:");
		lbl_merk.setFont(Mainframe.schrift);
		lbl_merk.setPreferredSize(new Dimension(breite, höhe));

		txt_merk = new RoundJTextField(eintrag.getBemerkung());
		txt_merk.setFont(Mainframe.schrift);
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

		JLabel lbl_serie = new JLabel("Serie | Band:");
		lbl_serie.setFont(Mainframe.schrift);
		lbl_serie.setPreferredSize(new Dimension(breite, höhe));

		txt_serie = new RoundJTextField(eintrag.getSerie());
		txt_serie.setFont(Mainframe.schrift);
		txt_serie.setPreferredSize(new Dimension(50, höhe));
		txt_serie.setBorder(standardBorder);
		txt_serie.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txt_serie.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txt_serie.getText().substring(0, typed);

					if (!txt_serie.getText().equals("")) {
						String[] serien = autoCompletion(typedString, "serie");
						for (int i = 0; i < serien.length && serien[i] != null; i++) {
							int autorenLength = serien[i].length();
							String setText = serien[i].substring(typed, autorenLength);
							txt_serie.setText(typedString + setText);
							txt_serie.setCaretPosition(typed);
							txt_serie.setSelectionStart(typed);
							txt_serie.setSelectionEnd(serien[i].length());

						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txt_serie.getCaretPosition();
					txt_serie.setText(txt_serie.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txt_serie.getCaretPosition();
					txt_serie.setText(txt_serie.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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

		txt_seriePart = new RoundJTextField(eintrag.getSeriePart());
		txt_seriePart.setFont(Mainframe.schrift);
		txt_seriePart.setPreferredSize(new Dimension(50, höhe));
		txt_seriePart.setBorder(standardBorder);
		txt_seriePart.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					speichern(eintrag);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txt_seriePart.getText().length() > 2) {
					txt_seriePart.setText("");
					txt_seriePart.setBackground(new Color(255, 105, 105));
				} else
					txt_seriePart.setBackground(Color.white);
			}

		});
		txt_seriePart.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txt_seriePart.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txt_seriePart.setBorder(activeBorder);

			}

		});

		JLabel lbl_ebook = new JLabel("E-Book:");
		lbl_ebook.setFont(Mainframe.schrift);
		lbl_ebook.setPreferredSize(new Dimension(breite, höhe));

		check_ebook = new JCheckBox();
		check_ebook.setFont(Mainframe.schrift);
		check_ebook.setSelected(eintrag.isEbook());

		/*
		 * Set Center Layout
		 */

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.ipady = 15;
		panel_center.add(lbl_author, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_author, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panel_center.add(lbl_title, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_title, c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panel_center.add(lbl_merk, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_merk, c);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panel_center.add(lbl_serie, c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 8;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_serie, c);
		c.gridx = 9;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 0);
		panel_center.add(txt_seriePart, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panel_center.add(lbl_ebook, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(check_ebook, c);
		panel_center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		/*
		 * create components for Panel South
		 */
		check_von = new JCheckBox("ausgeliehen von");
		check_von.setFont(Mainframe.schrift);
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

		check_an = new JCheckBox("ausgeliehen an");
		check_an.setFont(Mainframe.schrift);
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

		txt_leihVon = new RoundJTextField(eintrag.getAusgeliehen_von());
		txt_leihVon.setFont(Mainframe.schrift);
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

		txt_leihAn = new RoundJTextField(eintrag.getAusgeliehen_an());
		txt_leihAn.setFont(Mainframe.schrift);
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

		JButton btn_add = new JButton("Speichern");
		btn_add.setFont(Mainframe.schrift);
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				speichern(eintrag);
			}
		});

		JButton btn_abort = new JButton("Abbrechen");
		btn_abort.setFont(Mainframe.schrift);
		btn_abort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		/*
		 * add components into Panel South
		 */
		panel_south.add(check_von);
		panel_south.add(check_an);
		panel_south.add(txt_leihVon);
		panel_south.add(txt_leihAn);
		panel_south.add(btn_add);
		panel_south.add(btn_abort);
		panel_south.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		/*
		 * create TextArea for Description
		 */
		JTextArea txt_desc = new JTextArea(10, 30);
		txt_desc.setText(eintrag.getDesc());
		txt_desc.setEnabled(false);
		txt_desc.setLineWrap(true);
		txt_desc.setWrapStyleWord(true);
		txt_desc.setFont(Mainframe.descSchrift);
		txt_desc.setDisabledTextColor(Color.BLACK);
		this.setSize(this.getWidth(), this.getHeight() + (Mainframe.descSchrift.getSize() - 16) * 14);
		JScrollPane scroll_desc = new JScrollPane(txt_desc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		txt_desc.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					e.getPoint();
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemDelDesc = new JMenuItem("Beschreibung löschen");
				menu.add(itemDelDesc);
				menu.show(txt_desc, e.getX(), e.getY());
				itemDelDesc.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						boolean state = Database.delDesc(eintrag.getBid());
						if (state == true) {
							// JOptionPane.showMessageDialog(null, "Beschreibung erfolgreich gelöscht");
							eintrag.setDesc(null);
							dispose();
							new Dialog_edit_Booklist(einträge, index, treeModel, rootNode);
						} else {
							JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten");
						}
					}

				});

			}
		});

		panel_south_border.add(scroll_desc, BorderLayout.SOUTH);
		panel_south_border.add(panel_south, BorderLayout.CENTER);

		this.add(panel_north, BorderLayout.NORTH);
		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_east_border, BorderLayout.EAST);
		this.add(panel_south_border, BorderLayout.SOUTH);

		if (Mainframe.getTreeSelection() == "") {
			Mainframe.search(txt_author.getText());
		} else {
			Mainframe.search(Mainframe.getTreeSelection());
		}

		this.setVisible(true);
		this.setResizable(false);

	}

	public String[] autoCompletion(String search, String field) {
		String[] returnArray = null;
		if (field.equals("autor")) {
			int j = 0;
			int anz_autoren = BookListModel.autoren.size();
			String[] result = new String[anz_autoren];
			for (int i = 0; i < anz_autoren; i++) {
				if (BookListModel.autoren.get(i).startsWith(search)) {
					result[j] = BookListModel.autoren.get(i);
					j++;
				}
			}
			returnArray = new String[j];
			for (int i = 0; i < j; i++) {
				if (result[i] != null) {
					returnArray[i] = result[i];
				}

			}
		} else if (field.equals("serie")) {
			int j = 0;
			String[] serien = BookListModel.getSerienVonAutor(txt_author.getText());
			String[] result = new String[serien.length];
			for (int i = 0; i < serien.length; i++) {
				if (serien[i].startsWith(search)) {
					result[j] = serien[i];
					j++;
				}
			}
			returnArray = new String[j];
			for (int i = 0; i < j; i++) {
				if (result[i] != null) {
					returnArray[i] = result[i];
				}

			}
		}
		return returnArray;
	}

	public void speichern(Book_Booklist eintrag) {
		int bid = eintrag.getBid();
		String oldAutor = eintrag.getAutor();
		String oldTitel = eintrag.getTitel();
		String oldMerk = eintrag.getBemerkung();
		String oldSerie = eintrag.getSerie();
		String oldSeriePart = eintrag.getSeriePart();
		boolean oldEbook = eintrag.isEbook();
		boolean oldAusgeliehen = eintrag.isAusgeliehen();

		String oldNameAusgVon = eintrag.getAusgeliehen_von();
		String oldNameAusgAn = eintrag.getAusgeliehen_an();

		String newAutor = txt_author.getText().trim();
		String newTitel = txt_title.getText().trim();
		String newBemerkung = txt_merk.getText().trim();
		String newSerie = txt_serie.getText().trim();
		String newSeriePart = txt_seriePart.getText();
		boolean newEbook = check_ebook.isSelected();
		boolean newAusgAn = check_an.isSelected();
		boolean newAusgVon = check_von.isSelected();

		if (!txt_author.getText().isEmpty() && !txt_title.getText().isEmpty()) {
			if (checkInput(newAutor, newTitel, Mainframe.einträge.getIndexOf(oldAutor, oldTitel))) {
				if (!oldAutor.equals(newAutor)) {
					eintrag.setAutor(newAutor);
					Database.updateBooklistEntry(bid, "autor", newAutor);
				}
				if (!oldTitel.equals(newTitel)) {
					eintrag.setTitel(newTitel);
					Database.updateBooklistEntry(bid, "titel", newTitel);
				}
				if (!oldMerk.equals(newBemerkung)) {
					eintrag.setBemerkung(newBemerkung);
					Database.updateBooklistEntry(bid, "bemerkung", newBemerkung);
				}
				if (!oldSerie.equals(newSerie)) {
					eintrag.setSerie(newSerie);
					Database.updateBooklistEntry(bid, "serie", newSerie);
				}
				if (!oldSeriePart.equals(newSeriePart)) {
					eintrag.setSeriePart(newSeriePart);
					Database.updateBooklistEntry(bid, "seriePart", newSeriePart);
				}
				if (oldEbook != newEbook) {
					eintrag.setEbook(newEbook);
					String ebook_str = "0";
					if (newEbook)
						ebook_str = "1";
					Database.updateBooklistEntry(bid, "ebook", ebook_str);
				}
				if (oldAusgeliehen == true) {
					if (newAusgAn && oldNameAusgAn.length() == 0) {
						eintrag.setAusgeliehen_an(txt_leihAn.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "an");
						Database.updateBooklistEntry(bid, "name", txt_leihAn.getText());
					}
					if (newAusgVon && oldNameAusgVon.length() == 0) {
						eintrag.setAusgeliehen_von(txt_leihVon.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "von");
						Database.updateBooklistEntry(bid, "name", txt_leihVon.getText());
					}
					if (!newAusgAn && !newAusgVon) {
						eintrag.setAusgeliehen(false);
						eintrag.setAusgeliehen_von("");
						eintrag.setAusgeliehen_an("");
						Database.updateBooklistEntry(bid, "ausgeliehen", "nein");
						Database.updateBooklistEntry(bid, "name", "");
					}
				}
				if (oldAusgeliehen == false) {
					if (newAusgAn) {
						eintrag.setAusgeliehen(true);
						eintrag.setAusgeliehen_an(txt_leihAn.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "an");
						Database.updateBooklistEntry(bid, "name", txt_leihAn.getText());
					} else if (newAusgVon) {
						eintrag.setAusgeliehen(true);
						eintrag.setAusgeliehen_von(txt_leihVon.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "von");
						Database.updateBooklistEntry(bid, "name", txt_leihVon.getText());
					}
				}
				Mainframe.logger.info("Buch geändert: " + eintrag.getAutor() + "-" + eintrag.getTitel());
				dispose();
			} else {
				Mainframe.logger.info("Buch ändern: Bereits vorhanden!");
				txt_title.setText("Buch bereits vorhanden!");
				txt_title.setBackground(new Color(255, 105, 105));
			}
		} else {
			if (txt_author.getText().isEmpty()) {
				Mainframe.logger.info("Buch ändern: Autor nicht gesetzt!");
				txt_author.setBackground(new Color(255, 105, 105));
			}
			if (txt_title.getText().isEmpty()) {
				Mainframe.logger.info("Buch ändern: Titel nicht gesetzt!");
				txt_title.setBackground(new Color(255, 105, 105));
			}
		}
		BookListModel.autorenPrüfen();
		Mainframe.updateModel();
	}

	// Check New Autor & Titel if there already exists the same
	public boolean checkInput(String newAutor, String newTitel, int index) {
		for (int i = 0; i < Mainframe.einträge.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.einträge.getElementAt(i);
			if (eintrag.getAutor().equals(newAutor) && eintrag.getTitel().equals(newTitel)) {
				if (i != index) {
					Mainframe.logger.info("Buch ändern: Autor & Titel bereits vorhanden");
					return false;
				}
			}
		}
		return true;
	}

	public static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				Mainframe.logger.trace(e.getMessage());
			}
		}
		return false;
	}

	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			Mainframe.logger.error(e.getMessage());
		}
		return false;
	}

	public ImageIcon showImg(Book_Booklist eintrag) {
		Image img = null;
		try {
			img = eintrag.getPic();
		} catch (Exception e) {
			Mainframe.logger.error(e.getMessage());
		}
		if (img != null) {
			return new ImageIcon(img);
		} else {
			return null;
		}

	}

	private void setRatingGUI() {
		if (eintrag.getRating() == 1) {
			lbl_firstStar.setIcon(halfStar);
			lbl_secondStar.setIcon(noStar);
			lbl_thirdStar.setIcon(noStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 2) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(noStar);
			lbl_thirdStar.setIcon(noStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 3) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(halfStar);
			lbl_thirdStar.setIcon(noStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 4) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(noStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 5) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(halfStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 6) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(fullStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 7) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(fullStar);
			lbl_fourthStar.setIcon(halfStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 8) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(fullStar);
			lbl_fourthStar.setIcon(fullStar);
			lbl_fifthStar.setIcon(noStar);
		} else if (eintrag.getRating() == 9) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(fullStar);
			lbl_fourthStar.setIcon(fullStar);
			lbl_fifthStar.setIcon(halfStar);
		} else if (eintrag.getRating() == 10) {
			lbl_firstStar.setIcon(fullStar);
			lbl_secondStar.setIcon(fullStar);
			lbl_thirdStar.setIcon(fullStar);
			lbl_fourthStar.setIcon(fullStar);
			lbl_fifthStar.setIcon(fullStar);
		} else {
			lbl_firstStar.setIcon(noStar);
			lbl_secondStar.setIcon(noStar);
			lbl_thirdStar.setIcon(noStar);
			lbl_fourthStar.setIcon(noStar);
			lbl_fifthStar.setIcon(noStar);
		}
		currentShownRating = 0;

	}

	private void setRating(MouseEvent e, int i) {
		int half = lbl_firstStar.getWidth() / 2;
		int mouse = e.getX();

		if (mouse > half) {
			int rate = i * 2;
			Mainframe.logger.trace("Rating set: " + rate);
			eintrag.setRating(rate);
		} else {
			int rate = i * 2 - 1;
			Mainframe.logger.trace("Rating set: " + rate);
			eintrag.setRating(rate);
		}

		Mainframe.executor.submit(() -> {
			try {
				ack = true;
				panel_east_rating.add(lbl_ackRating);
				lbl_ackRating.setVisible(true);
				Thread.sleep(2000);
				panel_east_rating.remove(lbl_ackRating);
				lbl_ackRating.setVisible(false);
				panel_east_rating.repaint();
				ack = false;
			} catch (InterruptedException e1) {
				Mainframe.logger.error(e1.getMessage());
			}
		});
	}
}
