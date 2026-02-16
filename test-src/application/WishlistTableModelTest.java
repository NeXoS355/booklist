package application;

import gui.Localization;
import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class WishlistTableModelTest {

  @BeforeAll
  static void init() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(WishlistTableModelTest.class);
    }
    Localization.setLocale(Locale.ENGLISH);
  }

  @SuppressWarnings("unchecked")
  private WishlistListModel createEmptyModel() throws Exception {
    // Clear static books list and create instance without calling DB constructor
    Field booksField = WishlistListModel.class.getDeclaredField("books");
    booksField.setAccessible(true);
    ArrayList<Book_Wishlist> books = (ArrayList<Book_Wishlist>) booksField.get(null);
    books.clear();

    var unsafeClass = Class.forName("sun.misc.Unsafe");
    Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    var unsafe = unsafeField.get(null);
    var allocateMethod = unsafeClass.getMethod("allocateInstance", Class.class);
    return (WishlistListModel) allocateMethod.invoke(unsafe, WishlistListModel.class);
  }

  /** Adds a book directly to the static books list (bypasses fireIntervalAdded). */
  @SuppressWarnings("unchecked")
  private void addBookDirect(Book_Wishlist book) throws Exception {
    Field booksField = WishlistListModel.class.getDeclaredField("books");
    booksField.setAccessible(true);
    ArrayList<Book_Wishlist> books = (ArrayList<Book_Wishlist>) booksField.get(null);
    books.add(book);
  }

  private Book_Wishlist createWishBook(String author, String title, String series,
                                       String vol, String note) {
    return new Book_Wishlist(author, title, note, series, vol,
        new Timestamp(System.currentTimeMillis()), false);
  }

  // Empty model must produce a table with 0 rows
  @Test
  void emptyModel_zeroRows() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals(0, table.getRowCount());
  }

  // Column count must always be 4 (author, title, series, note)
  @Test
  void columnCount_isFour() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals(4, table.getColumnCount());
  }

  // Column names must match the English localization
  @Test
  void columnNames_matchLocalization() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals("Author", table.getColumnName(0));
    assertEquals("Title", table.getColumnName(1));
    assertEquals("Series", table.getColumnName(2));
    assertEquals("Note", table.getColumnName(3));
  }

  // Author and title columns must contain the correct values
  @Test
  void authorAndTitle_correctValues() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    addBookDirect(createWishBook("Tolkien", "Der Hobbit", "", "", ""));

    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals("Tolkien", table.getValueAt(0, 0));
    assertEquals("Der Hobbit", table.getValueAt(0, 1));
  }

  // Series column must combine series name and volume with " - "
  @Test
  void seriesColumn_combinesNameAndVol() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    addBookDirect(createWishBook("Tolkien", "Die Gefährten", "HdR", "1", ""));

    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals("HdR - 1", table.getValueAt(0, 2));
  }

  // Note column must contain the note text
  @Test
  void noteColumn_showsNote() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    addBookDirect(createWishBook("Author", "Title", "", "", "Geschenk"));

    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals("Geschenk", table.getValueAt(0, 3));
  }

  // Row count must match the number of books in the model
  @Test
  void rowCount_matchesBookCount() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    addBookDirect(createWishBook("A1", "T1", "", "", ""));
    addBookDirect(createWishBook("A2", "T2", "", "", ""));

    WishlistTableModel table = new WishlistTableModel(listModel);
    assertEquals(2, table.getRowCount());
  }

  // All cells must be non-editable
  @Test
  void isCellEditable_alwaysFalse() throws Exception {
    WishlistListModel listModel = createEmptyModel();
    addBookDirect(createWishBook("Author", "Title", "S", "1", "Note"));

    WishlistTableModel table = new WishlistTableModel(listModel);
    for (int col = 0; col < table.getColumnCount(); col++) {
      assertFalse(table.isCellEditable(0, col));
    }
  }
}
