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

import application.HandleConfig;

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
		JLabel lblFontSize = new JLabel("Schriftgrößen");
		lblFontSize.setFont(Mainframe.defaultFont);
		this.add(lblFontSize, c);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.05;
		c.gridwidth = 1;
		JLabel lblFontGeneral = new JLabel("Allgemein");
		this.add(lblFontGeneral, c);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] font = { 12, 14, 16, 18, 20 };
		JComboBox<Integer> cmbFont = new JComboBox<Integer>(font);
		cmbFont.setSelectedItem(Mainframe.defaultFont.getSize());
		this.add(cmbFont, c);
		c.gridx = 0;
		c.gridy = 2;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblFontDesc = new JLabel("Beschreibung");
		this.add(lblFontDesc, c);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 0.1;
		c.gridwidth = 1;
		Integer[] fontDesc = { 12, 14, 16, 18, 20 };
		JComboBox<Integer> cmbFontDesc = new JComboBox<Integer>(fontDesc);
		cmbFontDesc.setSelectedItem(Mainframe.descFont.getSize());
		this.add(cmbFontDesc, c);

		c.gridx = 0;
		c.gridy = 3;
		c.weightx = 0.5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JLabel lblFeatures = new JLabel("optionale Features");
		lblFeatures.setFont(Mainframe.defaultFont);
		this.add(lblFeatures, c);

		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 0.1;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblAutoDownload = new JLabel("AutoDownload");
		lblAutoDownload.setToolTipText("Bei Anlage eines neune Buches werden direkt Infos aus den APIs abgerufen");
		this.add(lblAutoDownload, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] arrayAutoDownload = { 0, 1 };
		JComboBox<Integer> cmbAutoDownload = new JComboBox<Integer>(arrayAutoDownload);
		cmbAutoDownload.setSelectedItem(HandleConfig.autoDownload);
		this.add(cmbAutoDownload, c);

		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblOnDemand = new JLabel("Load on Demand");
		lblOnDemand.setToolTipText("Cover & Beschreibungen werden erst beim öffnen des Bearbeiten Dialogs geladen.");
		this.add(lblOnDemand, c);
		c.gridx = 1;
		c.gridy = 5;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] arrayOnDemand = { 0, 1 };
		JComboBox<Integer> cmbOnDemand = new JComboBox<Integer>(arrayOnDemand);
		cmbOnDemand.setSelectedItem(HandleConfig.loadOnDemand);
		this.add(cmbOnDemand, c);
		
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblSearchParam = new JLabel("Search Parameter");
		lblSearchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		this.add(lblSearchParam, c);
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 0.5;
		c.gridwidth = 1;
		String[] arraySearchParam = { "t", "at" };
		JComboBox<String> cmbSearchParam = new JComboBox<String>(arraySearchParam);
		cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
		this.add(cmbSearchParam, c);
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblDebug = new JLabel("Debug");
		lblDebug.setToolTipText("Ausführlichkeit der Logging Aktivität 0=WARN,1=INFO,2=TRACE");
		this.add(lblDebug, c);
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] arrayDebug = { 0, 1, 2};
		JComboBox<Integer> cmbDebug = new JComboBox<Integer>(arrayDebug);
		cmbDebug.setSelectedItem(HandleConfig.debug);
		this.add(cmbDebug, c);

		JButton btnSave = new JButton("Speichern");
		btnSave.setFont(Mainframe.defaultFont);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try (PrintWriter out = new PrintWriter("config.conf")) {
					Mainframe.logger.info("Save Settings");
					//set Parameters which can be changed on the fly
					HandleConfig.loadOnDemand = (int) cmbOnDemand.getSelectedItem();
					HandleConfig.autoDownload = (int) cmbAutoDownload.getSelectedItem();
					HandleConfig.searchParam = (String) cmbSearchParam.getSelectedItem();

					//write new config file
					out.println("fontSize=" + cmbFont.getSelectedItem());
					out.println("descFontSize=" + cmbFontDesc.getSelectedItem());
					out.println("autoDownload=" + cmbAutoDownload.getSelectedItem());
					out.println("loadOnDemand=" + cmbOnDemand.getSelectedItem());
					out.println("searchParam=" + cmbSearchParam.getSelectedItem());
					out.println("debug=" + cmbDebug.getSelectedItem());
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
					Mainframe.logger.error("Fehler beim speichern der Einstellungen");
				}
				dispose();
			}
		});
		c.gridx = 0;
		c.gridy = 99;
		c.weightx = 0.5;
		c.gridwidth = 1;
		this.add(btnSave, c);

		JButton btnAbort = new JButton("Abbrechen");
		btnAbort.setFont(Mainframe.defaultFont);
		btnAbort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		c.gridx = 1;
		c.gridy = 99;
		c.weightx = 0.5;
		c.gridwidth = 1;
		this.add(btnAbort, c);

		this.setVisible(true);
	}

}
