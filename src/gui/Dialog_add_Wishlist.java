package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import application.Book_Wishlist;
import application.WishlistListModel;

public class Dialog_add_Wishlist extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txt_author;
	private JTextField txt_title;
	private JTextField txt_merk;
	private JTextField txt_serie;
	private JTextField txt_seriePart;
	private Font standardFont = new Font("standard", Font.BOLD, 14);
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_add_Wishlist(WishlistListModel einträge) {
		this.setTitle("Buch hinzufügen");
		this.setSize(new Dimension(500, 365));
		this.setLocation(200, 200);
		this.setAlwaysOnTop(true);

		URL iconURL = getClass().getResource("/resources/Liste.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 10, 10));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridBagLayout());
		GridBagConstraints center_c = new GridBagConstraints();
		center_c.ipady = 20;
		int padding_c = 15;

		JPanel panel_south = new JPanel();
		panel_south.setLayout(new GridLayout(1, 2, 10, 10));

		int höhe = 60;
		int breite = 100;

		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(standardFont);
		lbl_author.setSize(new Dimension(breite, höhe));
		panel_west.add(lbl_author);

		txt_author = new JTextField();
		txt_author.setFont(standardFont);
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
		center_c.fill = GridBagConstraints.HORIZONTAL;
		center_c.gridx = 0;
		center_c.gridy = 0;
		center_c.weightx = 0.5;
		center_c.gridwidth = 4;
		center_c.insets = new Insets(0, 0, padding_c, 0);
		panel_center.add(txt_author, center_c);

		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(standardFont);
		lbl_title.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_title);

		txt_title = new JTextField();
		txt_title.setFont(standardFont);
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
		center_c.gridx = 0;
		center_c.gridy = 1;
		center_c.weightx = 0.5;
		center_c.gridwidth = 4;
		center_c.insets = new Insets(padding_c, 0, padding_c, 0);
		panel_center.add(txt_title, center_c);

		JLabel lbl_merk = new JLabel("Bemerkung:");
		lbl_merk.setFont(standardFont);
		lbl_merk.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_merk);

		txt_merk = new JTextField();
		txt_merk.setFont(standardFont);
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
		center_c.gridx = 0;
		center_c.gridy = 2;
		center_c.weightx = 0.5;
		center_c.gridwidth = 4;
		center_c.insets = new Insets(padding_c, 0, padding_c, 0);
		panel_center.add(txt_merk, center_c);

		JLabel lbl_serie = new JLabel("Serie:");
		lbl_serie.setFont(standardFont);
		lbl_serie.setPreferredSize(new Dimension(breite, höhe));
		panel_west.add(lbl_serie);

		txt_serie = new JTextField();
		txt_serie.setFont(standardFont);
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
		center_c.gridx = 0;
		center_c.gridy = 3;
		center_c.weightx = 4;
		center_c.gridwidth = 2;
		center_c.insets = new Insets(padding_c, 0, 0, 0);
		panel_center.add(txt_serie, center_c);

		txt_seriePart = new JTextField();
		txt_seriePart.setFont(standardFont);
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
		center_c.gridx = 2;
		center_c.gridy = 3;
		center_c.weightx = 0.5;
		center_c.gridwidth = 1;
		panel_center.add(txt_seriePart, center_c);

		JButton btn_add = new JButton("hinzufügen");
		btn_add.setFont(standardFont);
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addBuch();
			}
		});
		panel_south.add(btn_add);

		JButton btn_abort = new JButton("abbrechen");
		btn_abort.setFont(standardFont);
		btn_abort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		panel_south.add(btn_abort);

		this.add(panel_west, BorderLayout.WEST);
		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_south, BorderLayout.SOUTH);
		this.add(new JLabel(""), BorderLayout.NORTH); // oberer Abstand vom JFrame

		this.setVisible(true);
		this.setModal(true);
		this.setResizable(false);

	}

	public void addBuch() {
		try {
			if (!txt_author.getText().isEmpty() && !txt_title.getText().isEmpty()) {
				String autor = txt_author.getText();
				String titel = txt_title.getText();
				String bemerkung = txt_merk.getText();
				String serie = txt_serie.getText();
				String seriePart = txt_seriePart.getText();
				Timestamp datum = new Timestamp(System.currentTimeMillis());
				if (!Duplicant(autor, titel)) {
					wishlist.Wishlisteinträge.add(new Book_Wishlist(autor, titel, bemerkung, serie, seriePart, datum, true));
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
		} catch (SQLException ex) {
			ex.printStackTrace();
			txt_title.setForeground(Color.white);
			txt_title.setBackground(new Color(255, 105, 105));
			if (ex.getSQLState() == "23505") {
				txt_title.setText("Buch bereits vorhanden!");
			}
		}
		wishlist.updateModel();
	}

	public boolean Duplicant(String autor, String titel) {
		for (int i = 0; i < wishlist.Wishlisteinträge.getSize(); i++) {
			Book_Wishlist eintrag = wishlist.Wishlisteinträge.getElementAt(i);
			if (eintrag.getAutor().equals(autor) && eintrag.getTitel().equals(titel)) {
				return true;
			}
		}
		return false;
	}

}
