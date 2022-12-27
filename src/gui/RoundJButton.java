package gui;

import java.awt.Graphics;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.beans.ConstructorProperties;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;

public class RoundJButton extends JButton {
	
    private Shape shape;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    /**
     * Creates a button with no set text or icon.
     */
    public RoundJButton() {
        this(null, null);
    }
    
    /**
     * Creates a button with text.
     *
     * @param text  the text of the button
     */
    @ConstructorProperties({"text"})
    public RoundJButton(String text) {
        this(text, null);
    }
    
    /**
     * Creates a button with initial text and an icon.
     *
     * @param text  the text of the button
     * @param icon  the Icon image to display on the button
     */
    public RoundJButton(String text, Icon icon) {
        setOpaque(false);
        setContentAreaFilled(false);
        // Create the model
        setModel(new DefaultButtonModel());

        // initialize
        init(text, icon);
    }
    
    
    protected void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        super.paintComponent(g);
   }
   protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        g.drawRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
   }
   public boolean contains(int x, int y) {
        if (shape == null || !shape.getBounds().equals(getBounds())) {
            shape = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 30, 30);
        }
        return shape.contains(x, y);
   }
 
	
}
