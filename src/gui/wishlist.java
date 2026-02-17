package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.Objects;

import javax.swing.*;
import javax.swing.table.JTableHeader;

import application.Book_Booklist;
import com.formdev.flatlaf.util.UIScale;
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
	// Nutzt Mainframe.defaultFont für konsistente Skalierung

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
		this.setSize(UIScale.scale(700), UIScale.scale(700));
		this.setLocationRelativeTo(owner);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

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

		table.setModel(display);
		CustomTableCellRenderer tableRenderer = new CustomTableCellRenderer(this.getTitle());
		table.setDefaultRenderer(Object.class, tableRenderer);
		JTableHeader header = table.getTableHeader();
		CustomTableHeaderRenderer tableHeaderRenderer = new CustomTableHeaderRenderer();
		header.setDefaultRenderer(tableHeaderRenderer);
		table.setShowHorizontalLines(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		table.setFont(Mainframe.defaultFont);
		table.setRowHeight(UIScale.scale(table.getRowHeight() + 6));
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

		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listScrollPane.getViewport().setBackground(UIManager.getColor("Table.background"));
		JScrollBar verticalScrollBar = listScrollPane.getVerticalScrollBar();
		verticalScrollBar.setUI(new CustomScrollBar());

		int fabSize = UIScale.scale(48);
		JLayeredPane layeredPane = new JLayeredPane();
		layeredPane.setLayout(null);
		layeredPane.add(listScrollPane, Integer.valueOf(1));

		JButton btnFab = new JButton("+") {
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
		btnFab.setFont(Mainframe.defaultFont.deriveFont(Font.BOLD, Mainframe.defaultFont.getSize() * 1.5f));
		btnFab.setForeground(Color.WHITE);
		btnFab.setContentAreaFilled(false);
		btnFab.setOpaque(false);
		btnFab.setBorderPainted(false);
		btnFab.setFocusPainted(false);
		btnFab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnFab.setSize(fabSize, fabSize);
		btnFab.putClientProperty("JButton.buttonType", "none");
		btnFab.addActionListener(e -> {
			new Dialog_add_Wishlist(instance);
			updateModel();
		});
		layeredPane.add(btnFab, Integer.valueOf(2));

		layeredPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int margin = UIScale.scale(20);
				listScrollPane.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());
				btnFab.setLocation(layeredPane.getWidth() - fabSize - margin, layeredPane.getHeight() - fabSize - margin);
			}
		});

		Mainframe.logger.info("Wishlist: Frame created successfully");
		this.add(layeredPane, BorderLayout.CENTER);
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
