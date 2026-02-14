package gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * DocumentFilter that limits the maximum number of characters in a text field.
 * Silently prevents input beyond the limit — no error, no feedback needed.
 */
public class LengthDocumentFilter extends DocumentFilter {
    private final int maxLength;

    public LengthDocumentFilter(int maxLength) {
        this.maxLength = maxLength;
    }

    @Override
    public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
        if (fb.getDocument().getLength() + string.length() <= maxLength) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
        int newLength = fb.getDocument().getLength() - length + (text != null ? text.length() : 0);
        if (newLength <= maxLength) {
            super.replace(fb, offset, length, text, attrs);
        }
    }
}
