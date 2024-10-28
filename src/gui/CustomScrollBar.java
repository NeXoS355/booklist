package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;

import javax.swing.JButton;
import javax.swing.plaf.basic.BasicScrollBarUI;

import application.HandleConfig;

public class CustomScrollBar extends BasicScrollBarUI {

    @Override
    protected void configureScrollBarColors() {
    	if (HandleConfig.darkmode == 1) {
            this.thumbColor = Color.GRAY;
            this.trackColor = Color.DARK_GRAY;
    	} else {
            this.thumbColor = Color.GRAY;
            this.trackColor = Color.LIGHT_GRAY;
    	}

    }
    @Override
    protected JButton createDecreaseButton(int orientation) {
        // Gibt einen Button mit Groesse (0, 0) zurueck, um den Pfeil zu0 verstecken
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
        // Gibt einen Button mit Groesse (0, 0) zurueck, um den Pfeil zu verstecken
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
