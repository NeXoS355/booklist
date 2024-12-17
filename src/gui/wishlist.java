package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import application.Book_Booklist;
import application.Book_Wishlist;
import application.HandleConfig;
import application.WishlistListModel;
import application.WishlistTableModel;

public class wishlist extends JFrame {


	@Serial
	private static final long serialVersionUID = 1L;
	private static wishlist instance;
	private static WishlistTableModel display;
	public static WishlistListModel wishlistEntries;
	private static final JTable table = new JTable();
	private static int lastHoverRow = -1;
	public static final Font defaultFont = new Font("Roboto", Font.PLAIN, 16);

	/**
	 * wishlist Constructor
	 * 
	 * @param owner - set the owner of this Frame
	 * @param visible - set the default visible state
	 */
	public wishlist(Frame owner, boolean visible) {
		super("Wishlist");
		instance = this;
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(700, 700);
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		if (HandleConfig.darkmode == 1) {
			table.getTableHeader().setOpaque(false);
			table.setBackground(Color.DARK_GRAY);
			table.getTableHeader().setBackground(Color.DARK_GRAY);
			table.getTableHeader().setForeground(Color.WHITE);
			this.getContentPane().setBackground(Color.DARK_GRAY);
		}

		Mainframe.logger.info("Wishlist: start creating Frame");

		wishlistEntries = new WishlistListModel();
		display = new WishlistTableModel(wishlistEntries);

		JPanel north_panel = new JPanel();
		north_panel.setLayout(new BorderLayout(5, 5));

		north_panel.add(new JPanel(), BorderLayout.CENTER);

		JButton btnAdd = ButtonsFactory.createButton("+");
		btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 20));
		btnAdd.addActionListener(e -> new Dialog_add_Wishlist(instance));
		north_panel.add(btnAdd, BorderLayout.WEST);

		table.setModel(display);
		CustomTableCellRenderer tableRenderer = new CustomTableCellRenderer(this.getTitle());
		table.setDefaultRenderer(Object.class, tableRenderer);
		JTableHeader header = table.getTableHeader();
		CustomTableHeaderRenderer tableHeaderRenderer = new CustomTableHeaderRenderer();
		header.setDefaultRenderer(tableHeaderRenderer);
		table.setShowHorizontalLines(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setFont(defaultFont);
		table.setRowHeight(table.getRowHeight() + 6);
		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
					Mainframe.logger.info("Clickcount: {};Wishlist open Edit Dialog ", e.getClickCount());
					new Dialog_edit_Wishlist(instance, wishlistEntries, index);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					JTable table2 = (JTable) e.getSource();
					int row = table2.rowAtPoint(e.getPoint());
					if (row > -1)
						table2.setRowSelectionInterval(row, row);
					showMenu(e);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					JTable table2 = (JTable) e.getSource();
					int row = table2.rowAtPoint(e.getPoint());
					if (row > -1)
						table2.setRowSelectionInterval(row, row);
					showMenu(e);
				}
			}

			public void mouseExited(MouseEvent e) {
				tableRenderer.clearHoveredRow();
				table.repaint();
				lastHoverRow = -1;
			}

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBook = new JMenuItem(Localization.get("contextMenu.addBook"));
				JMenuItem itemDelBook = new JMenuItem(Localization.get("contextMenu.delBook"));
				JMenuItem itemChanBook = new JMenuItem(Localization.get("contextMenu.editBook"));
				JMenuItem itemConvertBook = new JMenuItem(Localization.get("contextMenu.convertBook"));
				menu.add(itemAddBook);
				menu.add(itemChanBook);
				menu.add(itemDelBook);
				menu.add(itemConvertBook);
				menu.show(table, e.getX(), e.getY());
				itemAddBook.addActionListener(e4 -> {
                    if (Objects.equals(e4.getActionCommand(), Localization.get("contextMenu.addBook"))) {
                        new Dialog_add_Wishlist(instance);
                        Mainframe.logger.info("Menu;Wishlist open Add Dialog");
                    }
                    updateModel();
                });
				itemDelBook.addActionListener(e3 -> {
                    if (Objects.equals(e3.getActionCommand(), Localization.get("contextMenu.delBook"))) {
                        deleteBook();
                        updateModel();
                    }
                });
				itemChanBook.addActionListener(e2 -> {
                    if (Objects.equals(e2.getActionCommand(), Localization.get("contextMenu.editBook"))) {
                        String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
                        String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
                        int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
                        Mainframe.logger.info("Menu;Wishlist open Edit Dialog");
                        new Dialog_edit_Wishlist(instance, wishlistEntries, index);
                        updateModel();
                    }
                });
				itemConvertBook.addActionListener(e1 -> {
                    if (Objects.equals(e1.getActionCommand(), Localization.get("contextMenu.convertBook"))) {
                        String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
                        String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
                        int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
                        Book_Wishlist wishBook = wishlistEntries.getElementAt(index);

                        String author = wishBook.getAuthor();
                        String title = wishBook.getTitle();
                        String series = wishBook.getSeries();
                        String vol = wishBook.getSeriesVol();
                        String note = wishBook.getNote();

                        String desc = "";
                        String isbn = "";
                        Timestamp date = new Timestamp(System.currentTimeMillis());
                        boolean ebook = false;
                        String borrowedTo = "";
                        String borrowedFrom = "";
                        boolean boolBorrowed = false;
                        Mainframe.allEntries.add(new Book_Booklist(author, title, boolBorrowed, borrowedTo,
                                borrowedFrom, note, series, vol, ebook, 0, null, desc, isbn, date, true));
                        wishlistEntries.delete(index);
                        updateModel();
                        Mainframe.updateModel();
                        Mainframe.logger.info("Menu;Wishlist Book converted;{},{}", author ,title);
                    }
                });
			}

		});

		table.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					deleteBook();
					updateModel();
				}

			}
		});
		table.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {

				JTable table2 = (JTable) e.getSource();
				int row = table2.rowAtPoint(e.getPoint());
				if (lastHoverRow != row) {
					tableRenderer.setHoveredRow(row);
					table.repaint();
					lastHoverRow = row;
				}
			}
		});

		JPanel mid_panel = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		if (HandleConfig.darkmode == 1)
			listScrollPane.getViewport().setBackground(new Color(75, 75, 75));
		mid_panel.add(listScrollPane, BorderLayout.CENTER);

		Mainframe.logger.info("Wishlist: Frame created successfully");
		this.add(north_panel, BorderLayout.NORTH);
		this.add(mid_panel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(visible);

	}

	/**
	 * updates the table Model to paint the changes
	 * 
	 */
	public static void updateModel() {
		display = new WishlistTableModel(wishlistEntries);
		table.setModel(display);
		Mainframe.logger.info("Wishlist Model updated");
	}

	/**
	 * deletes a book from the wishlist depending on the currently selected entry
	 * 
	 */
	public void deleteBook() {
		int[] selected = table.getSelectedRows();
        for (int j : selected) {
            String searchAuthor = (String) table.getValueAt(j, 0);
            String searchTitle = (String) table.getValueAt(j, 1);
            int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
            int response = JOptionPane.showConfirmDialog(this, Localization.get("book.deleteQuestion"), Localization.get("text.delete"),
                    JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                wishlistEntries.delete(index);
            }
            Mainframe.logger.info("Wishlist Book deleted: {};{}", searchAuthor, searchTitle);
        }
		updateModel();
	}

}
