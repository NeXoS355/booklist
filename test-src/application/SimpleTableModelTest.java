package application;

import gui.Localization;
import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class SimpleTableModelTest {

  @BeforeAll
  static void init() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(SimpleTableModelTest.class);
    }
    Localization.setLocale(Locale.ENGLISH);
    SimpleTableModel.initColumnNames();
  }

  private Book_Booklist createBook(String author, String title, boolean ebook,
                                   String series, String vol, double rating) {
    return new Book_Booklist(author, title, false, "", "", "", series, vol,
        ebook, rating, null, null, null, new Timestamp(System.currentTimeMillis()), false);
  }

  // Empty BookListModel must produce a table with 0 rows
  @Test
  void emptyModel_zeroRows() {
    BookListModel books = new BookListModel(false);
    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals(0, table.getRowCount());
  }

  // Column count must always be 5 (ebook, author, title, series, rating)
  @Test
  void columnCount_isFive() {
    BookListModel books = new BookListModel(false);
    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals(5, table.getColumnCount());
  }

  // Column names must match the English localization
  @Test
  void columnNames_matchLocalization() {
    BookListModel books = new BookListModel(false);
    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("E-Book", table.getColumnName(0));
    assertEquals("Author", table.getColumnName(1));
    assertEquals("Title", table.getColumnName(2));
    assertEquals("Series", table.getColumnName(3));
    assertEquals("Rating", table.getColumnName(4));
  }

  // Ebook column must show "●" for ebooks and "○" for physical books
  @Test
  void ebookColumn_showsCorrectSymbol() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Author1", "EbookTitle", true, "", "", 0));
    books.add(createBook("Author2", "PhysicalTitle", false, "", "", 0));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("●", table.getValueAt(0, 0));
    assertEquals("○", table.getValueAt(1, 0));
  }

  // Author and title columns must contain the correct values
  @Test
  void authorAndTitle_correctValues() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Tolkien", "Der Hobbit", false, "", "", 0));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("Tolkien", table.getValueAt(0, 1));
    assertEquals("Der Hobbit", table.getValueAt(0, 2));
  }

  // Series column must combine series name and volume with " - "
  @Test
  void seriesColumn_combinesNameAndVol() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Tolkien", "Die Gefährten", false, "HdR", "1", 0));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("HdR - 1", table.getValueAt(0, 3));
  }

  // Rating column must show the rating when > 0
  @Test
  void ratingColumn_showsValue() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Author", "Title", false, "", "", 4.5));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("4.5", table.getValueAt(0, 4));
  }

  // Rating column must be empty when rating is 0
  @Test
  void ratingColumn_zeroRating_isEmpty() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Author", "Title", false, "", "", 0));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals("", table.getValueAt(0, 4));
  }

  // Row count must match the number of books in the model
  @Test
  void rowCount_matchesBookCount() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("A1", "T1", false, "", "", 0));
    books.add(createBook("A2", "T2", false, "", "", 0));
    books.add(createBook("A3", "T3", false, "", "", 0));

    SimpleTableModel table = new SimpleTableModel(books);
    assertEquals(3, table.getRowCount());
  }

  // All cells must be non-editable
  @Test
  void isCellEditable_alwaysFalse() {
    BookListModel books = new BookListModel(false);
    books.add(createBook("Author", "Title", false, "", "", 3.0));

    SimpleTableModel table = new SimpleTableModel(books);
    for (int col = 0; col < table.getColumnCount(); col++) {
      assertFalse(table.isCellEditable(0, col));
    }
  }
}
