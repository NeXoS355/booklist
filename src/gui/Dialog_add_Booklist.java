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

/**
 *  Dialog to add new Book to Booklist Table and DB
 */
public class Dialog_add_Booklist extends JDialog {

	private static final long serialVersionUID = 1L;
	private RoundJTextField txtAuthor;
	private RoundJTextField txtTitle;
	private JCheckBox checkFrom;
	private RoundJTextField txtBorrowedFrom;
	private JCheckBox checkTo;
	private RoundJTextField txtBorrowedTo;
	private RoundJTextField txtNote;
	private RoundJTextField txtSerie;
	private RoundJTextField txtSeriesVol;
	private JCheckBox checkEbook;
	private Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

	public Dialog_add_Booklist(BookListModel eintr�ge, DefaultTreeModel treeModel, DefaultMutableTreeNode rootNode) {
		this.setTitle("Buch hinzuf�gen");
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

		int h�he = 60;
		int breite = 100;

		/*
		 * Create Components for Panel West
		 */
		JLabel lbl_author = new JLabel("Autor:");
		lbl_author.setFont(Mainframe.defaultFont);
		lbl_author.setSize(new Dimension(breite, h�he));

		JPanel panel_west = new JPanel();
		panel_west.setLayout(new GridLayout(4, 1, 10, 10));
		panel_west.add(lbl_author);

		/*
		 * Create Components for Panel Center
		 */
		txtAuthor = new RoundJTextField();
		txtAuthor.setFont(Mainframe.defaultFont);
		txtAuthor.setText(Mainframe.getTreeSelection());
		txtAuthor.setPreferredSize(new Dimension(50, h�he));
		txtAuthor.setBorder(standardBorder);
		txtAuthor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtAuthor.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtAuthor.getText().substring(0, typed);

					if (!txtAuthor.getText().equals("")) {
						String[] autoren = autoCompletion(typedString, "autor");
						for (int i = 0; i < autoren.length && autoren[i] != null; i++) {
							int autorenLength = autoren[i].length();
							String setText = autoren[i].substring(typed, autorenLength);
							txtAuthor.setText(typedString + setText);
							txtAuthor.setCaretPosition(typed);
							txtAuthor.setSelectionStart(typed);
							txtAuthor.setSelectionEnd(autoren[i].length());

						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txtAuthor.getCaretPosition();
					txtAuthor.setText(txtAuthor.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txtAuthor.getCaretPosition();
					txtAuthor.setText(txtAuthor.getText().substring(0, typed));
					System.out.println(txtAuthor.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				} else if (!e.isActionKey()) {
					txtAuthor.setBackground(Color.white);
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

		JLabel lbl_title = new JLabel("Titel:");
		lbl_title.setFont(Mainframe.defaultFont);
		lbl_title.setPreferredSize(new Dimension(breite, h�he));
		panel_west.add(lbl_title);

		txtTitle = new RoundJTextField();
		txtTitle.setFont(Mainframe.defaultFont);
		txtTitle.setPreferredSize(new Dimension(50, h�he));
		txtTitle.setBorder(standardBorder);
		txtTitle.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				} else if (!e.isActionKey()) {
					if (txtTitle.getText().equals("Buch bereits vorhanden!")) {
						txtTitle.setText("");
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
				} else if (txtTitle.getText().equals("Buch bereits vorhanden!")) {
					txtTitle.setForeground(Color.black);
					txtTitle.setBackground(Color.white);
					txtTitle.setText("");
				}
			}

		});

		JLabel lbl_merk = new JLabel("Bemerkung:");
		lbl_merk.setFont(Mainframe.defaultFont);
		lbl_merk.setPreferredSize(new Dimension(breite, h�he));
		panel_west.add(lbl_merk);

		txtNote = new RoundJTextField();
		txtNote.setFont(Mainframe.defaultFont);
		txtNote.setPreferredSize(new Dimension(50, h�he));
		txtNote.setBorder(standardBorder);
		txtNote.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
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

		JLabel lbl_serie = new JLabel("Serie | Band:");
		lbl_serie.setFont(Mainframe.defaultFont);
		lbl_serie.setPreferredSize(new Dimension(breite, h�he));
		panel_west.add(lbl_serie);

		txtSerie = new RoundJTextField();
		txtSerie.setFont(Mainframe.defaultFont);
		txtSerie.setPreferredSize(new Dimension(50, h�he));
		txtSerie.setBorder(standardBorder);
		txtSerie.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtSerie.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtSerie.getText().substring(0, typed);

					if (!txtSerie.getText().equals("")) {
						String[] serien = autoCompletion(typedString, "serie");
						for (int i = 0; i < serien.length && serien[i] != null; i++) {
							int autorenLength = serien[i].length();
							String setText = serien[i].substring(typed, autorenLength);
							txtSerie.setText(typedString + setText);
							txtSerie.setCaretPosition(typed);
							txtSerie.setSelectionStart(typed);
							txtSerie.setSelectionEnd(serien[i].length());
						}
					}
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
					typed = txtSerie.getCaretPosition();
					txtSerie.setText(txtSerie.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					typed = txtSerie.getCaretPosition();
					txtSerie.setText(txtSerie.getText().substring(0, typed));
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

		});
		txtSerie.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				txtSerie.setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				txtSerie.setBorder(activeBorder);

			}

		});

		txtSeriesVol = new RoundJTextField();
		txtSeriesVol.setFont(Mainframe.defaultFont);
		txtSeriesVol.setPreferredSize(new Dimension(50, h�he));
		txtSeriesVol.setBorder(standardBorder);
		txtSeriesVol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtSeriesVol.getText().length() > 2) {
					txtSeriesVol.setBackground(new Color(255, 105, 105));
					txtSeriesVol.setText("");
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

		JLabel lbl_ebook = new JLabel("E-Book:");
		lbl_ebook.setFont(Mainframe.defaultFont);
		lbl_ebook.setPreferredSize(new Dimension(breite, h�he));
		panel_west.add(lbl_ebook);

		checkEbook = new JCheckBox();
		checkEbook.setFont(Mainframe.defaultFont);
		checkEbook.setSelected(true);

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
		c.insets = new Insets(10, 0, 0, 0);
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
		panel_center.add(txtSerie, c);
		c.gridx = 2;
		c.gridy = 3;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 10, 0, 0);
		panel_center.add(txtSeriesVol, c);
		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.insets = new Insets(10, 0, 0, 0);
		panel_center.add(lbl_ebook, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel_center.add(checkEbook, c);

		/*
		 * create components for Panel South
		 */
		checkFrom = new JCheckBox("ausgeliehen von");
		checkFrom.setFont(Mainframe.defaultFont);
		checkFrom.setSelected(false);
		checkFrom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (checkFrom.isSelected()) {
					checkTo.setSelected(false);
					txtBorrowedTo.setVisible(false);
					txtBorrowedFrom.setVisible(true);
				} else {
					txtBorrowedFrom.setVisible(false);
				}
			}
		});

		checkTo = new JCheckBox("ausgeliehen an");
		checkTo.setFont(Mainframe.defaultFont);
		checkTo.setSelected(false);
		checkTo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (checkTo.isSelected()) {
					checkFrom.setSelected(false);
					txtBorrowedFrom.setVisible(false);
					txtBorrowedTo.setVisible(true);
				} else {
					txtBorrowedTo.setVisible(false);
				}
			}
		});

		txtBorrowedFrom = new RoundJTextField();
		txtBorrowedFrom.setFont(Mainframe.defaultFont);
		txtBorrowedFrom.setVisible(false);
		txtBorrowedFrom.setBorder(standardBorder);
		txtBorrowedFrom.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				}
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

		txtBorrowedTo = new RoundJTextField();
		txtBorrowedTo.setFont(Mainframe.defaultFont);
		txtBorrowedTo.setVisible(false);
		txtBorrowedTo.setBorder(standardBorder);
		txtBorrowedTo.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
					Mainframe.updateModel();
				}
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

		JButton btn_add = new JButton("hinzuf�gen");
		btn_add.setFont(Mainframe.defaultFont);
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addBook();
			}
		});

		JButton btn_abort = new JButton("abbrechen");
		btn_abort.setFont(Mainframe.defaultFont);
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
		panel_south.add(checkFrom);
		panel_south.add(checkTo);
		panel_south.add(txtBorrowedFrom);
		panel_south.add(txtBorrowedTo);
		panel_south.add(btn_add);
		panel_south.add(btn_abort);

		this.add(panel_west, BorderLayout.WEST);
		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_south, BorderLayout.SOUTH);

		this.setVisible(true);
		this.setModal(true);
		this.setResizable(false);

		if (!(Mainframe.getTreeSelection()).equals("")) {
			txtTitle.requestFocus();
		}

	}

	/** adds the Book to Booklist
	  * 
	*/
	public void addBook() {
		try {
			if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
				Book_Booklist book = null;
				String autor = txtAuthor.getText();
				String titel = txtTitle.getText();
				String bemerkung = txtNote.getText();
				String serie = txtSerie.getText();
				String seriePart = txtSeriesVol.getText();
				boolean ebook = checkEbook.isSelected();
				Timestamp datum = new Timestamp(System.currentTimeMillis());
				if (checkInput(autor, titel)) {
					if (checkTo.isSelected()) {
						book = new Book_Booklist(autor, titel, true, txtBorrowedTo.getText(), "", bemerkung, serie,
								seriePart, ebook, null, null, null, datum, true);
						Mainframe.entries.add(book);
					} else if (checkFrom.isSelected()) {
						book = new Book_Booklist(autor, titel, true, "", txtBorrowedFrom.getText(), bemerkung, serie,
								seriePart, ebook, null, null, null, datum, true);
						Mainframe.entries.add(book);
					} else {
						book = new Book_Booklist(autor, titel, bemerkung, serie, seriePart, ebook, null, null, null,
								datum, true);
						Mainframe.entries.add(book);
					}
					if (HandleConfig.autoDownload == 1)
						HandleWebInfo.DownloadWebPage(book, 2, false);

					dispose();
				} else {
					txtTitle.setText("Buch bereits vorhanden!");
					txtTitle.setBackground(new Color(255, 105, 105));
				}
			} else {
				if (txtAuthor.getText().isEmpty()) {
					txtAuthor.setBackground(new Color(255, 105, 105));
				}
				if (txtTitle.getText().isEmpty()) {
					txtTitle.setBackground(new Color(255, 105, 105));
				}
			}
			Mainframe.setLastSearch(txtAuthor.getText());
		} catch (SQLException ex) {
			ex.printStackTrace();
			txtTitle.setForeground(Color.white);
			txtTitle.setBackground(new Color(255, 105, 105));
			if (ex.getSQLState() == "23505") {
				txtTitle.setText("Buch bereits vorhanden!");
			}
		}
		Mainframe.updateModel();
		if (Mainframe.getTreeSelection() == "") {
			Mainframe.search(txtAuthor.getText());
		} else {
			Mainframe.search(Mainframe.getTreeSelection());
		}
		BookListModel.checkAuthors();
	}

	/** add the autoComplete feature to "autor" and "serie"
	 * 
	 * @param search - currently typed String
	 * @param field - sets variable based on which field is active
	 * 
	 * @return String array with matching authors or series
	 *  
	*/
	public String[] autoCompletion(String search, String field) {
		String[] returnArray = null;
		if (field.equals("autor")) {
			int j = 0;
			int anz_autoren = BookListModel.authors.size();
			String[] result = new String[anz_autoren];
			for (int i = 0; i < anz_autoren; i++) {
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
			String[] serien = BookListModel.getSeriesFromAuthor(txtAuthor.getText());
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

	/** checks if author with the same title already exists
	 * 
	 * @param author - name of Author
	 * @param title - Book title
	 * 
	 * @return "false" if already exists else "true"
	 *  
	*/
	public boolean checkInput(String author, String title) {
		for (int i = 0; i < Mainframe.entries.getSize(); i++) {
			Book_Booklist eintrag = Mainframe.entries.getElementAt(i);
			if (eintrag.getAuthor().equals(author) && eintrag.getTitle().equals(title)) {
				return false;
			}
		}
		return true;
	}

}
