package gui;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
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
import javax.swing.JScrollBar;
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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import application.BookListModel;
import application.Book_Booklist;
import application.HandleConfig;
import application.SimpleTableModel;
import data.Database;

/**
 * Main Window to show Table of Entries and Tree of authors
 */
public class Mainframe extends JFrame {

	private static final long serialVersionUID = 1L;
	// log4j to log in file called app.log
	public static Logger logger = null;
	/*
	 * executor for Multithreading Mainframe.executor.submit(() -> {
	 * 
	 * });
	 */
	public static ExecutorService executor = Executors.newFixedThreadPool(10);

	public static Font defaultFont = new Font("Roboto", Font.PLAIN, 16);
	public static Font descFont = new Font("Roboto", Font.PLAIN, 16);
	static JTable table = new JTable();
	public static BookListModel entries;
	private static DefaultListModel<Book_Booklist> filter;
	private static SimpleTableModel tableDisplay;
	private static int lastTableHoverRow = -1;
	private static TreePath lastPath = null;
	private static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode");
	private static DefaultMutableTreeNode autorNode = new DefaultMutableTreeNode("AutorNode");
	private static DefaultMutableTreeNode serieNode = new DefaultMutableTreeNode("SerieNode");
	private static DefaultTreeModel treeModel;
	private static JTree tree = new JTree(treeModel);
	private JTextField txt_search;
	private static Mainframe instance;
	private static String treeSelection;
	private static String lastSearch = "";

	public static int prozEbook = 0;
	public static int prozAuthor = 0;
	public static int prozTitle = 0;
	public static int prozSeries = 0;
	public static int prozRating = 0;

	private static String version = "3.1.1";

	private Mainframe() throws HeadlessException {
		super("B�cherliste");
		super.setBackground(Color.BLACK);

		logger = LogManager.getLogger(getClass());
		logger.trace("start creating Frame & readConfig");
		HandleConfig.readConfig();
		if (HandleConfig.debug.equals("WARN")) {
			Configurator.setLevel(logger, Level.WARN);
		} else if (HandleConfig.debug.equals("INFO")) {
			Configurator.setLevel(logger, Level.INFO);
		} else if (HandleConfig.debug.equals("TRACE")) {
			Configurator.setLevel(logger, Level.TRACE);
		}

		this.setLayout(new BorderLayout(10, 10));
		this.setLocationByPlatform(true);
		this.setSize(1300, 800);
		this.setResizable(true);

		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			if (HandleConfig.darkmode == 1) {
				// Change Colors for Darkmode
				UIManager.put("Panel.background", Color.DARK_GRAY);

				UIManager.put("Label.foreground", Color.WHITE);

				UIManager.put("CheckBox.background", Color.DARK_GRAY);
				UIManager.put("CheckBox.foreground", Color.WHITE);

				UIManager.put("ComboBox.background", Color.WHITE);
				UIManager.put("ComboBox.foreground", Color.BLACK);

				UIManager.put("Menu.background", Color.DARK_GRAY);
				UIManager.put("Menu.foreground", Color.WHITE);
				UIManager.put("Menu.opaque", true);
				UIManager.put("MenuBar.border", 0);
				UIManager.put("MenuItem.background", new Color(90, 90, 90));
				UIManager.put("MenuItem.foreground", Color.WHITE);
				UIManager.put("MenuItem.opaque", true);
				UIManager.put("PopupMenu.border", Color.DARK_GRAY);

				UIManager.put("Table.background", Color.DARK_GRAY);
				UIManager.put("Table.foreground", Color.WHITE);

				UIManager.put("OptionPane.background", Color.DARK_GRAY);
				UIManager.put("OptionPane.messageForeground", Color.WHITE);

				UIManager.put("ScrollPane.background", Color.DARK_GRAY);
				UIManager.put("SplitPane.background", Color.DARK_GRAY);

				UIManager.put("TextField.background", Color.DARK_GRAY);
				UIManager.put("TextField.inactiveBackground", Color.GRAY);
				UIManager.put("TextField.foreground", new Color(220, 220, 220));
				UIManager.put("TextField.caretForeground", UIManager.get("TextField.foreground"));

				UIManager.put("TextArea.background", Color.DARK_GRAY);
				UIManager.put("TextArea.inactiveForeground", Color.WHITE);

				tree.setBackground(Color.DARK_GRAY);
				table.getTableHeader().setOpaque(false);
				table.setBackground(Color.DARK_GRAY);
				table.getTableHeader().setBackground(Color.DARK_GRAY);
				table.getTableHeader().setForeground(Color.WHITE);
				this.getContentPane().setBackground(Color.DARK_GRAY);

			} else {
				UIManager.put("TextArea.inactiveForeground", Color.BLACK);
			}
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
				| IllegalAccessException e) {
			logger.error(e.getMessage());
		}

		logger.trace("Finished create Frame & readConfig. Start creating Lists and readDB");
		entries = new BookListModel();
		filter = new DefaultListModel<Book_Booklist>();
		tableDisplay = new SimpleTableModel(entries);

		logger.trace("Finished creating List & DB. Start creating GUI Components");

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(10, 5));

		txt_search = new CustomTextField();
		txt_search.setToolTipText("Suchtext");
		txt_search.setText("Suche ... (" + entries.getSize() + ")");
		setSearchTextInactive();
		txt_search.setMargin(new Insets(0, 10, 0, 0));
		txt_search.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				table.clearSelection();
			}

		});

		txt_search.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					search(txt_search.getText());
					tree.clearSelection();
					setLastSearch(txt_search.getText());
					if (tableDisplay.getRowCount() == 0) {
						updateModel();
						JOptionPane.showMessageDialog(getParent(), "Keine �bereinstimmung gefunden");
					}
				}
			}
		});
		txt_search.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				setSearchTextInactive();
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (txt_search.getText().contains("Suche ..."))
					txt_search.setText("");
				setSearchTextActive();
			}
		});
		panel.add(txt_search, BorderLayout.CENTER);

		JButton btn_add = ButtonsFactory.createButton("+");
		btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_add_Booklist(entries, treeModel, rootNode);
				txt_search.setText("Suche ... (" + entries.getSize() + ")");
			}
		});

		panel.add(btn_add, BorderLayout.WEST);

		JButton btn_search = ButtonsFactory.createButton("suchen");
		btn_search.setFont(btn_search.getFont().deriveFont(Font.BOLD, 13));
		btn_search.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				search(txt_search.getText());
				setSearchTextInactive();
				tree.clearSelection();
				setLastSearch(txt_search.getText());
				if (entries.getSize() == 0) {
					updateModel();
					JOptionPane.showMessageDialog(getParent(), "Keine �bereinstimmung gefunden");
				}
			}
		});

		panel.add(btn_search, BorderLayout.EAST);

		JPanel pnlMen� = new JPanel();
		pnlMen�.setLayout(new BorderLayout());
		panel.add(pnlMen�, BorderLayout.NORTH);

		JMenuBar menue = new JMenuBar();
		JMenu datei = new JMenu("Datei");
		JMenu extras = new JMenu("Extras");
		JMenu hilfe = new JMenu("Hilfe");

		JMenuItem backup = new JMenuItem("DB Backup");
		backup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean ret = createBackup();
				if (ret)
					JOptionPane.showMessageDialog(getParent(), "Backup erfolgreich.");
				else
					JOptionPane.showMessageDialog(getParent(), "Backup fehlgeschlagen oder nicht vollst�ndig.");
			}
		});
		JMenuItem close = new JMenuItem("Schlie�en");
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		JMenuItem wishlist = new JMenuItem("Wunschliste");
		wishlist.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new wishlist();
			}
		});
		JMenuItem update = new JMenuItem("check update");
		update.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				URL url = null;
				try {
					url = new URI("https://github.com/NeXoS355/booklist/releases/latest/download/Bucherliste.jar")
							.toURL();
					try (BufferedInputStream in = new BufferedInputStream(url.openStream());
							FileOutputStream fileOutputStream = new FileOutputStream("latest.jar")) {
						byte dataBuffer[] = new byte[1024];
						int bytesRead;
						while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
							fileOutputStream.write(dataBuffer, 0, bytesRead);
						}
						ProcessBuilder pb = new ProcessBuilder("java", "-jar", "latest.jar", "version");
						logger.info("Command: " + pb.command());
						Process proc = pb.start();
				        // Warte darauf, dass der Prozess abgeschlossen wird
				        int exitCode = proc.waitFor();
				        System.out.println("Prozess beendet mit Exit-Code: " + exitCode);

				        // InputStream lesen (kontinuierlich statt mit available())
				        try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
				             BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {

				            // Ausgabe des Prozesses lesen
				            String line;
				            while ((line = reader.readLine()) != null) {
				                System.out.println("OUT: " + line);
				            }

				            // Fehlerausgabe lesen (falls vorhanden)
				            while ((line = errorReader.readLine()) != null) {
				                System.out.println("ERR: " + line);
				            }
				        }
					} catch (IOException e1) {
						logger.error(e1.getMessage());
					} catch (InterruptedException e1) {
						logger.error(e1.getMessage());
					}
				} catch (MalformedURLException | URISyntaxException e1) {
					// TODO Auto-generated catch block
					logger.error(e1.getMessage());
				}

			}
		});
		JMenuItem about = new JMenuItem("�ber");
		about.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String javaVersion = System.getProperty("java.version");
				String text = "https://github.com/NeXoS355/booklist" + "\n\nProgram Version: " + version
						+ "\nDB Layout Version: " + Database.readCurrentLayoutVersion() + "\n\nLocal Java Version: "
						+ javaVersion + "\nApache Derby Version: " + Database.readCurrentDBVersion();
				JOptionPane.showMessageDialog(null, text);
			}
		});
		JMenuItem ExcelExport = new JMenuItem("CSV Export");
		ExcelExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int antwort = JOptionPane.showConfirmDialog(null,
						"Es wird eine csv Datei im Programmpfad abgelegt.\nFortfahren?", "Export",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					boolean check = Database.CSVExport();
					if (check) {
						JOptionPane.showMessageDialog(null, "Liste erfolgreich exportiert!");
					} else {
						JOptionPane.showMessageDialog(null, "Datei konnte nicht geschrieben werden!");
					}
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
		JMenuItem info = new JMenuItem("Info");
		info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_info();
			}
		});
		JMenuItem apiAbruf = new JMenuItem("API Abruf");
		apiAbruf.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!HandleConfig.apiToken.equals("") && !HandleConfig.apiURL.equals("")) {
					downloadFromApi();
				} else {
					JOptionPane.showMessageDialog(null,
							"API Token oder URL nicht gefunden.\nBitte die Einstellungen �berpr�fen!");
				}
			}
		});

		JMenuItem apiUpload = new JMenuItem("API Upload");
		apiUpload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!HandleConfig.apiToken.equals("") && !HandleConfig.apiURL.equals("")) {
					uploadToApi();
				} else {
					JOptionPane.showMessageDialog(null,
							"API Token oder URL nicht gefunden.\nBitte die Einstellungen �berpr�fen!");
				}
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
		extras.add(apiAbruf);
		extras.add(apiUpload);
		extras.add(info);
		hilfe.add(update);
		hilfe.add(about);
		pnlMen�.add(menue, BorderLayout.WEST);

		JLabel lblVersion = new JLabel("Version: " + version);

		lblVersion.setFont(new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize()));
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlMen�.add(lblVersion, BorderLayout.EAST);
		pnlMen�.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		logger.trace("Finished creating GUI Components. Start creating Table Contents");

		table.setModel(tableDisplay);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		CustomTableCellRenderer tableRenderer = new CustomTableCellRenderer();
		table.setDefaultRenderer(Object.class, tableRenderer);
		JTableHeader header = table.getTableHeader();
		header.setDefaultRenderer(tableRenderer);
		table.setFont(defaultFont);
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setRowHeight(table.getRowHeight() + 6);
		table.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {

				JTable table2 = (JTable) e.getSource();
				int row = table2.rowAtPoint(e.getPoint());
				if (lastTableHoverRow != row) {
					tableRenderer.setHoveredRow(row);
					repaintTableRow(row);
					repaintTableRow(lastTableHoverRow);
					lastTableHoverRow = row;
				}
			}

		});
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 1);
					String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 2);
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

			public void mouseExited(MouseEvent e) {
				tableRenderer.clearHoveredRow();
				repaintTableRow(lastTableHoverRow);
				lastTableHoverRow = -1;
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzuf�gen");
				JMenuItem itemDelBuch = new JMenuItem("Buch l�schen");
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
						if (e.getActionCommand() == "Buch hinzuf�gen") {
							new Dialog_add_Booklist(entries, treeModel, rootNode);
						}
					}
				});
				itemDelBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch l�schen") {
							deleteBook();
						}
					}
				});
				itemChanBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch bearbeiten") {
							String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 1);
							String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 2);
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
							BookListModel.analyzeAuthor((String) table.getValueAt(table.getSelectedRow(), 1));
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
					deleteBook();
				}
				BookListModel.checkAuthors();
				txt_search.setText("Suche ... (" + entries.getSize() + ")");
			}
		});

		logger.trace("end creating Table content. Start creating Tree Contents + ScrollPane");

		JPanel pnl_mid = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		if (HandleConfig.darkmode == 1)
			listScrollPane.getViewport().setBackground(new Color(75, 75, 75));
		JScrollBar tableVerticalScrollBar = listScrollPane.getVerticalScrollBar();
		tableVerticalScrollBar.setUI(new CustomScrollBar());

		pnl_mid.add(listScrollPane, BorderLayout.CENTER);

		rootNode.removeAllChildren();
		BookListModel.checkAuthors();
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setShowsRootHandles(false);
		CustomTreeCellRenderer renderer = new CustomTreeCellRenderer();
		tree.setCellRenderer(renderer);
		tree.putClientProperty("JTree.lineStyle", "None");

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

			public void mouseExited(MouseEvent e) {
				repaintTreeBound(null);
				renderer.setHoveredRow(-1);
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzuf�gen");
				menu.add(itemAddBuch);
				menu.show(tree, e.getX(), e.getY());
				itemAddBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand().equals("Buch hinzuf�gen")) {
							new Dialog_add_Booklist(entries, treeModel, rootNode);
						}
					}
				});
			}

		});
		// F�ge einen MouseMotionListener hinzu, um die Mausposition zu verfolgen
		tree.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				// Berechne die Zeile, �ber der die Maus schwebt
				int row = tree.getRowForLocation(e.getX(), e.getY());

//				 Wenn sich die Zeile ge�ndert hat, aktualisiere den Renderer
				if (row != renderer.hoveredRow) {
					TreePath currentPath = tree.getPathForLocation(e.getX(), e.getY());
					if (currentPath != null && !currentPath.equals(lastPath)) {
						repaintTreeBound(currentPath);
						renderer.setHoveredRow(row);
					}

				}
			}
		});
		JScrollPane treeScrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		if (HandleConfig.darkmode == 1)
			treeScrollPane.getViewport().setBackground(new Color(75, 75, 75));
		JScrollBar treeVerticalScrollBar = treeScrollPane.getVerticalScrollBar();
		treeVerticalScrollBar.setUI(new CustomScrollBar());
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
				logger.trace("Close Database");
				Database.closeConnection();
				if (HandleConfig.backup == 2) {
					createBackup();
				} else if (HandleConfig.backup == 1) {
					if (JOptionPane.showConfirmDialog(null, "Backup erstellen?", "Backup?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						boolean ret = createBackup();
						if (ret)
							JOptionPane.showMessageDialog(getParent(), "Backup erfolgreich.");
						else
							JOptionPane.showMessageDialog(getParent(), "Backup fehlgeschlagen oder nicht vollst�ndig.");
					}
				}
				logger.trace("Window closing");
			}

			public void windowClosed(java.awt.event.WindowEvent windowEvent) {
				if (HandleConfig.backup == 2) {
					createBackup();
				} else if (HandleConfig.backup == 1) {
					if (JOptionPane.showConfirmDialog(null, "Backup erstellen?", "Backup?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
						boolean ret = createBackup();
						if (ret)
							JOptionPane.showMessageDialog(getParent(), "Backup erfolgreich.");
						else
							JOptionPane.showMessageDialog(getParent(), "Backup fehlgeschlagen oder nicht vollst�ndig.");
					}
				}
				logger.trace("Window closed");
				System.exit(1);
			}
		});
		logger.trace("Init completed");
	}

	/**
	 * deletes the currently selected entry from Booklist
	 * 
	 */
	public static void deleteBook() {
		int[] selected = table.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			String searchAutor = (String) table.getValueAt(selected[i], 1);
			String searchTitel = (String) table.getValueAt(selected[i], 2);
			int index = entries.getIndexOf(searchAutor, searchTitel);
			if (selected.length != 0) {
				int antwort = JOptionPane.showConfirmDialog(null,
						"Wirklich '" + searchAutor + " - " + searchTitel + "' l�schen?", "L�schen",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					entries.delete(index);
				}
				BookListModel.checkAuthors();
			} else {
				JOptionPane.showMessageDialog(null, "Es wurde kein Buch ausgew�hlt");
			}
			logger.trace("Book deleted: " + searchAutor + ";" + searchTitel);
		}
		if (!treeSelection.equals(""))
			search(treeSelection);
		else
			search(getLastSearch());
	}

	/**
	 * updates the JTree
	 * 
	 */
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
					Mainframe.logger.info("Mainframe Keine Serie gefunden zu " + autor);
				}

			}

		}

		treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		tree.revalidate();
		tree.repaint();
		Mainframe.logger.trace("Mainframe Node updated");

	}

	/**
	 * updates the table with current model
	 * 
	 */
	public static void updateModel() {
		tableDisplay = new SimpleTableModel(entries);
		table.setModel(tableDisplay);
		treeSelection = "";
		setTableLayout();
		Mainframe.logger.trace("Mainframe Model updated");
	}

	/**
	 * copy multiple files to specified directory
	 * 
	 * @param from - Source file or Directory
	 * @param to   - Destination Directory
	 */
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

	/**
	 * copy single specified file to specified directory
	 * 
	 * @param file - file to copy
	 * @param to   - path where to save the file
	 */
	private static void copyFileToDirectory(File file, File to) throws IOException {
		if (!to.exists()) {
			to.mkdirs();
		}
		File n = new File(to.getAbsolutePath() + "/" + file.getName());
		Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * sets the JTable Column Layout
	 * 
	 */
	public static void setTableLayout() {
		TableColumnModel columnModel = table.getColumnModel();

		int total = columnModel.getTotalColumnWidth();
		int minProzEbook = total * 5 / 100;
		int minProzAuthor = total * 10 / 100;
		int minProzTitle = total * 10 / 100;
		int minProzSeries = total * 10 / 100;
		int minProzRating = total * 5 / 100;

		for (int i = 0; i < SimpleTableModel.columnNames.length; i++) {
			if (SimpleTableModel.columnNames[i].equals("E-Book")) {
				columnModel.getColumn(i).setMinWidth(minProzEbook);
				columnModel.getColumn(i).setMaxWidth(50);
				columnModel.getColumn(i).setPreferredWidth(prozEbook);
			} else if (SimpleTableModel.columnNames[i].equals("Autor")) {
				columnModel.getColumn(i).setMinWidth(minProzAuthor);
				columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
				columnModel.getColumn(i).setPreferredWidth(prozAuthor);
			} else if (SimpleTableModel.columnNames[i].equals("Titel")) {
				columnModel.getColumn(i).setMinWidth(minProzTitle);
				columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
				columnModel.getColumn(i).setPreferredWidth(prozTitle);
			} else if (SimpleTableModel.columnNames[i].equals("Serie")) {
				columnModel.getColumn(i).setMinWidth(minProzSeries);
				columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
				columnModel.getColumn(i).setPreferredWidth(prozSeries);
			} else if (SimpleTableModel.columnNames[i].equals("Rating")) {
				columnModel.getColumn(i).setMinWidth(minProzRating);
				columnModel.getColumn(i).setMaxWidth(50);
				columnModel.getColumn(i).setPreferredWidth(prozRating);
			}

		}

	}

	/**
	 * search table entries with specified String
	 * 
	 * @param text - search String
	 */
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
		}
		if (filter.getSize() > 0) {
			tableDisplay = new SimpleTableModel(filter);
			table.setModel(tableDisplay);
			setTableLayout();
		} else {
			JOptionPane.showMessageDialog(null, "Es gab leider keine Treffer!");
			updateModel();
		}
	}

	/**
	 * creates a full file based copy of all important files
	 * 
	 * @return return success state True=success False=failure
	 */
	public static boolean createBackup() {
		try {
			String filepath = Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
			String[] fileArray = filepath.split("/");
			String filename = fileArray[fileArray.length - 1];
			if (filename.contains(".jar")) {
				Date dt = new Date();
				Long LongTime = dt.getTime();
				String StrTime = Long.toString(LongTime).substring(0, LongTime.toString().length() - 3);
				copyFilesInDirectory(new File("BooklistDB"), new File("Sicherung/" + StrTime + "/BooklistDB"));
				copyFileToDirectory(new File("derby.log"), new File("Sicherung/" + StrTime));
				copyFileToDirectory(new File("config.conf"), new File("Sicherung/" + StrTime));
				copyFileToDirectory(new File(filename), new File("Sicherung/" + StrTime));
				Mainframe.logger.info("Backup created");
				return true;
			} else {
				Mainframe.logger.error("Error while creating Backup. Could not extract filename.");
				return false;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			Mainframe.logger.error("Error while creating Backup. IOException");
			Mainframe.logger.error(e1.toString());
			return false;
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
			Mainframe.logger.error("Error while creating Backup. URISyntaxException");
			Mainframe.logger.error(e1.toString());
			return false;
		}
	}

	/**
	 * download the saved books via API from the webApp
	 */
	private void downloadFromApi() {
		try {
			logger.trace("Web API request: " + HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken);
			URL getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
			HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			logger.trace("Web API GET responseCode: " + responseCode);
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// API-Antwort lesen (Rohdaten)
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuilder response = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				String jsonResponse = response.toString();
				logger.trace("Web API GET response: " + jsonResponse);
				JsonElement jsonElement = JsonParser.parseString(jsonResponse);
				int imported = 0;
				int rejected = 0;
				String importedBooks = "";
				String rejectedBooks = "";
				if (jsonElement.isJsonArray()) {
					JsonArray jsonArray = jsonElement.getAsJsonArray();
					for (JsonElement element : jsonArray) {
						JsonObject jsonObject = element.getAsJsonObject();
						// Felder als String-Variablen speichern
						String author = jsonObject.get("author").getAsString();
						String title = jsonObject.get("title").getAsString();
						String series = jsonObject.has("series") ? jsonObject.get("series").getAsString() : "";
						String seriesPart = jsonObject.has("series_part") ? jsonObject.get("series_part").getAsString()
								: "";
						String note = jsonObject.has("note") ? jsonObject.get("note").getAsString() : "";
						String ebook = jsonObject.has("ebook") ? jsonObject.get("ebook").getAsString() : null;
						boolean boolEbook = false;
						if (ebook.equals("1")) {
							boolEbook = true;
						} else {
							boolEbook = false;
						}
						if (seriesPart.equals("0"))
							seriesPart = "";

						boolean duplicate = false;
						for (int i = 0; i < entries.getSize(); i++) {
							if (entries.getElementAt(i).getAuthor().equals(author)
									&& entries.getElementAt(i).getTitle().equals(title) && !duplicate) {
								duplicate = true;
								rejectedBooks = rejectedBooks + "\n" + author + " - " + title;
								rejected += 1;
							}
						}
						;
						if (!duplicate) {
							Book_Booklist imp = new Book_Booklist(author, title, note, series, seriesPart, boolEbook, 0,
									null, "", "", new Timestamp(System.currentTimeMillis()), true);
							importedBooks = importedBooks + "\n" + author + " - " + title;
							imported += 1;
							Mainframe.entries.add(imp);
							BookListModel.checkAuthors();
						}

					}
				}
				in.close();
				String importString = "";
				if (rejected > 0)
					importString = "Anzahl B�cher importiert: " + imported + importedBooks + "\nDupletten erkannt:"
							+ rejectedBooks;
				else
					importString = "Anzahl B�cher importiert: " + imported + "\n" + importedBooks;
				if (imported >= 1 || rejected > 0) {

					JOptionPane.showMessageDialog(null, importString);

					try {
						// URL des Endpunkts
						URL deleteUrl = new URI(HandleConfig.apiURL + "/api/delete.php").toURL();
						con = (HttpURLConnection) deleteUrl.openConnection();
						// POST-Anfrage einstellen
						con.setRequestMethod("POST");
						con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
						con.setDoOutput(true);

						// Der Token, der gesendet werden soll
						String postData = "token=" + HandleConfig.apiToken;

						// Daten in den OutputStream schreiben
						try (OutputStream os = con.getOutputStream()) {
							byte[] input = postData.getBytes("utf-8");
							os.write(input, 0, input.length);
						}

						// Antwortcode �berpr�fen
						responseCode = con.getResponseCode();
						logger.trace("Web API DELETE books responseCode: " + responseCode);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				} else {
					JOptionPane.showMessageDialog(null, "Keine B�cher zum abrufen gefunden.");
				}
				con.disconnect();
			} else {
				JOptionPane.showMessageDialog(null, "Get request failed.");
			}
		} catch (URISyntaxException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		} catch (IOException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		}
	}

	/**
	 * upload all current Books to the webApp with the corresponding Token
	 */
	private void uploadToApi() {
		try {
			// URL des Endpunkts
			URL deleteUrl = new URI(HandleConfig.apiURL + "/api/deleteSynced.php").toURL();
			HttpURLConnection con = (HttpURLConnection) deleteUrl.openConnection();
			con = (HttpURLConnection) deleteUrl.openConnection();
			// POST-Anfrage einstellen
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setDoOutput(true);

			// Der Token, der gesendet werden soll
			String postData = "token=" + HandleConfig.apiToken;

			// Daten in den OutputStream schreiben
			try (OutputStream os = con.getOutputStream()) {
				byte[] input = postData.getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			int responseCode = con.getResponseCode();
			logger.trace("Web API DELETE SyncedBooks responseCode: " + responseCode);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		try {
			// URL des API-Endpunkts
			logger.trace("Web API request: " + HandleConfig.apiURL + "/api/upload.php?token=" + HandleConfig.apiToken);
			URL postUrl;
			postUrl = new URI(HandleConfig.apiURL + "/api/upload.php?token=" + HandleConfig.apiToken).toURL();

			// Verbindung aufbauen
			HttpURLConnection con = (HttpURLConnection) postUrl.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			con.setDoOutput(true);

			// GSON-Instanz erstellen
			Gson gson = new Gson();
			JsonArray jsonArray = new JsonArray();
			// Nur die relevanten Felder in ein JSON-Array umwandeln

			for (Book_Booklist book : BookListModel.getBooks()) {
				JsonObject jsonBook = new JsonObject();
				jsonBook.addProperty("bid", book.getBid());
				jsonBook.addProperty("author", book.getAuthor());
				jsonBook.addProperty("title", book.getTitle());
				jsonArray.add(jsonBook);

			}

			// JSON-Daten senden
			String jsonInputString = gson.toJson(jsonArray);

			try (OutputStream os = con.getOutputStream()) {
				byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			// Antwort vom Server lesen
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				logger.trace("B�cher erfolgreich hochgeladen!");
				JOptionPane.showMessageDialog(this, "B�cher erfolgreich hochgeladen!");
			} else {
				logger.error("Fehler beim Hochladen der B�cher: " + responseCode);
				JOptionPane.showMessageDialog(this, "Fehler beim Hochladen der B�cher: " + responseCode);
			}

			// Verbindung schlie�en
			con.disconnect();
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		} catch (ProtocolException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		} catch (IOException e) {
			logger.error(e.getMessage());
			JOptionPane.showMessageDialog(null, "Es ist ein Fehler aufgetreten.");
		}

	}

	/**
	 * get current Tree Selection for other Classes
	 * 
	 * @return current Tree Selection
	 */
	public static String getTreeSelection() {
		return treeSelection;
	}

	/**
	 * get last searched text for other classes
	 * 
	 * @return last searched text
	 */
	public static String getLastSearch() {
		return lastSearch;
	}

	/**
	 * sets global Variable to the last searched String
	 * 
	 * @param lastSearch - String of last searched text
	 */
	public static void setLastSearch(String lastSearch) {
		Mainframe.lastSearch = lastSearch;
	}

	/**
	 * repaints only one specific Row of the main Table
	 * 
	 * @param row - Rownumber to be repainted
	 */
	public void repaintTableRow(int row) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.repaint(table.getCellRect(row, i, true));
		}
	}

	public void repaintTreeBound(TreePath path) {
		// Begrenze das Repaint auf den alten und neuen Pfad
		if (lastPath != null) {
			Rectangle lastBounds = tree.getPathBounds(lastPath);
			if (lastBounds != null) {
				tree.repaint(lastBounds); // Alte Position neu zeichnen
			}
		}

		lastPath = path;

		Rectangle currentBounds = tree.getPathBounds(path);
		if (currentBounds != null) {
			tree.repaint(currentBounds); // Neue Position neu zeichnen
		}
	}

	/**
	 * set Inactive Color of search TextField
	 */
	public void setSearchTextInactive() {
		txt_search.setForeground(Color.GRAY);

	}

	/**
	 * set Active Color of search TextField
	 */
	public void setSearchTextActive() {
		if (HandleConfig.darkmode == 1) {
			txt_search.setForeground(Color.WHITE);
			txt_search.setCaretColor(Color.WHITE);
		} else {
			txt_search.setForeground(Color.BLACK);
		}

	}

	/**
	 * start Instance of Mainframe
	 * 
	 * @param args -
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			for (String s : args) {
				if (s.equals("version")) {
					System.out.println(version);
				}
			}
		} else {
			getInstance();
		}
	}

	/**
	 * start Mainframe for instance
	 * 
	 * @return Mainframe Object
	 */
	public static Mainframe getInstance() {
		if (instance == null) {
			System.out.println("create new Instance");
			instance = new Mainframe();
		}

		return instance;
	}

}
