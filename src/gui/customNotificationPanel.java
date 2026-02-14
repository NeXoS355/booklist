package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

import com.formdev.flatlaf.util.UIScale;
import static gui.Mainframe.*;

public class customNotificationPanel extends JPanel {

    final JLabel notificationLabel;
    int timer;
    final int oriTimer;
    private float opacity = 0f;
    private int slideOffset;
    private Timer animationTimer;
    private boolean fadingOut = false;

    private static final int MAX_WIDTH = 450;
    private static final int ARC = 12;
    private static final int SLIDE_DISTANCE = 40;

    public customNotificationPanel(String message, int timer) {
        setLayout(new BorderLayout(UIScale.scale(8), 0));
        setBorder(BorderFactory.createEmptyBorder(
                UIScale.scale(6), UIScale.scale(12),
                UIScale.scale(6), UIScale.scale(12)));
        message = message.trim();

        // Icon
        String iconText = message.toLowerCase().contains("fehler") ||
                message.toLowerCase().contains("error") ? "\u26A0" : "\u2713";
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setForeground(new Color(255, 255, 255, 200));
        iconLabel.setFont(Mainframe.defaultFont);
        add(iconLabel, BorderLayout.WEST);

        notificationLabel = new JLabel();
        notificationLabel.setForeground(Color.WHITE);
        notificationLabel.setFont(Mainframe.defaultFont);
        add(notificationLabel, BorderLayout.CENTER);

        this.timer = timer;
        this.oriTimer = timer;

        setBackground(new Color(50, 50, 50));
        setOpaque(false);

        applyText(message);

        // Hover: Timer pausieren
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { customNotificationPanel.this.timer = 50000; }
            @Override
            public void mouseExited(MouseEvent e) { customNotificationPanel.this.timer = oriTimer; }
        });

        // Animation vorbereiten
        slideOffset = UIScale.scale(SLIDE_DISTANCE);
        opacity = 0f;

        // In LayeredPane einfügen und positionieren
        activeNotifications.add(this);
        layeredPane.add(this, Integer.valueOf(2));
        setVisible(true);
        Mainframe.updateLocationAndBounds();

        // Slide-In/Fade-Out Animation
        animationTimer = new Timer(12, e -> {
            if (!fadingOut) {
                opacity = Math.min(1f, opacity + 0.08f);
                slideOffset = Math.max(0, slideOffset - UIScale.scale(3));
                if (opacity >= 1f && slideOffset <= 0) {
                    animationTimer.stop();
                }
            } else {
                opacity = Math.max(0f, opacity - 0.06f);
                if (opacity <= 0f) {
                    animationTimer.stop();
                    removeNotification();
                }
            }
            repaint();
        });
        animationTimer.start();
    }

    private void removeNotification() {
        setVisible(false);
        activeNotifications.remove(this);
        layeredPane.remove(this);
        Mainframe.updateLocationAndBounds();
    }

    public void startFadeOut() {
        fadingOut = true;
        if (!animationTimer.isRunning()) {
            animationTimer.start();
        }
    }

    private void applyText(String message) {
        int maxW = UIScale.scale(MAX_WIDTH);
        int tableW = table.getWidth() - UIScale.scale(32);
        int w = Math.min(maxW, tableW);

        FontMetrics fm = getFontMetrics(Mainframe.defaultFont);
        int padding = UIScale.scale(60);
        int textWidth = fm.stringWidth(message.replaceAll("<\\W?\\w*>", "")) + padding;

        if (textWidth > w) {
            // Mehrzeilig: HTML-Umbruch
            int availW = w - padding;
            notificationLabel.setText("<html><body style='width:" + availW + "px'>" + message + "</body></html>");
        } else {
            notificationLabel.setText(message);
            w = textWidth;
        }

        // Preferred Size setzen, Höhe wird vom LayoutManager bestimmt
        setPreferredSize(new Dimension(w, getPreferredSize().height));
        setSize(w, getPreferredSize().height);
    }

    public void setText(String text) {
        applyText(text);
        Mainframe.updateLocationAndBounds();
        this.timer = this.oriTimer;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.translate(0, slideOffset);

        int arc = UIScale.scale(ARC);

        // Schatten
        g2d.setColor(new Color(0, 0, 0, 40));
        g2d.fill(new RoundRectangle2D.Double(2, 2, getWidth() - 2, getHeight() - 2, arc, arc));

        // Hintergrund
        g2d.setColor(getBackground());
        g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 2, getHeight() - 2, arc, arc));

        g2d.dispose();
    }

    @Override
    protected void paintChildren(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2.translate(0, slideOffset);
        super.paintChildren(g2);
        g2.dispose();
    }

    public void addBookReference(int index) {
        notificationLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
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
