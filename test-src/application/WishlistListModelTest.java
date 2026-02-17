package application;

import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WishlistListModel using Reflection to bypass the DB-calling constructor.
 * The static 'books' field is manipulated directly.
 */
class WishlistListModelTest {

  private WishlistListModel model;
  private ArrayList<Book_Wishlist> books;

  @BeforeAll
  static void initLogger() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(WishlistListModelTest.class);
    }
  }

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setUp() throws Exception {
    // Access the static books list directly to avoid DB calls
    Field booksField = WishlistListModel.class.getDeclaredField("books");
    booksField.setAccessible(true);
    books = (ArrayList<Book_Wishlist>) booksField.get(null);
    books.clear();

    // Create instance without calling DB constructor via Unsafe
    var unsafeClass = Class.forName("sun.misc.Unsafe");
    Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
    unsafeField.setAccessible(true);
    var unsafe = unsafeField.get(null);
    var allocateMethod = unsafeClass.getMethod("allocateInstance", Class.class);
    model = (WishlistListModel) allocateMethod.invoke(unsafe, WishlistListModel.class);
  }

  private Book_Wishlist createWishBook(String author, String title) {
    return new Book_Wishlist(author, title, "", "", "",
        new Timestamp(System.currentTimeMillis()), false);
  }

  /** Helper: adds a book directly to the static list (bypasses fireIntervalAdded). */
  private void addBook(Book_Wishlist book) {
    books.add(book);
  }

  // Adding a book must increase size by one
  @Test
  void add_increasesSize() {
    addBook(createWishBook("Tolkien", "Der Hobbit"));
    assertEquals(1, model.getSize());
  }

  // getElementAt must return the added book
  @Test
  void getElementAt_returnsCorrectBook() {
    Book_Wishlist book = createWishBook("Tolkien", "Der Hobbit");
    addBook(book);
    assertEquals(book, model.getElementAt(0));
  }

  // delete(Book_Wishlist) must remove the book from the list
  @Test
  void delete_byBook_removesEntry() {
    Book_Wishlist book = createWishBook("Author", "Title");
    addBook(book);
    books.remove(book);
    assertEquals(0, model.getSize());
  }

  // getSize must return the correct count after multiple adds
  @Test
  void getSize_afterMultipleAdds() {
    addBook(createWishBook("A1", "T1"));
    addBook(createWishBook("A2", "T2"));
    addBook(createWishBook("A3", "T3"));
    assertEquals(3, model.getSize());
  }

  // getIndexOf must return the correct index (case-insensitive)
  @Test
  void getIndexOf_existingBook_returnsIndex() {
    addBook(createWishBook("Tolkien", "Der Hobbit"));
    addBook(createWishBook("Rowling", "Harry Potter"));
    assertEquals(1, model.getIndexOf("rowling", "harry potter"));
  }

  // getIndexOf must return 0 when no match is found
  @Test
  void getIndexOf_nonExistent_returnsNegative() {
    addBook(createWishBook("Tolkien", "Der Hobbit"));
    assertEquals(-1, model.getIndexOf("Unknown", "Unknown"));
  }

  // indexOf must return the list index of the given book object
  @Test
  void indexOf_returnsCorrectPosition() {
    Book_Wishlist book1 = createWishBook("A1", "T1");
    Book_Wishlist book2 = createWishBook("A2", "T2");
    addBook(book1);
    addBook(book2);
    assertEquals(0, model.indexOf(book1));
    assertEquals(1, model.indexOf(book2));
  }

  // indexOf must return -1 for a book not in the list
  @Test
  void indexOf_notInList_returnsMinusOne() {
    addBook(createWishBook("A1", "T1"));
    Book_Wishlist other = createWishBook("Other", "Other");
    assertEquals(-1, model.indexOf(other));
  }

  // Adding then deleting must leave the list empty
  @Test
  void addAndDelete_netZero() {
    Book_Wishlist book = createWishBook("Author", "Title");
    addBook(book);
    books.remove(book);
    assertEquals(0, model.getSize());
  }
}
