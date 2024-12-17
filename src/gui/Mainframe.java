package gui;

import application.BookListModel;
import application.Book_Booklist;
import application.HandleConfig;
import application.SimpleTableModel;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
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

    public static Font defaultFont = new Font("Roboto", Font.PLAIN, 16);
    public static Font descFont = new Font("Roboto", Font.PLAIN, 16);
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
    private static final JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//    private static Timer animationTimer;
    private static Mainframe instance;
    private static String treeSelection;
    private static String lastSearch = "";
    private static boolean apiConnected = false;
    private static wishlist wishlist_instance;
    private static JMenuItem openWebApi;
    private static JMenuItem apiDownload;
    private static JMenuItem apiUpload;
    private static String version;
    private JTextField txt_search;
    public static int defaultFrameWidth = 1300;
    public static int defaultFrameHeight = 800;
    public static int startX = 150;
    public static int startY = 150;

    public static final Color darkmodeBackgroundColor = new Color(32,32,32);
    public static final Color darkmodeAccentColor = new Color(70,73,75);

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
        assert iconURL != null;
        ImageIcon icon = new ImageIcon(iconURL);
        this.setIconImage(icon.getImage());

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

        cleanup();
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

        try {
//            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel(new FlatLightLaf());
            if (HandleConfig.darkmode == 1) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
                // Change Colors for Darkmode
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("Panel.background", darkmodeBackgroundColor);
                UIManager.put("ScrollPane.background", darkmodeBackgroundColor);
                UIManager.put("SplitPane.background", darkmodeBackgroundColor);
                this.getContentPane().setBackground(darkmodeBackgroundColor);
                tree.setBackground(darkmodeBackgroundColor);
            } else {
                UIManager.put("TextArea.inactiveForeground", Color.BLACK);
            }
        } catch (UnsupportedLookAndFeelException e) {
            logger.error(e.getMessage());
        }

        logger.info("Finished create Frame & readConfig. Start creating Lists and readDB");
        allEntries = new BookListModel(true);
        tableDisplay = new SimpleTableModel(allEntries);

        logger.info("Finished creating List & DB. Start creating GUI Components");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 5));

        txt_search = new CustomTextField();
        txt_search.setToolTipText("Suchtext");
        txt_search.setText(MessageFormat.format(Localization.get("search.text"),allEntries.getSize()));
        setSearchTextColorActive(false);
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
                        JOptionPane.showMessageDialog(Mainframe.getInstance(), "Keine übereinstimmung gefunden");
                    }
                }
            }
        });
        txt_search.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                setSearchTextColorActive(false);
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (txt_search.getText().contains(Localization.get("search.shortText")))
                    txt_search.setText("");
                setSearchTextColorActive(true);
            }
        });
        panel.add(txt_search, BorderLayout.CENTER);

        JButton btn_add = ButtonsFactory.createButton("+");
        btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
        btn_add.addActionListener(e -> {
            new Dialog_add_Booklist(Mainframe.getInstance());
            txt_search.setText(MessageFormat.format(Localization.get("search.text"),allEntries.getSize()));
        });

        panel.add(btn_add, BorderLayout.WEST);

        JButton btn_search = ButtonsFactory.createButton(Localization.get("search.button"));
        btn_search.setFont(btn_search.getFont().deriveFont(Font.BOLD, 13));
        btn_search.addActionListener(e -> {
            search(txt_search.getText());
            setSearchTextColorActive(false);
            tree.clearSelection();
            setLastSearch(txt_search.getText());
            if (allEntries.getSize() == 0) {
                updateModel();
                JOptionPane.showMessageDialog(Mainframe.getInstance(), Localization.get("search.error"));
            }
        });

        panel.add(btn_search, BorderLayout.EAST);

        JPanel pnlMenu = new JPanel();
        pnlMenu.setLayout(new BorderLayout());
        panel.add(pnlMenu, BorderLayout.NORTH);

        JMenuBar menue = new JMenuBar();
        JMenu datei = new JMenu(Localization.get("menu.file"));
        JMenu extras = new JMenu(Localization.get("menu.extras"));
        JMenu hilfe = new JMenu(Localization.get("menu.help"));

        JMenuItem backup = new JMenuItem("DB Backup");
        backup.addActionListener(e -> {
            boolean ret = createBackup();
            if (ret)
                JOptionPane.showMessageDialog(Mainframe.getInstance(), Localization.get("backup.success"));
            else
                JOptionPane.showMessageDialog(Mainframe.getInstance(),
                        Localization.get("backup.error"));
        });
        JMenuItem close = new JMenuItem(Localization.get("menu.close"));
        close.addActionListener(e -> dispose());
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
                    + javaVersion + "\nApache Derby Version: " + Database.readCurrentDBVersion();
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
            Desktop desktop = Desktop.getDesktop();
            URI oURL;
            try {
                oURL = new URI(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);
                desktop.browse(oURL);
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
        extras.add(ExcelExport);
        extras.add(backup);
        extras.add(wishlist);
        extras.add(apiDownload);
        extras.add(apiUpload);
        extras.add(openWebApi);
        extras.add(info);
        hilfe.add(update);
        hilfe.add(about);
        pnlMenu.add(menue, BorderLayout.WEST);

        JLabel lblVersion = new JLabel(Localization.get("text.version") +": " + version);

        lblVersion.setFont(new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize()));
        lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
        pnlMenu.add(lblVersion, BorderLayout.EAST);
        pnlMenu.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        logger.info("Finished creating GUI Components. Start creating Table Contents");

        table.setModel(tableDisplay);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
        CustomTableCellRenderer tableRenderer = new CustomTableCellRenderer(this.getTitle());
        table.setDefaultRenderer(Object.class, tableRenderer);
        CustomTableHeaderRenderer tableHeaderRenderer = new CustomTableHeaderRenderer();
        JTableHeader header = table.getTableHeader();
        header.setDefaultRenderer(tableHeaderRenderer);
        table.setFont(defaultFont);
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
                    int index = allEntries.getIndexOf(searchAutor, searchTitel);
                    new Dialog_edit_Booklist(Mainframe.getInstance(), allEntries, index, treeModel);
                }
                txt_search.setText(MessageFormat.format(Localization.get("search.text"),allEntries.getSize()));
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
                txt_search.setText(MessageFormat.format(Localization.get("search.text"),allEntries.getSize()));
            }
        });
        logger.info("Start creating Tree Contents + ScrollPane");

        JPanel pnl_mid = new JPanel(new BorderLayout());

        if (HandleConfig.darkmode == 1)
            listScrollPane.getViewport().setBackground(new Color(75, 75, 75));
        JScrollBar tableVerticalScrollBar = listScrollPane.getVerticalScrollBar();
        tableVerticalScrollBar.setUI(new CustomScrollBar());
        layeredPane.setLayout(null);
        layeredPane.add(listScrollPane, Integer.valueOf(1));       // Die Tabelle im unteren Layer

        pnl_mid.add(layeredPane, BorderLayout.CENTER);

        rootNode.removeAllChildren();
        allEntries.checkAuthors();
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
                    boolean isAutor = tree.getSelectionPath().getLastPathComponent().toString().contains(Localization.get("tree.root"));
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
                    txt_search.setText(MessageFormat.format(Localization.get("search.text"),allEntries.getSize()));
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
        if (HandleConfig.darkmode == 1)
            treeScrollPane.getViewport().setBackground(new Color(75, 75, 75));
        JScrollBar treeVerticalScrollBar = treeScrollPane.getVerticalScrollBar();
        treeVerticalScrollBar.setUI(new CustomScrollBar());
        treeScrollPane.setPreferredSize(new Dimension(300, pnl_mid.getHeight()));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, layeredPane);
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
                        }}
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
        this.setLocation(startX,startY);
        this.setVisible(visible);

        checkApiConnection();
        showLastBookWithoutRating();

        listScrollPane.setBounds(0,0, layeredPane.getWidth(), layeredPane.getHeight());
        logger.info("Init completed");
    }

    private void cleanup() {
        Mainframe.executor.submit(() -> {
            try {
                File file = new File("latest.jar");
                if (file.exists()) {
                    Path path = Paths.get("latest.jar");
                    boolean deleted = Files.deleteIfExists(path);
                    if (deleted)
                        logger.info("File detected and deleted: {}", path);
                    else
                        logger.warn("File detected but could not be deleted: {}", path);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }

        });
    }

    private static void updateLocationAndBounds() {
        for (JPanel notification : activeNotifications) {
            int index = activeNotifications.indexOf(notification) + 1;
            notification.setLocation(0, splitPane.getHeight() - index*30 - index*5);
        }
        listScrollPane.setBounds(0,0, layeredPane.getWidth(), layeredPane.getHeight());
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

            int answer = JOptionPane.showConfirmDialog(null,
                    MessageFormat.format(Localization.get("book.deleteQuestion"),searchAuthor,searchTitle), Localization.get("q.delete"),
                    JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                allEntries.delete(index);
            }
            allEntries.checkAuthors();
            showNotification(MessageFormat.format(Localization.get("book.deleted"),searchAuthor,searchTitle));
            logger.info("Book deleted: {};{}", searchAuthor, searchTitle);
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
        boolean success = false;
        if (!to.exists()) {
            success = to.mkdirs();
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

        int total = columnModel.getTotalColumnWidth();
        int minProzEbook = total * 5 / 100;
        int minProzAuthor = total * 10 / 100;
        int minProzTitle = total * 10 / 100;
        int minProzSeries = total * 10 / 100;
        int minProzRating = total * 5 / 100;

        for (int i = 0; i < SimpleTableModel.columnNames.length; i++) {
            switch (SimpleTableModel.columnNames[i]) {
                case "E-Book" -> {
                    columnModel.getColumn(i).setMinWidth(minProzEbook);
                    columnModel.getColumn(i).setMaxWidth(50);
                    columnModel.getColumn(i).setPreferredWidth(prozEbook);
                }
                case "Autor" -> {
                    columnModel.getColumn(i).setMinWidth(minProzAuthor);
                    columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
                    columnModel.getColumn(i).setPreferredWidth(prozAuthor);
                }
                case "Titel" -> {
                    columnModel.getColumn(i).setMinWidth(minProzTitle);
                    columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
                    columnModel.getColumn(i).setPreferredWidth(prozTitle);
                }
                case "Serie" -> {
                    columnModel.getColumn(i).setMinWidth(minProzSeries);
                    columnModel.getColumn(i).setMaxWidth(Integer.MAX_VALUE);
                    columnModel.getColumn(i).setPreferredWidth(prozSeries);
                }
                case "Rating" -> {
                    columnModel.getColumn(i).setMinWidth(minProzRating);
                    columnModel.getColumn(i).setMaxWidth(50);
                    columnModel.getColumn(i).setPreferredWidth(prozRating);
                }
            }

        }

    }

    /**
     * shows Notification
     *
     * @param message       - Message to show on Notification
     * @param timeout       - how long should the Notification be shown in seconds
     * @param bookLinkIndex - Bookindex to reference in MouseListener. -1 for no Link
     */
    public static customNotificationPanel showNotification(String message, int timeout, int bookLinkIndex) {
        // Benachrichtigungsleiste erstellen
        customNotificationPanel notificationPanel = new customNotificationPanel(message, timeout);
        // Rufe den Callback auf (im Event-Dispatch-Thread für GUI-Sicherheit)

        notificationPanel.setLocation(0, splitPane.getHeight() - ((activeNotifications.size()) * 30) - ((activeNotifications.size()) * 5));
        if (bookLinkIndex >= 0)
            notificationPanel.addBookReference(bookLinkIndex);
        notificationPanel.repaint();
        Mainframe.executor.submit(() -> {
            try {
//                    animate(true);
                while (notificationPanel.timer > 0) {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                    notificationPanel.timer -= 1;
                }

//                    animate(false);
                SwingUtilities.invokeLater(() -> {
                    notificationPanel.setVisible(false);
                    activeNotifications.remove(notificationPanel);
                    updateLocationAndBounds();
                });
            } catch (InterruptedException e) {
                activeNotifications.remove(notificationPanel);
                updateLocationAndBounds();
                throw new RuntimeException(e);
            }

        });
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
        Timestamp timespan = new Timestamp(System.currentTimeMillis()-1209600033);
        for (int i = 0; i < allEntries.getSize(); i++) {
            Book_Booklist entry = allEntries.getElementAt(i);
                if (newestBook == null) {
                    if (entry.getRating() == 0 && entry.getDate().after(timespan)) {
                        newestBook = entry;
                    }
                } else if ((newestBook.getDate().before(entry.getDate()) && entry.getRating() == 0 && entry.getDate().after(timespan))) {
                    newestBook = entry;
                }
            }

        if (newestBook != null) {
            int index = allEntries.getIndexOf(newestBook.getAuthor(),newestBook.getTitle());
            showNotification(MessageFormat.format(Localization.get("book.doRating"),newestBook.getTitle(),newestBook.getAuthor()) , 15, index);
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
            String filepath = Mainframe.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String[] fileArray = filepath.split("/");
            String filename = fileArray[fileArray.length - 1];
            if (filename.contains(".jar")) {
                Date dt = new Date();
                long LongTime = dt.getTime();
                String StrTime = Long.toString(LongTime).substring(0, Long.toString(LongTime).length() - 3);
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
                    openWebApi.setEnabled(true);
                    apiDownload.setEnabled(true);
                    apiUpload.setEnabled(true);
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
                openWebApi.setEnabled(false);
                apiDownload.setEnabled(false);
                apiUpload.setEnabled(false);
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
            logger.info("Web API Download request: {}/api/get.php?token={}", HandleConfig.apiURL, HandleConfig.apiToken);
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
                            Mainframe.allEntries.add(imp);
                            allEntries.checkAuthors();
                        }

                    }
                }
                in.close();
                if (rejected > 0) {
                        for (String tmp : rejectedBooks) {
                            showNotification(MessageFormat.format(Localization.get("api.importDeclined"),tmp), 15);
                        }
                }
                if (imported >= 1 || rejected > 0) {
                        for(String tmp : importedBooks) {
                            showNotification(MessageFormat.format(Localization.get("api.importSuccess"),tmp),15);
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

                        if (imported >= 1) {
                            uploadToApi(showUi);
                            downloaded = true;
                        }
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
            deleteUrl.openConnection();
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
            logger.info("Web API request: {}/api/upload.php?token={}", HandleConfig.apiURL, HandleConfig.apiToken);

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
                    showNotification(MessageFormat.format(Localization.get("api.uploadError"),responseCode));
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
     * set Active Color of search TextField
     */
    public void setSearchTextColorActive(boolean value) {
        if (value) {
            if (HandleConfig.darkmode == 1) {
                txt_search.setForeground(Color.WHITE);
                txt_search.setCaretColor(Color.WHITE);
            } else {
                txt_search.setForeground(Color.BLACK);
            }
        } else {
            txt_search.setForeground(Color.GRAY);
        }
    }

    /**
     * update the jar file with the already downloaded latest.jar
     */
    public static void update() {
        String fileName = new File(
                Mainframe.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
        try (PrintWriter out = new PrintWriter("update.log")) {
            System.out.println("For more Information please see update.log");
            Thread.sleep(2000);
            File source = new File("latest.jar");
            File dest = new File(fileName);
            InputStream is = null;
            OutputStream os = null;
            out.println("UPDATER: initialize");
            out.println("UPDATER: detected fileName: " + fileName);
            try {
                is = new FileInputStream(source);
                os = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int length;
                out.println("UPDATER: overwriting file");
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                out.println("UPDATER: writing complete");
                out.println("UPDATER: build process");
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", fileName);
                out.println("UPDATER: " + pb.command());
                pb.start();
                out.println("UPDATER: process started");
                out.println("UPDATER: SUCCESS");
            } catch (IOException e) {
                out.println(e.getMessage());
            } finally {
                try {
                    assert is != null;
                    is.close();
                    assert os != null;
                    os.close();
                } catch (IOException e) {
                    Mainframe.logger.error(e.getMessage());
                }

            }
            out.println("UPDATER: update finished");
        } catch (FileNotFoundException | InterruptedException e1) {
            Mainframe.logger.error(e1.getMessage());
        }

    }

    /**
     * downloads the latest Release jar and checks the versions. If there is a new
     * version. Update is started immediately
     */
    private void checkUpdate() {
        Mainframe.executor.submit(() -> {
            URL url;
            try {
                url = new URI("https://github.com/NeXoS355/booklist/releases/latest/download/Booklist.jar").toURL();
                // Initialisiere HTTP-Verbindung, um Content-Length zu erhalten
                HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                int contentLength = httpConnection.getContentLength();
                int fileSize = 0;
                if (contentLength > -1) {
                    fileSize = contentLength/1000;
                    System.out.println("Größe der Datei: " + contentLength/1000 + " KB");
                }
                customNotificationPanel notification1 = showNotification(Localization.get("update.download"), 10);
                try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream("latest.jar")) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    int downloadedBytes = 0;
                    int progress;
                    int old_progress = 0;
                    System.out.println("Start reading latest.jar");
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        downloadedBytes += bytesRead;
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        // Fortschritt berechnen und ausgeben
                        progress = (int) ((((double) downloadedBytes /1000) / (double) fileSize) * 100);
                        if (progress > old_progress)
                            notification1.setText(Localization.get("update.download") + " " + progress + "%");
                        old_progress = progress;
                    }
                    System.out.println("Finished reading latest.jar");
                    fileOutputStream.close();
                    notification1.setText(Localization.get("update.versionCheck"));
                    System.out.println("create Process 'latest.jar version'");
                    ProcessBuilder pb = new ProcessBuilder("java", "-jar", "latest.jar", "version");
                    logger.info("Update - Command: {}", pb.command());
                    Process proc = pb.start();
                    // Warte darauf, dass der Prozess abgeschlossen wird
                    int exitCode = proc.waitFor();
                    notification1.setText(Localization.get("update.versionCheckFin"));
                    logger.info("Update - Process closed with Exit-Code: {}", exitCode);

                    // InputStream lesen (kontinuierlich statt mit available())
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                         BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {

                        // Ausgabe des Prozesses lesen
                        String line;
                        while ((line = reader.readLine()) != null) {
                            logger.info("Update - detected version: {}", line);
                            StringBuilder strCurVer = new StringBuilder();
                            int intCurVer;
                            String[] splitString = version.split("[.]");
                            for (String s : splitString) {
                                strCurVer.append(s);
                            }
                            intCurVer = Integer.parseInt(strCurVer.toString());

                            StringBuilder strDownloadedVer = new StringBuilder();
                            int intDownloadedVer;
                            splitString = line.split("[.]");
                            for (String s : splitString) {
                                strDownloadedVer.append(s);
                            }
                            intDownloadedVer = Integer.parseInt(strDownloadedVer.toString());

                            if (intDownloadedVer > intCurVer) {
                                notification1.setText(Localization.get("update.update"));
                                int antwort = JOptionPane.showConfirmDialog(Mainframe.getInstance(),
                                        MessageFormat.format(Localization.get("q.update"),line),
                                        "Update", JOptionPane.YES_NO_OPTION);
                                if (antwort == JOptionPane.YES_OPTION) {
                                    boolean ret = createBackup();
                                    if (ret) {
                                        String fileName = new File(Mainframe.class.getProtectionDomain()
                                                .getCodeSource().getLocation().getPath()).getName();
                                        pb = new ProcessBuilder("java", "-jar", fileName, "update");
                                        logger.info("Update - Command: {}", pb.command());
                                        pb.start();
                                        logger.info("Update - Process started");
                                        System.exit(0);
                                    }
                                }

                            } else {
                                notification1.setText(Localization.get("update.noUpdate"));
                                cleanup();
                            }
                        }

                        // Fehlerausgabe lesen (falls vorhanden)
                        while ((line = errorReader.readLine()) != null) {
                            logger.error("Update - Error: {}", line);
                        }
                    }
                } catch (IOException | InterruptedException e1) {
                    showNotification("Update error. See app.log for details");
                    logger.error(e1.getMessage());
                }
            } catch (URISyntaxException | IOException e1) {
                showNotification("Update error. See app.log for details");
                logger.error(e1.getMessage());
            }

        });
    }

    /**
     * start Instance of Mainframe
     *
     * @param args - Arguments to trigger different functions
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            for (String s : args) {
                switch (s) {
                    case "version":
                        new Mainframe();
                        System.out.println(version);
                        System.exit(0);
                        break;
                    case "update":
                        update();
                        System.exit(0);
                    default:
                        System.out.println("argument not recognized. Exiting.");
                        System.exit(0);
                }
            }
        } else {
            createInstance(true);
        }
    }

    /**
     * start Mainframe for instance
     *
     * @param visible  - should the Mainframe be visible?
     */
    public static void createInstance(boolean visible) {
        if (instance == null) {
            System.out.println("creating Instance with visible=" + visible);
            instance = new Mainframe(visible);
        }
    }

    /**
     * get Mainframe  instance
     *
     * @return Mainframe Object
     */
    public static Mainframe getInstance() {
        return instance;
    }

}
