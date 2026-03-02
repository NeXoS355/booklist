package gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AutoCompleteField extends CustomTextField {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int MAX_SUGGESTIONS = 10;

    private final Supplier<List<String>> dataSource;
    private final JPopupMenu suggestionsPopup = new JPopupMenu();
    private List<String> currentMatches = List.of();
    private int selectedIndex = -1;

    public AutoCompleteField(Supplier<List<String>> dataSource) {
        this("", dataSource);
    }

    public AutoCompleteField(String initialText, Supplier<List<String>> dataSource) {
        super(initialText);
        this.dataSource = dataSource;
        init();
    }

    private void init() {
        suggestionsPopup.setFocusable(false);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { showSuggestions(); }
            @Override public void removeUpdate(DocumentEvent e) { showSuggestions(); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!suggestionsPopup.isVisible()) return;
                int count = suggestionsPopup.getComponentCount();
                if (count == 0) return;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN -> {
                        selectedIndex = Math.min(selectedIndex + 1, count - 1);
                        updateSelection();
                        e.consume();
                    }
                    case KeyEvent.VK_UP -> {
                        selectedIndex = Math.max(selectedIndex - 1, -1);
                        updateSelection();
                        e.consume();
                    }
                    case KeyEvent.VK_ENTER -> {
                        if (selectedIndex >= 0 && selectedIndex < currentMatches.size()) {
                            setText(currentMatches.get(selectedIndex));
                            suggestionsPopup.setVisible(false);
                            selectedIndex = -1;
                            e.consume(); // Dialog-ENTER nur blockieren wenn ein Item selektiert ist
                        }
                    }
                    case KeyEvent.VK_ESCAPE -> {
                        suggestionsPopup.setVisible(false);
                        selectedIndex = -1;
                        e.consume(); // Dialog nicht schließen, nur Popup
                    }
                }
            }
        });
    }

    private void showSuggestions() {
        String typedText = getText().trim().toLowerCase();
        suggestionsPopup.setVisible(false);
        suggestionsPopup.removeAll();
        selectedIndex = -1;

        if (typedText.isEmpty()) return;

        currentMatches = dataSource.get().stream()
                .filter(s -> s.toLowerCase().startsWith(typedText))
                .limit(MAX_SUGGESTIONS)
                .collect(Collectors.toList());

        if (currentMatches.isEmpty()) return;

        for (String suggestion : currentMatches) {
            JMenuItem item = new JMenuItem(suggestion);
            item.addActionListener(ev -> {
                setText(suggestion);
                suggestionsPopup.setVisible(false);
                selectedIndex = -1;
            });
            suggestionsPopup.add(item);
        }

        suggestionsPopup.show(this, 0, getHeight());
        // kein grabFocus() — Fokus bleibt korrekt im Textfeld
    }

    private void updateSelection() {
        int count = suggestionsPopup.getComponentCount();
        for (int i = 0; i < count; i++) {
            ((JMenuItem) suggestionsPopup.getComponent(i)).setArmed(i == selectedIndex);
        }
    }
}
