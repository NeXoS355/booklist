package gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.tree.DefaultTreeModel;

import com.formdev.flatlaf.util.UIScale;
import application.BorrowStatus;
import application.Book_Booklist;
import application.HandleConfig;
import application.BookListModel;
import application.GetBookInfosFromWeb;
import application.SimpleTableModel;
import data.Database;

/**
 * Dialog to change Entry in Booklist Table and DB
 */
public class Dialog_edit_Booklist extends JDialog {

  @Serial
  private static final long serialVersionUID = 1L;
  private final CustomTextField txtAuthor;
  private final CustomTextField txtTitle;
  private final JCheckBox checkFrom;
  private final CustomTextField txtBorrowedFrom;
  private final JCheckBox checkTo;
  private final CustomTextField txtBorrowedTo;
  private final CustomTextField txtNote;
  private final CustomTextField txtSeries;
  private final CustomTextField txtSeriesVol;
  private final JCheckBox checkEbook;
  private final JButton btnAdd;
  private JLabel lblPic;
  private final StarRatingPanel starRatingPanel;
  private final JLabel lblAckRating;
  private boolean ack = false;
  private final JPanel panelEastRating = new JPanel(new GridBagLayout());
  private JTextArea txtDesc;
  private final Book_Booklist entry;

  /**
   * Dialog Edit Constructor
   *
   * @param owner     - set the owner of this Frame
   * @param bookModel - current entries of the Booktable
   * @param treeModel - current entries of the Authortree
   */
  public Dialog_edit_Booklist(Frame owner, BookListModel bookModel, int index, DefaultTreeModel treeModel) {

    Mainframe.logger.info("Book edit: start creating Frame");
    this.setTitle(Localization.get("t.editBook"));
    this.setSize(new Dimension(UIScale.scale(600), UIScale.scale(685)));
    this.setLocationRelativeTo(owner);
    this.setAlwaysOnTop(true);

    entry = bookModel.getElementAt(index);

    if (HandleConfig.loadOnDemand == 1) {
      BookListModel.loadOnDemand(entry);
    }

    URL ackRatingIconURL = getClass().getResource("/resources/ackRating.png");
    if (ackRatingIconURL == null) {
      Mainframe.logger.error("Resource not found: /resources/ackRating.png");
    }
    ImageIcon ackRating = ackRatingIconURL != null ? new ImageIcon(ackRatingIconURL) : new ImageIcon();

    URL iconURL = getClass().getResource("/resources/Icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      this.setIconImage(icon.getImage());
    } else {
      Mainframe.logger.error("Resource not found: /resources/Icon.png");
    }

    this.setLayout(new BorderLayout(10, 10));

    JPanel panelNorth = new JPanel();
    panelNorth.setLayout(new BorderLayout());

    JPanel panelWest = new JPanel();
    panelWest.setLayout(new GridLayout(4, 1, 10, 20));

    JPanel panelCenter = new JPanel();
    panelCenter.setLayout(new GridBagLayout());

    JPanel panelEastBorder = new JPanel();
    panelEastBorder.setLayout(new BorderLayout(10, 10));

    JPanel panelSouthBorder = new JPanel();
    panelSouthBorder.setLayout(new BorderLayout(10, 10));

    JPanel panelSouth = new JPanel();
    panelSouth.setLayout(new GridLayout(3, 2, 10, 10));

    int height = UIScale.scale(60);
    int width = UIScale.scale(100);

    /*
     * create and add components to Panel North
     */
    Font changeFont = Mainframe.defaultFont.deriveFont((float) Mainframe.defaultFont.getSize() * 0.75f);
    JPanel pnlNorthWest = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

    JLabel lblDate = new JLabel(MessageFormat.format(Localization.get("book.dateAdded"),
        new SimpleDateFormat("dd.MM.yyyy").format(entry.getDate())));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    LocalDate date = entry.getDate().toLocalDateTime().toLocalDate();
    CustomTextField txtDate = new CustomTextField(date.format(formatter));
    txtDate.setFont(changeFont);
    pnlNorthWest.add(lblDate);

    JPanel pnlNorthEast = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    JLabel lblIsbn = new JLabel("ISBN: " + entry.getIsbn(), SwingConstants.RIGHT);
    CustomTextField txtIsbn = new CustomTextField(entry.getIsbn());
    txtIsbn.setFont(changeFont);
    pnlNorthEast.add(lblIsbn);

    JButton btnOkEast = ButtonsFactory.createButton("ok");
    btnOkEast.addActionListener(e -> {
      if (!entry.getIsbn().equals(txtIsbn.getText())) {
        if (bookModel.getElementAt(index).setIsbn(txtIsbn.getText(), true)) {
          lblIsbn.setText("ISBN: " + entry.getIsbn());
          pnlNorthEast.remove(txtIsbn);
          pnlNorthEast.remove(btnOkEast);
          pnlNorthEast.add(lblIsbn);
        } else {
          txtIsbn.setEditable(false);
        }
      } else {
        pnlNorthEast.remove(txtIsbn);
        pnlNorthEast.remove(btnOkEast);
        pnlNorthEast.add(lblIsbn);
      }
      pnlNorthEast.revalidate(); // Löst die Neulayoutierung aus
      pnlNorthEast.repaint(); // Aktualisiert die Anzeige
    });

    JButton btnOkWest = ButtonsFactory.createButton("ok");
    btnOkWest.addActionListener(e -> {
      try {
        LocalDate newDate = LocalDate.parse(txtDate.getText(), formatter);
        if (!date.equals(newDate)) {
          bookModel.getElementAt(index).setDate(Timestamp.valueOf(newDate.atStartOfDay()), true);
          lblDate.setText(MessageFormat.format(Localization.get("book.dateAdded"),
              new SimpleDateFormat("dd.MM.yyyy").format(entry.getDate())));
        }
        pnlNorthWest.remove(txtDate);
        pnlNorthWest.remove(btnOkWest);
        pnlNorthWest.add(lblDate);
      } catch (DateTimeParseException ex) {
        txtDate.setEditable(false);
        Mainframe.logger.warn(ex.getMessage());
      }
      pnlNorthEast.revalidate(); // Löst die Neulayoutierung aus
      pnlNorthEast.repaint(); // Aktualisiert die Anzeige
    });

    lblDate.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemEdit = new JMenuItem(Localization.get("text.edit"));
        JMenuItem itemDel = new JMenuItem(Localization.get("text.delete"));
        menu.add(itemEdit);
        menu.add(itemDel);
        menu.show(lblDate, e.getX(), e.getY());
        itemEdit.addActionListener(e5 -> {
          pnlNorthWest.remove(lblDate);
          pnlNorthWest.add(txtDate);
          pnlNorthWest.add(btnOkWest);
          pnlNorthWest.revalidate(); // Löst die Neulayoutierung aus
          pnlNorthWest.repaint(); // Aktualisiert die Anzeige
        });
        itemDel.addActionListener(e4 -> {
          entry.setIsbn("", true);
          lblDate.setText(MessageFormat.format(Localization.get("book.dateAdded"),
              new SimpleDateFormat("dd.MM.yyyy").format(entry.getDate())));
          pnlNorthWest.revalidate();
          pnlNorthWest.repaint();
        });

      }
    });
    lblIsbn.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemCopy = new JMenuItem(Localization.get("text.copy"));
        JMenuItem itemEdit = new JMenuItem(Localization.get("text.edit"));
        JMenuItem itemDel = new JMenuItem(Localization.get("text.delete"));
        menu.add(itemCopy);
        menu.add(itemEdit);
        menu.add(itemDel);
        menu.show(lblIsbn, e.getX(), e.getY());
        itemCopy.addActionListener(e6 -> {
          Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
          StringSelection selection = new StringSelection(entry.getIsbn());
          cb.setContents(selection, null);
        });
        itemEdit.addActionListener(e5 -> {
          for (Component component : pnlNorthEast.getComponents()) {
            if (component instanceof JLabel) {
              pnlNorthEast.remove(lblIsbn);
              pnlNorthEast.add(btnOkEast);
              pnlNorthEast.add(txtIsbn);
            }
          }
          pnlNorthEast.revalidate();
          pnlNorthEast.repaint();
        });
        itemDel.addActionListener(e4 -> {
          entry.setIsbn("", true);
          lblIsbn.setText("ISBN: ");
          pnlNorthEast.revalidate();
          pnlNorthEast.repaint();
        });

      }
    });

    panelNorth.add(pnlNorthWest, BorderLayout.WEST);
    panelNorth.add(pnlNorthEast, BorderLayout.EAST);
    panelNorth.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    /*
     * create and add components to Panel East
     */
    ImageIcon imgIcn = showImg(entry);
    if (imgIcn != null) {
      BufferedImage originalImage = new BufferedImage(imgIcn.getIconWidth(), imgIcn.getIconHeight(),
          BufferedImage.TYPE_INT_ARGB);
      // Original in BufferedImage zeichnen
      Graphics2D g2d = originalImage.createGraphics();
      g2d.drawImage(imgIcn.getImage(), 0, 0, null);
      g2d.dispose();

      // Höhenqualitative Skalierung
      BufferedImage scaledImage = getScaledImage(originalImage);
      lblPic = new JLabel(new ImageIcon(scaledImage));

      lblPic.setPreferredSize(new Dimension(UIScale.scale(160), UIScale.scale(280)));
      lblPic.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblPic.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (SwingUtilities.isLeftMouseButton(e)) {
            showCoverOverlay(entry);
          } else if (SwingUtilities.isRightMouseButton(e)) {
            showMenu(e);
          }
        }

        private void showMenu(MouseEvent e) {
          JPopupMenu menu = new JPopupMenu();
          JMenuItem itemDelPic = new JMenuItem(Localization.get("book.deletePic"));
          JMenuItem itemChanPic = new JMenuItem(Localization.get("book.editPic"));
          menu.add(itemChanPic);
          menu.add(itemDelPic);
          menu.show(lblPic, e.getX(), e.getY());
          itemChanPic.addActionListener(e4 -> {
            String webpage = JOptionPane.showInputDialog(null, Localization.get("book.changePic"));
            if (webpage != null && !webpage.isEmpty()) {
              GetBookInfosFromWeb.getBookInfoFromGoogleApiWebRequest(entry, 2, false);
              updateCoverDisplay(entry, panelEastBorder);
            }
          });
          itemDelPic.addActionListener(e3 -> {
            boolean state = GetBookInfosFromWeb.deletePic(entry.getBid());
            if (state) {
              entry.setPic(null);
              updateCoverDisplay(entry, panelEastBorder);
            } else {
              JOptionPane.showMessageDialog(null, "an error occurred");
            }
          });

        }
      });
      panelEastBorder.add(lblPic, BorderLayout.CENTER);

    } else {
      JButton btnDownloadInfo = ButtonsFactory.createButton("Download Info");
      btnDownloadInfo.addActionListener(arg0 -> Mainframe.executor.submit(() -> {
        int compResult1;
        int compResult2;
        compResult1 = GetBookInfosFromWeb.getBookInfoFromGoogleApiWebRequest(entry, 2, false);
        if (compResult1 < 75) {
          compResult2 = GetBookInfosFromWeb.getBookInfoFromGoogleApiWebRequest(entry, 2, true);
          if (compResult1 > compResult2) {
            GetBookInfosFromWeb.getBookInfoFromGoogleApiWebRequest(entry, 2, true);
          }
        }
        SwingUtilities.invokeLater(() -> {
          updateCoverDisplay(entry, panelEastBorder);
          if (entry.getIsbn() != null && !entry.getIsbn().isEmpty()) {
            lblIsbn.setText("ISBN: " + entry.getIsbn());
            pnlNorthEast.revalidate();
            pnlNorthEast.repaint();
          }
          if (txtDesc != null && entry.getDesc() != null && !entry.getDesc().isEmpty()) {
            txtDesc.setText(entry.getDesc());
          }
        });
      }));
      panelEastBorder.add(btnDownloadInfo, BorderLayout.CENTER);
    }

    /*
     * create and add components to Rating Panel
     */
    starRatingPanel = new StarRatingPanel();
    starRatingPanel.setRating(entry.getRating());

    starRatingPanel.setRatingChangeListener(newRating -> {
      if (!ack) {
        int segment = (int) (newRating * 2);
        setRating(segment);
      }
    });

    starRatingPanel.setRightClickListener(() -> {
      JPopupMenu menu = new JPopupMenu();
      JMenuItem itemDeleteRating = new JMenuItem(Localization.get("book.deleteRating"));
      menu.add(itemDeleteRating);
      menu.show(starRatingPanel, starRatingPanel.getWidth() / 2, starRatingPanel.getHeight() / 2);
      itemDeleteRating.addActionListener(e2 -> {
        if (Objects.equals(e2.getActionCommand(), Localization.get("book.deleteRating"))) {
          setRating(0);
          starRatingPanel.setRating(0);
        }
      });
    });

    lblAckRating = new JLabel(ackRating);
    lblAckRating.setVisible(false);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    panelEastRating.add(starRatingPanel, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.SOUTH;
    panelEastRating.add(lblAckRating, gbc);

    panelEastRating.setAlignmentX(0);

    panelEastBorder.add(panelEastRating, BorderLayout.SOUTH);

    /*
     * create and add components to Panel Center
     */
    JLabel lblAuthor = new JLabel(Localization.get("label.author") + ":");
    lblAuthor.setFont(Mainframe.defaultFont);
    lblAuthor.setPreferredSize(new Dimension(width, height));

    txtAuthor = new CustomTextField(entry.getAuthor());
    txtAuthor.setPreferredSize(new Dimension(50, height));
    ((AbstractDocument) txtAuthor.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));

    txtAuthor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ENTER -> save(entry); // Speichern
          case KeyEvent.VK_ESCAPE -> dispose(); // Abbrechen
        }
      }
    });
    JPopupMenu suggestionsPopup = new JPopupMenu();
    txtAuthor.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        showSuggestions();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        showSuggestions();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        // not used
      }

      private void showSuggestions() {
        String typedText = txtAuthor.getText().trim();

        // Popup zurücksetzen
        suggestionsPopup.setVisible(false);
        suggestionsPopup.removeAll();

        if (typedText.isEmpty()) {
          return; // Keine Eingabe -> nichts tun
        }

        String[] suggestions = autoCompletion(typedText, "author");

        if (suggestions.length == 0) {
          return; // Keine Vorschläge -> nichts anzeigen
        }

        // Vorschläge hinzufügen
        for (String suggestion : suggestions) {
          JMenuItem item = new JMenuItem(suggestion);
          item.addActionListener(e -> {
            txtAuthor.setText(suggestion);
            suggestionsPopup.setVisible(false);
          });
          suggestionsPopup.add(item);
        }

        // Popup anzeigen
        suggestionsPopup.show(txtAuthor, 0, txtAuthor.getHeight());
        txtAuthor.grabFocus();
      }
    });

    JLabel lblTitle = new JLabel(Localization.get("label.title") + ":");
    lblTitle.setFont(Mainframe.defaultFont);
    lblTitle.setPreferredSize(new Dimension(width, height));

    txtTitle = new CustomTextField(entry.getTitle());
    txtTitle.setPreferredSize(new Dimension(50, height));
    ((AbstractDocument) txtTitle.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtTitle.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          save(entry);
        } else if (!e.isActionKey()) {
          if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
            txtTitle.setText("");
            btnAdd.setEnabled(true);
          }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }
    });

    JLabel lblNote = new JLabel(Localization.get("label.note") + ":");
    lblNote.setFont(Mainframe.defaultFont);
    lblNote.setPreferredSize(new Dimension(width, height));

    txtNote = new CustomTextField(entry.getNote());
    txtNote.setPreferredSize(new Dimension(50, height));
    ((AbstractDocument) txtNote.getDocument()).setDocumentFilter(new LengthDocumentFilter(100));
    txtNote.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          save(entry);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });

    JLabel lblSeries = new JLabel(Localization.get("label.series") + " | " + Localization.get("label.vol") + ":");
    lblSeries.setFont(Mainframe.defaultFont);
    lblSeries.setPreferredSize(new Dimension(width, height));

    txtSeries = new CustomTextField(entry.getSeries());
    txtSeries.setPreferredSize(new Dimension(50, height));
    ((AbstractDocument) txtSeries.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtSeries.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case KeyEvent.VK_ENTER -> save(entry); // Speichern
          case KeyEvent.VK_ESCAPE -> dispose(); // Abbrechen
        }
      }
    });
    txtSeries.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        showSuggestions();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        showSuggestions();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        // not used
      }

      private void showSuggestions() {
        String typedText = txtSeries.getText().trim();

        // Popup zurücksetzen
        suggestionsPopup.setVisible(false);
        suggestionsPopup.removeAll();

        if (typedText.isEmpty()) {
          return; // Keine Eingabe -> nichts tun
        }

        String[] suggestions = autoCompletion(typedText, "series");

        if (suggestions.length == 0) {
          return; // Keine Vorschläge -> nichts anzeigen
        }

        // Vorschläge hinzufügen
        for (String suggestion : suggestions) {
          JMenuItem item = new JMenuItem(suggestion);
          item.addActionListener(e -> {
            txtSeries.setText(suggestion);
            suggestionsPopup.setVisible(false);
          });
          suggestionsPopup.add(item);
        }

        // Popup anzeigen
        suggestionsPopup.show(txtSeries, 0, txtSeries.getHeight());
        txtSeries.grabFocus();
      }
    });

    txtSeriesVol = new CustomTextField(entry.getSeriesVol());
    txtSeriesVol.setPreferredSize(new Dimension(50, height));
    ((AbstractDocument) txtSeriesVol.getDocument()).setDocumentFilter(new LengthDocumentFilter(2));
    txtSeriesVol.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          save(entry);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });

    JLabel lblEbook = new JLabel("E-Book:");
    lblEbook.setFont(Mainframe.defaultFont);
    lblEbook.setPreferredSize(new Dimension(width, height));

    checkEbook = new JCheckBox();
    checkEbook.setFont(Mainframe.defaultFont);
    checkEbook.setSelected(entry.isEbook());

    /*
     * Set Center Layout
     */
    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.05;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(10, 0, 0, 0);
    c.ipady = 15;
    panelCenter.add(lblAuthor, c);
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.5;
    c.gridwidth = 9;
    panelCenter.add(txtAuthor, c);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lblTitle, c);
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.5;
    c.gridwidth = 9;
    panelCenter.add(txtTitle, c);
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lblNote, c);
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.5;
    c.gridwidth = 9;
    panelCenter.add(txtNote, c);
    c.gridx = 0;
    c.gridy = 3;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lblSeries, c);
    c.gridx = 1;
    c.gridy = 3;
    c.weightx = 0.5;
    c.gridwidth = 8;
    panelCenter.add(txtSeries, c);
    c.gridx = 9;
    c.gridy = 3;
    c.weightx = 0.1;
    c.gridwidth = 1;
    c.insets = new Insets(10, 10, 0, 0);
    panelCenter.add(txtSeriesVol, c);
    c.gridx = 0;
    c.gridy = 4;
    c.weightx = 0.05;
    c.gridwidth = 1;
    c.insets = new Insets(10, 0, 0, 0);
    panelCenter.add(lblEbook, c);
    c.gridx = 1;
    c.gridy = 4;
    c.weightx = 0.5;
    c.gridwidth = 1;
    panelCenter.add(checkEbook, c);
    panelCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    /*
     * create components for Panel South
     */
    checkFrom = new JCheckBox(Localization.get("label.borrowed_from"));
    checkFrom.setFont(Mainframe.defaultFont);
    if (!entry.getBorrowedFrom().isEmpty())
      checkFrom.setSelected(true);
    checkFrom.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (checkFrom.isSelected()) {
          checkTo.setSelected(false);
          txtBorrowedTo.setVisible(false);
          txtBorrowedFrom.setText("");
          txtBorrowedFrom.setVisible(true);
        } else {
          txtBorrowedFrom.setVisible(false);
        }

      }
    });

    checkTo = new JCheckBox(Localization.get("label.borrowed_to"));
    checkTo.setFont(Mainframe.defaultFont);
    if (!entry.getBorrowedTo().isEmpty())
      checkTo.setSelected(true);
    checkTo.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        if (checkTo.isSelected()) {
          checkFrom.setSelected(false);
          txtBorrowedFrom.setVisible(false);
          txtBorrowedTo.setText("");
          txtBorrowedTo.setVisible(true);
        } else {
          txtBorrowedTo.setVisible(false);
        }
      }
    });

    txtBorrowedFrom = new CustomTextField(entry.getBorrowedFrom());
    if (entry.getBorrowedFrom().isEmpty())
      txtBorrowedFrom.setVisible(false);
    ((AbstractDocument) txtBorrowedFrom.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtBorrowedFrom.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          save(entry);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });

    txtBorrowedTo = new CustomTextField(entry.getBorrowedTo());
    if (entry.getBorrowedTo().isEmpty())
      txtBorrowedTo.setVisible(false);
    ((AbstractDocument) txtBorrowedTo.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtBorrowedTo.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          save(entry);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });

    btnAdd = ButtonsFactory.createButton(Localization.get("label.save"));
    btnAdd.setFont(Mainframe.defaultFont);
    btnAdd.addActionListener(e -> save(entry));

    JButton btnAbort = ButtonsFactory.createButton(Localization.get("label.abort"));
    btnAbort.setFont(Mainframe.defaultFont);
    btnAbort.addActionListener(arg0 -> dispose());

    /*
     * add components into Panel South
     */
    panelSouth.add(checkFrom);
    panelSouth.add(checkTo);
    panelSouth.add(txtBorrowedFrom);
    panelSouth.add(txtBorrowedTo);
    panelSouth.add(btnAdd);
    panelSouth.add(btnAbort);
    panelSouth.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    /*
     * create TextArea for Description
     */
    txtDesc = new JTextArea(10, 30);
    txtDesc.setText(entry.getDesc());
    txtDesc.setEnabled(false);
    txtDesc.setLineWrap(true);
    txtDesc.setWrapStyleWord(true);
    txtDesc.setFont(Mainframe.descFont);
    // txtDesc.setDisabledTextColor(Color.BLACK);
    int baseDescSize = com.formdev.flatlaf.util.UIScale.unscale(Mainframe.descFont.getSize());
    this.setSize(this.getWidth(), this.getHeight() + (baseDescSize - 16) * 14);
    JScrollPane scrollDesc = new JScrollPane(txtDesc, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JScrollBar tableVerticalScrollBar = scrollDesc.getVerticalScrollBar();
    tableVerticalScrollBar.setUI(new CustomScrollBar());

    txtDesc.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          showMenu(e);
        }
      }

      private void showMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem itemDelDesc = new JMenuItem(Localization.get("book.deleteDesc"));
        menu.add(itemDelDesc);
        menu.show(txtDesc, e.getX(), e.getY());
        itemDelDesc.addActionListener(e1 -> {
          boolean state = Database.delDesc(entry.getBid());
          if (state) {
            entry.setDesc(null, true);
            txtDesc.setText("");
            txtDesc.revalidate();
            txtDesc.repaint();
          } else {
            JOptionPane.showMessageDialog(null, "An Error occurred");
          }
        });

      }
    });

    panelSouthBorder.add(scrollDesc, BorderLayout.SOUTH);
    panelSouthBorder.add(panelSouth, BorderLayout.CENTER);

    this.add(panelNorth, BorderLayout.NORTH);
    this.add(panelCenter, BorderLayout.CENTER);
    this.add(panelEastBorder, BorderLayout.EAST);
    this.add(panelSouthBorder, BorderLayout.SOUTH);

    Mainframe.logger.info("Book edit: Frame successfully created");
    this.setVisible(true);
    this.setResizable(false);

  }

  /**
   * add the autoComplete feature to "autor" and "serie"
   *
   * @param search - currently typed String
   * @param field  - sets variable based on which field is active
   * @return String array with matching authors or series
   */
  public String[] autoCompletion(String search, String field) {
    // Eingabe validieren
    if (search == null || search.isEmpty() || field == null) {
      return new String[0];
    }
    search = search.trim().toLowerCase();
    List<String> suggestions;
    if (field.equals("author")) {
      String finalSearch = search;
      suggestions = Mainframe.allEntries.authors.stream()
          .filter(author -> author.toLowerCase().startsWith(finalSearch))
          .collect(Collectors.toList());
    } else if (field.equals("series")) {
      String author = txtAuthor.getText();
      if (author == null || author.isEmpty()) {
        return new String[0];
      }
      String[] series = Mainframe.allEntries.getSeriesFromAuthor(author);
      suggestions = new ArrayList<>();
      for (String s : series) {
        if (s.toLowerCase().startsWith(search)) {
          suggestions.add(s);
        }
      }
    } else {
      return new String[0];
    }
    return suggestions.toArray(new String[0]);
  }

  /**
   * save new or updates Entry in Booklist and DB
   *
   * @param entry - new Booklist entry
   */
  public void save(Book_Booklist entry) {
    int bid = entry.getBid();
    String oldAutor = entry.getAuthor();
    String oldTitel = entry.getTitle();
    String oldNote = entry.getNote();
    String oldSeries = entry.getSeries();
    String oldSeriesVol = entry.getSeriesVol();
    boolean oldEbook = entry.isEbook();
    boolean oldBorrowed = entry.isBorrowed();

    String oldNameBorrowedFrom = entry.getBorrowedFrom();
    String oldNameBorrowedTo = entry.getBorrowedTo();

    String newAuthor = txtAuthor.getText().trim();
    String newTitle = txtTitle.getText().trim();
    String newNote = txtNote.getText().trim();
    String newSeries = txtSeries.getText().trim();
    String newSeriesVol = txtSeriesVol.getText();
    boolean newEbook = checkEbook.isSelected();
    boolean newBorrwoedTo = checkTo.isSelected();
    boolean newBorrwoedFrom = checkFrom.isSelected();

    if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
      if (checkInput(newAuthor, newTitle, Mainframe.allEntries.getIndexOf(oldAutor, oldTitel))) {
        if (!oldAutor.equals(newAuthor)) {
          entry.setAuthor(newAuthor);
          Database.updateBooklistEntry(bid, "author", newAuthor);
        }
        if (!oldTitel.equals(newTitle)) {
          entry.setTitle(newTitle);
          Database.updateBooklistEntry(bid, "title", newTitle);
        }
        if (!oldNote.equals(newNote)) {
          entry.setNote(newNote);
          Database.updateBooklistEntry(bid, "note", newNote);
        }
        if (!oldSeries.equals(newSeries)) {
          entry.setSeries(newSeries);
          Database.updateBooklistEntry(bid, "series", newSeries);
        }
        if (!oldSeriesVol.equals(newSeriesVol)) {
          entry.setSeriesVol(newSeriesVol);
          Database.updateBooklistEntry(bid, "series_vol", newSeriesVol);
        }
        if (oldEbook != newEbook) {
          entry.setEbook(newEbook);
          String ebook_str = "0";
          if (newEbook)
            ebook_str = "1";
          Database.updateBooklistEntry(bid, "ebook", ebook_str);
        }
        if (oldBorrowed) {
          if (newBorrwoedTo && oldNameBorrowedTo.isEmpty()) {
            entry.setBorrowedTo(txtBorrowedTo.getText());
            Database.updateBooklistEntry(bid, "borrow_status", BorrowStatus.LENT_TO.getDbValue());
            Database.updateBooklistEntry(bid, "borrower", txtBorrowedTo.getText());
          }
          if (newBorrwoedFrom && oldNameBorrowedFrom.isEmpty()) {
            entry.setBorrowedFrom(txtBorrowedFrom.getText());
            Database.updateBooklistEntry(bid, "borrow_status", BorrowStatus.BORROWED_FROM.getDbValue());
            Database.updateBooklistEntry(bid, "borrower", txtBorrowedFrom.getText());
          }
          if (!newBorrwoedTo && !newBorrwoedFrom) {
            entry.setBorrowed(false);
            entry.setBorrowedFrom("");
            entry.setBorrowedTo("");
            Database.updateBooklistEntry(bid, "borrow_status", BorrowStatus.NONE.getDbValue());
            Database.updateBooklistEntry(bid, "borrower", "");
          }
        }
        if (!oldBorrowed) {
          if (newBorrwoedTo) {
            entry.setBorrowed(true);
            entry.setBorrowedTo(txtBorrowedTo.getText());
            Database.updateBooklistEntry(bid, "borrow_status", BorrowStatus.LENT_TO.getDbValue());
            Database.updateBooklistEntry(bid, "borrower", txtBorrowedTo.getText());
          } else if (newBorrwoedFrom) {
            entry.setBorrowed(true);
            entry.setBorrowedFrom(txtBorrowedFrom.getText());
            Database.updateBooklistEntry(bid, "borrow_status", BorrowStatus.BORROWED_FROM.getDbValue());
            Database.updateBooklistEntry(bid, "borrower", txtBorrowedFrom.getText());
          }
        }
        Mainframe.logger.info("Book edit: {}-{}", entry.getAuthor(), entry.getTitle());

        Mainframe.showNotification(
            MessageFormat.format(Localization.get("book.edited"), entry.getAuthor(), entry.getTitle()));
        dispose();
      } else {
        Mainframe.logger.info("Book edit: already exist!");
        txtTitle.setText(Localization.get("text.duplicateError"));
        txtTitle.setBackground(new Color(255, 105, 105));
        btnAdd.setEnabled(false);
      }
    } else {
      if (txtAuthor.getText().isEmpty()) {
        Mainframe.logger.info("Book edit: Autor nicht gesetzt!");
        txtAuthor.setBackground(new Color(255, 105, 105));
      }
      if (txtTitle.getText().isEmpty()) {
        Mainframe.logger.info("Book edit: Titel nicht gesetzt!");
        txtTitle.setBackground(new Color(255, 105, 105));
      }
    }
    Mainframe.allEntries.checkAuthors();
    Mainframe.updateModel();
    Mainframe.logger.info("Book edit: saved");
  }

  /**
   * Check New Autor & Titel if there already exists the same
   *
   * @param newAuthor - full author name
   * @param newTitle  - book title
   * @param index     - index of the Book to update
   * @return "false" if already exists else "true"
   */
  public boolean checkInput(String newAuthor, String newTitle, int index) {
    for (int i = 0; i < Mainframe.allEntries.getSize(); i++) {
      Book_Booklist eintrag = Mainframe.allEntries.getElementAt(i);
      if (eintrag.getAuthor().equals(newAuthor) && eintrag.getTitle().equals(newTitle)) {
        if (i != index) {
          Mainframe.logger.info("Book edit: Autor & Titel bereits vorhanden");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * shows the image from a Booklist Entry
   *
   * @param entry - Booklist Entry
   * @return ImageIcon of saved Bookcover
   */
  public ImageIcon showImg(Book_Booklist entry) {
    Image img = null;
    try {
      img = entry.getPic();
    } catch (Exception e) {
      Mainframe.logger.error(e.getMessage());
    }
    if (img != null) {
      return new ImageIcon(img);
    } else {
      return null;
    }

  }

  /**
   * Zeigt das Cover vergroessert als Overlay ueber dem Dialog an.
   * Klick auf das Overlay schliesst es wieder.
   */
  private void showCoverOverlay(Book_Booklist entry) {
    Image img = entry.getPic();
    if (img == null) return;

    JPanel overlay = new JPanel() {
      @Serial
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // Hintergrund abdunkeln
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Bild skalieren mit Seitenverhaeltnis
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        int padding = 40;
        int maxW = getWidth() - padding * 2;
        int maxH = getHeight() - padding * 2;
        double scale = Math.min((double) maxW / imgW, (double) maxH / imgH);
        int drawW = (int) (imgW * scale);
        int drawH = (int) (imgH * scale);
        int x = (getWidth() - drawW) / 2;
        int y = (getHeight() - drawH) / 2;
        g2.drawImage(img, x, y, drawW, drawH, null);

        // Bildgroesse und Dateigroesse dezent einblenden
        String sizeText = imgW + " × " + imgH + " px";
        int kb = entry.getPicSizeBytes() / 1024;
        if (kb > 0) sizeText += "  •  " + kb + " KB";
        g2.setFont(g2.getFont().deriveFont(11f));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(sizeText);
        int textH = fm.getAscent();
        int labelX = x + drawW - textW - 8;
        int labelY = y + drawH - 6;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(labelX - 4, labelY - textH - 2, textW + 8, textH + 6, 6, 6);
        g2.setColor(new Color(220, 220, 220, 200));
        g2.drawString(sizeText, labelX, labelY);
        g2.dispose();
      }
    };
    overlay.setOpaque(false);
    overlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    overlay.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        getRootPane().setGlassPane(new JPanel());
        getRootPane().getGlassPane().setVisible(false);
      }
    });

    getRootPane().setGlassPane(overlay);
    overlay.setVisible(true);
  }

  /**
   * Aktualisiert die Cover-Anzeige im East-Panel ohne Dialog-Neuaufbau.
   */
  private void updateCoverDisplay(Book_Booklist entry, JPanel panelEastBorder) {
    // Vorherige Komponente im CENTER entfernen
    BorderLayout layout = (BorderLayout) panelEastBorder.getLayout();
    Component center = layout.getLayoutComponent(BorderLayout.CENTER);
    if (center != null) {
      panelEastBorder.remove(center);
    }

    ImageIcon imgIcn = showImg(entry);
    if (imgIcn != null) {
      BufferedImage originalImage = new BufferedImage(imgIcn.getIconWidth(), imgIcn.getIconHeight(),
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = originalImage.createGraphics();
      g2d.drawImage(imgIcn.getImage(), 0, 0, null);
      g2d.dispose();
      BufferedImage scaledImage = getScaledImage(originalImage);
      lblPic = new JLabel(new ImageIcon(scaledImage));
      lblPic.setPreferredSize(new Dimension(UIScale.scale(160), UIScale.scale(280)));
      panelEastBorder.add(lblPic, BorderLayout.CENTER);
    } else {
      lblPic = null;
      JLabel placeholder = new JLabel("No Cover", SwingConstants.CENTER);
      panelEastBorder.add(placeholder, BorderLayout.CENTER);
    }

    panelEastBorder.revalidate();
    panelEastBorder.repaint();
  }

  /**
   * sets the definied Rating of Booklist entry and shows Acknowldge Icon
   *
   * @param segment - rating to set according to mouse position
   */
  private void setRating(int segment) {

    Mainframe.logger.info("Rating set: {}", segment);
    entry.setRating((double) segment / 2, true);
    starRatingPanel.setRating(entry.getRating());

    // Rating-Wert direkt in der Tabelle aktualisieren
    String ratingStr = segment > 0 ? Double.toString(entry.getRating()) : "";
    int ratingCol = -1;
    int authorCol = -1;
    int titleCol = -1;
    for (int c = 0; c < SimpleTableModel.columnKeys.length; c++) {
      switch (SimpleTableModel.columnKeys[c]) {
        case SimpleTableModel.KEY_RATING -> ratingCol = c;
        case SimpleTableModel.KEY_AUTHOR -> authorCol = c;
        case SimpleTableModel.KEY_TITLE -> titleCol = c;
      }
    }
    if (ratingCol >= 0 && authorCol >= 0 && titleCol >= 0) {
      for (int row = 0; row < Mainframe.table.getRowCount(); row++) {
        if (entry.getAuthor().equals(Mainframe.table.getValueAt(row, authorCol))
            && entry.getTitle().equals(Mainframe.table.getValueAt(row, titleCol))) {
          Mainframe.table.setValueAt(ratingStr, row, ratingCol);
          break;
        }
      }
    }

    Mainframe.executor.submit(() -> {
      try {
        ack = true;
        starRatingPanel.setEnabled(false);
        lblAckRating.setVisible(true);
        Thread.sleep(2000);
        lblAckRating.setVisible(false);
        starRatingPanel.setEnabled(true);
        panelEastRating.repaint();
        ack = false;
      } catch (InterruptedException e1) {
        Mainframe.logger.error(e1.getMessage());
      }
    });
  }

  private static BufferedImage getScaledImage(BufferedImage original) {
    int targetWidth = 128;
    int targetHeight = 203;

    // Mehrstufige Skalierung für bessere Qualität
    BufferedImage scaled = original;
    int currentWidth = original.getWidth();
    int currentHeight = original.getHeight();

    // Schrittweise Verkleinerung für bessere Qualität
    while (currentWidth > targetWidth * 2 || currentHeight > targetHeight * 2) {
      currentWidth = Math.max(currentWidth / 2, targetWidth);
      currentHeight = Math.max(currentHeight / 2, targetHeight);

      BufferedImage temp = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = temp.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.drawImage(scaled, 0, 0, currentWidth, currentHeight, null);
      g2.dispose();

      scaled = temp;
    }

    // Finale Skalierung auf die Zielgröße
    BufferedImage finalImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = finalImage.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.drawImage(scaled, 0, 0, targetWidth, targetHeight, null);
    g2.dispose();

    return finalImage;
  }


}
