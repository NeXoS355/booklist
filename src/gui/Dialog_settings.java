package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class Dialog_settings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Dialog_settings() {

		this.setTitle("Einstellungen");
		this.setModal(true);
		this.setLayout(new GridBagLayout());
		this.setSize(300, 360);
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 200);

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.ipady = 5;
		c.insets = new Insets(10, 10, 0, 10);
		JLabel lbl_fontSize = new JLabel("Schriftgrößen");
		lbl_fontSize.setFont(Mainframe.schrift);
		this.add(lbl_fontSize, c);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lbl_fontAllgemein = new JLabel("Allgemein");
		this.add(lbl_fontAllgemein, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] font = { 12, 14, 16, 18, 20 };
		JComboBox<Integer> cmb_font = new JComboBox<Integer>(font);
		cmb_font.setSelectedItem(Mainframe.schrift.getSize());
		this.add(cmb_font, c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lbl_fontDesc = new JLabel("Beschreibung");
		this.add(lbl_fontDesc, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.1;
		c.gridwidth = 1;
		Integer[] fontDesc = { 12, 14, 16, 18, 20 };
		JComboBox<Integer> cmb_fontDesc = new JComboBox<Integer>(fontDesc);
		cmb_fontDesc.setSelectedItem(Mainframe.descSchrift.getSize());
		this.add(cmb_fontDesc, c);

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JLabel lbl_features = new JLabel("optionale Features");
		lbl_features.setFont(Mainframe.schrift);
		this.add(lbl_features, c);

		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel lbl_autoDownload = new JLabel("AutoDownload");
		lbl_autoDownload.setToolTipText("Bei Anlage eines neune Buches werden direkt Infos aus den APIs abgerufen");
		this.add(lbl_autoDownload, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] array_autoDownload = { 0, 1 };
		JComboBox<Integer> cmb_autoDownload = new JComboBox<Integer>(array_autoDownload);
		cmb_autoDownload.setSelectedItem(HandleConfig.autoDownload);
		this.add(cmb_autoDownload, c);

		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lbl_onDemand = new JLabel("Load on Demand");
		lbl_onDemand.setToolTipText("Cover & Beschreibungen werden erst beim öffnen des Bearbeiten Dialogs geladen.");
		this.add(lbl_onDemand, c);
		c.gridx = 1;
		c.gridy = 5;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] array_onDemand = { 0, 1 };
		JComboBox<Integer> cmb_onDemand = new JComboBox<Integer>(array_onDemand);
		cmb_onDemand.setSelectedItem(HandleConfig.loadOnDemand);
		this.add(cmb_onDemand, c);
		
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lbl_searchParam = new JLabel("Search Parameter");
		lbl_searchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		this.add(lbl_searchParam, c);
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 0.5;
		c.gridwidth = 1;
		String[] array_searchParam = { "t", "at" };
		JComboBox<String> cmb_searchParam = new JComboBox<String>(array_searchParam);
		cmb_searchParam.setSelectedItem(HandleConfig.searchParam);
		this.add(cmb_searchParam, c);
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lbl_debugTimings = new JLabel("Show Debug Timings");
		lbl_debugTimings.setToolTipText("zeigt Ausführungsdauer nach dem Laden der Anwendung an");
		this.add(lbl_debugTimings, c);
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] array_debugTimings = { 0, 1};
		JComboBox<Integer> cmb_debugTimings = new JComboBox<Integer>(array_debugTimings);
		cmb_debugTimings.setSelectedItem(HandleConfig.debug_timings);
		this.add(cmb_debugTimings, c);

		JButton btn_save = new JButton("Speichern");
		btn_save.setFont(Mainframe.schrift);
		btn_save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try (PrintWriter out = new PrintWriter("config.conf")) {

					HandleConfig.loadOnDemand = (int) cmb_onDemand.getSelectedItem();
					HandleConfig.autoDownload = (int) cmb_autoDownload.getSelectedItem();
					HandleConfig.searchParam = (String) cmb_searchParam.getSelectedItem();

					out.println("fontSize=" + cmb_font.getSelectedItem());
					out.println("descFontSize=" + cmb_fontDesc.getSelectedItem());
					out.println("autoDownload=" + cmb_autoDownload.getSelectedItem());
					out.println("loadOnDemand=" + cmb_onDemand.getSelectedItem());
					out.println("searchParam=" + cmb_searchParam.getSelectedItem());
				} catch (FileNotFoundException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
//				JOptionPane.showMessageDialog(null, "Die Änderungen werden nach einem Neustart übernommen");
				dispose();
			}
		});
		c.gridx = 0;
		c.gridy = 99;
		c.weightx = 0.5;
		c.gridwidth = 1;
		this.add(btn_save, c);

		JButton btn_abort = new JButton("Abbrechen");
		btn_abort.setFont(Mainframe.schrift);
		btn_abort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		c.gridx = 1;
		c.gridy = 99;
		c.weightx = 0.5;
		c.gridwidth = 1;
		this.add(btn_abort, c);

		this.setVisible(true);
	}

}
