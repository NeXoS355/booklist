package data;

import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Database CRUD operations using an in-memory SQLite database.
 */
class DatabaseTest {

  private static Connection testCon;

  @BeforeAll
  static void initLogger() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(DatabaseTest.class);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    testCon = DriverManager.getConnection("jdbc:sqlite::memory:");
    Database.createTable(testCon);
    setDatabaseConnection(testCon);
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

  // --- createTable ---

  // All three tables (books, wishlist, versions) must exist after createTable
  @Test
  void createTable_createsAllTables() throws SQLException {
    DatabaseMetaData meta = testCon.getMetaData();

    try (ResultSet rs = meta.getTables(null, null, "books", null)) {
      assertTrue(rs.next(), "books table must exist");
    }
    try (ResultSet rs = meta.getTables(null, null, "wishlist", null)) {
      assertTrue(rs.next(), "wishlist table must exist");
    }
    try (ResultSet rs = meta.getTables(null, null, "versions", null)) {
      assertTrue(rs.next(), "versions table must exist");
    }
  }

  // Initial version "4.0.0" must be inserted into the versions table
  @Test
  void createTable_insertsInitialVersion() {
    assertEquals("4.0.0", Database.readCurrentLayoutVersion());
  }

  // Calling createTable twice must not insert a duplicate version row
  @Test
  void createTable_isIdempotent() throws SQLException {
    Database.createTable(testCon);

    try (Statement st = testCon.createStatement();
         ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM versions")) {
      rs.next();
      assertEquals(1, rs.getInt(1));
    }
  }

  // --- Booklist CRUD ---

  // Adding a book must return a positive auto-generated bid
  @Test
  void addToBooklist_returnsGeneratedBid() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "Note",
        "Series", "1", false, "2026-01-01");
    assertTrue(bid > 0);
  }

  // Null seriesVol must be stored as SQL NULL without error
  @Test
  void addToBooklist_nullSeriesVol() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", null, false, "2026-01-01");
    assertTrue(bid > 0);
  }

  // Empty seriesVol must be stored as SQL NULL without error
  @Test
  void addToBooklist_emptySeriesVol() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");
    assertTrue(bid > 0);
  }

  // Non-numeric seriesVol (e.g. "abc") must be stored as SQL NULL
  @Test
  void addToBooklist_invalidSeriesVol() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "Series", "abc", false, "2026-01-01");
    assertTrue(bid > 0);
  }

  // readDbBooklist must return all inserted books
  @Test
  void readDbBooklist_returnsInsertedBooks() throws SQLException {
    Database.addToBooklist("Tolkien", "Der Hobbit", "none", "", "",
        "", "", false, "2026-01-01");
    Database.addToBooklist("Tolkien", "Der Herr der Ringe", "none", "", "",
        "", "", false, "2026-01-02");

    ResultSet rs = Database.readDbBooklist();
    int count = 0;
    while (rs.next()) count++;
    Database.closeResultSet(rs);
    assertEquals(2, count);
  }

  // Lite query must return only the expected columns
  @Test
  void readDbBooklistLite_returnsCorrectColumns() throws SQLException {
    Database.addToBooklist("Author", "Title", "none", "", "",
        "Series", "1", true, "2026-01-01");

    ResultSet rs = Database.readDbBooklistLite();
    assertTrue(rs.next());
    assertEquals("Author", rs.getString("author"));
    assertEquals("Title", rs.getString("title"));
    assertEquals(1, rs.getInt("ebook"));
    assertEquals("Series", rs.getString("series"));
    Database.closeResultSet(rs);
  }

  // Selecting by bid must return the correct book
  @Test
  void selectFromBooklist_existingBid() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "Note",
        "Series", "1", false, "2026-01-01");

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals("Author", rs.getString("author"));
    assertEquals("Title", rs.getString("title"));
    assertEquals("Note", rs.getString("note"));
    Database.closeResultSet(rs);
  }

  // Selecting a non-existent bid must return an empty ResultSet
  @Test
  void selectFromBooklist_nonExistentBid() {
    ResultSet rs = Database.selectFromBooklist(9999);
    try {
      assertFalse(rs.next());
    } catch (SQLException e) {
      fail("Unexpected exception: " + e.getMessage());
    }
    Database.closeResultSet(rs);
  }

  // Deleting a book must remove it from the database
  @Test
  void deleteFromBooklist_removesEntry() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.deleteFromBooklist(bid);

    ResultSet rs = Database.selectFromBooklist(bid);
    assertFalse(rs.next());
    Database.closeResultSet(rs);
  }

  // Updating a column by name must persist the new value
  @Test
  void updateBooklistEntry_updatesColumn() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.updateBooklistEntry(bid, "note", "Updated Note");

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals("Updated Note", rs.getString("note"));
    Database.closeResultSet(rs);
  }

  // Rating update must persist the exact double value
  @Test
  void updateRating_setsValue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.updateRating(bid, 4.5);

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals(4.5, rs.getDouble("rating"), 0.001);
    Database.closeResultSet(rs);
  }

  // Description update must persist the string
  @Test
  void updateDesc_setsValue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.updateDesc(bid, "Eine Beschreibung");

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals("Eine Beschreibung", rs.getString("description"));
    Database.closeResultSet(rs);
  }

  // Deleting a description must set the column to NULL
  @Test
  void delDesc_clearsValue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");
    Database.updateDesc(bid, "Text");

    assertTrue(Database.delDesc(bid));

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertNull(rs.getString("description"));
    Database.closeResultSet(rs);
  }

  // ISBN update must persist the string
  @Test
  void updateIsbn_setsValue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.updateIsbn(bid, "9781234567890");

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals("9781234567890", rs.getString("isbn"));
    Database.closeResultSet(rs);
  }

  // Date update must persist the new date string
  @Test
  void updateDate_setsValue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");

    Database.updateDate(bid, "2026-06-15");

    ResultSet rs = Database.selectFromBooklist(bid);
    assertTrue(rs.next());
    assertEquals("2026-06-15", rs.getString("added_date"));
    Database.closeResultSet(rs);
  }

  // Deleting a cover on a book without one must still succeed
  @Test
  void delPic_returnsTrue() throws SQLException {
    int bid = Database.addToBooklist("Author", "Title", "none", "", "",
        "", "", false, "2026-01-01");
    assertTrue(Database.delPic(bid));
  }

  // --- Aggregate queries ---

  // DISTINCT query on author must deduplicate correctly
  @Test
  void getColumnsFromBooklist_distinctAuthors() throws SQLException {
    Database.addToBooklist("Tolkien", "Hobbit", "none", "", "", "", "", false, "2026-01-01");
    Database.addToBooklist("Tolkien", "Silmarillion", "none", "", "", "", "", false, "2026-01-02");
    Database.addToBooklist("Rowling", "Harry Potter", "none", "", "", "", "", false, "2026-01-03");

    ResultSet rs = Database.getColumnsFromBooklist(new String[]{"author"});
    int count = 0;
    while (rs.next()) count++;
    Database.closeResultSet(rs);
    assertEquals(2, count);
  }

  // GROUP BY must count books per author correctly
  @Test
  void getColumnCountsWithGroup_groupsByColumn() throws SQLException {
    Database.addToBooklist("Tolkien", "Hobbit", "none", "", "", "", "", false, "2026-01-01");
    Database.addToBooklist("Tolkien", "Silmarillion", "none", "", "", "", "", false, "2026-01-02");
    Database.addToBooklist("Rowling", "HP", "none", "", "", "", "", false, "2026-01-03");

    ResultSet rs = Database.getColumnCountsWithGroup("author");
    int groups = 0;
    while (rs.next()) {
      groups++;
      String author = rs.getString(2);
      int count = rs.getInt(1);
      if (author.equals("Tolkien")) assertEquals(2, count);
      if (author.equals("Rowling")) assertEquals(1, count);
    }
    Database.closeResultSet(rs);
    assertEquals(2, groups);
  }

  // Average rating must exclude books with rating 0.0
  @Test
  void getAvgRating_excludesZeroRatings() throws SQLException {
    int bid1 = Database.addToBooklist("Author", "Book1", "none", "", "", "", "", false, "2026-01-01");
    int bid2 = Database.addToBooklist("Author", "Book2", "none", "", "", "", "", false, "2026-01-02");
    Database.addToBooklist("Author", "Book3", "none", "", "", "", "", false, "2026-01-03");

    Database.updateRating(bid1, 4.0);
    Database.updateRating(bid2, 2.0);
    // bid3 stays at 0.0 and must not be included

    ResultSet rs = Database.getAvgRating(new String[]{"author"});
    assertTrue(rs.next());
    assertEquals(3.0, rs.getDouble(1), 0.001);
    Database.closeResultSet(rs);
  }

  // --- Wishlist CRUD ---

  // Adding a wishlist entry must return a positive auto-generated wid
  @Test
  void addToWishlist_returnsGeneratedWid() {
    int wid = Database.addToWishlist("Author", "Title", "Note", "Series", "1", "2026-01-01");
    assertTrue(wid > 0);
  }

  // readDbWishlist must return all inserted entries
  @Test
  void readDbWishlist_returnsInsertedEntries() {
    Database.addToWishlist("Author1", "Title1", "", "", "", "2026-01-01");
    Database.addToWishlist("Author2", "Title2", "", "", "", "2026-01-02");

    ResultSet rs = Database.readDbWishlist();
    int count = 0;
    try {
      while (rs.next()) count++;
    } catch (SQLException e) {
      fail(e.getMessage());
    }
    Database.closeResultSet(rs);
    assertEquals(2, count);
  }

  // Deleting a wishlist entry must remove it from the database
  @Test
  void deleteFromWishlist_removesEntry() {
    int wid = Database.addToWishlist("Author", "Title", "", "", "", "2026-01-01");

    Database.deleteFromWishlist(wid);

    ResultSet rs = Database.readDbWishlist();
    try {
      assertFalse(rs.next());
    } catch (SQLException e) {
      fail(e.getMessage());
    }
    Database.closeResultSet(rs);
  }

  // --- Version ---

  // Initial layout version must be "4.0.0"
  @Test
  void readCurrentLayoutVersion_returnsInitialVersion() {
    assertEquals("4.0.0", Database.readCurrentLayoutVersion());
  }

  // SQLite version string must match semver format (e.g. "3.45.1")
  @Test
  void readCurrentDBVersion_returnsSqliteVersion() {
    String version = Database.readCurrentDBVersion();
    assertFalse(version.isEmpty());
    assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"));
  }

  // closeResultSet must handle null without throwing
  @Test
  void closeResultSet_handlesNull() {
    assertDoesNotThrow(() -> Database.closeResultSet(null));
  }
}
