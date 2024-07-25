package gui;

import java.awt.BorderLayout;
import java.awt.Color;
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

/**
 * Dialog to change Entry in Booklist Table and DB
 */
public class Dialog_edit_Booklist extends JDialog {

	private static final long serialVersionUID = 1L;
	private RoundJTextField txtAuthor;
	private RoundJTextField txtTitle;
	private JCheckBox checkFrom;
	private RoundJTextField txtBorrowedFrom;
	private JCheckBox checkTo;
	private RoundJTextField txtBorrowedTo;
	private RoundJTextField txtNote;
	private RoundJTextField txtSeries;
	private RoundJTextField txtSeriesVol;
	private JCheckBox checkEbook;
	private JButton btnAdd;
	private JLabel lblPic;
	private JLabel lblStars;
	private ImageIcon zeroStar;
	private ImageIcon zeroHalfStar;
	private ImageIcon oneStar;
	private ImageIcon oneHalfStar;
	private ImageIcon twoStar;
	private ImageIcon twoHalfStar;
	private ImageIcon threeStar;
	private ImageIcon threeHalfStar;
	private ImageIcon fourStar;
	private ImageIcon fourHalfStar;
	private ImageIcon fifeStar;
	private JLabel lblAckRating;
	private ImageIcon ackRating;
	private boolean ack = false;
	private JPanel panelEastRating = new JPanel(new GridBagLayout());
	private Book_Booklist entry;
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_edit_Booklist(BookListModel entries, int index, DefaultTreeModel treeModel,
			DefaultMutableTreeNode rootNode) {

		Mainframe.logger.trace("Book edit: start creating Frame");
		this.setTitle("Buch bearbeiten");
		this.setSize(new Dimension(600, 645));
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 100);
		this.setAlwaysOnTop(true);

		entry = entries.getElementAt(index);

		if (HandleConfig.loadOnDemand == 1) {
			BookListModel.loadOnDemand(entry);
		}

		URL ackRatingIconURL = getClass().getResource("/resources/ackRating.png");
		ackRating = new ImageIcon(ackRatingIconURL);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panelNorth = new JPanel();
		panelNorth.setLayout(new GridLayout(1, 2));

		JPanel panelWest = new JPanel();
		panelWest.setLayout(new GridLayout(4, 1, 10, 20));

		JPanel panelCenter = new JPanel();
		panelCenter.setLayout(new GridBagLayout());

		JPanel panelEastBorder = new JPanel();
		panelEastBorder.setLayout(new BorderLayout(10, 10));

		JPanel panelSouthBorder = new JPanel();
		panelSouthBorder.setLayout(new BorderLayout(10, 10));

		JPanel panelSouth = new JPanel();
		panelSouth.setLayout(new GridLayout(3, 2, 10, 10));

		int heigth = 60;
		int width = 100;

		/*
		 * create and add components to Panel North
		 */
		JLabel lblDate = new JLabel("hinzugefügt am: " + new SimpleDateFormat("dd.MM.yyyy").format(entry.getDate()));

		JLabel lblIsbn = new JLabel("ISBN: " + entry.getIsbn(), SwingConstants.RIGHT);
		lblIsbn.addMouseListener(new MouseAdapter() {

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
				menu.show(lblIsbn, e.getX(), e.getY());
				itemCopy.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
						StringSelection selection = new StringSelection(entry.getIsbn());
						cb.setContents(selection, null);
					}
				});
				itemDel.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						entry.setIsbn(null, true);
						dispose();
						new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
					}
				});
			}
		});

		panelNorth.add(lblDate);
		panelNorth.add(lblIsbn);
		panelNorth.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		/*
		 * create and add components to Panel East
		 */
		ImageIcon imgIcn = showImg(entry);
		if (imgIcn != null) {
			Image img = imgIcn.getImage();
			Image newimg = img.getScaledInstance(128, 192, java.awt.Image.SCALE_SMOOTH);
			imgIcn = new ImageIcon(newimg);
			lblPic = new JLabel(imgIcn);
			lblPic.setPreferredSize(new Dimension(160, 280));
			lblPic.addMouseListener(new MouseAdapter() {

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
					menu.show(lblPic, e.getX(), e.getY());
					itemChanPic.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							String webpage = JOptionPane.showInputDialog(null, "Bitte URL einfügen");
							if (webpage != null && webpage != "") {
								HandleWebInfo.DownloadWebPage(entry, 2, false);
								lblPic = new JLabel(showImg(entry));
							}
						}
					});
					itemDelPic.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							boolean state = HandleWebInfo.deletePic(entry.getBid());
							if (state == true) {
								// JOptionPane.showMessageDialog(null, "Bild erfolgreich gelöscht");
								entry.setPic(null);
								dispose();
								new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
							} else {
								JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten");
							}
						}

					});

				}
			});
			panelEastBorder.add(lblPic, BorderLayout.CENTER);

		} else {
			JButton btnDownloadInfo = new JButton("Download Info");
			btnDownloadInfo.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					int compResult1 = 0;
					int compResult2 = 0;

					compResult1 = HandleWebInfo.DownloadWebPage(entry, 2, false);
					if (compResult1 < 75) {
						compResult2 = HandleWebInfo.DownloadWebPage(entry, 2, true);
						if (compResult1 > compResult2) {
							HandleWebInfo.DownloadWebPage(entry, 2, true);
						}
					}

					dispose();
					new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
				}
			});
			panelEastBorder.add(btnDownloadInfo, BorderLayout.CENTER);
		}

		
		/*
		 * create and add components to Rating Panel
		 */
		lblStars = new JLabel();
		URL zeroStarUrl = getClass().getResource("/resources/0Star.png");
		zeroStar = new ImageIcon(zeroStarUrl);
		URL zeroHalfStarUrl = getClass().getResource("/resources/0_5Star.png");
		zeroHalfStar = new ImageIcon(zeroHalfStarUrl);
		URL oneStarUrl = getClass().getResource("/resources/1Star.png");
		oneStar = new ImageIcon(oneStarUrl);
		URL oneHalfStarUrl = getClass().getResource("/resources/1_5Star.png");
		oneHalfStar = new ImageIcon(oneHalfStarUrl);
		URL twoStarUrl = getClass().getResource("/resources/2Star.png");
		twoStar = new ImageIcon(twoStarUrl);
		URL twoHalfStarUrl = getClass().getResource("/resources/2_5Star.png");
		twoHalfStar = new ImageIcon(twoHalfStarUrl);
		URL threeStarUrl = getClass().getResource("/resources/3Star.png");
		threeStar = new ImageIcon(threeStarUrl);
		URL threeHalfStarUrl = getClass().getResource("/resources/3_5Star.png");
		threeHalfStar = new ImageIcon(threeHalfStarUrl);
		URL fourStarUrl = getClass().getResource("/resources/4Star.png");
		fourStar = new ImageIcon(fourStarUrl);
		URL fourHalfStarUrl = getClass().getResource("/resources/4_5Star.png");
		fourHalfStar = new ImageIcon(fourHalfStarUrl);
		URL fifeStarUrl = getClass().getResource("/resources/5Star.png");
		fifeStar = new ImageIcon(fifeStarUrl);

		lblAckRating = new JLabel(ackRating);
		lblAckRating.setVisible(false);

		setRatingIcon();

		lblStars.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (!ack) {
					int segmentWidth = lblStars.getWidth() / 10;
					int mouse = e.getX();
					int segment = mouse / segmentWidth + 1;

					switch (segment) {
					case 1:
						lblStars.setIcon(zeroHalfStar);
						break;
					case 2:
						lblStars.setIcon(oneStar);
						break;
					case 3:
						lblStars.setIcon(oneHalfStar);
						break;
					case 4:
						lblStars.setIcon(twoStar);
						break;
					case 5:
						lblStars.setIcon(twoHalfStar);
						break;
					case 6:
						lblStars.setIcon(threeStar);
						break;
					case 7:
						lblStars.setIcon(threeHalfStar);
						break;
					case 8:
						lblStars.setIcon(fourStar);
						break;
					case 9:
						lblStars.setIcon(fourHalfStar);
						break;
					case 10:
						lblStars.setIcon(fifeStar);
						break;
					}
				}
			}
		});

		lblStars.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setRatingIcon();
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				int segmentWidth = lblStars.getWidth() / 10;
				int mouse = e.getX();
				int segment = mouse / segmentWidth + 1;
				
				if (SwingUtilities.isLeftMouseButton(e)) {
					setRating(segment);
				} else if (SwingUtilities.isRightMouseButton(e)) {
					showMenu(e);
				}
			}
			
			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemDeleteRating = new JMenuItem("Rating löschen");
				menu.add(itemDeleteRating);
				menu.show(lblStars, e.getX(), e.getY());
				itemDeleteRating.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Rating löschen") {
							setRating(0);
							setRatingIcon();
						}
					}
				});
			}
		});

		panelEastRating.add(lblStars);
		panelEastRating.add(lblAckRating);

		panelEastRating.setAlignmentX(0);

		panelEastBorder.add(panelEastRating, BorderLayout.SOUTH);

		/*
		 * create and add components to Panel Center
		 */
		JLabel lblAuthor = new JLabel("Autor:");
		lblAuthor.setFont(Mainframe.defaultFont);
		lblAuthor.setPreferredSize(new Dimension(width, heigth));

		txtAuthor = new RoundJTextField(entry.getAuthor());
		txtAuthor.setFont(Mainframe.defaultFont);
		txtAuthor.setPreferredSize(new Dimension(50, heigth));
		txtAuthor.setBorder(standardBorder);

		txtAuthor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtAuthor.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtAuthor.getText().substring(0, typed);

					if (!txtAuthor.getText().equals("")) {
						String[] authors = autoCompletion(typedString, "autor");
						for (int i = 0; i < authors.length && authors[i] != null; i++) {
							int authorsLength = authors[i].length();
							String setText = authors[i].substring(typed, authorsLength);
							txtAuthor.setText(typedString + setText);
							txtAuthor.setCaretPosition(typed);
							txtAuthor.setSelectionStart(typed);
							txtAuthor.setSelectionEnd(authors[i].length());

						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txtAuthor.getCaretPosition();
					txtAuthor.setText(txtAuthor.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txtAuthor.getCaretPosition();
					txtAuthor.setText(txtAuthor.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					save(entry);
				} else if (!e.isActionKey()) {
					txtAuthor.setBackground(Color.white);
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();

			}

			public void keyPressed(KeyEvent e) {

			}

		});
		txtAuthor.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtAuthor.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtAuthor.setBorder(activeBorder);

			}
		});

		JLabel lblTitle = new JLabel("Titel:");
		lblTitle.setFont(Mainframe.defaultFont);
		lblTitle.setPreferredSize(new Dimension(width, heigth));
		txtTitle = new RoundJTextField(entry.getTitle());
		txtTitle.setFont(Mainframe.defaultFont);
		txtTitle.setPreferredSize(new Dimension(50, heigth));
		txtTitle.setBorder(standardBorder);
		txtTitle.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					save(entry);
				} else if (!e.isActionKey()) {
					if (txtTitle.getText().equals("Buch bereits vorhanden!")) {
						txtTitle.setText("");
						btnAdd.setEnabled(true);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtTitle.getText().length() > 50) {
					txtTitle.setEditable(false);
					txtTitle.setText("Nicht mehr als 50 Zeichen!");
					txtTitle.setBackground(new Color(255, 105, 105));
				}

			}

		});
		txtTitle.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtTitle.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtTitle.setBorder(activeBorder);

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (txtTitle.getText().equals("Nicht mehr als 50 Zeichen!")) {
					txtTitle.setEditable(true);
					txtTitle.setForeground(Color.black);
					txtTitle.setBackground(Color.white);
					txtTitle.setText("");
				}
			}

		});

		JLabel lblNote = new JLabel("Bemerkung:");
		lblNote.setFont(Mainframe.defaultFont);
		lblNote.setPreferredSize(new Dimension(width, heigth));

		txtNote = new RoundJTextField(entry.getNote());
		txtNote.setFont(Mainframe.defaultFont);
		txtNote.setPreferredSize(new Dimension(50, heigth));
		txtNote.setBorder(standardBorder);
		txtNote.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					save(entry);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txtNote.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtNote.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtNote.setBorder(activeBorder);

			}

		});

		JLabel lblSeries = new JLabel("Serie | Band:");
		lblSeries.setFont(Mainframe.defaultFont);
		lblSeries.setPreferredSize(new Dimension(width, heigth));

		txtSeries = new RoundJTextField(entry.getSeries());
		txtSeries.setFont(Mainframe.defaultFont);
		txtSeries.setPreferredSize(new Dimension(50, heigth));
		txtSeries.setBorder(standardBorder);
		txtSeries.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtSeries.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtSeries.getText().substring(0, typed);

					if (!txtSeries.getText().equals("")) {
						String[] series = autoCompletion(typedString, "serie");
						for (int i = 0; i < series.length && series[i] != null; i++) {
							int authorsLength = series[i].length();
							String setText = series[i].substring(typed, authorsLength);
							txtSeries.setText(typedString + setText);
							txtSeries.setCaretPosition(typed);
							txtSeries.setSelectionStart(typed);
							txtSeries.setSelectionEnd(series[i].length());

						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txtSeries.getCaretPosition();
					txtSeries.setText(txtSeries.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txtSeries.getCaretPosition();
					txtSeries.setText(txtSeries.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					save(entry);
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txtSeries.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtSeries.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtSeries.setBorder(activeBorder);

			}

		});

		txtSeriesVol = new RoundJTextField(entry.getSeriesVol());
		txtSeriesVol.setFont(Mainframe.defaultFont);
		txtSeriesVol.setPreferredSize(new Dimension(50, heigth));
		txtSeriesVol.setBorder(standardBorder);
		txtSeriesVol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					save(entry);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtSeriesVol.getText().length() > 2) {
					txtSeriesVol.setText("");
					txtSeriesVol.setBackground(new Color(255, 105, 105));
				} else
					txtSeriesVol.setBackground(Color.white);
			}

		});
		txtSeriesVol.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtSeriesVol.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtSeriesVol.setBorder(activeBorder);

			}

		});

		JLabel lblEbook = new JLabel("E-Book:");
		lblEbook.setFont(Mainframe.defaultFont);
		lblEbook.setPreferredSize(new Dimension(width, heigth));

		checkEbook = new JCheckBox();
		checkEbook.setFont(Mainframe.defaultFont);
		checkEbook.setSelected(entry.isEbook());

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
		panelCenter.add(lblAuthor, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelCenter.add(txtAuthor, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panelCenter.add(lblTitle, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelCenter.add(txtTitle, c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panelCenter.add(lblNote, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.5;
		c.gridwidth = 9;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelCenter.add(txtNote, c);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panelCenter.add(lblSeries, c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 8;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelCenter.add(txtSeries, c);
		c.gridx = 9;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 0);
		panelCenter.add(txtSeriesVol, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panelCenter.add(lblEbook, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panelCenter.add(checkEbook, c);
		panelCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		/*
		 * create components for Panel South
		 */
		checkFrom = new JCheckBox("ausgeliehen von");
		checkFrom.setFont(Mainframe.defaultFont);
		if (!entry.getBorrowedFrom().isEmpty())
			checkFrom.setSelected(true);
		checkFrom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkFrom.isSelected()) {
					checkTo.setSelected(false);
					txtBorrowedTo.setVisible(false);
					txtBorrowedFrom.setText("");
					txtBorrowedFrom.setVisible(true);
				} else {
					txtBorrowedFrom.setVisible(false);
				}

			}
		});

		checkTo = new JCheckBox("ausgeliehen an");
		checkTo.setFont(Mainframe.defaultFont);
		if (!entry.getBorrowedTo().isEmpty())
			checkTo.setSelected(true);
		checkTo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (checkTo.isSelected()) {
					checkFrom.setSelected(false);
					txtBorrowedFrom.setVisible(false);
					txtBorrowedTo.setText("");
					txtBorrowedTo.setVisible(true);
				} else {
					txtBorrowedTo.setVisible(false);
				}
			}
		});

		txtBorrowedFrom = new RoundJTextField(entry.getBorrowedFrom());
		txtBorrowedFrom.setFont(Mainframe.defaultFont);
		txtBorrowedFrom.setBorder(standardBorder);
		if (entry.getBorrowedFrom().isEmpty())
			txtBorrowedFrom.setVisible(false);
		txtBorrowedFrom.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					save(entry);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txtBorrowedFrom.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtBorrowedFrom.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtBorrowedFrom.setBorder(activeBorder);

			}

		});

		txtBorrowedTo = new RoundJTextField(entry.getBorrowedTo());
		txtBorrowedTo.setFont(Mainframe.defaultFont);
		txtBorrowedTo.setBorder(standardBorder);
		if (entry.getBorrowedTo().isEmpty())
			txtBorrowedTo.setVisible(false);
		txtBorrowedTo.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					save(entry);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txtBorrowedTo.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtBorrowedTo.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtBorrowedTo.setBorder(activeBorder);

			}

		});

		btnAdd = new JButton("Speichern");
		btnAdd.setFont(Mainframe.defaultFont);
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				save(entry);
			}
		});

		JButton btnAbort = new JButton("Abbrechen");
		btnAbort.setFont(Mainframe.defaultFont);
		btnAbort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		/*
		 * add components into Panel South
		 */
		panelSouth.add(checkFrom);
		panelSouth.add(checkTo);
		panelSouth.add(txtBorrowedFrom);
		panelSouth.add(txtBorrowedTo);
		panelSouth.add(btnAdd);
		panelSouth.add(btnAbort);
		panelSouth.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		/*
		 * create TextArea for Description
		 */
		JTextArea txtDesc = new JTextArea(10, 30);
		txtDesc.setText(entry.getDesc());
		txtDesc.setEnabled(false);
		txtDesc.setLineWrap(true);
		txtDesc.setWrapStyleWord(true);
		txtDesc.setFont(Mainframe.descFont);
		txtDesc.setDisabledTextColor(Color.BLACK);
		this.setSize(this.getWidth(), this.getHeight() + (Mainframe.descFont.getSize() - 16) * 14);
		JScrollPane scrollDesc = new JScrollPane(txtDesc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		txtDesc.addMouseListener(new MouseAdapter() {

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
				menu.show(txtDesc, e.getX(), e.getY());
				itemDelDesc.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						boolean state = Database.delDesc(entry.getBid());
						if (state == true) {
							// JOptionPane.showMessageDialog(null, "Beschreibung erfolgreich gelöscht");
							entry.setDesc(null, true);
							dispose();
							new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
						} else {
							JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten");
						}
					}

				});

			}
		});

		panelSouthBorder.add(scrollDesc, BorderLayout.SOUTH);
		panelSouthBorder.add(panelSouth, BorderLayout.CENTER);

		this.add(panelNorth, BorderLayout.NORTH);
		this.add(panelCenter, BorderLayout.CENTER);
		this.add(panelEastBorder, BorderLayout.EAST);
		this.add(panelSouthBorder, BorderLayout.SOUTH);

		if (Mainframe.getTreeSelection() == "") {
			Mainframe.search(txtAuthor.getText());
		} else {
			Mainframe.search(Mainframe.getTreeSelection());
		}
		Mainframe.logger.trace("Book edit: Frame successfully created");
		this.setVisible(true);
		this.setResizable(false);

	}

	/**
	 * add the autoComplete feature to "autor" and "serie"
	 * 
	 * @param search - currently typed String
	 * @param field  - sets variable based on which field is active
	 * 
	 * @return String array with matching authors or series
	 * 
	 */
	public String[] autoCompletion(String search, String field) {
		String[] returnArray = null;
		if (field.equals("autor")) {
			int j = 0;
			int authorCount = BookListModel.authors.size();
			String[] result = new String[authorCount];
			for (int i = 0; i < authorCount; i++) {
				if (BookListModel.authors.get(i).startsWith(search)) {
					result[j] = BookListModel.authors.get(i);
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
			String[] series = BookListModel.getSeriesFromAuthor(txtAuthor.getText());
			String[] result = new String[series.length];
			for (int i = 0; i < series.length; i++) {
				if (series[i].startsWith(search)) {
					result[j] = series[i];
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

	/**
	 * save new or updates Entry in Booklist and DB
	 * 
	 * @param entry - new Booklist entry
	 */
	public void save(Book_Booklist entry) {
		int bid = entry.getBid();
		String oldAutor = entry.getAuthor();
		String oldTitel = entry.getTitle();
		String oldNote = entry.getNote();
		String oldSeries = entry.getSeries();
		String oldSeriesVol = entry.getSeriesVol();
		boolean oldEbook = entry.isEbook();
		boolean oldBorrowed = entry.isBorrowed();

		String oldNameBorrowedFrom = entry.getBorrowedFrom();
		String oldNameBorrowedTo = entry.getBorrowedTo();

		String newAuthor = txtAuthor.getText().trim();
		String newTitle = txtTitle.getText().trim();
		String newNote = txtNote.getText().trim();
		String newSeries = txtSeries.getText().trim();
		String newSeriesVol = txtSeriesVol.getText();
		boolean newEbook = checkEbook.isSelected();
		boolean newBorrwoedTo = checkTo.isSelected();
		boolean newBorrwoedFrom = checkFrom.isSelected();

		if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
			if (checkInput(newAuthor, newTitle, Mainframe.entries.getIndexOf(oldAutor, oldTitel))) {
				if (!oldAutor.equals(newAuthor)) {
					entry.setAuthor(newAuthor);
					Database.updateBooklistEntry(bid, "autor", newAuthor);
				}
				if (!oldTitel.equals(newTitle)) {
					entry.setTitle(newTitle);
					Database.updateBooklistEntry(bid, "titel", newTitle);
				}
				if (!oldNote.equals(newNote)) {
					entry.setNote(newNote);
					Database.updateBooklistEntry(bid, "bemerkung", newNote);
				}
				if (!oldSeries.equals(newSeries)) {
					entry.setSeries(newSeries);
					Database.updateBooklistEntry(bid, "serie", newSeries);
				}
				if (!oldSeriesVol.equals(newSeriesVol)) {
					entry.setSeriesVol(newSeriesVol);
					Database.updateBooklistEntry(bid, "seriePart", newSeriesVol);
				}
				if (oldEbook != newEbook) {
					entry.setEbook(newEbook);
					String ebook_str = "0";
					if (newEbook)
						ebook_str = "1";
					Database.updateBooklistEntry(bid, "ebook", ebook_str);
				}
				if (oldBorrowed == true) {
					if (newBorrwoedTo && oldNameBorrowedTo.length() == 0) {
						entry.setBorrowedTo(txtBorrowedTo.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "an");
						Database.updateBooklistEntry(bid, "name", txtBorrowedTo.getText());
					}
					if (newBorrwoedFrom && oldNameBorrowedFrom.length() == 0) {
						entry.setBorrowedFrom(txtBorrowedFrom.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "von");
						Database.updateBooklistEntry(bid, "name", txtBorrowedFrom.getText());
					}
					if (!newBorrwoedTo && !newBorrwoedFrom) {
						entry.setBorrowed(false);
						entry.setBorrowedFrom("");
						entry.setBorrowedTo("");
						Database.updateBooklistEntry(bid, "ausgeliehen", "nein");
						Database.updateBooklistEntry(bid, "name", "");
					}
				}
				if (oldBorrowed == false) {
					if (newBorrwoedTo) {
						entry.setBorrowed(true);
						entry.setBorrowedTo(txtBorrowedTo.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "an");
						Database.updateBooklistEntry(bid, "name", txtBorrowedTo.getText());
					} else if (newBorrwoedFrom) {
						entry.setBorrowed(true);
						entry.setBorrowedFrom(txtBorrowedFrom.getText());
						Database.updateBooklistEntry(bid, "ausgeliehen", "von");
						Database.updateBooklistEntry(bid, "name", txtBorrowedFrom.getText());
					}
				}
				Mainframe.logger.info("Buch geändert: " + entry.getAuthor() + "-" + entry.getTitle());
				dispose();
			} else {
				Mainframe.logger.info("Buch ändern: Bereits vorhanden!");
				txtTitle.setText("Buch bereits vorhanden!");
				txtTitle.setBackground(new Color(255, 105, 105));
				btnAdd.setEnabled(false);
			}
		} else {
			if (txtAuthor.getText().isEmpty()) {
				Mainframe.logger.info("Book edit: Autor nicht gesetzt!");
				txtAuthor.setBackground(new Color(255, 105, 105));
			}
			if (txtTitle.getText().isEmpty()) {
				Mainframe.logger.info("Book edit: Titel nicht gesetzt!");
				txtTitle.setBackground(new Color(255, 105, 105));
			}
		}
		BookListModel.checkAuthors();
		Mainframe.updateModel();
		Mainframe.logger.trace("Book edit: saved");
	}

	/**
	 * Check New Autor & Titel if there already exists the same
	 * 
	 * @param newAuthor - full author name
	 * @param newTitle  - book title
	 * @param index     - index of the Book to update
	 * 
	 * @return "false" if already exists else "true"
	 */
	public boolean checkInput(String newAuthor, String newTitle, int index) {
		for (int i = 0; i < Mainframe.entries.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.entries.getElementAt(i);
			if (eintrag.getAuthor().equals(newAuthor) && eintrag.getTitle().equals(newTitle)) {
				if (i != index) {
					Mainframe.logger.info("Book edit: Autor & Titel bereits vorhanden");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * shows the image from an Booklist Entry
	 * 
	 * @param entry - Booklist Entry
	 * 
	 * @return ImageIcon of saved Bookcover
	 */
	public ImageIcon showImg(Book_Booklist entry) {
		Image img = null;
		try {
			img = entry.getPic();
		} catch (Exception e) {
			Mainframe.logger.error(e.getMessage());
		}
		if (img != null) {
			return new ImageIcon(img);
		} else {
			return null;
		}

	}

	/**
	 * sets the correct Rating Icon according to current Rating
	 * 
	 */
	private void setRatingIcon() {
		if (entry.getRating() == 1) {
			lblStars.setIcon(zeroHalfStar);
		} else if (entry.getRating() == 2) {
			lblStars.setIcon(oneStar);
		} else if (entry.getRating() == 3) {
			lblStars.setIcon(oneHalfStar);
		} else if (entry.getRating() == 4) {
			lblStars.setIcon(twoStar);
		} else if (entry.getRating() == 5) {
			lblStars.setIcon(twoHalfStar);
		} else if (entry.getRating() == 6) {
			lblStars.setIcon(threeStar);
		} else if (entry.getRating() == 7) {
			lblStars.setIcon(threeHalfStar);
		} else if (entry.getRating() == 8) {
			lblStars.setIcon(fourStar);
		} else if (entry.getRating() == 9) {
			lblStars.setIcon(fourHalfStar);
		} else if (entry.getRating() == 10) {
			lblStars.setIcon(fifeStar);
		} else {
			lblStars.setIcon(zeroStar);
		}
	}

	/**
	 * sets the definied Rating of Booklist entry and shows Acknowldge Icon
	 * 
	 * @param segment - rating to set according to mouse position
	 * 
	 */
	private void setRating(int segment) {

		Mainframe.logger.trace("Rating set: " + segment);
		entry.setRating(segment, true);

		Mainframe.executor.submit(() -> {
			try {
				ack = true;
				lblAckRating.setVisible(true);
				Thread.sleep(2000);
				lblAckRating.setVisible(false);
				panelEastRating.repaint();
				ack = false;
			} catch (InterruptedException e1) {
				Mainframe.logger.error(e1.getMessage());
			}
		});
	}

}
