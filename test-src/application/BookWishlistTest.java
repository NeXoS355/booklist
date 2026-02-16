package application;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class BookWishlistTest {

  private Book_Wishlist createBook(String author, String title) {
    return new Book_Wishlist(author, title, "", "", "", new Timestamp(System.currentTimeMillis()), false);
  }

  // Constructor must assign all fields correctly
  @Test
  void constructor_setsFields() {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    Book_Wishlist book = new Book_Wishlist("Author", "Title", "Note", "Series", "2", now, false);

    assertEquals("Author", book.getAuthor());
    assertEquals("Title", book.getTitle());
    assertEquals("Note", book.getNote());
    assertEquals("Series", book.getSeries());
    assertEquals("2", book.getSeriesVol());
    assertEquals(now, book.getDate());
  }

  // toString must return "author    |    title"
  @Test
  void toString_format() {
    Book_Wishlist book = createBook("Tolkien", "Der Hobbit");
    assertEquals("Tolkien    |    Der Hobbit", book.toString());
  }

  // Books with same author+title must be equal and have same hashCode
  @Test
  void equals_sameAuthorAndTitle() {
    Book_Wishlist a = createBook("Author", "Title");
    Book_Wishlist b = createBook("Author", "Title");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  // Different author must make books unequal
  @Test
  void equals_differentAuthor() {
    assertNotEquals(createBook("A1", "T"), createBook("A2", "T"));
  }

  // Different title must make books unequal
  @Test
  void equals_differentTitle() {
    assertNotEquals(createBook("A", "T1"), createBook("A", "T2"));
  }

  // Comparing with null must return false
  @Test
  void equals_null() {
    assertNotEquals(null, createBook("A", "T"));
  }

  // Comparing with a different class must return false
  @Test
  void equals_differentClass() {
    assertNotEquals("string", createBook("A", "T"));
  }

  // Same instance must be equal to itself
  @Test
  void equals_sameInstance() {
    Book_Wishlist book = createBook("A", "T");
    assertEquals(book, book);
  }

  // All simple getters/setters must store and retrieve values correctly
  @Test
  void settersAndGetters() {
    Book_Wishlist book = createBook("A", "T");

    book.setAuthor("New");
    assertEquals("New", book.getAuthor());

    book.setTitle("New Title");
    assertEquals("New Title", book.getTitle());

    book.setNote("Note");
    assertEquals("Note", book.getNote());

    book.setSeries("Series");
    assertEquals("Series", book.getSeries());

    book.setSeriesVol("3");
    assertEquals("3", book.getSeriesVol());

    book.setWid(99);
    assertEquals(99, book.getWid());

    Timestamp now = new Timestamp(System.currentTimeMillis());
    book.setDate(now);
    assertEquals(now, book.getDate());
  }
}
