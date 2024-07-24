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
import javax.swing.table.TableColumnModel;

import application.BookListModel;
import application.HandleConfig;

public class Dialog_settings extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static JComboBox<Integer> cmbFont;
	private static JComboBox<Integer> cmbFontDesc;
	private static JComboBox<Integer> cmbAutoDownload;
	private static JComboBox<Integer> cmbOnDemand;
	private static JComboBox<String> cmbSearchParam;
	private static JComboBox<String> cmbDebug;
	private static JComboBox<Boolean> cmbUseDB;
	private static JComboBox<Integer> cmbBackup;

	public Dialog_settings() {

		this.setTitle("Einstellungen");
		this.setModal(true);
		this.setLayout(new GridBagLayout());
		this.setSize(300, 450);
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
		cmbFont = new JComboBox<Integer>(font);
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
		cmbFontDesc = new JComboBox<Integer>(fontDesc);
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
		lblAutoDownload.setToolTipText("Bei Anlage eines neuen Buches werden direkt Infos aus den APIs abgerufen");
		this.add(lblAutoDownload, c);
		c.gridx = 1;
		c.gridy = 4;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] arrayAutoDownload = { 0, 1 };
		cmbAutoDownload = new JComboBox<Integer>(arrayAutoDownload);
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
		cmbOnDemand = new JComboBox<Integer>(arrayOnDemand);
		cmbOnDemand.setSelectedItem(HandleConfig.loadOnDemand);
		this.add(cmbOnDemand, c);
		c.gridx = 0;
		c.gridy = 6;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblUseDB = new JLabel("Nutze Datenbank");
		lblUseDB.setToolTipText("Benutzt die Datenbank für Suchanfragen, Vergleiche und andere Abfragen");
		this.add(lblUseDB, c);
		c.gridx = 1;
		c.gridy = 6;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Boolean[] arrayUseDB = { false, true };
		cmbUseDB = new JComboBox<Boolean>(arrayUseDB);
		cmbUseDB.setSelectedItem(BookListModel.useDB);
		this.add(cmbUseDB, c);
		c.gridx = 0;
		c.gridy = 7;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblSearchParam = new JLabel("Suchparameter");
		lblSearchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		this.add(lblSearchParam, c);
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 0.5;
		c.gridwidth = 1;
		String[] arraySearchParam = { "t", "at" };
		cmbSearchParam = new JComboBox<String>(arraySearchParam);
		cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
		this.add(cmbSearchParam, c);
		c.gridx = 0;
		c.gridy = 8;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblDebug = new JLabel("Debug");
		lblDebug.setToolTipText("Ausführlichkeit der Logging Aktivität");
		this.add(lblDebug, c);
		c.gridx = 1;
		c.gridy = 8;
		c.weightx = 0.5;
		c.gridwidth = 1;
		String[] arrayDebug = { "WARN", "INFO",  "TRACE"};
		cmbDebug = new JComboBox<String>(arrayDebug);
		cmbDebug.setSelectedItem(HandleConfig.debug);
		this.add(cmbDebug, c);
		c.gridx = 0;
		c.gridy = 9;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel lblBackup = new JLabel("Backupverhalten");
		lblBackup.setToolTipText("0= Kein Backup beim schließen; 1=Abfrage beim schließen; 2=automatisches Backup beim schließen");
		this.add(lblBackup, c);
		c.gridx = 1;
		c.gridy = 9;
		c.weightx = 0.5;
		c.gridwidth = 1;
		Integer[] arrayBackup = { 0, 1, 2 };
		cmbBackup = new JComboBox<Integer>(arrayBackup);
		cmbBackup.setSelectedItem(HandleConfig.backup);
		this.add(cmbBackup, c);
		

		JButton btnSave = new JButton("Speichern");
		btnSave.setFont(Mainframe.defaultFont);
		btnSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveSettings();
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

	public static void saveSettings() {
		try (PrintWriter out = new PrintWriter("config.conf")) {
			Mainframe.logger.info("Save Settings");
			// set Parameters which can be changed on the fly
			HandleConfig.loadOnDemand = (int) cmbOnDemand.getSelectedItem();
			HandleConfig.autoDownload = (int) cmbAutoDownload.getSelectedItem();
			HandleConfig.searchParam = (String) cmbSearchParam.getSelectedItem();
			HandleConfig.debug = (String) cmbDebug.getSelectedItem();
			HandleConfig.backup = (int) cmbBackup.getSelectedItem();

			out.println("fontSize=" + cmbFont.getSelectedItem());
			out.println("descFontSize=" + cmbFontDesc.getSelectedItem());
			out.println("autoDownload=" + cmbAutoDownload.getSelectedItem());
			out.println("loadOnDemand=" + cmbOnDemand.getSelectedItem());
			out.println("useDB=" + cmbUseDB.getSelectedItem());
			out.println("searchParam=" + cmbSearchParam.getSelectedItem());
			out.println("debug=" + cmbDebug.getSelectedItem());
			out.println("backup=" + cmbBackup.getSelectedItem());
			
			TableColumnModel columnModel = Mainframe.table.getColumnModel();
			
			StringBuilder strWidth = new StringBuilder();
			strWidth.append("layoutWidth=");
			strWidth.append(columnModel.getColumn(0).getWidth());
			strWidth.append(",");
			strWidth.append(columnModel.getColumn(1).getWidth());
			strWidth.append(",");
			strWidth.append(columnModel.getColumn(2).getWidth());
			strWidth.append(",");
			strWidth.append(columnModel.getColumn(3).getWidth());
			strWidth.append(",");
			strWidth.append(columnModel.getColumn(4).getWidth());
			
			out.println(strWidth);
			
			StringBuilder strColumnTitle = new StringBuilder();
			strColumnTitle.append("layoutSort=");
			strColumnTitle.append(columnModel.getColumn(0).getHeaderValue());
			strColumnTitle.append(",");
			strColumnTitle.append(columnModel.getColumn(1).getHeaderValue());
			strColumnTitle.append(",");
			strColumnTitle.append(columnModel.getColumn(2).getHeaderValue());
			strColumnTitle.append(",");
			strColumnTitle.append(columnModel.getColumn(3).getHeaderValue());
			strColumnTitle.append(",");
			strColumnTitle.append(columnModel.getColumn(4).getHeaderValue());
			
			out.println(strColumnTitle);
			

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			Mainframe.logger.error("Fehler beim speichern der Einstellungen");
		}
	}

}
