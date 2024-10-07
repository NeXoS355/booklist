package gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableColumnModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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
	private static JComboBox<Integer> cmbDark;
	private static JTextField txtApiUrl;
	private static JTextField txtApiToken;
	private static JLabel lblQrCode;

	public Dialog_settings() {

		this.setTitle("Einstellungen");
		this.setModal(true);
		this.setLayout(new BorderLayout());
		this.setSize(650, 500);
		this.setLocationByPlatform(true);

		JPanel pnlLeft = new JPanel();
		pnlLeft.setLayout(new GridBagLayout());
		
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
		pnlLeft.add(lblFontSize, c);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		JLabel lblFontGeneral = new JLabel("Allgemein");
		pnlLeft.add(lblFontGeneral, c);
		c.gridx = 1;
		c.gridy = 1;
		Integer[] font = { 12, 14, 16, 18, 20 };
		cmbFont = new JComboBox<Integer>(font);
		cmbFont.setSelectedItem(Mainframe.defaultFont.getSize());
		pnlLeft.add(cmbFont, c);
		c.gridx = 0;
		c.gridy = 2;
		JLabel lblFontDesc = new JLabel("Beschreibung");
		pnlLeft.add(lblFontDesc, c);
		c.gridx = 1;
		c.gridy = 2;
		Integer[] fontDesc = { 12, 14, 16, 18, 20 };
		cmbFontDesc = new JComboBox<Integer>(fontDesc);
		cmbFontDesc.setSelectedItem(Mainframe.descFont.getSize());
		pnlLeft.add(cmbFontDesc, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		JLabel lblFeatures = new JLabel("optionale Features");
		lblFeatures.setFont(Mainframe.defaultFont);
		pnlLeft.add(lblFeatures, c);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		JLabel lblAutoDownload = new JLabel("AutoDownload");
		lblAutoDownload.setToolTipText("Bei Anlage eines neuen Buches werden direkt Infos aus den APIs abgerufen");
		pnlLeft.add(lblAutoDownload, c);
		c.gridx = 1;
		c.gridy = 4;
		Integer[] arrayAutoDownload = { 0, 1 };
		cmbAutoDownload = new JComboBox<Integer>(arrayAutoDownload);
		cmbAutoDownload.setSelectedItem(HandleConfig.autoDownload);
		pnlLeft.add(cmbAutoDownload, c);
		c.gridx = 0;
		c.gridy = 5;
		JLabel lblOnDemand = new JLabel("Load on Demand");
		lblOnDemand.setToolTipText("Cover & Beschreibungen werden erst beim öffnen des Bearbeiten Dialogs geladen.");
		pnlLeft.add(lblOnDemand, c);
		c.gridx = 1;
		c.gridy = 5;
		Integer[] arrayOnDemand = { 0, 1 };
		cmbOnDemand = new JComboBox<Integer>(arrayOnDemand);
		cmbOnDemand.setSelectedItem(HandleConfig.loadOnDemand);
		pnlLeft.add(cmbOnDemand, c);
		c.gridx = 0;
		c.gridy = 6;
		JLabel lblUseDB = new JLabel("Nutze Datenbank");
		lblUseDB.setToolTipText("Benutzt die Datenbank für Suchanfragen, Vergleiche und andere Abfragen");
		pnlLeft.add(lblUseDB, c);
		c.gridx = 1;
		c.gridy = 6;
		Boolean[] arrayUseDB = { false, true };
		cmbUseDB = new JComboBox<Boolean>(arrayUseDB);
		cmbUseDB.setSelectedItem(BookListModel.useDB);
		pnlLeft.add(cmbUseDB, c);
		c.gridx = 0;
		c.gridy = 7;
		JLabel lblSearchParam = new JLabel("Suchparameter");
		lblSearchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		pnlLeft.add(lblSearchParam, c);
		c.gridx = 1;
		c.gridy = 7;
		String[] arraySearchParam = { "t", "at" };
		cmbSearchParam = new JComboBox<String>(arraySearchParam);
		cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
		pnlLeft.add(cmbSearchParam, c);
		c.gridx = 0;
		c.gridy = 8;
		JLabel lblDebug = new JLabel("Debug");
		lblDebug.setToolTipText("Ausführlichkeit der Logging Aktivität");
		pnlLeft.add(lblDebug, c);
		c.gridx = 1;
		c.gridy = 8;
		String[] arrayDebug = { "WARN", "INFO", "TRACE" };
		cmbDebug = new JComboBox<String>(arrayDebug);
		cmbDebug.setSelectedItem(HandleConfig.debug);
		pnlLeft.add(cmbDebug, c);
		c.gridx = 0;
		c.gridy = 9;
		JLabel lblBackup = new JLabel("Backupverhalten");
		lblBackup.setToolTipText(
				"0= Kein Backup beim schließen; 1=Abfrage beim schließen; 2=automatisches Backup beim schließen");
		pnlLeft.add(lblBackup, c);
		c.gridx = 1;
		c.gridy = 9;
		Integer[] arrayBackup = { 0, 1, 2 };
		cmbBackup = new JComboBox<Integer>(arrayBackup);
		cmbBackup.setSelectedItem(HandleConfig.backup);
		pnlLeft.add(cmbBackup, c);
		c.gridx = 0;
		c.gridy = 10;
		JLabel lblDark = new JLabel("Darkmode");
		lblBackup.setToolTipText(
				"0= Light Mode; 1=Dark Mode");
		pnlLeft.add(lblDark, c);
		c.gridx = 1;
		c.gridy = 10;
		Integer[] arrayDark = { 0, 1 };
		cmbDark = new JComboBox<Integer>(arrayDark);
		cmbDark.setSelectedItem(HandleConfig.darkmode);
		pnlLeft.add(cmbDark, c);
		JButton btnSave = ButtonsFactory.createButton("Speichern");
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
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		pnlLeft.add(btnSave, c);
		
		JButton btnAbort = ButtonsFactory.createButton("Abbrechen");
		btnAbort.setFont(Mainframe.defaultFont);
		btnAbort.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		c.gridx = 1;
		c.gridy = 99;
		c.anchor = GridBagConstraints.EAST;
		pnlLeft.add(btnAbort, c);
		
		/*
		 * Define the right Panel for API Settings
		 */
		JPanel pnlRight = new JPanel();
		pnlRight.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		
		c.ipady = 5;
		c.insets = new Insets(10, 10, 0, 10);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		JLabel lblApiUrl = new JLabel("Web API URL");
		lblApiUrl.setToolTipText("URL der web API (https://...api.php)");
		pnlRight.add(lblApiUrl, c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		String apiUrl = HandleConfig.apiURL;
		txtApiUrl = new JTextField(apiUrl);
		pnlRight.add(txtApiUrl, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		JLabel lblApiToken = new JLabel("Web API Token");
		lblApiToken.setToolTipText("Identifzierungstoken der web API");
		pnlRight.add(lblApiToken, c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 3;
		String apiToken = HandleConfig.apiToken;
		txtApiToken = new JTextField(apiToken);
		txtApiToken.setEditable(false);
		pnlRight.add(txtApiToken, c);

		JButton btnGenToken = ButtonsFactory.createButton("generiere");
		btnGenToken.setFont(Mainframe.defaultFont);
		btnGenToken.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int antwort = JOptionPane.showConfirmDialog(null,
						"Wirklich neuen Token genrieren?\nDie ausstehenden Bücher mit dem alten Token können dann nicht mehr abgerufen werden.",
						"generieren", JOptionPane.YES_NO_OPTION);
				if (antwort == JOptionPane.YES_OPTION) {
					String token = HandleConfig.generateRandomToken(64);
					txtApiToken.setText(token);
					HandleConfig.apiToken = token;
					generateQRCode(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);
				}
			}
		});
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
//		c.anchor = GridBagConstraints.CENTER;
		pnlRight.add(btnGenToken, c);

		// QR code label
		lblQrCode = new JLabel();
		generateQRCode(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);
		JPanel qrPanel = new JPanel();
		qrPanel.add(lblQrCode);
		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 7;
		pnlRight.add(qrPanel, c);

		
		this.add(pnlLeft, BorderLayout.WEST);
		this.add(pnlRight, BorderLayout.CENTER);

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
			if (HandleConfig.apiURL.length() > 0) {
				if (HandleConfig.apiURL.substring(HandleConfig.apiURL.length() - 1).equals("/")) {
					HandleConfig.apiURL = HandleConfig.apiURL.substring(0, HandleConfig.apiURL.length() - 1);
					System.out.println(HandleConfig.apiURL);
				}
			}
			if (HandleConfig.apiURL.length() > 0) {
				try {
					Mainframe.logger.trace("Web API request: " + HandleConfig.apiURL + "/api/get.php");
					URL getUrl;
					getUrl = new URI(HandleConfig.apiURL + "/api/get.php?token=" + HandleConfig.apiToken).toURL();
					HttpURLConnection con = (HttpURLConnection) getUrl.openConnection();
					con.setRequestMethod("GET");
					int responseCode = con.getResponseCode();
					Mainframe.logger.trace("Web API GET responseCode: " + responseCode);
					if (responseCode != HttpURLConnection.HTTP_OK) {
						JOptionPane.showMessageDialog(null, "Verbindung zur API fehlgeschlagen");
					}
				} catch (MalformedURLException e) {
					Mainframe.logger.error("Fehler prüfen der Verbindung");
					Mainframe.logger.error(e.getMessage());
				} catch (URISyntaxException e) {
					Mainframe.logger.error("Fehler prüfen der Verbindung");
					Mainframe.logger.error(e.getMessage());
				} catch (ProtocolException e) {
					Mainframe.logger.error("Fehler prüfen der Verbindung");
					Mainframe.logger.error(e.getMessage());
				} catch (IOException e) {
					Mainframe.logger.error("Fehler prüfen der Verbindung");
					Mainframe.logger.error(e.getMessage());
				}

			}

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
			out.println("darkmode=" + cmbDark.getSelectedItem());

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

	// Method to generate and display a QR code
	private void generateQRCode(String url) {
		try {
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			Map<EncodeHintType, Object> hints = new HashMap<>();
			hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
			BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200, hints);
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
			lblQrCode.setIcon(new ImageIcon(qrImage));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
