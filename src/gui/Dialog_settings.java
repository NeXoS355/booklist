package gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import com.formdev.flatlaf.util.UIScale;
import application.HandleConfig;

public class Dialog_settings extends JDialog {

  @Serial
  private static final long serialVersionUID = 1L;
  private static JComboBox<String> cmbLang;
  private static JComboBox<Integer> cmbFont;
  private static JComboBox<Integer> cmbFontDesc;
  private static JCheckBox chkAutoDownload;
  private static JCheckBox chkOnDemand;
  private static JComboBox<String> cmbSearchParam;
  private static JComboBox<String> cmbDebug;
  private static JComboBox<Integer> cmbBackup;
  private static JCheckBox chkDark;
  private static JComboBox<String> cmbScale;
  private static JTextField txtApiUrl;
  private static JTextField txtApiToken;
  private static JLabel lblQrCode;
  private static boolean restartNeeded = false;

  public Dialog_settings(Frame owner, boolean modal) {
    this.setTitle(Localization.get("t.settings"));
    this.setModal(modal);
    this.setLayout(new BorderLayout());
    this.setSize(UIScale.scale(670), UIScale.scale(515));
    this.setLocationRelativeTo(owner);

    URL connectionUrl;
    if (Mainframe.isApiConnected())
      connectionUrl = getClass().getResource("/resources/connection_good.png");
    else
      connectionUrl = getClass().getResource("/resources/connection_bad.png");
    if (connectionUrl == null) {
      Mainframe.logger.error("Resource not found: connection icon");
      return;
    }
    ImageIcon conIcon = new ImageIcon(connectionUrl);

    URL copyUrl;
    if (HandleConfig.darkmode == 1)
      copyUrl = getClass().getResource("/resources/copy_inv.png");
    else
      copyUrl = getClass().getResource("/resources/copy.png");
    if (copyUrl == null) {
      Mainframe.logger.error("Resource not found: copy icon");
      return;
    }
    ImageIcon copyIcon = new ImageIcon(copyUrl);

    JPanel pnlLeft = new JPanel();
    pnlLeft.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();

    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.ipady = 5;
    c.insets = new Insets(10, 10, 0, 10);
    JLabel lblLang = new JLabel(Localization.get("settings.language"));
    pnlLeft.add(lblLang, c);
    c.gridx = 1;
    c.gridy = 0;
    c.fill = GridBagConstraints.NONE;
    String[] lang = { "Deutsch", "English" };
    cmbLang = new JComboBox<>(lang);
    cmbLang.setSelectedItem(HandleConfig.lang);
    cmbLang.addItemListener(e -> restartNeeded = true);
    pnlLeft.add(cmbLang, c);
    c.gridx = 0;
    c.gridy = 1;
    c.gridwidth = 4;
    c.anchor = GridBagConstraints.CENTER;
    JLabel lblFontSize = new JLabel(Localization.get("settings.fontSize"));
    lblFontSize.setFont(Mainframe.defaultFont);
    pnlLeft.add(lblFontSize, c);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 2;
    JLabel lblFontGeneral = new JLabel(Localization.get("settings.general"));
    pnlLeft.add(lblFontGeneral, c);
    c.gridx = 1;
    c.gridy = 2;
    Integer[] font = { 12, 14, 16, 18, 20 };
    cmbFont = new JComboBox<>(font);
    cmbFont.setSelectedItem(UIScale.unscale(Mainframe.defaultFont.getSize()));
    pnlLeft.add(cmbFont, c);
    c.gridx = 0;
    c.gridy = 3;
    JLabel lblFontDesc = new JLabel(Localization.get("settings.description"));
    pnlLeft.add(lblFontDesc, c);
    c.gridx = 1;
    c.gridy = 3;
    Integer[] fontDesc = { 12, 14, 16, 18, 20 };
    cmbFontDesc = new JComboBox<>(fontDesc);
    cmbFontDesc.setSelectedItem(UIScale.unscale(Mainframe.descFont.getSize()));
    pnlLeft.add(cmbFontDesc, c);
    c.gridx = 0;
    c.gridy = 4;
    c.gridwidth = 2;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.NONE;
    JLabel lblFeatures = new JLabel(Localization.get("settings.optional"));
    lblFeatures.setFont(Mainframe.defaultFont);
    pnlLeft.add(lblFeatures, c);
    c.gridx = 0;
    c.gridy = 5;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    JLabel lblAutoDownload = new JLabel(Localization.get("settings.autoDownload"));
    lblAutoDownload.setToolTipText(Localization.get("settings.tipAutoDownload"));
    pnlLeft.add(lblAutoDownload, c);
    c.gridx = 1;
    c.gridy = 5;
    chkAutoDownload = new JCheckBox();
    chkAutoDownload.setSelected(HandleConfig.autoDownload == 1);
    pnlLeft.add(chkAutoDownload, c);
    c.gridx = 0;
    c.gridy = 6;
    JLabel lblOnDemand = new JLabel(Localization.get("settings.LoadOnDemand"));
    lblOnDemand.setToolTipText(Localization.get("settings.tipLoadOnDemand"));
    pnlLeft.add(lblOnDemand, c);
    c.gridx = 1;
    c.gridy = 6;
    chkOnDemand = new JCheckBox();
    chkOnDemand.setSelected(HandleConfig.loadOnDemand == 1);
    pnlLeft.add(chkOnDemand, c);
    c.gridx = 0;
    c.gridy = 7;
    JLabel lblSearchParam = new JLabel(Localization.get("settings.searchParameter"));
    lblSearchParam.setToolTipText(Localization.get("settings.tipSearchParameter"));
    pnlLeft.add(lblSearchParam, c);
    c.gridx = 1;
    c.gridy = 7;
    String[] arraySearchParam = { "t", "at" };
    cmbSearchParam = new JComboBox<>(arraySearchParam);
    cmbSearchParam.setSelectedItem(HandleConfig.searchParam);
    pnlLeft.add(cmbSearchParam, c);
    c.gridx = 0;
    c.gridy = 8;
    JLabel lblDebug = new JLabel(Localization.get("settings.debug"));
    lblDebug.setToolTipText(Localization.get("settings.tipDebug"));
    pnlLeft.add(lblDebug, c);
    c.gridx = 1;
    c.gridy = 8;
    String[] arrayDebug = { "WARN", "INFO" };
    cmbDebug = new JComboBox<>(arrayDebug);
    cmbDebug.setSelectedItem(HandleConfig.debug);
    pnlLeft.add(cmbDebug, c);
    c.gridx = 0;
    c.gridy = 9;
    JLabel lblBackup = new JLabel(Localization.get("settings.backup"));
    lblBackup.setToolTipText(
        Localization.get("settings.tipBackup"));
    pnlLeft.add(lblBackup, c);
    c.gridx = 1;
    c.gridy = 9;
    Integer[] arrayBackup = { 0, 1, 2 };
    cmbBackup = new JComboBox<>(arrayBackup);
    cmbBackup.setSelectedItem(HandleConfig.backup);
    pnlLeft.add(cmbBackup, c);
    c.gridx = 0;
    c.gridy = 10;
    JLabel lblDark = new JLabel(Localization.get("settings.darkmode"));
    lblDark.setToolTipText(Localization.get("settings.tipDarkmode"));
    pnlLeft.add(lblDark, c);
    c.gridx = 1;
    c.gridy = 10;
    chkDark = new JCheckBox();
    chkDark.setSelected(HandleConfig.tmpDarkmode == 1);
    chkDark.addItemListener(e -> restartNeeded = true);
    pnlLeft.add(chkDark, c);
    c.gridx = 0;
    c.gridy = 11;
    JLabel lblScaling = new JLabel(Localization.get("settings.scaling"));
    lblScaling.setToolTipText(Localization.get("settings.tipScaling"));
    pnlLeft.add(lblScaling, c);
    c.gridx = 1;
    c.gridy = 11;
    String[] scaleOptions = {
        Localization.get("settings.scalingAuto"), "100%", "125%", "150%", "200%"
    };
    cmbScale = new JComboBox<>(scaleOptions);
    switch (HandleConfig.uiScale) {
      case "1.0"  -> cmbScale.setSelectedItem("100%");
      case "1.25" -> cmbScale.setSelectedItem("125%");
      case "1.5"  -> cmbScale.setSelectedItem("150%");
      case "2.0"  -> cmbScale.setSelectedItem("200%");
      default     -> cmbScale.setSelectedIndex(0);
    }
    cmbScale.addItemListener(e -> restartNeeded = true);
    pnlLeft.add(cmbScale, c);
    JButton btnSave = ButtonsFactory.createButton(Localization.get("label.save"));
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

    JButton btnAbort = ButtonsFactory.createButton(Localization.get("label.abort"));
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
    lblApiUrl.setToolTipText("Web API URL (https://example.org:4444)");
    pnlRight.add(lblApiUrl, c);
    JLabel lblApiUrlCon = new JLabel(conIcon);
    if (Mainframe.isApiConnected())
      lblApiUrlCon.setToolTipText(Localization.get("api.connected"));
    else
      lblApiUrlCon.setToolTipText(Localization.get("api.notConnected"));
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
    lblApiToken.setToolTipText(Localization.get("settings.tipToken"));
    pnlRight.add(lblApiToken, c);
    c.gridx = 1;
    c.gridy = 1;
    c.gridwidth = 3;
    String apiToken = HandleConfig.apiToken;
    txtApiToken = new JTextField(apiToken);
    txtApiToken.setEditable(false);
    pnlRight.add(txtApiToken, c);

    JButton btnGenToken = ButtonsFactory.createButton(Localization.get("settings.generateToken"));
    btnGenToken.setFont(Mainframe.defaultFont);
    btnGenToken.addActionListener(e -> {
      int antwort = JOptionPane.showConfirmDialog(null,
          Localization.get("q.generateToken"),
          "Token", JOptionPane.YES_NO_OPTION);
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
    boolean apiChanged = !HandleConfig.apiToken.equals(txtApiToken.getText())
        || !HandleConfig.apiURL.equals(txtApiUrl.getText());

    try {
      HandleConfig.lang = (String) cmbLang.getSelectedItem();
      Mainframe.defaultFont = new Font("Roboto", Font.PLAIN, UIScale.scale((Integer) cmbFont.getSelectedItem()));
      Mainframe.descFont = new Font("Roboto", Font.PLAIN, UIScale.scale((Integer) cmbFontDesc.getSelectedItem()));
      HandleConfig.loadOnDemand = chkOnDemand.isSelected() ? 1 : 0;
      HandleConfig.autoDownload = chkAutoDownload.isSelected() ? 1 : 0;
      HandleConfig.searchParam = (String) cmbSearchParam.getSelectedItem();
      HandleConfig.debug = (String) cmbDebug.getSelectedItem();
      HandleConfig.backup = (int) cmbBackup.getSelectedItem();
      HandleConfig.tmpDarkmode = chkDark.isSelected() ? 1 : 0;
      HandleConfig.uiScale = switch ((String) cmbScale.getSelectedItem()) {
        case "100%" -> "1.0";
        case "125%" -> "1.25";
        case "150%" -> "1.5";
        case "200%" -> "2.0";
        default     -> "";
      };
      HandleConfig.apiToken = txtApiToken.getText();
      HandleConfig.apiURL = txtApiUrl.getText();
    } catch (NullPointerException e) {
      Mainframe.logger.error(e.getMessage());
      JOptionPane.showMessageDialog(null, Localization.get("settings.saveError"));
    }
    if (!HandleConfig.apiURL.isEmpty()) {
      if (HandleConfig.apiURL.endsWith("/")) {
        HandleConfig.apiURL = HandleConfig.apiURL.substring(0, HandleConfig.apiURL.length() - 1);
        System.out.println(HandleConfig.apiURL);
      }
    }
    if (apiChanged)
      Mainframe.executor.submit(Mainframe::checkApiConnection);
    Mainframe.executor.submit(HandleConfig::writeSettings);
    if (restartNeeded)
      JOptionPane.showMessageDialog(null, Localization.get("settings.restart"));
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
      Mainframe.logger.error("Erro while generating QR Code");
      Mainframe.logger.error(e1.getMessage());
    }
  }
}
