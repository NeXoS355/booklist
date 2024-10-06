package gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import application.HandleConfig;

public class ButtonsFactory {
	
	static BufferedImage imageActive = null;
	static BufferedImage imageInActive = null;
	
    public static JButton createButton (String text) {
        JButton button = new JButton (text);
        button.setContentAreaFilled (false);
        button.setOpaque(true);
        button.setFocusPainted(false);
        

        if (HandleConfig.darkmode == 1) {
        	button.setBackground(Color.DARK_GRAY);
        	button.setForeground(Color.LIGHT_GRAY);
        	
        	if(text.equals("suchen")) {

        		try {
        			imageActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inv.png"));
        			imageInActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inactive.png"));
        			button.setIcon(new ImageIcon(imageInActive));
        		} catch (IOException e1) {
        			e1.printStackTrace();
        		}

        	}
        	
            button.addMouseListener(new MouseAdapter() {
    			
    			@Override
    			public void mouseExited(MouseEvent e) {
    				button.setBackground(Color.DARK_GRAY);
    				button.setForeground(Color.LIGHT_GRAY);
    				
    				if(text.equals("suchen")) {
    					button.setIcon(new ImageIcon(imageInActive));
    				}
    				
    			}
    			
    			@Override
    			public void mouseEntered(MouseEvent e) {
    				button.setBackground(new Color(75,75,75));
    				button.setForeground(Color.WHITE);
    				
    				if(text.equals("suchen")) {
    					button.setIcon(new ImageIcon(imageActive));
    				}
    			}
    			
    		});
        } else {
        	button.setForeground(new Color(75,75,75));
        	
        	if(text.equals("suchen")) {

        		try {
        			imageActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe.png"));
        			imageInActive = ImageIO.read(Mainframe.class.getResource("/resources/lupe_inactive.png"));
        			button.setIcon(new ImageIcon(imageInActive));
        		} catch (IOException e1) {
        			e1.printStackTrace();
        		}

        	}
        	
            button.addMouseListener(new MouseAdapter() {
    			@Override
    			public void mouseExited(MouseEvent e) {
    				button.setBackground(new Color(240,240,240));
    	        	button.setForeground(new Color(75,75,75));
    				
    				if(text.equals("suchen")) {
    					button.setIcon(new ImageIcon(imageInActive));
    				}
    			}
    			
    			@Override
    			public void mouseEntered(MouseEvent e) {
    				button.setBackground(new Color(220,220,220));
    				button.setForeground(Color.BLACK);
    				
    				if(text.equals("suchen")) {
    					button.setIcon(new ImageIcon(imageActive));
    				}
    				
    			}
    			
    		});
        }
        

        
        return button;
    }
}
