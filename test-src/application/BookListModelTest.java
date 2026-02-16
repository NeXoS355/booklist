package application;

import data.Database;
import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class BookListModelTest {

  private static Connection testCon;

  @BeforeAll
  static void initLogger() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(BookListModelTest.class);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    testCon = DriverManager.getConnection("jdbc:sqlite::memory:");
    Database.createTable(testCon);
    setDatabaseConnection(testCon);
    BookListModel.useDB = false;
  }

  @AfterEach
  void tearDown() throws Exception {
    if (testCon != null && !testCon.isClosed()) {
      testCon.close();
    }
    setDatabaseConnection(null);
  }

  private static void setDatabaseConnection(Connection con) throws Exception {
    Field conField = Database.class.getDeclaredField("con");
    conField.setAccessible(true);
    conField.set(null, con);
  }

  private Book_Booklist createBook(String author, String title) {
    return new Book_Booklist(author, title, false, "", "", "", "", "",
        false, 0.0, null, null, null, new Timestamp(System.currentTimeMillis()), false);
  }

  private Book_Booklist createBookWithSeries(String author, String title, String series, String vol) {
    return new Book_Booklist(author, title, false, "", "", "", series, vol,
        false, 0.0, null, null, null, new Timestamp(System.currentTimeMillis()), false);
  }

  // --- Constructor ---

  // BookListModel(false) must create an empty model
  @Test
  void constructor_false_createsEmptyModel() {
    BookListModel model = new BookListModel(false);
    assertEquals(0, model.getSize());
    assertTrue(model.getBooks().isEmpty());
  }

  // --- add / getSize / getElementAt ---

  // Adding a book must increase the size by one
  @Test
  void add_increasesSize() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    assertEquals(1, model.getSize());
  }

  // getElementAt must return the correct book
  @Test
  void getElementAt_returnsCorrectBook() {
    BookListModel model = new BookListModel(false);
    Book_Booklist book = createBook("Tolkien", "Der Hobbit");
    model.add(book);
    assertEquals(book, model.getElementAt(0));
  }

  // addElement must also add books to the internal list
  @Test
  void addElement_addsBook() {
    BookListModel model = new BookListModel(false);
    model.addElement(createBook("Author", "Title"));
    assertEquals(1, model.getSize());
  }

  // --- getIndexOf ---

  // getIndexOf must return the correct index for a matching book (case-insensitive)
  @Test
  void getIndexOf_existingBook_returnsIndex() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    model.add(createBook("Rowling", "Harry Potter"));
    assertEquals(1, model.getIndexOf("rowling", "harry potter"));
  }

  // getIndexOf must return 0 when no match is found
  @Test
  void getIndexOf_nonExistent_returnsZero() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    assertEquals(0, model.getIndexOf("Unknown", "Unknown"));
  }

  // getIndexOf must be case-insensitive
  @Test
  void getIndexOf_caseInsensitive() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    assertEquals(0, model.getIndexOf("TOLKIEN", "DER HOBBIT"));
  }

  // --- getBookCountForAuthor ---

  // getBookCountForAuthor must count only books from the specified author
  @Test
  void getBookCountForAuthor_countsCorrectly() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    model.add(createBook("Tolkien", "Silmarillion"));
    model.add(createBook("Rowling", "Harry Potter"));
    assertEquals(2, model.getBookCountForAuthor("Tolkien"));
    assertEquals(1, model.getBookCountForAuthor("Rowling"));
  }

  // getBookCountForAuthor must return 0 for an unknown author
  @Test
  void getBookCountForAuthor_unknownAuthor_returnsZero() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));
    assertEquals(0, model.getBookCountForAuthor("Unknown"));
  }

  // --- getSeriesFromAuthor (useDB=false) ---

  // getSeriesFromAuthor must return distinct series for the given author
  @Test
  void getSeriesFromAuthor_returnsDistinctSeries() {
    BookListModel model = new BookListModel(false);
    model.add(createBookWithSeries("Tolkien", "Die Gefährten", "Herr der Ringe", "1"));
    model.add(createBookWithSeries("Tolkien", "Die zwei Türme", "Herr der Ringe", "2"));
    model.add(createBookWithSeries("Tolkien", "Der Hobbit", "", ""));

    String[] series = model.getSeriesFromAuthor("Tolkien");
    assertEquals(1, series.length);
    assertEquals("Herr der Ringe", series[0]);
  }

  // getSeriesFromAuthor must not return series from other authors
  @Test
  void getSeriesFromAuthor_filtersOtherAuthors() {
    BookListModel model = new BookListModel(false);
    model.add(createBookWithSeries("Tolkien", "Die Gefährten", "HdR", "1"));
    model.add(createBookWithSeries("Rowling", "Stein der Weisen", "Harry Potter", "1"));

    String[] series = model.getSeriesFromAuthor("Tolkien");
    assertEquals(1, series.length);
    assertEquals("HdR", series[0]);
  }

  // getSeriesFromAuthor must return empty array when author has no series
  @Test
  void getSeriesFromAuthor_noSeries_returnsEmpty() {
    BookListModel model = new BookListModel(false);
    model.add(createBook("Tolkien", "Der Hobbit"));

    String[] series = model.getSeriesFromAuthor("Tolkien");
    assertEquals(0, series.length);
  }

  // getSeriesFromAuthor uses contains() for author matching
  @Test
  void getSeriesFromAuthor_partialAuthorMatch() {
    BookListModel model = new BookListModel(false);
    model.add(createBookWithSeries("J.R.R. Tolkien", "Die Gefährten", "HdR", "1"));

    String[] series = model.getSeriesFromAuthor("Tolkien");
    assertEquals(1, series.length);
  }

  // --- DB-dependent static methods (with in-memory SQLite) ---

  // getMostOf must return the author(s) with the most books
  @Test
  void getMostOf_returnsMostFrequent() throws SQLException {
    Database.addToBooklist("Tolkien", "Hobbit", "none", "", "", "", "", false, "2026-01-01");
    Database.addToBooklist("Tolkien", "Silmarillion", "none", "", "", "", "", false, "2026-01-02");
    Database.addToBooklist("Rowling", "HP", "none", "", "", "", "", false, "2026-01-03");

    var result = BookListModel.getMostOf("author");
    assertEquals(1, result.size());
    assertEquals("Tolkien", result.get(0));
  }

  // getMostOf must return multiple values when there is a tie
  @Test
  void getMostOf_tie_returnsAll() throws SQLException {
    Database.addToBooklist("Tolkien", "Hobbit", "none", "", "", "", "", false, "2026-01-01");
    Database.addToBooklist("Rowling", "HP", "none", "", "", "", "", false, "2026-01-02");

    var result = BookListModel.getMostOf("author");
    assertEquals(2, result.size());
  }

  // getBestRatingOf must return the author with the highest average rating
  @Test
  void getBestRatingOf_returnsHighestAvg() throws SQLException {
    int bid1 = Database.addToBooklist("Tolkien", "Hobbit", "none", "", "", "", "", false, "2026-01-01");
    int bid2 = Database.addToBooklist("Rowling", "HP", "none", "", "", "", "", false, "2026-01-02");
    Database.updateRating(bid1, 5.0);
    Database.updateRating(bid2, 3.0);

    var result = BookListModel.getBestRatingOf("author");
    assertEquals(1, result.size());
    assertEquals("Tolkien", result.get(0));
  }

  // getEbookCount must correctly count ebooks and physical books
  @Test
  void getEbookCount_countsCorrectly() throws SQLException {
    Database.addToBooklist("A", "T1", "none", "", "", "", "", true, "2026-01-01");
    Database.addToBooklist("A", "T2", "none", "", "", "", "", true, "2026-01-02");
    Database.addToBooklist("A", "T3", "none", "", "", "", "", false, "2026-01-03");

    assertEquals(2, BookListModel.getEbookCount(1));
    assertEquals(1, BookListModel.getEbookCount(0));
  }

  // getBooksPerYear must return HTML with year counts
  @Test
  void getBooksPerYear_returnsHtmlString() throws SQLException {
    Database.addToBooklist("A", "T1", "none", "", "", "", "", false, "2026-01-01");
    Database.addToBooklist("A", "T2", "none", "", "", "", "", false, "2026-06-15");

    String result = BookListModel.getBooksPerYear();
    assertTrue(result.startsWith("<html>"));
    assertTrue(result.endsWith("</html>"));
    assertTrue(result.contains("2026"));
  }

  // getBooksPerYear with no books must return empty HTML wrapper
  @Test
  void getBooksPerYear_empty_returnsHtmlOnly() {
    String result = BookListModel.getBooksPerYear();
    assertEquals("<html></html>", result);
  }
}
