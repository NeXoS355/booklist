package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
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
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import application.Book_Booklist;
import application.Book_Wishlist;
import application.WishlistListModel;
import data.Database;

public class Dialog_edit_Wishlist extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RoundJTextField txt_author;
	private RoundJTextField txt_title;
	private RoundJTextField txt_merk;
	private RoundJTextField txt_serie;
	private RoundJTextField txt_seriePart;
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_edit_Wishlist(WishlistListModel einträge, int index) {
		Mainframe.logger.trace("Wishlist Book edit: start creating Frame");
		this.setTitle("Buch bearbeiten");
		this.setSize(new Dimension(500, 300));
		this.setLocation(200, 200);
		this.setAlwaysOnTop(true);

		Book_Wishlist eintrag = einträge.getElementAt(index);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_north = new JPanel();
		panel_north.setLayout(new GridLayout(1, 1));
		panel_north.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 10, 20));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridBagLayout());

		JPanel panel_east_border = new JPanel();
		panel_east_border.setLayout(new BorderLayout(10, 10));

		JPanel panel_east_grid = new JPanel();
		panel_east_grid.setLayout(new GridLayout(4, 1, 10, 20));
		panel_east_border.add(panel_east_grid, BorderLayout.WEST);

		JPanel panel_south = new JPanel();
		panel_south.setLayout(new GridLayout(1, 2, 10, 10));
		panel_south.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		int höhe = 60;
		int breite = 100;

		JLabel lbl_datum = new JLabel("Datum: " + new SimpleDateFormat("dd.MM.yyyy").format(eintrag.getDate()));
		panel_north.add(lbl_datum);

		// Empty Panel top Gap
		JLabel lbl_empty1 = new JLabel("");
		lbl_empty1.setFont(Mainframe.defaultFont);
		lbl_empty1.setPreferredSize(new Dimension(breite, 10));
		panel_north.add(lbl_empty1);
		// Ende topGap

		/*
		 * create and add components to Panel Center
		 */
		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(Mainframe.defaultFont);
		lbl_author.setPreferredSize(new Dimension(breite, höhe));
		txt_author = new RoundJTextField(eintrag.getAuthor());
		txt_author.setFont(Mainframe.defaultFont);
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
		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(Mainframe.defaultFont);
		lbl_title.setPreferredSize(new Dimension(breite, höhe));
		txt_title = new RoundJTextField(eintrag.getTitle());
		txt_title.setFont(Mainframe.defaultFont);
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
		lbl_merk.setFont(Mainframe.defaultFont);
		lbl_merk.setPreferredSize(new Dimension(breite, höhe));

		txt_merk = new RoundJTextField(eintrag.getNote());
		txt_merk.setFont(Mainframe.defaultFont);
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
		lbl_serie.setFont(Mainframe.defaultFont);
		lbl_serie.setPreferredSize(new Dimension(breite, höhe));

		txt_serie = new RoundJTextField(eintrag.getSeries());
		txt_serie.setFont(Mainframe.defaultFont);
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

		txt_seriePart = new RoundJTextField(eintrag.getSeriesVol());
		txt_seriePart.setFont(Mainframe.defaultFont);
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
		c.gridwidth = 2;
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
		c.gridwidth = 2;
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
		c.gridwidth = 2;
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
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txt_serie, c);
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 0);
		panel_center.add(txt_seriePart, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panel_center.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JButton btn_add = new JButton("Speichern");
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				speichern(eintrag);
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

		Mainframe.logger.trace("Wishlist Book edit: Frame created successfully");
		this.setVisible(true);
		this.setResizable(false);

	}

	public void speichern(Book_Wishlist eintrag) {
		if (!txt_author.getText().isEmpty() && !txt_title.getText().isEmpty()) {
			String oldAutor = eintrag.getAuthor();
			String oldTitel = eintrag.getTitle();
			String newAutor = txt_author.getText().trim();
			String newTitel = txt_title.getText().trim();
			String newBemerkung = txt_merk.getText().trim();
			String newSerie = txt_serie.getText().trim();
			String newSeriePart = txt_seriePart.getText();
			Timestamp datum = new Timestamp(System.currentTimeMillis());
			if (!Duplicant(newAutor, newTitel, wishlist.wishlistEntries.getIndexOf(newAutor, newTitel))) {
				Database.deleteFromWishlist(oldAutor, oldTitel);
				Database.addToWishlist(newAutor, newTitel, newBemerkung, newSerie, newSeriePart, datum.toString());
				eintrag.setAuthor(newAutor);
				eintrag.setTitle(newTitel);
				eintrag.setNote(newBemerkung);
				eintrag.setSeries(newSerie);
				eintrag.setSeriesVol(newSeriePart);
				eintrag.setDate(datum);
				dispose();
				Mainframe.logger.trace("Wishlist Book edit: Book saved successfully");
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
		wishlist.updateModel();
	}

	public boolean Duplicant(String autor, String titel, int index) {
		for (int i = 0; i < wishlist.wishlistEntries.getSize(); i++) {
			Book_Wishlist eintrag = wishlist.wishlistEntries.getElementAt(i);
			if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
				return true;
			}
		}
		for (int i = 0; i < Mainframe.entries.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.entries.getElementAt(i);
			if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
				return true;
			}
		}
		return false;
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

}
