package gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyTreeCellRenderer(){ 
        openIcon = createImageIcon("resources/User.png"); 
        closedIcon = createImageIcon("resources/User.png");
        textNonSelectionColor=Color.BLACK;
        textSelectionColor=Color.BLACK;
        backgroundNonSelectionColor=Color.WHITE;
        backgroundSelectionColor=Color.LIGHT_GRAY;
        borderSelectionColor=Color.DARK_GRAY;
        setFont(new Font("Roboto", Font.PLAIN, 16));
    } 
	
     
    protected ImageIcon createImageIcon(String path) { 
        java.net.URL imgURL = getClass().getClassLoader().getResource(path); 
        if (imgURL != null) { 
            return new ImageIcon(imgURL); 
        } 
        System.err.println(path + " nicht gefunden!"); 
        return null; 
    } 

}
