package gui;

import java.awt.Dimension;
import java.io.Serial;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class CustomScrollBar extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {
        this.thumbColor = UIManager.getColor("ScrollBar.thumb");
        this.trackColor = UIManager.getColor("ScrollBar.track");
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return new JButton() {
			@Serial
            private static final long serialVersionUID = 1L;

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 0);
            }
        };
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return new JButton() {
			@Serial
            private static final long serialVersionUID = 1L;

			@Override
            public Dimension getPreferredSize() {
                return new Dimension(0, 0);
            }
        };
    }

}
