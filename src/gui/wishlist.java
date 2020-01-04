package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import application.WishlistListModel;
import application.WishlistTableModel;



public class wishlist extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static WishlistTableModel anzeige;
	private static WishlistListModel einträge;
	private static JTable table = new JTable();
	Font schrift = new Font("Roboto", Font.BOLD, 16);
	
	wishlist() {
		super("Wunschliste");
		this.setLayout(new BorderLayout(10, 10));
		this.setSize(700, 700);
		
		einträge = new WishlistListModel();
		anzeige = new WishlistTableModel(einträge);
		
		JPanel north_panel = new JPanel();
		north_panel.setLayout(new BorderLayout(5, 5));
		
		north_panel.add(new JPanel(),BorderLayout.CENTER);
		
		JButton btn_add = new JButton();
		btn_add.setText("+");
		btn_add.setFont(btn_add.getFont().deriveFont(Font.BOLD, 20));
		btn_add.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
		north_panel.add(btn_add, BorderLayout.WEST);
		
		
		table.setModel(anzeige);
		table.setFont(schrift);
		table.setRowHeight(table.getRowHeight()+6);
		JPanel mid_panel = new JPanel(new BorderLayout());
		JScrollPane listScrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mid_panel.add(listScrollPane, BorderLayout.CENTER);
		
		this.add(north_panel, BorderLayout.NORTH);
		this.add(mid_panel, BorderLayout.CENTER);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}

}
