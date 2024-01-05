package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import application.Book_Booklist;
import application.HandleConfig;
import application.HandleWebInfo;
import application.BookListModel;

public class Dialog_add_Booklist extends JDialog {

	/**
	 * 
	 */
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
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_add_Booklist(BookListModel einträge, DefaultTreeModel treeModel, DefaultMutableTreeNode rootNode) {
		this.setTitle("Buch hinzufügen");
		this.setSize(new Dimension(500, 400));
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 200);
		this.setAlwaysOnTop(true);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridBagLayout());

		int höhe = 60;
		int breite = 100;

		/*
		 * Create Components for Panel West
		 */
		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(Mainframe.schrift);
		lbl_author.setSize(new Dimension(breite, höhe));
		
		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 10, 10));
		panel_west.add(lbl_author);
		

		
		/*
		 * Create Components for Panel Center
		 */
		txt_author = new RoundJTextField();
		txt_author.setFont(Mainframe.schrift);
		txt_author.setText(Mainframe.getTreeSelection());
		txt_author.setPreferredSize(new Dimension(50, höhe));
		txt_author.setBorder(standardBorder);
		txt_author.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				} else if (!e.isActionKey()) {
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

		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(Mainframe.schrift);
		lbl_title.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_title);

		txt_title = new RoundJTextField();
		txt_title.setFont(Mainframe.schrift);
		txt_title.setPreferredSize(new Dimension(50, höhe));
		txt_title.setBorder(standardBorder);
		txt_title.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
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
				} else if (txt_title.getText().equals("Buch bereits vorhanden!")) {
					txt_title.setForeground(Color.black);
					txt_title.setBackground(Color.white);
					txt_title.setText("");
				}
			}

		});

		JLabel lbl_merk = new JLabel("Bemerkung:");
		lbl_merk.setFont(Mainframe.schrift);
		lbl_merk.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_merk);

		txt_merk = new RoundJTextField();
		txt_merk.setFont(Mainframe.schrift);
		txt_merk.setPreferredSize(new Dimension(50, höhe));
		txt_merk.setBorder(standardBorder);
		txt_merk.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				}
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
		panel_west.add(lbl_serie);

		txt_serie = new RoundJTextField();
		txt_serie.setFont(Mainframe.schrift);
		txt_serie.setPreferredSize(new Dimension(50, höhe));
		txt_serie.setBorder(standardBorder);
		txt_serie.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
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

		txt_seriePart = new RoundJTextField();
		txt_seriePart.setFont(Mainframe.schrift);
		txt_seriePart.setPreferredSize(new Dimension(50, höhe));
		txt_seriePart.setBorder(standardBorder);
		txt_seriePart.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txt_seriePart.getText().length() > 2) {
					txt_seriePart.setBackground(new Color(255, 105, 105));
					txt_seriePart.setText("");
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
		panel_west.add(lbl_ebook);
		
		check_ebook = new JCheckBox();
		check_ebook.setFont(Mainframe.schrift);
		check_ebook.setSelected(true);
		
		/*
		 * Set Center Layout
		 */
		GridBagConstraints c = new GridBagConstraints();		
		panel_center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.ipady = 15;
		panel_center.add(lbl_author,c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_author,c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		c.insets = new Insets(10,0,0,0);
		panel_center.add(lbl_title,c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_title,c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panel_center.add(lbl_merk,c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_merk,c);
		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panel_center.add(lbl_serie,c);
		c.gridx = 1;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_serie,c);		
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10,10,0,0);
		panel_center.add(txt_seriePart, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.insets = new Insets(10,0,0,0);
		panel_center.add(lbl_ebook, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(check_ebook, c);

		
		/*
		 * create components for Panel South
		 */
		check_von = new JCheckBox("ausgeliehen von");
		check_von.setFont(Mainframe.schrift);
		check_von.setSelected(false);
		check_von.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (check_von.isSelected()) {
					check_an.setSelected(false);
					txt_leihAn.setVisible(false);
					txt_leihVon.setVisible(true);
				} else {
					txt_leihVon.setVisible(false);
				}
			}
		});


		check_an = new JCheckBox("ausgeliehen an");
		check_an.setFont(Mainframe.schrift);
		check_an.setSelected(false);
		check_an.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (check_an.isSelected()) {
					check_von.setSelected(false);
					txt_leihVon.setVisible(false);
					txt_leihAn.setVisible(true);
				} else {
					txt_leihAn.setVisible(false);
				}
			}
		});


		txt_leihVon = new RoundJTextField();
		txt_leihVon.setFont(Mainframe.schrift);
		txt_leihVon.setVisible(false);
		txt_leihVon.setBorder(standardBorder);
		txt_leihVon.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				}
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


		txt_leihAn = new RoundJTextField();
		txt_leihAn.setFont(Mainframe.schrift);
		txt_leihAn.setVisible(false);
		txt_leihAn.setBorder(standardBorder);
		txt_leihAn.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
					Mainframe.updateModel();
				}
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


		JButton btn_add = new JButton("hinzufügen");
		btn_add.setFont(Mainframe.schrift);
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addBuch();
			}
		});


		JButton btn_abort = new JButton("abbrechen");
		btn_abort.setFont(Mainframe.schrift);
		btn_abort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		/*
		 * add components to Panel South
		 */
		JPanel panel_south = new JPanel();
		panel_south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		panel_south.setLayout(new GridLayout(3, 2, 10, 10));
		panel_south.add(check_von);
		panel_south.add(check_an);
		panel_south.add(txt_leihVon);
		panel_south.add(txt_leihAn);
		panel_south.add(btn_add);
		panel_south.add(btn_abort);

		this.add(panel_west, BorderLayout.WEST);
		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_south, BorderLayout.SOUTH);

		this.setVisible(true);
		this.setModal(true);
		this.setResizable(false);

		if (!(Mainframe.getTreeSelection()).equals("")) {
			txt_title.requestFocus();
		}

	}

	public void addBuch() {
		try {
			if (!txt_author.getText().isEmpty() && !txt_title.getText().isEmpty()) {
				Book_Booklist book = null;
				String autor = txt_author.getText();
				String titel = txt_title.getText();
				String bemerkung = txt_merk.getText();
				String serie = txt_serie.getText();
				String seriePart = txt_seriePart.getText();
				boolean ebook = check_ebook.isSelected();
				Timestamp datum = new Timestamp(System.currentTimeMillis());
				if (checkInput(autor, titel)) {
					if (check_an.isSelected()) {
						book = new Book_Booklist(autor, titel, true, txt_leihAn.getText(), "", bemerkung, serie,
								seriePart,ebook, null, null, null, datum, true);
						Mainframe.einträge.add(book);
					} else if (check_von.isSelected()) {
						book = new Book_Booklist(autor, titel, true, "", txt_leihVon.getText(), bemerkung, serie,
								seriePart,ebook, null, null, null, datum, true);
						Mainframe.einträge.add(book);
					} else {
						book = new Book_Booklist(autor, titel, bemerkung, serie, seriePart,ebook, null, null, null, datum,
								true);
						Mainframe.einträge.add(book);
					}
					if (HandleConfig.autoDownload == 1)
						HandleWebInfo.DownloadWebPage(book,2,false);

					dispose();
				} else {
					txt_title.setText("Buch bereits vorhanden!");
					txt_title.setBackground(new Color(255, 105, 105));
				}
			} else {
				if (txt_author.getText().isEmpty()) {
					txt_author.setBackground(new Color(255, 105, 105));
				}
				if (txt_title.getText().isEmpty()) {
					txt_title.setBackground(new Color(255, 105, 105));
				}
			}
			Mainframe.setLastSearch(txt_author.getText());
		} catch (SQLException ex) {
			ex.printStackTrace();
			txt_title.setForeground(Color.white);
			txt_title.setBackground(new Color(255, 105, 105));
			if (ex.getSQLState() == "23505") {
				txt_title.setText("Buch bereits vorhanden!");
			}
		}
		Mainframe.updateModel();
		if (Mainframe.getTreeSelection() == "") {
			Mainframe.search(txt_author.getText());
		} else {
			Mainframe.search(Mainframe.getTreeSelection());
		}
		BookListModel.autorenPrüfen();
	}

	public boolean checkInput(String autor, String titel) {
		for (int i = 0; i < Mainframe.einträge.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.einträge.getElementAt(i);
			if (eintrag.getAutor().equals(autor) && eintrag.getTitel().equals(titel)) {
				return false;
			}
		}
		return true;
	}

}
