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
	private static WishlistTableModel anzeige;
	public static WishlistListModel Wishlisteintr�ge;
	private static JTable table = new JTable();
	Font schrift = new Font("Roboto", Font.BOLD, 16);

	public wishlist() {
		super("Wunschliste");
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(700, 700);
		this.setLocation(150, 150);

		Wishlisteintr�ge = new WishlistListModel();
		anzeige = new WishlistTableModel(Wishlisteintr�ge);

		JPanel north_panel = new JPanel();
		north_panel.setLayout(new BorderLayout(5, 5));

		north_panel.add(new JPanel(), BorderLayout.CENTER);

		JButton btn_add = new JButton();
		btn_add.setText("+");
		btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Dialog_add_Wishlist(Wishlisteintr�ge);
			}
		});
		north_panel.add(btn_add, BorderLayout.WEST);

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && SwingUtilities.isLeftMouseButton(e)) {
					String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
					String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
					int index = Wishlisteintr�ge.getIndexOf(searchAutor, searchTitel);
					new Dialog_edit_Wishlist(Wishlisteintr�ge, index);
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
				JMenuItem itemAddBook = new JMenuItem("Buch hinzuf�gen");
				JMenuItem itemDelBook = new JMenuItem("Buch l�schen");
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
						if (e.getActionCommand() == "Buch hinzuf�gen") {
							new Dialog_add_Wishlist(Wishlisteintr�ge);
						}
						updateModel();
					}
				});
				itemDelBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch l�schen") {
							deleteBuch();
							updateModel();
						}
					}
				});
				itemChanBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch bearbeiten") {
							String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
							String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
							int index = Wishlisteintr�ge.getIndexOf(searchAutor, searchTitel);
							new Dialog_edit_Wishlist(Wishlisteintr�ge, index);
							updateModel();
						}
					}
				});
				itemConvertBook.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						if (e.getActionCommand() == "Buch konvertieren") {
							String searchAutor = (String) table.getValueAt(table.getSelectedRow(), 0);
							String searchTitel = (String) table.getValueAt(table.getSelectedRow(), 1);
							int index = Wishlisteintr�ge.getIndexOf(searchAutor, searchTitel);
							Book_Wishlist wishBook = Wishlisteintr�ge.getElementAt(index);

							String autor = wishBook.getAutor();
							String titel = wishBook.getTitel();
							String serie = wishBook.getSerie();
							String vol = wishBook.getSeriePart();
							String merk = wishBook.getBemerkung();

							Image pic = null;
							String desc = "";
							String isbn = "";
							Timestamp datum = new Timestamp(System.currentTimeMillis());;
							boolean ebook = false;
							String ausgeliehen_an = "";
							String ausgeliehen_von = "";
							boolean boolAusgeliehen = false;

							Mainframe.eintr�ge.add(new Book_Booklist(autor, titel, boolAusgeliehen, ausgeliehen_an,
									ausgeliehen_von, merk, serie, vol, ebook, pic, desc, isbn, datum, true));
							Wishlisteintr�ge.delete(index);
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
					deleteBuch();
					updateModel();
				}

			}
		});

		table.setModel(anzeige);
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
		anzeige = new WishlistTableModel(Wishlisteintr�ge);
		table.setModel(anzeige);
		System.out.println("Wishlist Model updated");
	}

	public void deleteBuch() {
		int[] selected = table.getSelectedRows();
		for (int i = 0; i < selected.length; i++) {
			String searchAutor = (String) table.getValueAt(selected[i], 0);
			String searchTitel = (String) table.getValueAt(selected[i], 1);
			int index = Wishlisteintr�ge.getIndexOf(searchAutor, searchTitel);
			if (selected.length != 0) {
				int antwort = JOptionPane.showConfirmDialog(null, "Wirklich '" + searchTitel + "' l�schen?", "L�schen",
						JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					Wishlisteintr�ge.delete(index);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Es wurde kein Buch ausgew�hlt");
			}
		}
		updateModel();
	}

}
