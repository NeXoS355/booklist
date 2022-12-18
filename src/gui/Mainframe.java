package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import application.BookListModel;
import application.Book_Booklist;
import application.SimpleTableModel;
import data.Database;

public class Mainframe extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	Font schrift = new Font("Roboto", Font.BOLD, 16);
	private static JTable table = new JTable();
	public static BookListModel eintr�ge;
	private static DefaultListModel<Book_Booklist> filter;
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
	private String version = "Ver. 2.2.4  (12.2022)  ";
	private int pressedVersionCount = 0;

	private Mainframe() throws HeadlessException {
		super("B�cherliste");
		
		this.setLayout(new BorderLayout(10, 10));
		this.setLocation(100, 100);
		this.setSize(1300, 1000);
		URL iconURL = getClass().getResource("/resources/Icon.png");
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		this.setIconImage(icon.getImage());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		eintr�ge = new BookListModel();
		filter = new DefaultListModel<Book_Booklist>();
		anzeige = new SimpleTableModel(eintr�ge);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout(5, 5));

		txt_search = new JTextField();
		txt_search.setToolTipText("Suchtext");
		txt_search.setText("Suche ... (" + eintr�ge.getSize() + ")");
		txt_search.setForeground(Color.gray);
		txt_search.setFont(schrift);
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
						JOptionPane.showMessageDialog(getParent(), "Keine �bereinstimmung gefunden");
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
				new Dialog_add_Booklist(eintr�ge, treeModel, rootNode);
				txt_search.setText("Suche ... (" + eintr�ge.getSize() + ")");
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
				if (eintr�ge.getSize() == 0) {
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
					JOptionPane.showMessageDialog(getParent(), "Backup erfolgreich.");
					
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(getParent(), "Backup fehlgeschlagen.");
				}
			}
		});
		JMenuItem close = new JMenuItem("Schlie�en");
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
				JOptionPane.showMessageDialog(null, "Apache Derby 10.16.1.1 (May 19, 2022)");
			}
		});

		menue.add(datei);
		datei.add(wishlist);
		datei.add(backup);
		datei.add(dbVersion);
		datei.add(close);
		pnlMen�.add(menue, BorderLayout.WEST);

		JLabel lblVersion = new JLabel(version);
		Font newLabelFont = new Font(lblVersion.getFont().getName(), Font.BOLD, lblVersion.getFont().getSize());
		lblVersion.setFont(newLabelFont);
		lblVersion.setHorizontalAlignment(SwingConstants.RIGHT);
		lblVersion.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseExited(MouseEvent e) {
				pressedVersionCount = 0;
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				pressedVersionCount = 0;
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				pressedVersionCount++;
				if (pressedVersionCount == 5) {
					JOptionPane.showMessageDialog(null, Database.readCurrentLayoutVersion());
				}
			}
		});
		pnlMen�.add(lblVersion, BorderLayout.EAST);
		
		table.setModel(anzeige);
		table.setFont(schrift);
		table.setShowVerticalLines(false);
		table.setSelectionBackground(Color.LIGHT_GRAY);
		table.setRowHeight(table.getRowHeight()+6);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = eintr�ge.getIndexOf(searchAutor, searchTitel);
					new Dialog_edit_Booklist(eintr�ge, index, treeModel, rootNode);
				}
				txt_search.setText("Suche ... (" + eintr�ge.getSize() + ")");
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
				JMenuItem itemAddBuch = new JMenuItem("Buch hinzuf�gen");
				JMenuItem itemDelBuch = new JMenuItem("Buch l�schen");
				JMenuItem itemChanBuch = new JMenuItem("Buch bearbeiten");
				menu.add(itemAddBuch);
				menu.add(itemChanBuch);
				menu.add(itemDelBuch);
				menu.show(table, e.getX(), e.getY());
				itemAddBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch hinzuf�gen") {
							new Dialog_add_Booklist(eintr�ge, treeModel, rootNode);
						}
					}
				});
				itemDelBuch.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch l�schen") {
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
							int index = eintr�ge.getIndexOf(searchAutor, searchTitel);
							new Dialog_edit_Booklist(eintr�ge, index, treeModel, rootNode);
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
				BookListModel.autorenPr�fen();
				txt_search.setText("Suche ... (" + eintr�ge.getSize() + ")");
			}
		});

		JPanel pnl_mid = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnl_mid.add(listScrollPane,BorderLayout.CENTER);

		rootNode.removeAllChildren();
		BookListModel.autorenPr�fen();
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
					txt_search.setText("Suche ... (" + eintr�ge.getSize() + ")");
				}
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
							new Dialog_add_Booklist(eintr�ge, treeModel, rootNode);
						}
					}
				});
			}

		});
		JScrollPane treeScrollPane = new JScrollPane(tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		treeScrollPane.setPreferredSize(new Dimension(300, pnl_mid.getHeight()));
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treeScrollPane,listScrollPane);
		this.add(splitPane, BorderLayout.CENTER);
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
			int index = eintr�ge.getIndexOf(searchAutor, searchTitel);
			if (selected.length != 0) {
				int antwort = JOptionPane.showConfirmDialog(null, "Wirklich '" + searchAutor + " - " + searchTitel + "' l�schen?", "L�schen",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					eintr�ge.delete(index);
				}
				BookListModel.autorenPr�fen();
			} else {
				JOptionPane.showMessageDialog(null, "Es wurde kein Buch ausgew�hlt");
			}
		}
		if (treeSelection != "")
			search(treeSelection);
		else
			search(getLastSearch());
		updateModel();
	}

	public static void updateNode() {
		rootNode = new DefaultMutableTreeNode("Autoren (" + BookListModel.autoren.size() + ")");
		treeModel = new DefaultTreeModel(rootNode);
		for (int i = 0; i < BookListModel.autoren.size(); i++) {
			String autor = BookListModel.autoren.get(i);
			autorNode = new DefaultMutableTreeNode(autor);
			treeModel.insertNodeInto(autorNode, rootNode, i);
			if(BookListModel.hatAutorSerie(autor)) {
				try {
					String[] serien = BookListModel.getSerienVonAutor(autor);
					for (int j = 0;j < serien.length;j++) {
						serieNode = new DefaultMutableTreeNode(serien[j]);
						treeModel.insertNodeInto(serieNode, autorNode, j);
					}
				} catch(NullPointerException e) {
					System.out.println("Mainframe Keine Serie gefunden zu " + autor);
				}
 
			}

		}

		treeModel = new DefaultTreeModel(rootNode);
		tree.setModel(treeModel);
		tree.revalidate();
		tree.repaint();
		System.out.println("Mainframe Node updated");
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
		anzeige = new SimpleTableModel(eintr�ge);
		table.setModel(anzeige);
		treeSelection = "";
		System.out.println("Mainframe Model updated");
	}

	public static int anz_b�cherAutor(String text) {
		int anz = 0;
		for (int i = 0; i < eintr�ge.getSize(); i++) {
			Book_Booklist eintrag = eintr�ge.getElementAt(i);
			String autor = eintrag.getAutor();
			if (autor.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static int anz_b�cherSerie(String text) {
		int anz = 0;
		for (int i = 0; i < eintr�ge.getSize(); i++) {
			Book_Booklist eintrag = eintr�ge.getElementAt(i);
			String serie = eintrag.getSerie();
			if (serie.equals(text))
				anz += 1;
		}
		return anz;
	}

	public static void search(String text) {
		System.out.println("Mainframe search for: " + text);
		filter.clear();
		text = text.toUpperCase();
		for (int i = 0; i < eintr�ge.getSize(); i++) {
			Book_Booklist eintrag = eintr�ge.getElementAt(i);
			String autor = eintrag.getAutor().toUpperCase();
			String titel = eintrag.getTitel().toUpperCase();
			String bemerkung = eintrag.getBemerkung().toUpperCase();
			String leihVon = eintrag.getAusgeliehen_von().toUpperCase();
			String leihAn = eintrag.getAusgeliehen_an().toUpperCase();
			String serie = eintrag.getSerie().toUpperCase();
			if (autor.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			} else if (titel.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			} else if (bemerkung.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			} else if (leihVon.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			} else if (leihAn.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			} else if (serie.contains(text)) {
				filter.addElement(eintr�ge.getElementAt(i));
			}
		}
		if (filter.getSize()>0) {
		anzeige = new SimpleTableModel(filter);
		table.setModel(anzeige);
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
