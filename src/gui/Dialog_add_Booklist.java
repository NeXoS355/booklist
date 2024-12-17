package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import application.Book_Booklist;
import application.HandleConfig;
import application.GetBookInfosFromWeb;

import static gui.Mainframe.allEntries;
import static gui.Mainframe.showNotification;

/**
 * Dialog to add new Book to Booklist Table and DB
 */
public class Dialog_add_Booklist extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
	private final CustomTextField txtAuthor;
	private final CustomTextField txtTitle;
	private final JCheckBox checkFrom;
	private final CustomTextField txtBorrowedFrom;
	private final JCheckBox checkTo;
	private final CustomTextField txtBorrowedTo;
	private final CustomTextField txtNote;
	private final CustomTextField txtSerie;
	private final CustomTextField txtSeriesVol;
	private final JCheckBox checkEbook;
	private final JButton btn_add;

	/**
	 * Dialog Add Constructor
	 * 
	 * @param owner     - set the owner of this Frame
	 */
	public Dialog_add_Booklist(Frame owner) {
		Mainframe.logger.info("Book add: start creating Frame");
		this.setTitle(Localization.get("t.addBook"));
		this.setSize(new Dimension(500, 420));
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
		JLabel lbl_author = new JLabel(Localization.get("label.author")+ ":");
		lbl_author.setFont(Mainframe.defaultFont);
		lbl_author.setSize(new Dimension(width, height));

		/*
		 * Create Components for Panel Center
		 */
		txtAuthor = new CustomTextField();
		txtAuthor.setText(Mainframe.getTreeSelection());
		txtAuthor.setPreferredSize(new Dimension(50, height));
		txtAuthor.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtAuthor.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtAuthor.getText().substring(0, typed);

					if (!txtAuthor.getText().isEmpty()) {
						String[] autoren = autoCompletion(typedString, "author");
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
				} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (txtAuthor.getText().length() > 50) {
					txtAuthor.setEditable(false);
					txtAuthor.setText(Localization.get("text.longError"));
				}
			}

		});

		JLabel lbl_title = new JLabel(Localization.get("label.title") + ":");
		lbl_title.setFont(Mainframe.defaultFont);
		lbl_title.setPreferredSize(new Dimension(width, height));

		txtTitle = new CustomTextField();
		txtTitle.setPreferredSize(new Dimension(50, height));
		txtTitle.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				} else if (!e.isActionKey()) {
					if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
						txtTitle.setText("");
						btn_add.setEnabled(true);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtTitle.getText().length() > 50) {
					txtTitle.setEditable(false);
					txtTitle.setText(Localization.get("text.longError"));
				}
			}
		});

		JLabel lbl_merk = new JLabel(Localization.get("label.note") + ":");
		lbl_merk.setFont(Mainframe.defaultFont);
		lbl_merk.setPreferredSize(new Dimension(width, height));

		txtNote = new CustomTextField();
		txtNote.setPreferredSize(new Dimension(50, height));
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

		JLabel lbl_serie = new JLabel(Localization.get("label.series")+ " | " + Localization.get("label.vol"));
		lbl_serie.setFont(Mainframe.defaultFont);
		lbl_serie.setPreferredSize(new Dimension(width, height));

		txtSerie = new CustomTextField();
		txtSerie.setPreferredSize(new Dimension(50, height));
		txtSerie.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				int typed = txtSerie.getCaretPosition();

				if (e.getKeyCode() >= 65 && e.getKeyCode() <= 105) {

					String typedString = txtSerie.getText().substring(0, typed);

					if (!txtSerie.getText().isEmpty()) {
						String[] serien = autoCompletion(typedString, "series");
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

		txtSeriesVol = new CustomTextField();
		txtSeriesVol.setPreferredSize(new Dimension(50, height));
		txtSeriesVol.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					addBook();
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dispose();
				if (txtSeriesVol.getText().length() > 2) {
					txtSeriesVol.setEditable(false);
					txtSeriesVol.setText("");
				} else
					txtSeriesVol.setBackground(UIManager.getColor("TextField.background"));
			}
		});

		JLabel lbl_ebook = new JLabel("E-Book:");
		lbl_ebook.setFont(Mainframe.defaultFont);
		lbl_ebook.setPreferredSize(new Dimension(width, height));

		checkEbook = new JCheckBox();
		checkEbook.setFont(Mainframe.defaultFont);
		checkEbook.setSelected(true);

		/*
		 * Set Center Layout
		 */
		GridBagConstraints c = new GridBagConstraints();
		panel_center.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
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
		checkFrom = new JCheckBox(Localization.get("label.borrowed_from"));
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

		checkTo = new JCheckBox(Localization.get("label.borrowed_to"));
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

		txtBorrowedFrom = new CustomTextField();
		txtBorrowedFrom.setVisible(false);
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

		txtBorrowedTo = new CustomTextField();
		txtBorrowedTo.setVisible(false);
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

		btn_add = ButtonsFactory.createButton(Localization.get("label.save"));
		btn_add.setFont(Mainframe.defaultFont);
		btn_add.addActionListener(e -> addBook());

		JButton btn_abort = ButtonsFactory.createButton(Localization.get("label.abort"));
		btn_abort.setFont(Mainframe.defaultFont);
		btn_abort.addActionListener(arg0 -> dispose());

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

		this.add(panel_center, BorderLayout.CENTER);
		this.add(panel_south, BorderLayout.SOUTH);

		this.setVisible(true);
		this.setModal(true);
		this.setResizable(false);

		Mainframe.logger.info("Book add: Frame successfully created");

		if (!(Mainframe.getTreeSelection()).isEmpty()) {
			txtTitle.requestFocus();
		}

	}

	/**
	 * adds the Book to Booklist
	 * 
	 */
	public void addBook() {
		Mainframe.logger.info("Book add: start saving");
		if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
			String autor = txtAuthor.getText();
			String titel = txtTitle.getText();
			String bemerkung = txtNote.getText();
			String serie = txtSerie.getText();
			String seriePart = txtSeriesVol.getText();
			boolean ebook = checkEbook.isSelected();
			Timestamp datum = new Timestamp(System.currentTimeMillis());
			if (checkInput(autor, titel)) {
				Book_Booklist book;
				if (checkTo.isSelected()) {
					book = new Book_Booklist(autor, titel, true, txtBorrowedTo.getText(), "", bemerkung, serie,
							seriePart, ebook, 0, null, null, null, datum, true);
					allEntries.add(book);
					showNotification(MessageFormat.format(Localization.get("book.added"),autor,titel));
				} else if (checkFrom.isSelected()) {
					book = new Book_Booklist(autor, titel, true, "", txtBorrowedFrom.getText(), bemerkung, serie,
							seriePart, ebook, 0, null, null, null, datum, true);
					allEntries.add(book);
					showNotification(MessageFormat.format(Localization.get("book.added"),autor,titel));
				} else {
					book = new Book_Booklist(autor, titel, bemerkung, serie, seriePart, ebook, 0, null, null, null,
							datum, true);
					allEntries.add(book);
					showNotification(MessageFormat.format(Localization.get("book.added"),autor,titel));
				}
				if (HandleConfig.autoDownload == 1) {
					Mainframe.executor.submit(() -> {
						Book_Booklist downloadBook = allEntries.getElementAt(allEntries.getIndexOf(autor, titel));
						GetBookInfosFromWeb.getBookInfoFromGoogleApiWebRequest(downloadBook, 2, false);
					});
				}
				allEntries.checkAuthors();
				Mainframe.setLastSearch(txtAuthor.getText());
				if (Mainframe.getTreeSelection().isEmpty()) {
					Mainframe.search(txtAuthor.getText());
				} else {
					Mainframe.search(Mainframe.getTreeSelection());
				}
				dispose();

			} else {
				txtTitle.setText(Localization.get("text.duplicateError"));
				btn_add.setEnabled(false);
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
		if (field.equals("author")) {
			int j = 0;
			int anz_autoren = allEntries.authors.size();
			String[] result = new String[anz_autoren];
			for (int i = 0; i < anz_autoren; i++) {
				if (allEntries.authors.get(i).startsWith(search)) {
					result[j] = allEntries.authors.get(i);
					j++;
				}
			}
			returnArray = new String[j];
			for (int i = 0; i < j; i++) {
				if (result[i] != null) {
					returnArray[i] = result[i];
				}

			}
		} else if (field.equals("series")) {
			int j = 0;
			String[] serien = allEntries.getSeriesFromAuthor(txtAuthor.getText());
			String[] result = new String[serien.length];
            for (String s : serien) {
                if (s.startsWith(search)) {
                    result[j] = s;
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
	 * checks if author with the same title already exists
	 * 
	 * @param author - name of Author
	 * @param title  - Book title
	 * 
	 * @return "false" if already exists else "true"
	 * 
	 */
	public boolean checkInput(String author, String title) {
		for (int i = 0; i < allEntries.getSize(); i++) {
			Book_Booklist eintrag = allEntries.getElementAt(i);
			if (eintrag.getAuthor().equals(author) && eintrag.getTitle().equals(title)) {
				return false;
			}
		}
		return true;
	}

}
