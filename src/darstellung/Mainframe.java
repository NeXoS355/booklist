package darstellung;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
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

import application.Book;
import application.BookListModel;
import application.SimpleTableModel;

public class Mainframe extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Font schrift = new Font("Roboto", Font.BOLD, 16);
	private static JTable table = new JTable();
	public static BookListModel einträge;
	private static DefaultListModel<Book> filter;
	private static SimpleTableModel anzeige;
	private static DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("rootNode");
	private static DefaultMutableTreeNode autorNode = new DefaultMutableTreeNode("AutorNode");
	private static DefaultMutableTreeNode serieNode = new DefaultMutableTreeNode("SerieNode");
	private static DefaultTreeModel treeModel;
	private static JTree tree = new JTree(treeModel);
	private JTextField txt_search;
	private static Mainframe instance;
	private static String treeSelection;
	private static String lastSearch = "";
	private String version = "Ver. 2.0.1  (12.2019)  ";

	private Mainframe() throws HeadlessException {
		super("Bücherliste");
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(1300, 1000);
//		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		URL iconURL = getClass().getResource("/resources/Liste.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		einträge = new BookListModel();
		filter = new DefaultListModel<Book>();
		BookListModel.autorenPrüfen();
		anzeige = new SimpleTableModel(einträge);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));

		txt_search = new JTextField();
		txt_search.setToolTipText("Suchtext");
		txt_search.setText("Suche ... (" + einträge.getSize() + ")");
		txt_search.setForeground(Color.gray);
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
					if (anzeige.getRowCount() == 0) {
						updateModel();
						JOptionPane.showMessageDialog(getParent(), "Keine Übereinstimmung gefunden");
					}
				}
			}
		});
		panel.add(txt_search, BorderLayout.CENTER);

		JButton btn_add = new JButton();
		btn_add.setText("+");
		btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_add(einträge, treeModel, rootNode);
				txt_search.setText("Suche ... (" + einträge.getSize() + ")");
			}
		});
		panel.add(btn_add, BorderLayout.WEST);

		JButton btn_search = new JButton("suchen");
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
				if (einträge.getSize() == 0) {
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
		JMenuItem backup = new JMenuItem("DB Backup");
		backup.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Date dt = new Date();
					Long LongTime = dt.getTime();
					String StrTime = Long.toString(LongTime).substring(0, LongTime.toString().length() - 3);
					copyFilesInDirectory(new File("MyDB"), new File("Sicherung/" + StrTime + "/MyDB"));
					copyFileToDirectory(new File("derby.log"), new File("Sicherung/" + StrTime));
				} catch (IOException e1) {
					e1.printStackTrace();
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

		menue.add(datei);
		datei.add(backup);
		datei.add(close);
		pnlMenü.add(menue, BorderLayout.WEST);

		JLabel lblVersion = new JLabel(version);
		Font newLabelFont = new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize());
		lblVersion.setFont(newLabelFont);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		pnlMenü.add(lblVersion, BorderLayout.EAST);

		table.setModel(anzeige);
		table.setFont(schrift);
		table.setRowHeight(table.getRowHeight()+6);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = einträge.getIndexOf(searchAutor, searchTitel);
					new Dialog_edit(einträge, index, treeModel, rootNode);
				}
				txt_search.setText("Suche ... (" + einträge.getSize() + ")");
				if (SwingUtilities.isRightMouseButton(e)) {
					JTable table2 = (JTable) e.getSource();
					int row = table2.rowAtPoint(e.getPoint());
					if (row > -1)
						table2.setRowSelectionInterval(row, row);
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzufügen");
				JMenuItem itemDelBuch = new JMenuItem("Buch löschen");
				JMenuItem itemChanBuch = new JMenuItem("Buch bearbeiten");
				menu.add(itemAddBuch);
				menu.add(itemChanBuch);
				menu.add(itemDelBuch);
				menu.show(table, e.getX(), e.getY());
				itemAddBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch hinzufügen") {
							new Dialog_add(einträge, treeModel, rootNode);
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
							int index = einträge.getIndexOf(searchAutor, searchTitel);
							new Dialog_edit(einträge, index, treeModel, rootNode);
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
				BookListModel.autorenPrüfen();
				txt_search.setText("Suche ... (" + einträge.getSize() + ")");
			}
		});

		JPanel pnl_mid = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnl_mid.add(listScrollPane, BorderLayout.CENTER);

		rootNode.removeAllChildren();
		BookListModel.autorenPrüfen();
		updateNode();
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setFont(schrift);
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
					System.out.println(text);
					if (isAutor) {
						updateModel();
					} else {
						search(text);
						treeSelection = text;
					}
					if (isRightClick) {
						search(text);
						showMenu(e);
					}
					if (isAutor)
						setLastSearch("");
					else
						setLastSearch(text);
					table.clearSelection();
					txt_search.setText("Suche ... (" + einträge.getSize() + ")");
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
							new Dialog_add(einträge, treeModel, rootNode);
						}
					}
				});
			}

		});
		JScrollPane treeScrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(treeScrollPane, BorderLayout.WEST);

		this.add(pnl_mid, BorderLayout.CENTER);
		this.add(panel, BorderLayout.NORTH);
		updateModel();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	public static void deleteBuch() {
		int[] selected = table.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			String searchAutor = (String) table.getValueAt(selected[i], 0);
			String searchTitel = (String) table.getValueAt(selected[i], 1);
			int index = einträge.getIndexOf(searchAutor, searchTitel);
			if (selected.length != 0) {
				int antwort = JOptionPane.showConfirmDialog(null, "Wirklich '" + searchTitel + "' löschen?", "Löschen",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					einträge.delete(index);
				}
				BookListModel.autorenPrüfen();
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
		BookListModel.seriesPrüfen();
		rootNode = new DefaultMutableTreeNode("Autoren (" + BookListModel.autoren.size() + ")");
		treeModel = new DefaultTreeModel(rootNode);
		for (int i = 0; i < BookListModel.autoren.size(); i++) {
			String autor = BookListModel.autoren.get(i);
			autorNode = new DefaultMutableTreeNode(autor);
			treeModel.insertNodeInto(autorNode, rootNode, i);
			if(BookListModel.hatAutorSerie(autor)) {
				try {
					String[] serien = BookListModel.getSerienVonAutor(autor);
					int j = 0;
					while(serien[j]!=null) {
						serieNode = new DefaultMutableTreeNode(serien[j]);
						treeModel.insertNodeInto(serieNode, autorNode, j);
						j++;
					}
				} catch(NullPointerException e) {
					System.out.println("Keine Serie gefunden zu " + autor);
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
		anzeige = new SimpleTableModel(einträge);
		table.setModel(anzeige);
		treeSelection = "";
		System.out.println("Model updated");
	}

	public static int anz_bücherAutor(String text) {
		int anz = 0;
		for (int i = 0; i < einträge.getSize(); i++) {
			Book eintrag = einträge.getElementAt(i);
			String autor = eintrag.getAutor();
			if (autor.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static int anz_bücherSerie(String text) {
		int anz = 0;
		for (int i = 0; i < einträge.getSize(); i++) {
			Book eintrag = einträge.getElementAt(i);
			String serie = eintrag.getSerie();
			if (serie.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static void search(String text) {
		System.out.println("search for: " + text);
		filter.clear();
		text = text.toUpperCase();
		for (int i = 0; i < einträge.getSize(); i++) {
			Book eintrag = einträge.getElementAt(i);
			String autor = eintrag.getAutor().toUpperCase();
			String titel = eintrag.getTitel().toUpperCase();
			String bemerkung = eintrag.getBemerkung().toUpperCase();
			String leihVon = eintrag.getAusgeliehen_von().toUpperCase();
			String leihAn = eintrag.getAusgeliehen_an().toUpperCase();
			String serie = eintrag.getSerie().toUpperCase();
			if (autor.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			} else if (titel.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			} else if (bemerkung.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			} else if (leihVon.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			} else if (leihAn.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			} else if (serie.contains(text)) {
				filter.addElement(einträge.getElementAt(i));
			}
		}
		anzeige = new SimpleTableModel(filter);
		table.setModel(anzeige);
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
