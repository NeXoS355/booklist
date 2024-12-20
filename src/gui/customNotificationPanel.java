package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import static gui.Mainframe.*;

public class customNotificationPanel extends JPanel {

    final JLabel  notificationLabel;
    Dimension notificationSize;
    int timer;
    final int oriTimer;


    public customNotificationPanel(String message, int timer) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        message = message.trim();
        notificationLabel = new JLabel(message);
        this.timer = timer;
        this.oriTimer = timer;

        setSize(message);

        // MouseListener hinzufügen, um den Timer zu ändern
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setTimer(50000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setTimer(oriTimer);
            }
        });

        setBackground(Color.DARK_GRAY);
        setOpaque(false); // Hintergrundfarbe sichtbar machen

        notificationLabel.setForeground(Color.WHITE); // Schriftfarbe
        notificationLabel.setFont(Mainframe.defaultFont);
        add(notificationLabel, BorderLayout.CENTER); // Label zum Panel hinzufügen

        // Panel zur LayeredPane hinzufügen
        Mainframe.layeredPane.add(this, Integer.valueOf(2));
        activeNotifications.add(this);
        Mainframe.updateLocationAndBounds();

        setVisible(true);
        updateUI();
    }

    private void setTimer(int time) {
        this.timer = time;
    }

    private void setSize(String message) {
        // Panel-Größenbeschränkungen festlegen
        String compareMessage = message.replaceAll("<\\W?\\w*>","");
        // Breite und Höhe festlegen
        if (compareMessage.length() < 40 && table.getWidth() > 400) {
            notificationSize = new Dimension(400, 30);
        } else if (compareMessage.length() < 60 && table.getWidth() > 550) {
            notificationSize = new Dimension(550, 30);
        } else if (compareMessage.length() < 80 && table.getWidth() > 700) {
            notificationSize = new Dimension(700, 30);
        } else {
            notificationSize = new Dimension(table.getWidth()-10, 30);
        }
        setPreferredSize(notificationSize);
        setMaximumSize(notificationSize);
        setMinimumSize(notificationSize);
        int notificationIndex = activeNotifications.size();
        int yPos = splitPane.getHeight() - ((notificationIndex * 30) + (notificationIndex * 5));
        setBounds(0, yPos, (int) notificationSize.getWidth(), (int) notificationSize.getHeight());
    }

    public void setText(String text) {
        notificationLabel.setText(text);
        setSize(text);
        updateUI();
        this.timer = this.oriTimer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int arc = 20;
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getBackground());
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));
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