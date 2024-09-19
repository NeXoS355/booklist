package gui;

import javax.swing.*;
import java.awt.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;

import application.HandleConfig;

import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class Dialog_webapi extends JDialog {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField apiTokenField;
	private JTextField apiUrlField;
    private JLabel qrCodeLabel;

    public Dialog_webapi() {
        setTitle("API Config");
        setSize(200, 250);     
		this.setModal(true);
		this.setLocation(Mainframe.getInstance().getX() + 500, Mainframe.getInstance().getY() + 200);
        
        apiUrlField = new JTextField(HandleConfig.apiURL);
        apiUrlField.setEditable(false);
        
        // Create token field
        apiTokenField = new JTextField(HandleConfig.apiToken);
        apiTokenField.setEditable(false);
        
        // QR code label
        qrCodeLabel = new JLabel();
        generateQRCode(HandleConfig.apiURL + "?token=" + HandleConfig.apiToken);

        JPanel qrPanel = new JPanel();
        qrPanel.add(qrCodeLabel);

        add(qrPanel, BorderLayout.CENTER);
        
        this.setVisible(true);
    }



    // Method to generate and display a QR code
    private void generateQRCode(String url) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            qrCodeLabel.setIcon(new ImageIcon(qrImage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
