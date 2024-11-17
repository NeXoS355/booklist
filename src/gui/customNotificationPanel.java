package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import static gui.Mainframe.*;

public class customNotificationPanel extends JPanel {

    JLabel  notificationLabel;
    static Map<JPanel, Float> panelAlphaMap = new HashMap<>();
    static final Dimension notificationSize = new Dimension(600, 30); // Breite und Höhe festlegen
    static Point location = new Point(0, splitPane.getHeight() - activeNotifications.size()*30 - activeNotifications.size()*5);

    public customNotificationPanel(String message) {
        notificationLabel = new JLabel(message);
        setLayout(new FlowLayout(FlowLayout.LEFT));

//        AtomicReference<Float> alpha = new AtomicReference<>(0.0f);
//        panelAlphaMap.put(this, 1.0f); // Initialer Alpha-Wert bei 0 (unsichtbar)

        // Panel-Größenbeschränkungen festlegen
        setPreferredSize(notificationSize);
        setMaximumSize(notificationSize);
        setMinimumSize(notificationSize);

        setBackground(Color.DARK_GRAY);
        setOpaque(true); // Hintergrundfarbe sichtbar machen

        notificationLabel.setForeground(Color.WHITE); // Schriftfarbe
        notificationLabel.setFont(Mainframe.defaultFont);
        add(notificationLabel); // Label zum Panel hinzufügen

        int notificationIndex = activeNotifications.size()+1;
        int yPos = splitPane.getHeight() - ((notificationIndex * 30) + (notificationIndex * 5));

        setBounds(0, yPos, (int) notificationSize.getWidth(), (int) notificationSize.getHeight());

        // Panel zur LayeredPane hinzufügen
        Mainframe.layeredPane.add(this, Integer.valueOf(2));
        activeNotifications.add(this);
        setLocation(location);

        setVisible(true);
        updateUI();
    }

    public void setText(String text) {
        notificationLabel.setText(text);
        updateUI();
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        Float alpha = panelAlphaMap.getOrDefault(this, 1.0f); // Default 1.0f, falls nicht vorhanden
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        super.paintComponent(g2d);
        g2d.dispose();
    }

    public void addBookReference(int index) {
            // MouseListener hinzufügen, um den "Link" anklickbar zu machen
            notificationLabel.addMouseListener(            new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 1) {
                        // Link wurde angeklickt → neuen Dialog öffnen
                        new Dialog_edit_Booklist(Mainframe.getInstance(), allEntries, index, Mainframe.treeModel);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    notificationLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    notificationLabel.setCursor(Cursor.getDefaultCursor());
                }
            });

    }

}