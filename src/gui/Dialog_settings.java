package gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;
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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import application.HandleConfig;

public class Dialog_settings extends JDialog {

	@Serial
	private static final long serialVersionUID = 1L;
    private static JComboBox<Integer> cmbAutoDownload;
	private static JComboBox<Integer> cmbOnDemand;
	private static JComboBox<String> cmbSearchParam;
	private static JComboBox<String> cmbDebug;
	private static JComboBox<Integer> cmbBackup;
    private static JTextField txtApiUrl;
	private static JTextField txtApiToken;
	private static JLabel lblQrCode;

    public Dialog_settings(Frame owner, boolean modal) {
		this.setTitle("Einstellungen");
		this.setModal(modal);
		this.setLayout(new BorderLayout());
		this.setSize(670, 450);
		this.setLocationRelativeTo(owner);

        URL connectionUrl;
        if (Mainframe.isApiConnected())
			connectionUrl = getClass().getResource("/resources/connection_good.png");
		else
			connectionUrl = getClass().getResource("/resources/connection_bad.png");
        assert connectionUrl != null;
        ImageIcon conIcon = new ImageIcon(connectionUrl);

        URL copyUrl;
        if (HandleConfig.darkmode == 1)
			copyUrl = getClass().getResource("/resources/copy_inv.png");
		else
			copyUrl = getClass().getResource("/resources/copy.png");
        assert copyUrl != null;
        ImageIcon copyIcon = new ImageIcon(copyUrl);

		JPanel pnlLeft = new JPanel();
		pnlLeft.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
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
        JComboBox<Integer> cmbFont = new JComboBox<>(font);
		cmbFont.setSelectedItem(Mainframe.defaultFont.getSize());
		pnlLeft.add(cmbFont, c);
		c.gridx = 0;
		c.gridy = 2;
		JLabel lblFontDesc = new JLabel("Beschreibung");
		pnlLeft.add(lblFontDesc, c);
		c.gridx = 1;
		c.gridy = 2;
		Integer[] fontDesc = { 12, 14, 16, 18, 20 };
        JComboBox<Integer> cmbFontDesc = new JComboBox<>(fontDesc);
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
		cmbAutoDownload = new JComboBox<>(arrayAutoDownload);
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
		cmbOnDemand = new JComboBox<>(arrayOnDemand);
		cmbOnDemand.setSelectedItem(HandleConfig.loadOnDemand);
		pnlLeft.add(cmbOnDemand, c);
		c.gridx = 0;
		c.gridy = 7;
		JLabel lblSearchParam = new JLabel("Suchparameter");
		lblSearchParam.setToolTipText("Google API Suchparameter. t=Titel, at=Autor+Titel");
		pnlLeft.add(lblSearchParam, c);
		c.gridx = 1;
		c.gridy = 7;
		String[] arraySearchParam = { "t", "at" };
		cmbSearchParam = new JComboBox<>(arraySearchParam);
		cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
		pnlLeft.add(cmbSearchParam, c);
		c.gridx = 0;
		c.gridy = 8;
		JLabel lblDebug = new JLabel("Debug");
		lblDebug.setToolTipText("Ausführlichkeit der Logging Aktivität");
		pnlLeft.add(lblDebug, c);
		c.gridx = 1;
		c.gridy = 8;
		String[] arrayDebug = { "WARN", "INFO" };
		cmbDebug = new JComboBox<>(arrayDebug);
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
		cmbBackup = new JComboBox<>(arrayBackup);
		cmbBackup.setSelectedItem(HandleConfig.backup);
		pnlLeft.add(cmbBackup, c);
		c.gridx = 0;
		c.gridy = 10;
		JLabel lblDark = new JLabel("Darkmode");
		lblDark.setToolTipText("0= Light Mode; 1=Dark Mode");
		pnlLeft.add(lblDark, c);
		c.gridx = 1;
		c.gridy = 10;
		Integer[] arrayDark = { 0, 1 };
        JComboBox<Integer> cmbDark = new JComboBox<>(arrayDark);
		cmbDark.setSelectedItem(HandleConfig.darkmode);
		pnlLeft.add(cmbDark, c);
		JButton btnSave = ButtonsFactory.createButton("Speichern");
		btnSave.setFont(Mainframe.defaultFont);
		btnSave.addActionListener(e -> {
            saveSettings();
            dispose();
        });
		c.gridx = 0;
		c.gridy = 99;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		pnlLeft.add(btnSave, c);

		JButton btnAbort = ButtonsFactory.createButton("Abbrechen");
		btnAbort.setFont(Mainframe.defaultFont);
		btnAbort.addActionListener(arg0 -> dispose());
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

		c.ipady = 10;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		JLabel lblApiUrl = new JLabel("Web API URL");
		lblApiUrl.setToolTipText("URL der web API (https://example.org:4444)");
		pnlRight.add(lblApiUrl, c);
		JLabel lblApiUrlCon = new JLabel(conIcon);
		if (Mainframe.isApiConnected())
			lblApiUrlCon.setToolTipText("API ist verbunden");
		else
			lblApiUrlCon.setToolTipText("API ist nicht verbunden");
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		pnlRight.add(lblApiUrlCon, c);
		c.gridx = 1;
		c.gridy = 0;
		c.gridwidth = 3;
		String apiUrl = HandleConfig.apiURL;
		txtApiUrl = new JTextField(apiUrl);
		pnlRight.add(txtApiUrl, c);
		JButton btnTokenCopy = ButtonsFactory.createButton(copyIcon);
		btnTokenCopy.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection apiTokenStr = new StringSelection(txtApiToken.getText());
				clipboard.setContents(apiTokenStr, null);
			}

		});
		c.insets = new Insets(5, 10, 0, 0);
		c.gridx = 4;
		c.gridy = 1;
		c.gridwidth = 1;
		pnlRight.add(btnTokenCopy, c);
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
		btnGenToken.addActionListener(e -> {
            int antwort = JOptionPane.showConfirmDialog(null,
                    "Wirklich neuen Token genrieren?\nDie ausstehenden bücher mit dem alten Token können dann nicht mehr abgerufen werden.",
                    "generieren", JOptionPane.YES_NO_OPTION);
            if (antwort == JOptionPane.YES_OPTION) {
                String token = HandleConfig.generateRandomToken(64);
                txtApiToken.setText(token);
                HandleConfig.apiToken = token;
                generateQRCode(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);
            }
        });
		c.insets = new Insets(10, 10, 0, 10);
		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 2;
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

	@SuppressWarnings("DataFlowIssue")
    public static void saveSettings() {
			Mainframe.logger.info("Save Settings in Program");
			// set Parameters which can be changed on the fly
			try {
				HandleConfig.loadOnDemand = (int) cmbOnDemand.getSelectedItem();
				HandleConfig.autoDownload = (int) cmbAutoDownload.getSelectedItem();
				HandleConfig.searchParam = (String) cmbSearchParam.getSelectedItem();
				HandleConfig.debug = (String) cmbDebug.getSelectedItem();
				HandleConfig.backup = (int) cmbBackup.getSelectedItem();
				HandleConfig.apiToken = txtApiToken.getText();
				HandleConfig.apiURL = txtApiUrl.getText();
			} catch (NullPointerException e) {
				Mainframe.logger.error(e.getMessage());
			}
			if (!HandleConfig.apiURL.isEmpty()) {
				if (HandleConfig.apiURL.endsWith("/")) {
					HandleConfig.apiURL = HandleConfig.apiURL.substring(0, HandleConfig.apiURL.length() - 1);
					System.out.println(HandleConfig.apiURL);
				}
			}
		Mainframe.executor.submit(Mainframe::checkApiConnection);
		Mainframe.executor.submit(HandleConfig::writeSettings);
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
		} catch (Exception e1) {
			Mainframe.logger.error("Fehler beim generieren des QRCodes");
			Mainframe.logger.error(e1.getMessage());
		}
	}
}
