package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;

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

import application.Book_Booklist;
import application.Book_Wishlist;
import application.WishlistListModel;
import application.WishlistTableModel;

public class wishlist extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static WishlistTableModel display;
	public static WishlistListModel wishlistEntries;
	private static JTable table = new JTable();
	Font schrift = new Font("Roboto", Font.BOLD, 16);

	public wishlist() {
		super("Wunschliste");
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(700, 700);
		this.setLocation(150, 150);

		wishlistEntries = new WishlistListModel();
		display = new WishlistTableModel(wishlistEntries);

		JPanel north_panel = new JPanel();
		north_panel.setLayout(new BorderLayout(5, 5));

		north_panel.add(new JPanel(), BorderLayout.CENTER);

		JButton btnAdd = new JButton();
		btnAdd.setText("+");
		btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 20));
		btnAdd.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_add_Wishlist(wishlistEntries);
			}
		});
		north_panel.add(btnAdd, BorderLayout.WEST);

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
					new Dialog_edit_Wishlist(wishlistEntries, index);
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

			private void showMenu(MouseEvent e) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem itemAddBook = new JMenuItem("Buch hinzufügen");
				JMenuItem itemDelBook = new JMenuItem("Buch löschen");
				JMenuItem itemChanBook = new JMenuItem("Buch bearbeiten");
				JMenuItem itemConvertBook = new JMenuItem("Buch konvertieren");
				menu.add(itemAddBook);
				menu.add(itemChanBook);
				menu.add(itemDelBook);
				menu.add(itemConvertBook);
				menu.show(table, e.getX(), e.getY());
				itemAddBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch hinzufügen") {
							new Dialog_add_Wishlist(wishlistEntries);
						}
						updateModel();
					}
				});
				itemDelBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch löschen") {
							deleteBook();
							updateModel();
						}
					}
				});
				itemChanBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch bearbeiten") {
							String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
							String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
							int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
							new Dialog_edit_Wishlist(wishlistEntries, index);
							updateModel();
						}
					}
				});
				itemConvertBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch konvertieren") {
							String searchAuthor = (String) table.getValueAt(table.getSelectedRow(), 0);
							String searchTitle = (String) table.getValueAt(table.getSelectedRow(), 1);
							int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
							Book_Wishlist wishBook = wishlistEntries.getElementAt(index);

							String author = wishBook.getAuthor();
							String title = wishBook.getTitle();
							String series = wishBook.getSeries();
							String vol = wishBook.getSeriesVol();
							String note = wishBook.getNote();

							Image pic = null;
							String desc = "";
							String isbn = "";
							Timestamp date = new Timestamp(System.currentTimeMillis());;
							boolean ebook = false;
							String borrowedTo = "";
							String borrowedFrom = "";
							boolean boolBorrowed = false;

							Mainframe.entries.add(new Book_Booklist(author, title, boolBorrowed, borrowedTo,
									borrowedFrom, note, series, vol, ebook, pic, desc, isbn, date, true));
							wishlistEntries.delete(index);
							updateModel();
							Mainframe.updateModel();
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
					updateModel();
				}

			}
		});

		table.setModel(display);
		table.setFont(schrift);
		table.setRowHeight(22);

		JPanel mid_panel = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mid_panel.add(listScrollPane, BorderLayout.CENTER);

		this.add(north_panel, BorderLayout.NORTH);
		this.add(mid_panel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

	public static void updateModel() {
		display = new WishlistTableModel(wishlistEntries);
		table.setModel(display);
		System.out.println("Wishlist Model updated");
	}

	public void deleteBook() {
		int[] selected = table.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			String searchAuthor = (String) table.getValueAt(selected[i], 0);
			String searchTitle = (String) table.getValueAt(selected[i], 1);
			int index = wishlistEntries.getIndexOf(searchAuthor, searchTitle);
			if (selected.length != 0) {
				int response = JOptionPane.showConfirmDialog(null, "Wirklich '" + searchTitle + "' löschen?", "Löschen",
						JOptionPane.YES_NO_OPTION);
				if (response == JOptionPane.YES_OPTION) {
					wishlistEntries.delete(index);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Es wurde kein Buch ausgewählt");
			}
		}
		updateModel();
	}

}
