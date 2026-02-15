package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.net.URL;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.AbstractDocument;

import com.formdev.flatlaf.util.UIScale;
import application.Book_Booklist;
import application.Book_Wishlist;
import application.WishlistListModel;
import data.Database;

public class Dialog_edit_Wishlist extends JDialog {

  @Serial
  private static final long serialVersionUID = 1L;
  private final CustomTextField txtAuthor;
  private final CustomTextField txtTitle;
  private final CustomTextField txtNote;
  private final CustomTextField txtSeries;
  private final CustomTextField txtSeriesVol;
  private final Border standardBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 125), 2);
  private final Border activeBorder = BorderFactory.createLineBorder(new Color(70, 130, 180, 200), 4);

  public Dialog_edit_Wishlist(Frame owner, WishlistListModel entries, int index) {
    Mainframe.logger.info("Wishlist Book edit: start creating Frame");
    this.setTitle(Localization.get("t.editBook"));
    this.setSize(new Dimension(UIScale.scale(500), UIScale.scale(330)));
    this.setLocationRelativeTo(owner);
    this.setAlwaysOnTop(true);

    Book_Wishlist eintrag = entries.getElementAt(index);

    URL iconURL = getClass().getResource("/resources/Icon.png");
    if (iconURL != null) {
      ImageIcon icon = new ImageIcon(iconURL);
      this.setIconImage(icon.getImage());
    } else {
      Mainframe.logger.error("Resource not found: /resources/Icon.png");
    }

    this.setLayout(new BorderLayout(10, 10));

    JPanel panelNorth = new JPanel();
    panelNorth.setLayout(new GridLayout(1, 1));
    panelNorth.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    JPanel panelWest = new JPanel();
    panelWest.setLayout(new GridLayout(4, 1, 10, 20));

    JPanel panelCenter = new JPanel();
    panelCenter.setLayout(new GridBagLayout());

    JPanel panelEastBorder = new JPanel();
    panelEastBorder.setLayout(new BorderLayout(10, 10));

    JPanel panel_east_grid = new JPanel();
    panel_east_grid.setLayout(new GridLayout(4, 1, 10, 20));
    panelEastBorder.add(panel_east_grid, BorderLayout.WEST);

    JPanel panelSouth = new JPanel();
    panelSouth.setLayout(new GridLayout(1, 2, 10, 10));
    // panel_south.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    int height = UIScale.scale(60);
    int width = UIScale.scale(100);

    JLabel lbl_datum = new JLabel(MessageFormat.format(Localization.get("book.dateAdded"),
        new SimpleDateFormat("dd.MM.yyyy").format(eintrag.getDate())));
    panelNorth.add(lbl_datum);

    // Empty Panel top Gap
    JLabel lbl_empty1 = new JLabel("");
    lbl_empty1.setFont(Mainframe.defaultFont);
    lbl_empty1.setPreferredSize(new Dimension(width, 10));
    panelNorth.add(lbl_empty1);
    // Ende topGap

    /*
     * create and add components to Panel Center
     */
    JLabel lbl_author = new JLabel(Localization.get("label.author") + ":");
    lbl_author.setFont(Mainframe.defaultFont);
    lbl_author.setPreferredSize(new Dimension(width, height));
    txtAuthor = new CustomTextField(eintrag.getAuthor());
    txtAuthor.setPreferredSize(new Dimension(50, height));
    txtAuthor.setBorder(standardBorder);
    ((AbstractDocument) txtAuthor.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtAuthor.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          speichern(eintrag);
        else if (!e.isActionKey()) {
          txtAuthor.setBackground(UIManager.getColor("TextField.background"));
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }
    });
    txtAuthor.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        txtAuthor.setBorder(standardBorder);

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        txtAuthor.setBorder(activeBorder);

      }

    });
    JLabel lbl_title = new JLabel(Localization.get("label.title") + ":");
    lbl_title.setFont(Mainframe.defaultFont);
    lbl_title.setPreferredSize(new Dimension(width, height));
    txtTitle = new CustomTextField(eintrag.getTitle());
    txtTitle.setPreferredSize(new Dimension(50, height));
    txtTitle.setBorder(standardBorder);
    ((AbstractDocument) txtTitle.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtTitle.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          speichern(eintrag);
        } else if (!e.isActionKey()) {
          if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
            txtTitle.setText("");
          }
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });
    txtTitle.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        txtTitle.setBorder(standardBorder);

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        txtTitle.setBorder(activeBorder);

      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (txtTitle.getText().equals(Localization.get("text.duplicateError"))) {
          txtTitle.setForeground(UIManager.getColor("TextField.foreground"));
          txtTitle.setBackground(UIManager.getColor("TextField.background"));
          txtTitle.setText("");
        }
      }

    });

    JLabel lbl_merk = new JLabel(Localization.get("label.note") + ":");
    lbl_merk.setFont(Mainframe.defaultFont);
    lbl_merk.setPreferredSize(new Dimension(width, height));

    txtNote = new CustomTextField(eintrag.getNote());
    txtNote.setPreferredSize(new Dimension(50, height));
    txtNote.setBorder(standardBorder);
    ((AbstractDocument) txtNote.getDocument()).setDocumentFilter(new LengthDocumentFilter(100));
    txtNote.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          speichern(eintrag);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });
    txtNote.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        txtNote.setBorder(standardBorder);

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        txtNote.setBorder(activeBorder);

      }

    });

    JLabel lbl_serie = new JLabel(Localization.get("label.series") + " | " + Localization.get("label.vol") + ":");
    lbl_serie.setFont(Mainframe.defaultFont);
    lbl_serie.setPreferredSize(new Dimension(width, height));

    txtSeries = new CustomTextField(eintrag.getSeries());
    txtSeries.setPreferredSize(new Dimension(50, height));
    txtSeries.setBorder(standardBorder);
    ((AbstractDocument) txtSeries.getDocument()).setDocumentFilter(new LengthDocumentFilter(50));
    txtSeries.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          speichern(eintrag);
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });
    txtSeries.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        txtSeries.setBorder(standardBorder);

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        txtSeries.setBorder(activeBorder);

      }

    });

    txtSeriesVol = new CustomTextField(eintrag.getSeriesVol());
    txtSeriesVol.setPreferredSize(new Dimension(50, height));
    txtSeriesVol.setBorder(standardBorder);
    ((AbstractDocument) txtSeriesVol.getDocument()).setDocumentFilter(new LengthDocumentFilter(2));
    txtSeriesVol.addKeyListener(new KeyAdapter() {

      @Override
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
          speichern(eintrag);
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
          dispose();
      }

    });
    txtSeriesVol.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseExited(MouseEvent e) {
        txtSeriesVol.setBorder(standardBorder);

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        txtSeriesVol.setBorder(activeBorder);

      }

    });

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
    panelCenter.add(lbl_author, c);
    c.gridx = 1;
    c.gridy = 0;
    c.weightx = 0.5;
    c.gridwidth = 2;
    panelCenter.add(txtAuthor, c);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lbl_title, c);
    c.gridx = 1;
    c.gridy = 1;
    c.weightx = 0.5;
    c.gridwidth = 2;
    panelCenter.add(txtTitle, c);
    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lbl_merk, c);
    c.gridx = 1;
    c.gridy = 2;
    c.weightx = 0.5;
    c.gridwidth = 2;
    panelCenter.add(txtNote, c);
    c.gridx = 0;
    c.gridy = 3;
    c.weightx = 0.05;
    c.gridwidth = 1;
    panelCenter.add(lbl_serie, c);
    c.gridx = 1;
    c.gridy = 3;
    c.weightx = 0.5;
    c.gridwidth = 1;
    panelCenter.add(txtSeries, c);
    c.gridx = 2;
    c.gridy = 3;
    c.weightx = 0.1;
    c.gridwidth = 1;
    c.insets = new Insets(10, 10, 0, 0);
    panelCenter.add(txtSeriesVol, c);
    c.gridx = 0;
    c.gridy = 4;
    c.weightx = 0.1;
    c.gridwidth = 1;
    c.insets = new Insets(10, 0, 0, 0);
    panelCenter.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

    /*
     * create components for Panel South
     */
    JButton btnAdd = ButtonsFactory.createButton(Localization.get("label.save"));
    btnAdd.setFont(Mainframe.defaultFont);
    btnAdd.addActionListener(e -> speichern(eintrag));

    JButton btnAbort = ButtonsFactory.createButton(Localization.get("label.abort"));
    btnAbort.setFont(Mainframe.defaultFont);
    btnAbort.addActionListener(arg0 -> dispose());

    /*
     * add components into Panel South
     */
    panelSouth.add(btnAdd);
    panelSouth.add(btnAbort);
    panelSouth.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    this.add(panelNorth, BorderLayout.NORTH);
    this.add(panelWest, BorderLayout.WEST);
    this.add(panelCenter, BorderLayout.CENTER);
    this.add(panelEastBorder, BorderLayout.EAST);
    this.add(panelSouth, BorderLayout.SOUTH);

    Mainframe.logger.info("Wishlist Book edit: Frame created successfully");
    this.setVisible(true);
    this.setResizable(false);

  }

  public void speichern(Book_Wishlist eintrag) {
    if (!txtAuthor.getText().isEmpty() && !txtTitle.getText().isEmpty()) {
      String oldAutor = eintrag.getAuthor();
      String oldTitel = eintrag.getTitle();
      String newAutor = txtAuthor.getText().trim();
      String newTitel = txtTitle.getText().trim();
      String newBemerkung = txtNote.getText().trim();
      String newSerie = txtSeries.getText().trim();
      String newSeriePart = txtSeriesVol.getText();
      Timestamp datum = new Timestamp(System.currentTimeMillis());
      if (!Duplicant(newAutor, newTitel)) {
        Database.deleteFromWishlist(eintrag.getWid());
        int newWid = Database.addToWishlist(newAutor, newTitel, newBemerkung, newSerie, newSeriePart, datum.toString());
        eintrag.setWid(newWid);
        eintrag.setAuthor(newAutor);
        eintrag.setTitle(newTitel);
        eintrag.setNote(newBemerkung);
        eintrag.setSeries(newSerie);
        eintrag.setSeriesVol(newSeriePart);
        eintrag.setDate(datum);
        dispose();
        Mainframe.logger.info("Wishlist Book edit: Book saved successfully");
      } else {
        txtTitle.setText(Localization.get("text.duplicateError"));
        txtTitle.setBackground(new Color(255, 105, 105));
      }
    } else {
      if (txtAuthor.getText().isEmpty()) {
        txtAuthor.setBackground(new Color(255, 105, 105));
      }
      if (txtTitle.getText().isEmpty()) {
        txtTitle.setBackground(new Color(255, 105, 105));
      }
    }
    wishlist.updateModel();
  }

  public boolean Duplicant(String autor, String titel) {
    for (int i = 0; i < wishlist.wishlistEntries.getSize(); i++) {
      Book_Wishlist eintrag = wishlist.wishlistEntries.getElementAt(i);
      if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
        return true;
      }
    }
    for (int i = 0; i < Mainframe.allEntries.getSize(); i++) {
      Book_Booklist eintrag = Mainframe.allEntries.getElementAt(i);
      if (eintrag.getAuthor().equals(autor) && eintrag.getTitle().equals(titel)) {
        return true;
      }
    }
    return false;
  }

}
