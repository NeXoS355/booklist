package gui;

import application.BookListModel;
import application.Book_Booklist;
import application.HandleConfig;
import application.SimpleTableModel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.util.UIScale;
import com.google.gson.*;
import data.Database;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import javax.imageio.ImageIO;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * Main Window to show Table of Entries and Tree of authors
 */
public class Mainframe extends JFrame {

  @Serial
  private static final long serialVersionUID = 1L;
  // log4j to log in file called app.log
  public static Logger logger = null;
  /*
   * executor for Multithreading Mainframe.executor.submit(() -> {
   *
   * });
   */
  public static final ExecutorService executor = Executors.newFixedThreadPool(10);

  public static Font defaultFont;
  public static Font descFont;
  public static BookListModel allEntries;
  public static int prozEbook = 0;
  public static int prozAuthor = 0;
  public static int prozTitle = 0;
  public static int prozSeries = 0;
  public static int prozRating = 0;
  public static final JTable table = new JTable();
  private static SimpleTableModel tableDisplay;
  private static int lastTableHoverRow = -1;
  private static TreePath lastPath = null;
  private static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode");
  static DefaultTreeModel treeModel;
  private static final JTree tree = new JTree(treeModel);
  static final JLayeredPane layeredPane = new JLayeredPane();
  static final ArrayList<JPanel> activeNotifications = new ArrayList<>();
  public static JSplitPane splitPane;
  private static final JScrollPane listScrollPane = new JScrollPane(table,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  // private static Timer animationTimer;
  private static Mainframe instance;
  private static String treeSelection = "";
  private static String lastSearch = "";
  private static boolean apiConnected = false;
  private static wishlist wishlist_instance;
  private static JMenuItem openWebApi;
  private static JMenuItem apiDownload;
  private static JMenuItem apiUpload;
  private static String version;
  private static final String MIGRATION_BRIDGE_TAG = "4.0.1";
  private JTextField txt_search;
  private static JButton btnSearchReset;
  private static JButton btnFab;
  private static int FAB_SIZE = 48;
  public static int defaultFrameWidth = 1300;
  public static int defaultFrameHeight = 800;
  public static int startX = 150;
  public static int startY = 150;

  public static final Color darkmodeBackgroundColor = new Color(18, 18, 18);
  public static final Color darkmodeAccentColor = new Color(200, 155, 110);

  private Mainframe() {
    final Properties properties = new Properties();
    try {
      properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
      version = properties.getProperty("version");
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
  }

  private Mainframe(boolean visible) throws HeadlessException {
    super("Booklist");

    this.setLayout(new BorderLayout(10, 10));
    this.setLocationByPlatform(true);
    this.setResizable(true);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

    URL iconURL = getClass().getResource("/resources/Icon.png");
    // iconURL is null when not found
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      this.setIconImage(icon.getImage());
    } else {
      logger.error("Resource not found: /resources/Icon.png");
    }

    final Properties properties = new Properties();
    try {
      properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
      version = properties.getProperty("version");
    } catch (IOException e) {
      logger.error(e.getMessage());
    }

    logger = LogManager.getLogger(getClass());
    logger.info("start creating Frame & readConfig");

    HandleConfig.readConfig();
    if (HandleConfig.debug.equals("WARN")) {
      Configurator.setLevel(logger, Level.WARN);
    } else if (HandleConfig.debug.equals("INFO")) {
      Configurator.setLevel(logger, Level.INFO);
    }

    // Standard-Locale setzen
    if (HandleConfig.lang.equals("English")) {
      Localization.setLocale(Locale.ENGLISH);
      UIManager.put("OptionPane.yesButtonText", "Yes");
      UIManager.put("OptionPane.noButtonText", "No");
      UIManager.put("OptionPane.cancelButtonText", "Cancel");
      UIManager.put("OptionPane.closeButtonText", "Close");
    } else {
      Localization.setLocale(Locale.GERMAN);
      UIManager.put("OptionPane.yesButtonText", "Ja");
      UIManager.put("OptionPane.noButtonText", "Nein");
      UIManager.put("OptionPane.cancelButtonText", "Abbrechen");
      UIManager.put("OptionPane.closeButtonText", "Schließen");
    }

    cleanup();

    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel(new FlatLightLaf());
      if (HandleConfig.darkmode == 1) {
        UIManager.setLookAndFeel(new FlatDarkLaf());

        // --- Dark Mode: Refined Slate Palette ---
        UIManager.put("Component.accentColor", darkmodeAccentColor);
        UIManager.put("TextField.foreground", Color.WHITE);

        // Backgrounds
        UIManager.put("Panel.background", darkmodeBackgroundColor);
        UIManager.put("ScrollPane.background", darkmodeBackgroundColor);
        UIManager.put("SplitPane.background", darkmodeBackgroundColor);
        UIManager.put("SplitPaneDivider.draggingColor", darkmodeBackgroundColor);
        UIManager.put("SplitPane.dividerColor", darkmodeBackgroundColor);

        // Table
        UIManager.put("Table.background", darkmodeBackgroundColor);
        UIManager.put("Table.selectionBackground", new Color(56, 48, 38));
        UIManager.put("Table.selectionInactiveBackground", new Color(40, 40, 40));

        // Table Header — gleiche Farbe wie Zeilen, Unterscheidung durch Schrift
        UIManager.put("TableHeader.background", darkmodeBackgroundColor);
        UIManager.put("TableHeader.foreground", new Color(120, 120, 120));
        UIManager.put("TableHeader.bottomSeparatorColor", new Color(48, 48, 48));

        // Borders
        UIManager.put("Separator.foreground", new Color(48, 48, 48));

        // Tree
        UIManager.put("Tree.selectionBackground", new Color(48, 42, 34));
        UIManager.put("Tree.selectionForeground", Color.WHITE);

        this.getContentPane().setBackground(darkmodeBackgroundColor);
        tree.setBackground(darkmodeBackgroundColor);
      } else {
        UIManager.put("TextArea.inactiveForeground", Color.BLACK);

        UIManager.put("Table.selectionBackground", new Color(62, 62, 62));

        // Light Mode: Tree-Selection explizit setzen (sonst weiß auf weiß)
        UIManager.put("Tree.selectionBackground", new Color(210, 215, 225));
        UIManager.put("Tree.selectionForeground", Color.BLACK);
      }

      // --- Beide Modi: Menüleiste nahtlos in Hintergrund ---
      UIManager.put("MenuBar.borderColor", UIManager.getColor("Panel.background"));
      UIManager.put("MenuBar.background", UIManager.getColor("Panel.background"));
      UIManager.put("MenuBar.underlineSelectionColor", UIManager.getColor("Component.accentColor"));
      UIManager.put("MenuBar.border", BorderFactory.createEmptyBorder());

    } catch (UnsupportedLookAndFeelException e) {
      logger.error(e.getMessage());
    }

    // Fonts nach FlatLaf-Init skalieren
    defaultFont = defaultFont.deriveFont((float) UIScale.scale(defaultFont.getSize()));
    descFont = descFont.deriveFont((float) UIScale.scale(descFont.getSize()));
    FAB_SIZE = UIScale.scale(48);

    SimpleTableModel.initColumnNames();
    logger.info("Finished create Frame & readConfig. Start creating Lists and readDB");
    allEntries = new BookListModel(true);
    cleanupDerbyAfterMigration();
    tableDisplay = new SimpleTableModel(allEntries);

    logger.info("Finished creating List & DB. Start creating GUI Components");

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(UIScale.scale(10), UIScale.scale(5)));
    panel.setBorder(BorderFactory.createEmptyBorder(UIScale.scale(4), UIScale.scale(8), UIScale.scale(4), UIScale.scale(8)));

    txt_search = new CustomTextField();
    txt_search.setToolTipText("Suchtext");
    updateSearchPlaceholder();
    txt_search.setMargin(new Insets(UIScale.scale(4), UIScale.scale(10), UIScale.scale(4), 0));
    txt_search.putClientProperty("JComponent.roundRect", true);

    if (HandleConfig.darkmode == 1) {
        txt_search.putClientProperty("FlatLaf.style","focusColor: #FFFFFF; focusedBorderColor: #FFFFFF; focusWidth: 2");
    } else {
        txt_search.putClientProperty("FlatLaf.style","focusColor: #000000; focusedBorderColor: #000000; focusWidth: 2");
    }
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
            JOptionPane.showMessageDialog(Mainframe.getInstance(), Localization.get("search.error"));
          }
        }
      }
    });
    panel.add(txt_search, BorderLayout.CENTER);

    // Lupen-Icon als leadingIcon im Suchfeld
    try {
      String lupePath = HandleConfig.darkmode == 1 ? "/resources/lupe_inv.png" : "/resources/lupe.png";
      BufferedImage lupeImage = ImageIO.read(Objects.requireNonNull(Mainframe.class.getResource(lupePath)));
      txt_search.putClientProperty("JTextField.leadingIcon", new ImageIcon(lupeImage));
    } catch (IOException ex) {
      logger.error(ex.getMessage());
    }

    Color resetColor = new Color(200, 50, 50);
    Color resetHoverColor = new Color(160, 30, 30);
    btnSearchReset = new JButton("✕") {
      @Override
      protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        super.paintComponent(g);
      }
    };
    btnSearchReset.putClientProperty("JButton.buttonType", "none");
    btnSearchReset.setFont(btnSearchReset.getFont().deriveFont(Font.BOLD, 16f));
    btnSearchReset.setForeground(Color.WHITE);
    btnSearchReset.setBackground(resetColor);
    btnSearchReset.setOpaque(false);
    btnSearchReset.setContentAreaFilled(false);
    btnSearchReset.setToolTipText(Localization.get("search.reset"));
    btnSearchReset.setBorderPainted(false);
    btnSearchReset.setFocusable(false);
    btnSearchReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnSearchReset.setMargin(new Insets(0, 3, 0, 3));
    btnSearchReset.setPreferredSize(new Dimension(UIScale.scale(30), UIScale.scale(30)));
    btnSearchReset.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) { btnSearchReset.setBackground(resetHoverColor); }
      @Override
      public void mouseExited(MouseEvent e) { btnSearchReset.setBackground(resetColor); }
    });
    btnSearchReset.setVisible(false);
    btnSearchReset.addActionListener(e -> {
      txt_search.setText("");
      setLastSearch("");
      tree.clearSelection();
      updateModel();
      updateSearchPlaceholder();
      btnSearchReset.setVisible(false);
    });
    txt_search.putClientProperty("JTextField.trailingComponent", btnSearchReset);

    JPanel pnlMenu = new JPanel();
    pnlMenu.setLayout(new BorderLayout());
    panel.add(pnlMenu, BorderLayout.NORTH);

    JMenuBar menue = new JMenuBar();
    menue.setBorder(BorderFactory.createEmptyBorder());
    menue.setBorderPainted(false);
    JMenu datei = new JMenu(Localization.get("menu.file"));
    JMenu extras = new JMenu(Localization.get("menu.extras"));
    JMenu hilfe = new JMenu(Localization.get("menu.help"));

    JMenuItem close = new JMenuItem(Localization.get("menu.close"));
    close.addActionListener(e -> System.exit(0));
    JMenuItem wishlist = new JMenuItem(Localization.get("menu.wishlist"));
    wishlist.addActionListener(e -> {
      if (wishlist_instance == null)
        wishlist_instance = new wishlist(Mainframe.getInstance(), true);
      else
        wishlist_instance.setVisible(true);
    });
    JMenuItem update = new JMenuItem(Localization.get("menu.update"));
    update.addActionListener(e -> {
      logger.info("check for Updates");
      checkUpdate();
    });
    JMenuItem about = new JMenuItem(Localization.get("menu.about"));
    about.addActionListener(e -> {
      String javaVersion = System.getProperty("java.version");
      String text = "https://github.com/NeXoS355/booklist" + "\n\nProgram Version: " + version
          + "\nDB Layout Version: " + Database.readCurrentLayoutVersion() + "\n\nLocal Java Version: "
          + javaVersion + "\nSQLite Version: " + Database.readCurrentDBVersion();
      JOptionPane.showMessageDialog(Mainframe.getInstance(), text);
    });
    JMenuItem ExcelExport = new JMenuItem(Localization.get("menu.csv"));
    ExcelExport.addActionListener(e -> {
      int antwort = JOptionPane.showConfirmDialog(Mainframe.getInstance(),
          Localization.get("csv.question"), "Export",
          JOptionPane.YES_NO_OPTION);
      if (antwort == JOptionPane.YES_OPTION) {
        boolean check = Database.CSVExport();
        if (check) {
          JOptionPane.showMessageDialog(Mainframe.getInstance(), Localization.get("csv.success"));
        } else {
          JOptionPane.showMessageDialog(Mainframe.getInstance(),
              Localization.get("csv.error"));
        }
      }
    });
    JMenuItem settings = new JMenuItem(Localization.get("menu.settings"));
    settings.addActionListener(e -> new Dialog_settings(Mainframe.getInstance(), true));
    JMenuItem info = new JMenuItem(Localization.get("menu.info"));
    info.addActionListener(e -> new Dialog_info(Mainframe.getInstance()));
    apiDownload = new JMenuItem(Localization.get("menu.webapiget"));
    apiDownload.addActionListener(e -> Mainframe.executor.submit(() -> downloadFromApi(true)));

    apiUpload = new JMenuItem(Localization.get("menu.webapiupload"));
    apiUpload.addActionListener(e -> Mainframe.executor.submit(() -> uploadToApi(true)));
    openWebApi = new JMenuItem(Localization.get("menu.webappopen"));
    openWebApi.addActionListener(e -> {
      logger.info("open Web API Website");
      try {
        URI oURL = new URI(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
          Desktop.getDesktop().browse(oURL);
        } else {
          new ProcessBuilder("xdg-open", oURL.toString()).start();
        }
      } catch (URISyntaxException | IOException e1) {
        logger.error(e1.getMessage());
      }

    });

    if (!apiConnected) {
      openWebApi.setEnabled(false);
      apiDownload.setEnabled(false);
      apiUpload.setEnabled(false);
    }

    menue.add(datei);
    menue.add(extras);
    menue.add(hilfe);

    datei.add(settings);
    datei.add(close);
    JMenuItem manageBackups = new JMenuItem(Localization.get("menu.manageBackups"));
    manageBackups.addActionListener(e -> new Dialog_backup(Mainframe.getInstance()));

    extras.add(ExcelExport);
    extras.add(manageBackups);
    extras.add(wishlist);
    extras.add(apiDownload);
    extras.add(apiUpload);
    extras.add(openWebApi);
    extras.add(info);
    hilfe.add(update);
    hilfe.add(about);
    pnlMenu.add(menue, BorderLayout.WEST);

    JLabel lblVersion = new JLabel(Localization.get("text.version") + ": " + version);

    lblVersion.setFont(new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize()));
    lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
    pnlMenu.add(lblVersion, BorderLayout.EAST);
    pnlMenu.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

    logger.info("Finished creating GUI Components. Start creating Table Contents");

    table.setModel(tableDisplay);
    table.setAutoCreateRowSorter(true);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
    CustomTableCellRenderer tableRenderer = new CustomTableCellRenderer(this.getTitle());
    table.setDefaultRenderer(Object.class, tableRenderer);
    CustomTableHeaderRenderer tableHeaderRenderer = new CustomTableHeaderRenderer();
    JTableHeader header = table.getTableHeader();
    header.setDefaultRenderer(tableHeaderRenderer);
    table.setFont(defaultFont);
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setRowHeight(UIScale.scale(table.getRowHeight() + 22));
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
          int index = allEntries.getIndexOf(searchAutor, searchTitel);
          if (index >= 0)
            new Dialog_edit_Booklist(Mainframe.getInstance(), allEntries, index, treeModel);
        }
        updateSearchPlaceholder();
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
        JMenuItem itemAddBook = new JMenuItem(Localization.get("contextMenu.addBook"));
        JMenuItem itemDelBook = new JMenuItem(Localization.get("contextMenu.delBook"));
        JMenuItem itemChanBook = new JMenuItem(Localization.get("contextMenu.editBook"));
        JMenuItem itemAnalyzeAuthor = new JMenuItem(Localization.get("contextMenu.analyzeSeries"));
        menu.add(itemAddBook);
        menu.add(itemChanBook);
        menu.add(itemDelBook);
        menu.add(itemAnalyzeAuthor);
        menu.show(table, e.getX(), e.getY());
        itemAddBook.addActionListener(e2 -> {
          if (Objects.equals(e2.getActionCommand(), Localization.get("contextMenu.addBook"))) {
            new Dialog_add_Booklist(Mainframe.getInstance());
          }
        });
        itemDelBook.addActionListener(e3 -> {
          if (Objects.equals(e3.getActionCommand(), Localization.get("contextMenu.delBook"))) {
            deleteBook();
          }
        });
        itemChanBook.addActionListener(e4 -> {
          if (Objects.equals(e4.getActionCommand(), Localization.get("contextMenu.editBook"))) {
            String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 1);
            String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 2);
            int index = allEntries.getIndexOf(searchAutor, searchTitel);
            if (index >= 0)
              new Dialog_edit_Booklist(Mainframe.getInstance(), allEntries, index, treeModel);
          }
        });
        itemAnalyzeAuthor.addActionListener(e5 -> {
          if (Objects.equals(e5.getActionCommand(), Localization.get("contextMenu.analyzeSeries"))) {
            String seriesName = (String) table.getValueAt(table.getSelectedRow(), 3);
            seriesName = seriesName.split(" - [0-9]")[0];
            if (wishlist_instance == null)
              wishlist_instance = new wishlist(Mainframe.getInstance(), false);
            boolean success = allEntries.analyzeSeries(seriesName,
                (String) table.getValueAt(table.getSelectedRow(), 1));
            gui.wishlist.updateModel();
            if (!success) {
              JOptionPane.showMessageDialog(Mainframe.getInstance(),
                  Localization.get("analyze.error"));
            } else {
              wishlist_instance.setVisible(true);
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
        allEntries.checkAuthors();
        updateSearchPlaceholder();
      }
    });
    logger.info("Start creating Tree Contents + ScrollPane");

    JPanel pnl_mid = new JPanel(new BorderLayout());

    listScrollPane.getViewport().setBackground(UIManager.getColor("Table.background"));
    JScrollBar tableVerticalScrollBar = listScrollPane.getVerticalScrollBar();
    tableVerticalScrollBar.setUI(new CustomScrollBar());
    layeredPane.setLayout(null);
    layeredPane.add(listScrollPane, Integer.valueOf(1)); // Die Tabelle im unteren Layer

    // Floating Action Button (FAB) zum Hinzufügen von Büchern
    btnFab = new JButton("+") {
      @Serial
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color accent = UIManager.getColor("Component.accentColor");
        if (accent == null) accent = new Color(0, 120, 212);
        g2.setColor(getModel().isRollover() ? accent.brighter() : accent);
        g2.fillOval(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    btnFab.setFont(defaultFont.deriveFont(Font.BOLD, defaultFont.getSize() * 1.5f));
    btnFab.setForeground(Color.WHITE);
    btnFab.setContentAreaFilled(false);
    btnFab.setOpaque(false);
    btnFab.setBorderPainted(false);
    btnFab.setFocusPainted(false);
    btnFab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    btnFab.setSize(FAB_SIZE, FAB_SIZE);
    btnFab.putClientProperty("JButton.buttonType", "none");
    btnFab.setToolTipText(Localization.get("contextMenu.addBook"));
    btnFab.addActionListener(e -> {
      new Dialog_add_Booklist(Mainframe.getInstance());
      updateSearchPlaceholder();
    });
    layeredPane.add(btnFab, Integer.valueOf(2));

    // Position bei Resize aktualisieren
    layeredPane.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        int fabMargin = UIScale.scale(20);
        btnFab.setLocation(layeredPane.getWidth() - FAB_SIZE - fabMargin, layeredPane.getHeight() - FAB_SIZE - fabMargin);
      }
    });

    pnl_mid.add(layeredPane, BorderLayout.CENTER);

    rootNode.removeAllChildren();
    allEntries.checkAuthors();
    tree.setEditable(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setShowsRootHandles(false);
    CustomTreeCellRenderer renderer = new CustomTreeCellRenderer();
    tree.setCellRenderer(renderer);
    tree.putClientProperty("JTree.lineStyle", "None");
    tree.setRowHeight(UIScale.scale(24));

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
          boolean isAutor = tree.getSelectionPath().getLastPathComponent().toString()
              .contains(Localization.get("tree.root"));
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
          updateSearchPlaceholder();
        }
      }

      public void mouseExited(MouseEvent e) {
        repaintTreeBound(null);
        renderer.setHoveredRow(-1);
      }

      private void showMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemAddBuch = new JMenuItem(Localization.get("contextMenu.addBook"));
        menu.add(itemAddBuch);
        menu.show(tree, e.getX(), e.getY());
        itemAddBuch.addActionListener(e6 -> {
          if (e6.getActionCommand().equals(Localization.get("contextMenu.addBook"))) {
            new Dialog_add_Booklist(Mainframe.getInstance());
          }
        });
      }

    });
    // add MouseMotionListener to track the Mouseposition
    tree.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        // get current row of Mouse Location
        int row = tree.getRowForLocation(e.getX(), e.getY());

        // refresh if row changed
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
    treeScrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));
    treeScrollPane.setBorder(BorderFactory.createEmptyBorder());
    JScrollBar treeVerticalScrollBar = treeScrollPane.getVerticalScrollBar();
    treeVerticalScrollBar.setUI(new CustomScrollBar());
    treeScrollPane.setPreferredSize(new Dimension(UIScale.scale(300), pnl_mid.getHeight()));

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, layeredPane);
    splitPane.setBorder(BorderFactory.createEmptyBorder());
    this.add(splitPane, BorderLayout.CENTER);
    this.add(panel, BorderLayout.NORTH);

    logger.info("Finished creating Tree Contents + ScrollPane. Start Update Model & show GUI");

    // Hinzufügen des WindowListeners
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        HandleConfig.writeSettings();
        logger.info("Close Database");
        Database.closeConnection();
        if (HandleConfig.backup == 2) {
          logger.info("Frame Closing: Do Backup");
          createBackup();
        } else if (HandleConfig.backup == 1) {
          logger.info("Frame Closing: ask do Backup?");
          if (JOptionPane.showConfirmDialog(null, Localization.get("q.backup"), "Backup?", JOptionPane.YES_NO_OPTION,
              JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {

            boolean ret = createBackup();
            if (ret) {
              JOptionPane.showMessageDialog(Mainframe.getInstance(), Localization.get("backup.success"));
              logger.info("Frame Closing: Backup success");
            } else {
              JOptionPane.showMessageDialog(Mainframe.getInstance(),
                  Localization.get("backup.error"));
              logger.info("Frame Closing: Backup failed");
            }
          }
        }
        logger.info("Window closing");
      }
    });

    // ComponentListener hinzufügen, um auf Größenänderungen zu reagieren
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        updateLocationAndBounds();
      }

      @Override
      public void componentMoved(ComponentEvent e) {
        updateLocationAndBounds();
      }
    });

    updateModel();
    this.setSize(defaultFrameWidth, defaultFrameHeight);
    this.setLocation(startX, startY);
    this.setVisible(visible);

    Mainframe.executor.submit(() -> checkApiConnection());
    showLastBookWithoutRating();

    listScrollPane.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
    logger.info("Init completed");
  }

  private void cleanup() {
    Mainframe.executor.submit(() -> {
      try {
        File jarFile = new File(Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File baseDir = jarFile.getParentFile();
        File file = new File(baseDir, "latest.jar");
        if (file.exists()) {
          boolean deleted = Files.deleteIfExists(file.toPath());
          if (deleted)
            logger.info("File detected and deleted: {}", file.getAbsolutePath());
          else
            logger.warn("File detected but could not be deleted: {}", file.getAbsolutePath());
        }

      } catch (IOException | URISyntaxException e) {
        logger.error(e.getMessage());
      }

    });
  }

  /**
   * Räumt Derby-Dateien auf, nachdem die SQLite-Migration erfolgreich war.
   * Wird nach der DB-Initialisierung aufgerufen, damit booklist.db bereits existiert.
   */
  private void cleanupDerbyAfterMigration() {
    File sqliteDb = new File("booklist.db");
    File derbyDir = new File("BooklistDB");
    if (sqliteDb.exists() && derbyDir.exists() && derbyDir.isDirectory()) {
      Mainframe.executor.submit(() -> {
        try {
          deleteDirectoryRecursive(derbyDir.toPath());
          logger.info("Derby-Dateien nach erfolgreicher Migration entfernt: {}", derbyDir.getAbsolutePath());

          File derbyLog = new File("derby.log");
          if (derbyLog.exists()) {
            Files.deleteIfExists(derbyLog.toPath());
            logger.info("derby.log entfernt");
          }

          SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
              Mainframe.getInstance(),
              Localization.get("update.migrationDone"),
              "Info",
              JOptionPane.INFORMATION_MESSAGE));
        } catch (IOException e) {
          logger.error("Fehler beim Aufräumen der Derby-Dateien: {}", e.getMessage());
        }
      });
    }
  }

  private static void deleteDirectoryRecursive(Path path) throws IOException {
    if (Files.isDirectory(path)) {
      try (var entries = Files.newDirectoryStream(path)) {
        for (Path entry : entries) {
          deleteDirectoryRecursive(entry);
        }
      }
    }
    Files.deleteIfExists(path);
  }

  static void updateLocationAndBounds() {
    int notifGap = UIScale.scale(8);
    int marginLeft = UIScale.scale(16);
    int marginBottom = UIScale.scale(16);
    int yOffset = marginBottom;
    for (JPanel notification : activeNotifications) {
      yOffset += notification.getHeight() + notifGap;
      int xPos = marginLeft;
      int yPos = splitPane.getHeight() - yOffset;
      notification.setLocation(xPos, yPos);
      notification.setBounds(xPos, yPos, notification.getWidth(), notification.getHeight());
    }
    listScrollPane.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
    if (btnFab != null) {
      btnFab.setLocation(layeredPane.getWidth() - FAB_SIZE - 20, layeredPane.getHeight() - FAB_SIZE - 20);
    }
    // Revalidate und Repaint sofort aufrufen
    SwingUtilities.invokeLater(() -> {
      table.revalidate();
      table.repaint();
    });
  }

  /**
   * deletes the currently selected entry from Booklist
   */
  public static void deleteBook() {
    int[] selected = table.getSelectedRows();
    for (int j : selected) {
      String searchAuthor = (String) table.getValueAt(j, 1);
      String searchTitle = (String) table.getValueAt(j, 2);
      int index = allEntries.getIndexOf(searchAuthor, searchTitle);
      if (index < 0) continue;

      int answer = JOptionPane.showConfirmDialog(null,
          MessageFormat.format(Localization.get("book.deleteQuestion"), searchAuthor, searchTitle),
          Localization.get("q.delete"),
          JOptionPane.YES_NO_OPTION);
      if (answer == JOptionPane.YES_OPTION) {
        allEntries.delete(index);
        allEntries.checkAuthors();
        showNotification(MessageFormat.format(Localization.get("book.deleted"), searchAuthor, searchTitle));
        logger.info("Book deleted: {};{}", searchAuthor, searchTitle);
      }
    }
    if (!treeSelection.isEmpty())
      search(treeSelection);
    else
      search(getLastSearch());
  }

  /**
   * updates the JTree
   */
  public static void updateNode() {
    rootNode = new DefaultMutableTreeNode(Localization.get("tree.root") + " (" + allEntries.authors.size() + ")");
    treeModel = new DefaultTreeModel(rootNode);
    for (int i = 0; i < allEntries.authors.size(); i++) {
      String autor = allEntries.authors.get(i);
      DefaultMutableTreeNode autorNode = new DefaultMutableTreeNode(autor);
      treeModel.insertNodeInto(autorNode, rootNode, i);
      if (BookListModel.authorHasSeries(autor)) {
        try {
          String[] serien = allEntries.getSeriesFromAuthor(autor);
          for (int j = 0; j < serien.length; j++) {
            DefaultMutableTreeNode serieNode = new DefaultMutableTreeNode(serien[j]);
            treeModel.insertNodeInto(serieNode, autorNode, j);
          }
        } catch (NullPointerException e) {
          logger.info("Mainframe No Series found for {}", autor);
        }

      }

    }

    treeModel = new DefaultTreeModel(rootNode);
    tree.setModel(treeModel);
    tree.revalidate();
    tree.repaint();
    Mainframe.logger.info("Mainframe Node updated");

  }

  /**
   * updates the table with current model
   */
  public static void updateModel() {
    tableDisplay = new SimpleTableModel(allEntries);
    table.setModel(tableDisplay);
    treeSelection = "";
    setTableLayout();
    if (btnSearchReset != null) btnSearchReset.setVisible(false);
    Mainframe.logger.info("Mainframe Model updated");
  }

  /**
   * copy multiple files to specified directory
   *
   * @param from - Source file or Directory
   * @param to   - Destination Directory
   */
  public static void copyFilesInDirectory(File from, File to) {
    boolean success = false;
    if (!to.exists()) {
      success = to.mkdirs();
    }
    if (success) {
      for (File file : Objects.requireNonNull(from.listFiles())) {
        File n = new File(to.getAbsolutePath() + "/" + file.getName());
        if (file.isDirectory()) {
          copyFilesInDirectory(file, n);
        } else {
          try {
            Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException e) {
            Mainframe.logger.error(e.getMessage());
          }
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
    boolean success;
    if (!to.exists()) {
      success = to.mkdirs();
    } else {
      success = true;
    }
    if (success) {
      File n = new File(to.getAbsolutePath() + "/" + file.getName());
      Files.copy(file.toPath(), n.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
  }

  /**
   * sets the JTable Column Layout
   */
  public static void setTableLayout() {
    TableColumnModel columnModel = table.getColumnModel();

    // Mindestbreite fuer Ebook/Rating anhand FontMetrics berechnen (aufloesungsunabhaengig)
    FontMetrics fm = table.getFontMetrics(table.getFont());
    int minFixedWidth = fm.stringWidth("5.0") + 20; // breitester Inhalt + Padding

    int total = columnModel.getTotalColumnWidth();
    int minProzAuthor = total * 10 / 100;
    int minProzTitle = total * 10 / 100;
    int minProzSeries = total * 10 / 100;

    for (int i = 0; i < SimpleTableModel.columnKeys.length; i++) {
      switch (SimpleTableModel.columnKeys[i]) {
        case SimpleTableModel.KEY_EBOOK -> {
          columnModel.getColumn(i).setMinWidth(minFixedWidth);
          columnModel.getColumn(i).setMaxWidth(minFixedWidth);
          columnModel.getColumn(i).setPreferredWidth(minFixedWidth);
        }
        case SimpleTableModel.KEY_AUTHOR -> {
          columnModel.getColumn(i).setMinWidth(minProzAuthor);
          columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
          columnModel.getColumn(i).setPreferredWidth(prozAuthor);
        }
        case SimpleTableModel.KEY_TITLE -> {
          columnModel.getColumn(i).setMinWidth(minProzTitle);
          columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
          columnModel.getColumn(i).setPreferredWidth(prozTitle);
        }
        case SimpleTableModel.KEY_SERIES -> {
          columnModel.getColumn(i).setMinWidth(minProzSeries);
          columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
          columnModel.getColumn(i).setPreferredWidth(prozSeries);
        }
        case SimpleTableModel.KEY_RATING -> {
          columnModel.getColumn(i).setMinWidth(minFixedWidth);
          columnModel.getColumn(i).setMaxWidth(minFixedWidth);
          columnModel.getColumn(i).setPreferredWidth(minFixedWidth);
        }
      }

    }

  }

  /**
   * shows Notification
   *
   * @param message       - Message to show on Notification
   * @param timeout       - how long should the Notification be shown in seconds
   * @param bookLinkIndex - Bookindex to reference in MouseListener. -1 for no
   *                      Link
   */
  public static customNotificationPanel showNotification(String message, int timeout, int bookLinkIndex) {
    customNotificationPanel notificationPanel = new customNotificationPanel(message, timeout);
    if (bookLinkIndex >= 0)
      notificationPanel.addBookReference(bookLinkIndex);

    Timer swingTimer = new Timer(1000, null);
    swingTimer.addActionListener(e -> {
      notificationPanel.timer -= 1;
      if (notificationPanel.timer <= 0) {
        swingTimer.stop();
        notificationPanel.startFadeOut();
      }
    });
    swingTimer.start();

    return notificationPanel;
  }

  /**
   * shows Notification with default values for bookLinkIndex
   *
   * @param message - Message to show on Notification
   */
  public static customNotificationPanel showNotification(String message, int timeout) {
    return showNotification(message, timeout, -1);
  }

  /**
   * shows Notification with default values for timeout and bookLinkIndex
   *
   * @param message - Message to show on Notification
   */
  public static customNotificationPanel showNotification(String message) {
    return showNotification(message, 5, -1);
  }

  /**
   * zeigt eine Benachrichtigung mit dem zuletzt hinzugefügten Buch ohne Rating
   *
   */
  private static void showLastBookWithoutRating() {
    Book_Booklist newestBook = null;
    Timestamp timespan = new Timestamp(System.currentTimeMillis() - 1209600033);
    for (int i = 0; i < allEntries.getSize(); i++) {
      Book_Booklist entry = allEntries.getElementAt(i);
      if (entry.getDate() == null) continue;
      if (newestBook == null) {
        if (entry.getRating() == 0 && entry.getDate().after(timespan)) {
          newestBook = entry;
        }
      } else if ((newestBook.getDate().before(entry.getDate()) && entry.getRating() == 0
          && entry.getDate().after(timespan))) {
        newestBook = entry;
      }
    }

    if (newestBook != null) {
      int index = allEntries.getIndexOf(newestBook.getAuthor(), newestBook.getTitle());
      showNotification(
          MessageFormat.format(Localization.get("book.doRating"), newestBook.getTitle(), newestBook.getAuthor()), 15,
          index);
    }

  }

  /**
   * search table entries with specified String
   *
   * @param text - search String
   */
  public static void search(String text) {
    BookListModel filteredEntries = new BookListModel(false);
    text = text.toUpperCase();
    for (int i = 0; i < allEntries.getSize(); i++) {
      Book_Booklist eintrag = allEntries.getElementAt(i);
      String autor = eintrag.getAuthor().toUpperCase();
      String titel = eintrag.getTitle().toUpperCase();
      String bemerkung = eintrag.getNote().toUpperCase();
      String leihVon = eintrag.getBorrowedFrom().toUpperCase();
      String leihAn = eintrag.getBorrowedTo().toUpperCase();
      String serie = eintrag.getSeries().toUpperCase();
      if (autor.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      } else if (titel.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      } else if (bemerkung.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      } else if (leihVon.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      } else if (leihAn.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      } else if (serie.contains(text)) {
        filteredEntries.addElement(allEntries.getElementAt(i));
      }
    }
    if (filteredEntries.getSize() > 0) {
      tableDisplay = new SimpleTableModel(filteredEntries);
      table.setModel(tableDisplay);
      setTableLayout();
      btnSearchReset.setVisible(true);
    } else {
      showNotification(Localization.get("search.error"));
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
      File jarFile = new File(Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      String filename = jarFile.getName();
      File workingDir = new File(System.getProperty("user.dir"));
      if (filename.contains(".jar")) {
        Date dt = new Date();
        long LongTime = dt.getTime();
        String StrTime = Long.toString(LongTime).substring(0, Long.toString(LongTime).length() - 3);
        File backupDir = new File(workingDir, "Backup/" + StrTime);
        copyFileToDirectory(new File(workingDir, "booklist.db"), backupDir);
        copyFileToDirectory(new File(workingDir, "config.conf"), backupDir);
        copyFileToDirectory(jarFile, backupDir);
        Mainframe.logger.info("Backup created");
        return true;
      } else {
        Mainframe.logger.error("Error while creating Backup. Could not extract filename.");
        return false;
      }
    } catch (IOException e1) {
      Mainframe.logger.error("Error while creating Backup. IOException");
      Mainframe.logger.error(e1.toString());
      return false;
    } catch (URISyntaxException e1) {
      Mainframe.logger.error("Error while creating Backup. URISyntaxException");
      Mainframe.logger.error(e1.toString());
      return false;
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
   * checks the Connection to the supplied WebAPI URL with a short GET Request
   */
  public static void checkApiConnection() {
    if (!HandleConfig.apiURL.isEmpty()) {
      try {
        Mainframe.logger.info("Web API request: {}/api/get.php", HandleConfig.apiURL);
        URL getUrl;
        getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
        HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
        con.setConnectTimeout(2000);
        con.setRequestMethod("GET");
        long startTime = System.currentTimeMillis();
        int responseCode = con.getResponseCode();
        long responseTime = System.currentTimeMillis() - startTime;
        Mainframe.logger.info("Web API request: responseCode: {}", responseCode);
        Mainframe.logger.info("Web API request: responseTime: {}", responseTime + "ms");
        if (responseCode == HttpURLConnection.HTTP_OK) {
          apiConnected = true;
          SwingUtilities.invokeLater(() -> {
            openWebApi.setEnabled(true);
            apiDownload.setEnabled(true);
            apiUpload.setEnabled(true);
          });
        }
        if (apiConnected) {
          customNotificationPanel notification = showNotification(Localization.get("api.connectedDownload"));
          boolean downloaded = downloadFromApi(false);
          notification.setText(Localization.get("api.connectedUpload"));
          uploadToApi(false);
          if (downloaded) {
            notification.setText(Localization.get("api.successNewBooks"));
          } else {
            notification.setText(Localization.get("api.successNoBooks"));
          }

        }
      } catch (MalformedURLException e) {
        Mainframe.logger.error("MalformedURLException");
        Mainframe.logger.error(e.getMessage());
      } catch (URISyntaxException e) {
        Mainframe.logger.error("URISyntaxException");
        Mainframe.logger.error(e.getMessage());
      } catch (ProtocolException e) {
        Mainframe.logger.error("ProtocolException");
        Mainframe.logger.error(e.getMessage());
      } catch (IOException e) {
        Mainframe.logger.error("Verbindung zur API fehlgeschlagen");
        apiConnected = false;
        SwingUtilities.invokeLater(() -> {
          openWebApi.setEnabled(false);
          apiDownload.setEnabled(false);
          apiUpload.setEnabled(false);
        });
        showNotification(Localization.get("api.noConnect"));
        Mainframe.logger.error(e.getMessage());
      }

    }
  }

  public static boolean isApiConnected() {
    return apiConnected;
  }

  /**
   * download the saved books via API from the webApp
   */
  private static boolean downloadFromApi(boolean showUi) {
    boolean downloaded = false;
    try {
      logger.info("Web API Download request: {}/api/get.php?token=****{}", HandleConfig.apiURL, HandleConfig.apiToken.substring(HandleConfig.apiToken.length() - 4));
      URL getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
      HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
      con.setRequestMethod("GET");
      con.setConnectTimeout(5000);
      int responseCode = con.getResponseCode();
      logger.info("Web API GET responseCode: {}", responseCode);
      if (responseCode == HttpURLConnection.HTTP_OK) {
        // API-Antwort lesen (Rohdaten)
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }

        String jsonResponse = response.toString();
        logger.info("Web API GET response: {}", jsonResponse);
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        int imported = 0;
        int rejected = 0;
        ArrayList<String> importedBooks = new ArrayList<>();
        ArrayList<String> rejectedBooks = new ArrayList<>();
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
            boolean boolEbook = Objects.equals(ebook, "1");
            if (seriesPart.equals("0"))
              seriesPart = "";

            boolean duplicate = false;
            for (int i = 0; i < allEntries.getSize(); i++) {
              if (allEntries.getElementAt(i).getAuthor().equals(author)
                  && allEntries.getElementAt(i).getTitle().equals(title) && !duplicate) {
                duplicate = true;
                rejectedBooks.add(author + " - " + title);
                rejected += 1;
              }
            }
            if (!duplicate) {
              Book_Booklist imp = new Book_Booklist(author, title, note, series, seriesPart, boolEbook, 0,
                  null, "", "", new Timestamp(System.currentTimeMillis()), true);
              importedBooks.add(imp.getAuthor() + " - " + imp.getTitle());
              imported += 1;
              SwingUtilities.invokeLater(() -> {
                Mainframe.allEntries.add(imp);
                allEntries.checkAuthors();
              });
            }

          }
        }
        in.close();
        if (rejected > 0) {
          for (String tmp : rejectedBooks) {
            showNotification(MessageFormat.format(Localization.get("api.importDeclined"), tmp), 15);
          }
        }
        if (imported >= 1) {
          for (String tmp : importedBooks) {
            showNotification(MessageFormat.format(Localization.get("api.importSuccess"), tmp), 15);
          }
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
              byte[] input = postData.getBytes(StandardCharsets.UTF_8);
              os.write(input, 0, input.length);
            }

            // Antwortcode überprüfen
            responseCode = con.getResponseCode();
            logger.info("Web API DELETE books responseCode: {}", responseCode);

            uploadToApi(showUi);
            downloaded = true;
          } catch (Exception e) {
            logger.error(e.getMessage());
          }
        } else {
          if (showUi)
            showNotification(Localization.get("api.importNoBooks"));
        }
        con.disconnect();
      } else {
        if (showUi)
          JOptionPane.showMessageDialog(Mainframe.getInstance(), "Get request failed.");
      }
    } catch (URISyntaxException | IOException e) {
      logger.error(e.getMessage());
      if (showUi)
        JOptionPane.showMessageDialog(Mainframe.getInstance(), "Error in API Call");
    }
    return downloaded;
  }

  /**
   * upload all current Books to the webApp with the corresponding Token
   */
  private static void uploadToApi(boolean showUi) {
    try {
      URL deleteUrl = new URI(HandleConfig.apiURL + "/api/deleteSynced.php").toURL();
      HttpURLConnection con = (HttpURLConnection) deleteUrl.openConnection();
      // POST-Anfrage einstellen
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      con.setDoOutput(true);
      con.setConnectTimeout(5000);

      // Der Token, der gesendet werden soll
      String postData = "token=" + HandleConfig.apiToken;

      // Daten in den OutputStream schreiben
      try (OutputStream os = con.getOutputStream()) {
        byte[] input = postData.getBytes(StandardCharsets.UTF_8);
        os.write(input, 0, input.length);
      }
      int responseCode = con.getResponseCode();
      logger.info("Web API DELETE SyncedBooks responseCode: {}", responseCode);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }

    try {
      // URL des API-Endpunkts
      logger.info("Web API request: {}/api/upload.php?token=****{}", HandleConfig.apiURL, HandleConfig.apiToken.substring(HandleConfig.apiToken.length() - 4));

      HttpURLConnection con = getHttpURLConnection();

      // Antwort vom Server lesen
      int responseCode = con.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        logger.info("Books successfully uploaded");
        if (showUi)
          showNotification(Localization.get("api.uploadSuccess"));
      } else {
        logger.error("Error while uploading: {}", responseCode);
        if (showUi)
          showNotification(MessageFormat.format(Localization.get("api.uploadError"), responseCode));
      }

      // Verbindung schließen
      con.disconnect();
    } catch (URISyntaxException | IOException e) {
      logger.error(e.getMessage());
      if (showUi)
        JOptionPane.showMessageDialog(Mainframe.getInstance(), "Error while API Upload.");
    }

  }

  private static HttpURLConnection getHttpURLConnection() throws URISyntaxException, IOException {
    URL postUrl = new URI(HandleConfig.apiURL + "/api/upload.php?token=" + HandleConfig.apiToken).toURL();

    // Verbindung aufbauen
    HttpURLConnection con = (HttpURLConnection) postUrl.openConnection();
    con.setRequestMethod("POST");
    con.setRequestProperty("Content-Type", "application/json; utf-8");
    con.setRequestProperty("Accept", "application/json");
    con.setDoOutput(true);
    con.setConnectTimeout(5000);

    // GSON-Instanz erstellen
    Gson gson = new Gson();
    JsonArray jsonArray = new JsonArray();
    // Nur die relevanten Felder in ein JSON-Array umwandeln

    for (Book_Booklist book : allEntries.getBooks()) {
      JsonObject jsonBook = new JsonObject();
      jsonBook.addProperty("bid", book.getBid());
      jsonBook.addProperty("author", book.getAuthor());
      jsonBook.addProperty("title", book.getTitle());
      jsonBook.addProperty("series", book.getSeries());
      jsonBook.addProperty("series_part", book.getSeriesVol());
      jsonBook.addProperty("ebook", book.isEbook());
      jsonBook.addProperty("rating", book.getRating());
      jsonBook.addProperty("note", book.getNote() != null ? book.getNote() : "");
      // Im loadOnDemand-Modus sind ISBN und Beschreibung noch nicht geladen
      if (HandleConfig.loadOnDemand == 1) {
        Database.loadIsbnAndDescForSync(book);
      }
      jsonBook.addProperty("isbn", book.getIsbn() != null ? book.getIsbn() : "");
      jsonBook.addProperty("description", book.getDesc() != null ? book.getDesc() : "");
      jsonBook.addProperty("date_added", book.getDate() != null ? book.getDate().toString() : "");
      if (!book.getBorrowedTo().isEmpty()) {
        jsonBook.addProperty("ausgeliehen", "an");
        jsonBook.addProperty("borrow_name", book.getBorrowedTo());
      } else if (!book.getBorrowedFrom().isEmpty()) {
        jsonBook.addProperty("ausgeliehen", "von");
        jsonBook.addProperty("borrow_name", book.getBorrowedFrom());
      } else {
        jsonBook.addProperty("ausgeliehen", "nein");
        jsonBook.addProperty("borrow_name", "");
      }
      jsonArray.add(jsonBook);

    }

    // JSON-Daten senden
    String jsonInputString = gson.toJson(jsonArray);

    try (OutputStream os = con.getOutputStream()) {
      byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
      os.write(input, 0, input.length);
    }
    return con;
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
   * Updates the placeholder text of the search field with the current book count.
   */
  public void updateSearchPlaceholder() {
    txt_search.putClientProperty("JTextField.placeholderText",
        MessageFormat.format(Localization.get("search.text"), allEntries.getSize()));
  }

  /**
   * update the jar file with the already downloaded latest.jar
   */
  /**
   * Kopiert latest.jar ueber die aktuelle JAR und startet die App neu.
   * Wird als separater Prozess ausgefuehrt (java -jar app.jar update).
   */
  public static void update() {
    try {
      File jarFile = new File(Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      File baseDir = jarFile.getParentFile();
      try (PrintWriter log = new PrintWriter(new File(baseDir, "update.log"))) {
        Thread.sleep(2000);
        File source = new File(baseDir, "latest.jar");
        log.println("UPDATER: initialize");
        log.println("UPDATER: detected fileName: " + jarFile.getAbsolutePath());
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(jarFile)) {
          byte[] buffer = new byte[8192];
          int length;
          log.println("UPDATER: overwriting file");
          while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
          }
          log.println("UPDATER: writing complete");
          ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath());
          pb.directory(baseDir);
          log.println("UPDATER: starting " + pb.command());
          pb.start();
          log.println("UPDATER: SUCCESS");
        } catch (IOException e) {
          log.println("UPDATER: ERROR - " + e.getMessage());
        }
      }
    } catch (FileNotFoundException | InterruptedException | URISyntaxException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Prueft ueber die GitHub API ob ein Update verfuegbar ist.
   * Nur wenn eine neuere Version gefunden wird, wird die JAR heruntergeladen.
   */
  private void checkUpdate() {
    Mainframe.executor.submit(() -> {
      try {
        // Version ueber GitHub API pruefen (wenige KB statt gesamte JAR)
        customNotificationPanel notification = showNotification(Localization.get("update.versionCheck"), 15);
        URL apiUrl = new URI("https://api.github.com/repos/NeXoS355/booklist/releases/latest").toURL();
        HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
        apiConn.setRequestProperty("Accept", "application/vnd.github+json");
        apiConn.setRequestMethod("GET");

        if (apiConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
          notification.setText(Localization.get("api.noConnect"));
          logger.error("Update - GitHub API returned: {}", apiConn.getResponseCode());
          return;
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(apiConn.getInputStream(), StandardCharsets.UTF_8))) {
          String line;
          while ((line = reader.readLine()) != null) {
            response.append(line);
          }
        }
        apiConn.disconnect();

        JsonObject release = JsonParser.parseString(response.toString()).getAsJsonObject();
        String latestTag = release.get("tag_name").getAsString().replaceAll("^v", "");
        logger.info("Update - current version: {}, latest version: {}", version, latestTag);

        int currentVer = parseVersion(version);
        int latestVer = parseVersion(latestTag);

        if (latestVer <= currentVer) {
          notification.setText(Localization.get("update.noUpdate"));
          logger.info("Update - no update available");
          return;
        }

        // Update verfuegbar
        notification.setText(Localization.get("update.update"));
        int answer = JOptionPane.showConfirmDialog(Mainframe.getInstance(),
            MessageFormat.format(Localization.get("q.update"), latestTag),
            "Update", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
          return;
        }

        // Backup erstellen und JAR herunterladen
        boolean backupOk = createBackup();
        if (!backupOk) {
          logger.error("Update - backup failed, aborting update");
          return;
        }

        // Download-URL und SHA-256 Digest aus den Release Assets lesen
        String downloadUrl = "https://github.com/NeXoS355/booklist/releases/latest/download/Booklist.jar";
        String expectedDigest = null;
        if (release.has("assets")) {
          var assets = release.getAsJsonArray("assets");
          for (var asset : assets) {
            JsonObject assetObj = asset.getAsJsonObject();
            String name = assetObj.get("name").getAsString();
            if (name.endsWith(".jar")) {
              downloadUrl = assetObj.get("browser_download_url").getAsString();
              if (assetObj.has("digest") && !assetObj.get("digest").isJsonNull()) {
                String digest = assetObj.get("digest").getAsString();
                if (digest.startsWith("sha256:")) {
                  expectedDigest = digest.substring(7);
                }
              }
              break;
            }
          }
        }

        URL jarUrl = new URI(downloadUrl).toURL();
        HttpURLConnection httpConn = (HttpURLConnection) jarUrl.openConnection();
        int fileSize = httpConn.getContentLength();
        logger.info("Update - downloading JAR ({} KB)", fileSize > 0 ? fileSize / 1024 : "unknown");

        File jarFile = new File(Mainframe.class.getProtectionDomain()
            .getCodeSource().getLocation().toURI());
        File baseDir = jarFile.getParentFile();

        notification.setText(Localization.get("update.download"));
        File latestJar = new File(baseDir, "latest.jar");
        try (BufferedInputStream in = new BufferedInputStream(jarUrl.openStream());
             FileOutputStream fos = new FileOutputStream(latestJar)) {
          byte[] buffer = new byte[8192];
          int bytesRead;
          int downloaded = 0;
          int oldProgress = 0;
          while ((bytesRead = in.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            downloaded += bytesRead;
            if (fileSize > 0) {
              int progress = (int) ((double) downloaded / fileSize * 100);
              if (progress > oldProgress) {
                notification.setText(Localization.get("update.download") + " " + progress + "%");
                oldProgress = progress;
              }
            }
          }
        }
        logger.info("Update - download complete");

        // SHA-256 Prüfung
        if (expectedDigest != null) {
          java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
          try (java.io.InputStream fis = new java.io.FileInputStream(latestJar)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) {
              md.update(buf, 0, n);
            }
          }
          StringBuilder sb = new StringBuilder();
          for (byte b : md.digest()) {
            sb.append(String.format("%02x", b));
          }
          String actualDigest = sb.toString();
          if (!actualDigest.equals(expectedDigest)) {
            logger.error("Update - SHA-256 mismatch: expected {}, got {}", expectedDigest, actualDigest);
            latestJar.delete();
            notification.setText(Localization.get("update.error"));
            return;
          }
          logger.info("Update - SHA-256 verified OK");
        } else {
          logger.warn("Update - no SHA-256 digest available, skipping verification");
        }

        // Update starten: aktuelles JAR durch neues ersetzen
        ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath(), "update");
        pb.directory(baseDir);
        logger.info("Update - starting: {}", pb.command());
        pb.start();
        System.exit(0);

      } catch (URISyntaxException | IOException | java.security.NoSuchAlgorithmException e) {
        showNotification("Update error. See app.log for details");
        logger.error("Update error: {}", e.getMessage());
      }
    });
  }

  /**
   * Wandelt einen Versionsstring (z.B. "3.3.0") in einen int um (330).
   */
  static int parseVersion(String ver) {
    String[] parts = ver.split("[.]");
    int result = 0;
    for (int i = 0; i < parts.length; i++) {
      result = result * 1000 + Integer.parseInt(parts[i]);
    }
    return result;
  }

  /**
   * Ruft ein GitHub Release anhand eines Tags ab.
   */
  private static JsonObject fetchReleaseByTag(String tag) throws IOException, URISyntaxException {
    URL apiUrl = new URI("https://api.github.com/repos/NeXoS355/booklist/releases/tags/" + tag).toURL();
    HttpURLConnection apiConn = (HttpURLConnection) apiUrl.openConnection();
    apiConn.setRequestProperty("Accept", "application/vnd.github+json");
    apiConn.setRequestMethod("GET");

    if (apiConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException("GitHub API returned: " + apiConn.getResponseCode());
    }

    StringBuilder response = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(apiConn.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        response.append(line);
      }
    }
    apiConn.disconnect();
    return JsonParser.parseString(response.toString()).getAsJsonObject();
  }

  /**
   * Prüft ob eine Derby-DB vorhanden ist, die noch nicht nach SQLite migriert wurde,
   * und ob die aktuelle Version den DerbyMigrator nicht mehr an Bord hat.
   * Nur dann wird die Brückenversion benötigt.
   */
  private static boolean needsDerbyMigrationBridge() {
    if (!new File("BooklistDB").exists() || new File("booklist.db").exists()) {
      return false;
    }
    try {
      Class.forName("data.DerbyMigrator");
      return false; // DerbyMigrator vorhanden, kann selbst migrieren
    } catch (ClassNotFoundException e) {
      return true; // Derby entfernt, Brückenversion nötig
    }
  }

  /**
   * Lädt die Brückenversion (4.0.x) herunter, die die Derby→SQLite Migration durchführen kann.
   * Ersetzt die aktuelle JAR und startet neu.
   */
  private static void handleMigrationBridge() {
    System.out.println("Derby-DB erkannt, SQLite-DB fehlt. Brückenversion für Migration wird geladen...");

    try {
      UIManager.setLookAndFeel(new FlatDarkLaf());
    } catch (Exception ignored) {}

    // Logger initialisieren (wird von HandleConfig.readConfig() benötigt)
    logger = LogManager.getLogger(Mainframe.class);

    // Localization initialisieren (Config ist noch nicht gelesen, daher Sprache aus config.conf lesen)
    HandleConfig.readConfig();
    if (HandleConfig.lang.equals("English")) {
      Localization.setLocale(Locale.ENGLISH);
    } else {
      Localization.setLocale(Locale.GERMAN);
    }

    int answer = JOptionPane.showConfirmDialog(null,
        MessageFormat.format(Localization.get("q.updateMigration"), MIGRATION_BRIDGE_TAG),
        "Migration",
        JOptionPane.YES_NO_OPTION);
    if (answer != JOptionPane.YES_OPTION) {
      System.out.println("Migration abgelehnt. Programm wird beendet.");
      System.exit(0);
    }

    try {
      JsonObject release = fetchReleaseByTag(MIGRATION_BRIDGE_TAG);

      String downloadUrl = null;
      if (release.has("assets")) {
        for (var asset : release.getAsJsonArray("assets")) {
          JsonObject assetObj = asset.getAsJsonObject();
          if (assetObj.get("name").getAsString().endsWith(".jar")) {
            downloadUrl = assetObj.get("browser_download_url").getAsString();
            break;
          }
        }
      }

      if (downloadUrl == null) {
        JOptionPane.showMessageDialog(null,
            Localization.get("update.error"), "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }

      File jarFile = new File(Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      File baseDir = jarFile.getParentFile();
      File latestJar = new File(baseDir, "latest.jar");

      System.out.println("Lade Brückenversion herunter: " + downloadUrl);
      URL jarUrl = new URI(downloadUrl).toURL();
      try (BufferedInputStream in = new BufferedInputStream(jarUrl.openStream());
           FileOutputStream fos = new FileOutputStream(latestJar)) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
          fos.write(buffer, 0, bytesRead);
        }
      }

      System.out.println("Download abgeschlossen. Starte Update-Prozess...");
      ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarFile.getAbsolutePath(), "update");
      pb.directory(baseDir);
      pb.start();
      System.exit(0);

    } catch (Exception e) {
      System.err.println("Fehler bei Migration-Bridge: " + e.getMessage());
      JOptionPane.showMessageDialog(null,
          Localization.get("update.error"), "Error", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }

  /**
   * start Instance of Mainframe
   *
   * @param args - Arguments to trigger different functions
   */
  public static void main(String[] args) {
    // HiDPI-Skalierung: System-DPI erkennen und FlatLaf-Skalierung setzen
    if (System.getProperty("flatlaf.uiScale") == null) {
      try {
        int dpi = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        if (dpi > 96) {
          float scale = dpi / 96f;
          scale = Math.round(scale * 4f) / 4f;
          System.setProperty("flatlaf.uiScale", String.valueOf(scale));
        }
      } catch (Exception ignored) {
        // Fallback: keine Skalierung
      }
    }

    // Default-Fonts initialisieren (werden ggf. durch Config überschrieben)
    defaultFont = new Font("Roboto", Font.PLAIN, 16);
    descFont = new Font("Roboto", Font.PLAIN, 16);

    if (args.length > 0 && "update".equals(args[0])) {
      update();
      System.exit(0);
    } else if (needsDerbyMigrationBridge()) {
      handleMigrationBridge();
    } else {
      createInstance(true);
    }
  }

  /**
   * start Mainframe for instance
   *
   * @param visible - should the Mainframe be visible?
   */
  public static void createInstance(boolean visible) {
    if (instance == null) {
      System.out.println("Creating Instance with visible=" + visible);
      instance = new Mainframe(visible);
    }
  }

  /**
   * get Mainframe instance
   *
   * @return Mainframe Object
   */
  public static Mainframe getInstance() {
    return instance;
  }

  public static String getVersion() {
    return version;
  }

}
