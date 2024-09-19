package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
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
	private static JTextField txtApiUrl;
	private static JTextField txtApiToken;

	public Dialog_settings() {

		this.setTitle("Einstellungen");
		this.setModal(true);
		this.setLayout(new GridBagLayout());
		this.setSize(400, 550);
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 200);

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.weightx = 1.0;
		c.anchor = GridBagConstraints.CENTER;
		c.ipady = 5;
		c.insets = new Insets(10, 10, 0, 10);
		JLabel lblFontSize = new JLabel("Schriftgrößen");
		lblFontSize.setFont(Mainframe.defaultFont);
		this.add(lblFontSize, c);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		JLabel lblFontGeneral = new JLabel("Allgemein");
		this.add(lblFontGeneral, c);
		c.gridx = 1;
		c.gridy = 1;
		Integer[] font = { 12, 14, 16, 18, 20 };
		cmbFont = new JComboBox<Integer>(font);
		cmbFont.setSelectedItem(Mainframe.defaultFont.getSize());
		this.add(cmbFont, c);
		c.gridx = 0;
		c.gridy = 2;
		JLabel lblFontDesc = new JLabel("Beschreibung");
		this.add(lblFontDesc, c);
		c.gridx = 1;
		c.gridy = 2;
		Integer[] fontDesc = { 12, 14, 16, 18, 20 };
		cmbFontDesc = new JComboBox<Integer>(fontDesc);
		cmbFontDesc.setSelectedItem(Mainframe.descFont.getSize());
		this.add(cmbFontDesc, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JLabel lblFeatures = new JLabel("optionale Features");
		lblFeatures.setFont(Mainframe.defaultFont);
		this.add(lblFeatures, c);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblAutoDownload = new JLabel("AutoDownload");
		lblAutoDownload.setToolTipText("Bei Anlage eines neuen Buches werden direkt Infos aus den APIs abgerufen");
		this.add(lblAutoDownload, c);
		c.gridx = 1;
		c.gridy = 4;
		Integer[] arrayAutoDownload = { 0, 1 };
		cmbAutoDownload = new JComboBox<Integer>(arrayAutoDownload);
		cmbAutoDownload.setSelectedItem(HandleConfig.autoDownload);
		this.add(cmbAutoDownload, c);
		c.gridx = 0;
		c.gridy = 5;
		JLabel lblOnDemand = new JLabel("Load on Demand");
		lblOnDemand.setToolTipText("Cover & Beschreibungen werden erst beim öffnen des Bearbeiten Dialogs geladen.");
		this.add(lblOnDemand, c);
		c.gridx = 1;
		c.gridy = 5;
		Integer[] arrayOnDemand = { 0, 1 };
		cmbOnDemand = new JComboBox<Integer>(arrayOnDemand);
		cmbOnDemand.setSelectedItem(HandleConfig.loadOnDemand);
		this.add(cmbOnDemand, c);
		c.gridx = 0;
		c.gridy = 6;
		JLabel lblUseDB = new JLabel("Nutze Datenbank");
		lblUseDB.setToolTipText("Benutzt die Datenbank für Suchanfragen, Vergleiche und andere Abfragen");
		this.add(lblUseDB, c);
		c.gridx = 1;
		c.gridy = 6;
		Boolean[] arrayUseDB = { false, true };
		cmbUseDB = new JComboBox<Boolean>(arrayUseDB);
		cmbUseDB.setSelectedItem(BookListModel.useDB);
		this.add(cmbUseDB, c);
		c.gridx = 0;
		c.gridy = 7;
		JLabel lblSearchParam = new JLabel("Suchparameter");
		lblSearchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		this.add(lblSearchParam, c);
		c.gridx = 1;
		c.gridy = 7;
		String[] arraySearchParam = { "t", "at" };
		cmbSearchParam = new JComboBox<String>(arraySearchParam);
		cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
		this.add(cmbSearchParam, c);
		c.gridx = 0;
		c.gridy = 8;
		JLabel lblDebug = new JLabel("Debug");
		lblDebug.setToolTipText("Ausführlichkeit der Logging Aktivität");
		this.add(lblDebug, c);
		c.gridx = 1;
		c.gridy = 8;
		String[] arrayDebug = { "WARN", "INFO", "TRACE" };
		cmbDebug = new JComboBox<String>(arrayDebug);
		cmbDebug.setSelectedItem(HandleConfig.debug);
		this.add(cmbDebug, c);
		c.gridx = 0;
		c.gridy = 9;
		JLabel lblBackup = new JLabel("Backupverhalten");
		lblBackup.setToolTipText(
				"0= Kein Backup beim schließen; 1=Abfrage beim schließen; 2=automatisches Backup beim schließen");
		this.add(lblBackup, c);
		c.gridx = 1;
		c.gridy = 9;
		Integer[] arrayBackup = { 0, 1, 2 };
		cmbBackup = new JComboBox<Integer>(arrayBackup);
		cmbBackup.setSelectedItem(HandleConfig.backup);
		this.add(cmbBackup, c);
		c.gridx = 0;
		c.gridy = 10;
		JLabel lblApiUrl = new JLabel("Web API URL");
		lblApiUrl.setToolTipText("URL der web API (https://...api.php)");
		this.add(lblApiUrl, c);
		c.gridx = 1;
		c.gridy = 10;
		String apiUrl = HandleConfig.apiURL;
		txtApiUrl = new JTextField(apiUrl);
		this.add(txtApiUrl, c);
		c.gridx = 0;
		c.gridy = 11;
		JLabel lblApiToken = new JLabel("Web API Token");
		lblApiToken.setToolTipText("Identifzierungstoken der web API");
		this.add(lblApiToken, c);
		c.gridx = 1;
		c.gridy = 11;
		String apiToken = HandleConfig.apiToken;
		txtApiToken = new JTextField(apiToken);
		this.add(txtApiToken, c);

		JButton btnGenToken = new JButton("generiere");
		btnGenToken.setFont(Mainframe.defaultFont);
		btnGenToken.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String token = generateRandomToken(64);
				txtApiToken.setText(token);
				HandleConfig.apiToken = token;
			}
		});
		c.gridx = 0;
		c.gridy = 98;
		this.add(btnGenToken, c);
		
		JButton btnShowQR = new JButton("zeige QR Code");
		btnShowQR.setFont(Mainframe.defaultFont);
		btnShowQR.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				HandleConfig.apiURL = txtApiUrl.getText();
				new Dialog_webapi();
			}
		});
		c.gridx = 1;
		c.gridy = 98;
		this.add(btnShowQR, c);

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
			HandleConfig.apiToken = txtApiToken.getText();
			HandleConfig.apiURL = txtApiUrl.getText();

			out.println("fontSize=" + cmbFont.getSelectedItem());
			out.println("descFontSize=" + cmbFontDesc.getSelectedItem());
			out.println("autoDownload=" + cmbAutoDownload.getSelectedItem());
			out.println("loadOnDemand=" + cmbOnDemand.getSelectedItem());
			out.println("useDB=" + cmbUseDB.getSelectedItem());
			out.println("searchParam=" + cmbSearchParam.getSelectedItem());
			out.println("debug=" + cmbDebug.getSelectedItem());
			out.println("backup=" + cmbBackup.getSelectedItem());
			out.println("apiToken=" + txtApiToken.getText());
			out.println("apiURL=" + txtApiUrl.getText());

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

	// Method to generate a random token with 64 characters
	private String generateRandomToken(int length) {
		SecureRandom random = new SecureRandom();
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
	}

}
