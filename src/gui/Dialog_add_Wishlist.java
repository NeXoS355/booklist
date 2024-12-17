package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.net.URL;
import java.sql.Timestamp;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import application.Book_Booklist;
import application.Book_Wishlist;

public class Dialog_add_Wishlist extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private final CustomTextField txtAuthor;
	private final CustomTextField txtTitle;
	private final CustomTextField txtNote;
	private final CustomTextField txtSeries;
	private final CustomTextField txtSeriesVol;
	private final Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private final Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_add_Wishlist(Frame owner) {
		Mainframe.logger.info("Wishlist Book add: start creating Frame");
		this.setTitle(Localization.get("t.addBook"));
		this.setSize(new Dimension(500, 320));
		this.setLocationRelativeTo(owner);
		this.setAlwaysOnTop(true);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
        assert iconURL != null;
        ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());

		this.setLayout(new BorderLayout(10, 10));

		JPanel panel_center = new JPanel();
		panel_center.setLayout(new GridBagLayout());
		
		
		int height = 60;
		int width = 100;
		
		/*
		 * Create Components for Panel West
		 */
		JLabel lbl_author = new JLabel(Localization.get("label.author") + ":");
		lbl_author.setFont(Mainframe.defaultFont);
		lbl_author.setSize(new Dimension(width, height));
		
		/*
		 * Create Components for Panel Center
		 */
		txtAuthor = new CustomTextField();
		txtAuthor.setPreferredSize(new Dimension(50, height));
		txtAuthor.setBorder(standardBorder);
		txtAuthor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				} else if (!e.isActionKey()) {
					txtAuthor.setBackground(UIManager.getColor("TextField.background"));
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
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
		
		JLabel lbl_title = new JLabel(Localization.get("label.title") + ":");
		lbl_title.setFont(Mainframe.defaultFont);
		lbl_title.setPreferredSize(new Dimension(width, height));
		
		txtTitle = new CustomTextField();
		txtTitle.setPreferredSize(new Dimension(50, height));
		txtTitle.setBorder(standardBorder);
		txtTitle.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				} else if (!e.isActionKey()) {
					if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
						txtTitle.setText("");
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtTitle.getText().length() > 50) {
					txtTitle.setEditable(false);
					txtTitle.setText(Localization.get("text.longError"));
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
				if (txtTitle.getText().equals(Localization.get("text.longError"))) {
					txtTitle.setEditable(true);
					txtTitle.setBackground(UIManager.getColor("TextField.foreground"));
					txtTitle.setBackground(UIManager.getColor("TextField.background"));
					txtTitle.setText("");
				} else if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
					txtTitle.setBackground(UIManager.getColor("TextField.foreground"));
					txtTitle.setBackground(UIManager.getColor("TextField.background"));
					txtTitle.setText("");
				}
			}

		});

		JLabel lbl_merk = new JLabel(Localization.get("label.note") + ":");
		lbl_merk.setFont(Mainframe.defaultFont);
		lbl_merk.setPreferredSize(new Dimension(width, height));
		

		txtNote = new CustomTextField();
		txtNote.setPreferredSize(new Dimension(50, height));
		txtNote.setBorder(standardBorder);
		txtNote.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				}
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
		
		JLabel lbl_serie = new JLabel(Localization.get("label.series") + " | " + Localization.get("label.vol") + ":");
		lbl_serie.setFont(Mainframe.defaultFont);
		lbl_serie.setPreferredSize(new Dimension(width, height));
		

		txtSeries = new CustomTextField();
		txtSeries.setPreferredSize(new Dimension(50, height));
		txtSeries.setBorder(standardBorder);
		txtSeries.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
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
		
		txtSeriesVol = new CustomTextField();
		txtSeriesVol.setPreferredSize(new Dimension(50, height));
		txtSeriesVol.setBorder(standardBorder);
		txtSeriesVol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBuch();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtSeriesVol.getText().length() > 2) {
					txtSeriesVol.setBackground(new Color(255, 105, 105));
					txtSeriesVol.setText("");
				} else
					txtSeriesVol.setBackground(UIManager.getColor("TextField.background"));
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
		c.insets = new Insets(10, 0, 0, 0);
		panel_center.add(lbl_author, c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txtAuthor, c);
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		panel_center.add(lbl_title, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(txtTitle, c);
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
		panel_center.add(txtNote, c);
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
		panel_center.add(txtSeries, c);
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 0);
		panel_center.add(txtSeriesVol, c);

		JButton btn_add = ButtonsFactory.createButton(Localization.get("label.save"));
		btn_add.setFont(Mainframe.defaultFont);
		btn_add.addActionListener(e -> addBuch());

		JButton btn_abort = ButtonsFactory.createButton(Localization.get("label.abort"));
		btn_abort.setFont(Mainframe.defaultFont);
		btn_abort.addActionListener(arg0 -> dispose());
		
		/*
		 * add components to Panel South
		 */
		JPanel panel_south = new JPanel();
		panel_south.setLayout(new GridLayout(1, 2, 10, 10));
		panel_south.add(btn_add);
		panel_south.add(btn_abort);
		panel_south.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_south, BorderLayout.SOUTH);
//		this.add(new JLabel(""), BorderLayout.NORTH); // oberer Abstand vom JFrame

		Mainframe.logger.info("Wishlist Book add: Frame successfully created");
		this.setVisible(true);
		this.setModal(true);
		this.setResizable(false);

	}

	public void addBuch() {
        Mainframe.logger.info("Wishlist Book add: start saving");
        if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
            String autor = txtAuthor.getText();
            String titel = txtTitle.getText();
            String bemerkung = txtNote.getText();
            String serie = txtSeries.getText();
            String seriePart = txtSeriesVol.getText();
            Timestamp datum = new Timestamp(System.currentTimeMillis());
            if (!Duplicant(autor, titel)) {
                wishlist.wishlistEntries.add(new Book_Wishlist(autor, titel, bemerkung, serie, seriePart, datum, true));
                dispose();
            } else {
                txtTitle.setText(Localization.get("text.duplicateError"));
                txtTitle.setBackground(new Color(255, 105, 105));
            }
            Mainframe.logger.info("Book add: saved successfully");
        } else {
            if (txtAuthor.getText().isEmpty()) {
                txtAuthor.setBackground(new Color(255, 105, 105));
            }
            if (txtTitle.getText().isEmpty()) {
                txtTitle.setBackground(new Color(255, 105, 105));
            }
        }
        wishlist.updateModel();
	}

	public boolean Duplicant(String autor, String titel) {
		for (int i = 0; i < wishlist.wishlistEntries.getSize(); i++) {
			Book_Wishlist eintrag = wishlist.wishlistEntries.getElementAt(i);
			if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
				return true;
			}
		}
		for (int i = 0; i < Mainframe.allEntries.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.allEntries.getElementAt(i);
			if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
				return true;
			}
		}
		return false;
	}

}
