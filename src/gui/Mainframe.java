package gui;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import application.BookListModel;
import application.Book_Booklist;
import application.HandleConfig;
import application.SimpleTableModel;
import data.Database;

public class Mainframe extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Logger logger = null;
	public static ExecutorService executor = Executors.newFixedThreadPool(10);

	public static Font defaultFont = new Font("Roboto", Font.PLAIN, 16);
	public static Font descFont = new Font("Roboto", Font.PLAIN, 16);
	private static JTable table = new JTable();
	public static BookListModel entries;
	private static DefaultListModel<Book_Booklist> filter;
	private static SimpleTableModel tableDisplay;
	private static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode");
	private static DefaultMutableTreeNode autorNode = new DefaultMutableTreeNode("AutorNode");
	private static DefaultMutableTreeNode serieNode = new DefaultMutableTreeNode("SerieNode");
	private static DefaultTreeModel treeModel;
	private static JTree tree = new JTree(treeModel);
	private JTextField txt_search;
	private static Mainframe instance;
	private static String treeSelection;
	private static String lastSearch = "";
	private String version = "Ver. 2.6.2  (01.2024)  ";

	private Mainframe() throws HeadlessException {
		super("Bücherliste");

		logger = LogManager.getLogger(getClass());
		logger.trace("start creating Frame & readConfig");
		HandleConfig.readConfig();
		if (HandleConfig.debug == 0) {
			Configurator.setLevel(logger, Level.WARN);
		} else if (HandleConfig.debug == 1) {
			Configurator.setLevel(logger, Level.INFO);
		} else if (HandleConfig.debug == 2) {
			Configurator.setLevel(logger, Level.TRACE);
		}

		this.setLayout(new BorderLayout(10, 10));
		this.setLocation(100, 100);
		this.setSize(1300, 800);
		this.setResizable(true);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error(e.getMessage());
		}

		logger.trace("Finished create Frame & readConfig. Start creating Lists and readDB");
		entries = new BookListModel();
		filter = new DefaultListModel<Book_Booklist>();
		tableDisplay = new SimpleTableModel(entries);

		logger.trace("Finished creating List & DB. Start creating GUI Components");

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));

		txt_search = new JTextField();
		txt_search.setToolTipText("Suchtext");
		txt_search.setText("Suche ... (" + entries.getSize() + ")");
		txt_search.setForeground(Color.gray);
		txt_search.setFont(defaultFont);
		txt_search.setMargin(new Insets(0, 10, 0, 0));
		txt_search.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				txt_search.setForeground(Color.black);
				if (txt_search.getText().contains("Suche ..."))
					txt_search.setText("");
				table.clearSelection();
			}

		});

		txt_search.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					search(txt_search.getText());
					txt_search.setForeground(Color.gray);
					tree.clearSelection();
					setLastSearch(txt_search.getText());
					if (tableDisplay.getRowCount() == 0) {
						updateModel();
						JOptionPane.showMessageDialog(getParent(), "Keine Übereinstimmung gefunden");
					}
				}
			}
		});
		panel.add(txt_search, BorderLayout.CENTER);

		JButton btn_add = new JButton();
		btn_add.setFocusPainted(false);
		btn_add.setText("+");
		btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_add_Booklist(entries, treeModel, rootNode);
				txt_search.setText("Suche ... (" + entries.getSize() + ")");
			}
		});
		panel.add(btn_add, BorderLayout.WEST);

		JButton btn_search = new JButton("suchen");
		btn_search.setFocusPainted(false);
		btn_search.setFont(btn_search.getFont().deriveFont(Font.BOLD, 13));
		BufferedImage image = null;
		try {
			image = ImageIO.read(getClass().getResource("/resources/lupe.png"));
			btn_search.setIcon(new ImageIcon(image));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		btn_search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				search(txt_search.getText());
				txt_search.setForeground(Color.gray);
				tree.clearSelection();
				setLastSearch(txt_search.getText());
				if (entries.getSize() == 0) {
					updateModel();
					JOptionPane.showMessageDialog(getParent(), "Keine Übereinstimmung gefunden");
				}
			}
		});
		panel.add(btn_search, BorderLayout.EAST);

		JPanel pnlMenü = new JPanel();
		pnlMenü.setLayout(new BorderLayout());
		panel.add(pnlMenü, BorderLayout.NORTH);

		JMenuBar menue = new JMenuBar();
		JMenu datei = new JMenu("Datei");
		JMenu extras = new JMenu("Extras");
		JMenu hilfe = new JMenu("Hilfe");

		JMenuItem backup = new JMenuItem("DB Backup");
		backup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Date dt = new Date();
					Long LongTime = dt.getTime();
					String StrTime = Long.toString(LongTime).substring(0, LongTime.toString().length() - 3);
					copyFilesInDirectory(new File("BooklistDB"), new File("Sicherung/" + StrTime + "/BooklistDB"));
					copyFileToDirectory(new File("derby.log"), new File("Sicherung/" + StrTime));
					copyFileToDirectory(new File("config.conf"), new File("Sicherung/" + StrTime));
					copyFileToDirectory(new File("Bücherliste.jar"), new File("Sicherung/" + StrTime));
					JOptionPane.showMessageDialog(getParent(), "Backup erfolgreich.");

				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Backup fehlgeschlagen oder nicht vollständig.");
				}
			}
		});
		JMenuItem close = new JMenuItem("Schließen");
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(1);
			}
		});
		JMenuItem wishlist = new JMenuItem("Wunschliste");
		wishlist.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new wishlist();
			}
		});
		JMenuItem dbVersion = new JMenuItem("DB Version");
		dbVersion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String text = Database.readCurrentLayoutVersion() + "\nApache Derby 10.16.1.1 (May 19, 2022)";
				JOptionPane.showMessageDialog(null, text);
			}
		});
		JMenuItem ExcelExport = new JMenuItem("CSV Export");
		ExcelExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int check = Database.CSVExport();
				if (check == 1) {
					JOptionPane.showMessageDialog(null, "Liste erfolgreich exportiert!");
				} else {
					JOptionPane.showMessageDialog(null, "Datei konnte nicht geschrieben werden!");
				}
			}
		});
		JMenuItem settings = new JMenuItem("Einstellungen");
		settings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_settings();
			}
		});

		menue.add(datei);
		menue.add(extras);
		menue.add(hilfe);

		datei.add(settings);
		datei.add(close);
		extras.add(ExcelExport);
		extras.add(backup);
		extras.add(wishlist);
		hilfe.add(dbVersion);
		pnlMenü.add(menue, BorderLayout.WEST);

		JLabel lblVersion = new JLabel(version);

		lblVersion.setFont(new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize()));
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlMenü.add(lblVersion, BorderLayout.EAST);

		logger.trace("Finished creating GUI Components. Start creating Table Contents");

//		table = new JTable() {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
//				Component comp = super.prepareRenderer(renderer, row, col);
//				String cellValue = (String) getModel().getValueAt(row, col); // get the value from the cell
//				if (cellValue.equals("Frank Schätzing"))
//					comp.setBackground(Color.red);
//				else
//					comp.setBackground(Color.green);
//				return comp;
//			}
//		};
		table.setModel(tableDisplay);
		table.setFont(defaultFont);
		table.setShowVerticalLines(false);
		table.setSelectionBackground(Color.DARK_GRAY);
		table.setSelectionForeground(Color.WHITE);
		table.setRowHeight(table.getRowHeight() + 6);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = entries.getIndexOf(searchAutor, searchTitel);
					new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
				}
				txt_search.setText("Suche ... (" + entries.getSize() + ")");
				if (SwingUtilities.isRightMouseButton(e)) {
					JTable table2 = (JTable) e.getSource();
					int row = table2.rowAtPoint(e.getPoint());
					if (row > -1) {
						table2.setRowSelectionInterval(row, row);
					}
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzufügen");
				JMenuItem itemDelBuch = new JMenuItem("Buch löschen");
				JMenuItem itemChanBuch = new JMenuItem("Buch bearbeiten");
				JMenuItem itemAnalyzeAuthor = new JMenuItem("Autor analysieren (Beta)");
				menu.add(itemAddBuch);
				menu.add(itemChanBuch);
				menu.add(itemDelBuch);
				menu.add(itemAnalyzeAuthor);
				menu.show(table, e.getX(), e.getY());
				itemAddBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch hinzufügen") {
							new Dialog_add_Booklist(entries, treeModel, rootNode);
						}
					}
				});
				itemDelBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch löschen") {
							deleteBuch();
						}
					}
				});
				itemChanBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch bearbeiten") {
							String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
							String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
							int index = entries.getIndexOf(searchAutor, searchTitel);
							new Dialog_edit_Booklist(entries, index, treeModel, rootNode);
						}
					}
				});
				itemAnalyzeAuthor.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Autor analysieren (Beta)") {
							new wishlist();
							BookListModel.analyzeAuthor((String) table.getValueAt(table.getSelectedRow(), 0));
							gui.wishlist.updateModel();
						}
					}
				});
			}

		});
		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteBuch();
					updateModel();
				}
				BookListModel.checkAuthors();
				txt_search.setText("Suche ... (" + entries.getSize() + ")");
			}
		});

		logger.trace("end creating Table content. Start creating Tree Contents + ScrollPane");

		JPanel pnl_mid = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnl_mid.add(listScrollPane, BorderLayout.CENTER);

		rootNode.removeAllChildren();
		BookListModel.checkAuthors();
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);
		tree.setCellRenderer(new MyTreeCellRenderer());
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (tree.getSelectionPath() != null) {
					boolean isRightClick = SwingUtilities.isRightMouseButton(e);
					if (isRightClick) {
						JTree tree2 = (JTree) e.getSource();
						TreePath path = tree2.getClosestPathForLocation(e.getX(), e.getY());
						if (path != null)
							tree.setSelectionPath(path);
					}
					boolean isAutor = tree.getSelectionPath().getLastPathComponent().toString().contains("Autoren");
					String text = tree.getSelectionPath().getLastPathComponent().toString();
					String[] text_array = text.split(Pattern.quote(" ("));
					text = text_array[0];
					if (isAutor) {
						updateModel();
					} else {
						search(text);
						treeSelection = text;
					}
					if (isRightClick && !isAutor) {
						search(text);
						showMenu(e);
					}
					if (isAutor)
						setLastSearch("");
					else
						setLastSearch(text);
					table.clearSelection();
					txt_search.setText("Suche ... (" + entries.getSize() + ")");
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzufügen");
				menu.add(itemAddBuch);
				menu.show(tree, e.getX(), e.getY());
				itemAddBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals("Buch hinzufügen")) {
							new Dialog_add_Booklist(entries, treeModel, rootNode);
						}
					}
				});
			}

		});
		tree.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				tree.setSelectionPath(null);
			}

		});
		JScrollPane treeScrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		treeScrollPane.setPreferredSize(new Dimension(300, pnl_mid.getHeight()));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, listScrollPane);
		this.add(splitPane, BorderLayout.CENTER);
		this.add(panel, BorderLayout.NORTH);

		logger.trace("Finished creating Tree Contents + ScrollPane. Start Update Model & show GUI");

		updateModel();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent et) {
				logger.info("Close Database");
				Database.closeConnection();
				logger.info("Window closing");
			}
		});
		logger.trace("Init completed");
	}

	public static void deleteBuch() {
		int[] selected = table.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			String searchAutor = (String) table.getValueAt(selected[i], 0);
			String searchTitel = (String) table.getValueAt(selected[i], 1);
			int index = entries.getIndexOf(searchAutor, searchTitel);
			if (selected.length != 0) {
				int antwort = JOptionPane.showConfirmDialog(null,
						"Wirklich '" + searchAutor + " - " + searchTitel + "' löschen?", "Löschen",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					entries.delete(index);
				}
				BookListModel.checkAuthors();
			} else {
				JOptionPane.showMessageDialog(null, "Es wurde kein Buch ausgewählt");
			}
		}
		if (treeSelection != "")
			search(treeSelection);
		else
			search(getLastSearch());
		updateModel();
	}

	public static void updateNode() {
		rootNode = new DefaultMutableTreeNode("Autoren (" + BookListModel.authors.size() + ")");
		treeModel = new DefaultTreeModel(rootNode);
		for (int i = 0; i < BookListModel.authors.size(); i++) {
			String autor = BookListModel.authors.get(i);
			autorNode = new DefaultMutableTreeNode(autor);
			treeModel.insertNodeInto(autorNode, rootNode, i);
			if (BookListModel.authorHasSeries(autor)) {
				try {
					String[] serien = BookListModel.getSeriesFromAuthor(autor);
					for (int j = 0; j < serien.length; j++) {
						serieNode = new DefaultMutableTreeNode(serien[j]);
						treeModel.insertNodeInto(serieNode, autorNode, j);
					}
				} catch (NullPointerException e) {
					System.out.println("Mainframe Keine Serie gefunden zu " + autor);
				}

			}

		}

		treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		tree.revalidate();
		tree.repaint();
	}

	public static void copyFilesInDirectory(File from, File to) {
		if (!to.exists()) {
			to.mkdirs();
		}
		for (File file : from.listFiles()) {
			if (file.isDirectory()) {
				copyFilesInDirectory(file, new File(to.getAbsolutePath() + "/" + file.getName()));
			} else {
				try {
					File n = new File(to.getAbsolutePath() + "/" + file.getName());
					Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void copyFileToDirectory(File file, File to) throws IOException {
		if (!to.exists()) {
			to.mkdirs();
		}
		File n = new File(to.getAbsolutePath() + "/" + file.getName());
		Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	public static void updateModel() {
		tableDisplay = new SimpleTableModel(entries);
		table.setModel(tableDisplay);
		treeSelection = "";
	}

	public static int anz_bücherAutor(String text) {
		int anz = 0;
		for (int i = 0; i < entries.getSize(); i++) {
			Book_Booklist eintrag = entries.getElementAt(i);
			String autor = eintrag.getAuthor();
			if (autor.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static int anz_bücherSerie(String text) {
		int anz = 0;
		for (int i = 0; i < entries.getSize(); i++) {
			Book_Booklist eintrag = entries.getElementAt(i);
			String serie = eintrag.getSeries();
			if (serie.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static void search(String text) {
		filter.clear();
		text = text.toUpperCase();
		for (int i = 0; i < entries.getSize(); i++) {
			Book_Booklist eintrag = entries.getElementAt(i);
			String autor = eintrag.getAuthor().toUpperCase();
			String titel = eintrag.getTitle().toUpperCase();
			String bemerkung = eintrag.getNote().toUpperCase();
			String leihVon = eintrag.getBorrowedFrom().toUpperCase();
			String leihAn = eintrag.getBorrowedTo().toUpperCase();
			String serie = eintrag.getSeries().toUpperCase();
			if (tree.getSelectionCount() == 0) {
				if (autor.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (titel.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (bemerkung.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (leihVon.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (leihAn.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (serie.contains(text)) {
					filter.addElement(entries.getElementAt(i));
				}
			} else {
				if (autor.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (titel.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (bemerkung.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (leihVon.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (leihAn.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				} else if (serie.equals(text)) {
					filter.addElement(entries.getElementAt(i));
				}
			}
		}
		if (filter.getSize() > 0) {
			tableDisplay = new SimpleTableModel(filter);
			table.setModel(tableDisplay);
		} else {
			JOptionPane.showMessageDialog(null, "Es gab leider keine Treffer!");
		}
	}

	public static Mainframe getInstance() {
		if (instance == null) {
			instance = new Mainframe();
		}

		return instance;
	}

	public static void main(String[] args) {
		getInstance();
	}

	public static String getTreeSelection() {
		return treeSelection;
	}

	public static String getLastSearch() {
		return lastSearch;
	}

	public static void setLastSearch(String lastSearch) {
		Mainframe.lastSearch = lastSearch;
	}

}
