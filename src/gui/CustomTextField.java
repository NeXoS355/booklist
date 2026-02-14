package gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

import javax.swing.JTextField;
import javax.swing.UIManager;

public class CustomTextField extends JTextField {

	@Serial
	private static final long serialVersionUID = 1L;

	public CustomTextField() {
		super();
		init();
	}

	public CustomTextField(String text) {
		super(text);
		init();
	}

	private void init() {
		setFont(Mainframe.defaultFont);

		addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("editable")) {
				if (Boolean.FALSE.equals(evt.getNewValue())) {
					putClientProperty("JComponent.outline", "error");
				} else {
					putClientProperty("JComponent.outline", null);
				}
			}
		});

		addMouseListener(new MouseAdapter() {
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

}
