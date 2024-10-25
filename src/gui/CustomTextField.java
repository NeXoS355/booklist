package gui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;

public class CustomTextField extends JTextField {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private BoundedRangeModel visibility;

	private Border standardBorder = BorderFactory.createLineBorder(new Color(160, 160, 160, 125), 2);
	private Border activeBorder = BorderFactory.createLineBorder(new Color(160, 160, 160, 200), 4);
	private Border errorBorder = BorderFactory.createLineBorder(new Color(255, 105, 105), 4);

	/**
	 * Constructs a new <code>TextField</code>. A default model is created, the
	 * initial string is <code>null</code>, and the number of columns is set to 0.
	 */
	public CustomTextField() {
		super();
		setOpaque(false);
		setFont(Mainframe.defaultFont);

		addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("editable")) {
					if(evt.getNewValue().equals("true")) {
						setBorder(standardBorder);
					} else {
						setBorder(errorBorder);
					}
				}
			}
		});
		
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				if (isEditable()) {
					setBorder(standardBorder);
				} 
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (isEditable()) {
					setBorder(activeBorder);
				} 
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (!isEditable()) {
					setEditable(true);
					setForeground(UIManager.getColor("TextField.foreground"));
					setBackground(UIManager.getColor("TextField.background"));
					setText("");
				}
			}

		});
	}

	/**
	 * Constructs a new <code>TextField</code> initialized with the specified text.
	 * A default model is created and the number of columns is 0.
	 *
	 * @param text - the text to be displayed, or <code>null</code>
	 */
	public CustomTextField(String text) {
		this(null, text, 0);
		setOpaque(false);
		setFont(Mainframe.defaultFont);
		
		addPropertyChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getPropertyName().equals("editable")) {
					if(evt.getNewValue().equals("true")) {
						setBorder(standardBorder);
					} else {
						setBorder(errorBorder);
					}
				}
			}
		});
		
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseExited(MouseEvent e) {
				setBorder(standardBorder);

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				setBorder(activeBorder);

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (!isEditable()) {
					setEditable(true);
					setForeground(UIManager.getColor("TextField.foreground"));
					setBackground(UIManager.getColor("TextField.background"));
					setText("");
				}
			}

		});

	}

	/**
	 * Constructs a new <code>JTextField</code> that uses the given text storage
	 * model and the given number of columns. This is the constructor through which
	 * the other constructors feed. If the document is <code>null</code>, a default
	 * model is created.
	 *
	 * @param doc     - the text storage to use; if this is <code>null</code>, a
	 *                default will be provided by calling the
	 *                <code>createDefaultModel</code> method
	 * @param text    - the initial string to display, or <code>null</code>
	 * @param columns - the number of columns to use to calculate the preferred
	 *                width &gt;= 0; if <code>columns</code> is set to zero, the
	 *                preferred width will be whatever naturally results from the
	 *                component implementation
	 * @exception IllegalArgumentException if <code>columns</code> &lt; 0
	 */
	public CustomTextField(Document doc, String text, int columns) {
		if (columns < 0) {
			throw new IllegalArgumentException("columns less than zero.");
		}
		visibility = new DefaultBoundedRangeModel();
		visibility.addChangeListener(new ScrollRepainter());
		if (doc == null) {
			doc = createDefaultModel();
		}
		setDocument(doc);
		if (text != null) {
			setText(text);
		}
	}

	class ScrollRepainter implements ChangeListener, Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void stateChanged(ChangeEvent e) {
			repaint();
		}

	}
		
}
