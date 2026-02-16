package application;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class BookBooklistTest {

  private Book_Booklist createBook(String author, String title) {
    return new Book_Booklist(author, title, false, "", "", "", "", "", false, 0.0, null, null, null,
        new Timestamp(System.currentTimeMillis()), false);
  }

  // Full constructor must assign all fields correctly
  @Test
  void constructor_setsFields() {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    Book_Booklist book = new Book_Booklist("Author", "Title", false, "", "", "Note", "Series", "3",
        true, 4.5, null, "Desc", "9781234567890", now, false);

    assertEquals("Author", book.getAuthor());
    assertEquals("Title", book.getTitle());
    assertEquals("Note", book.getNote());
    assertEquals("Series", book.getSeries());
    assertEquals("3", book.getSeriesVol());
    assertTrue(book.isEbook());
    assertEquals(4.5, book.getRating());
    assertEquals("Desc", book.getDesc());
    assertEquals("9781234567890", book.getIsbn());
    assertEquals(now, book.getDate());
    assertFalse(book.isBorrowed());
  }

  // When borrowed=false, borrowedTo/From must be cleared regardless of input
  @Test
  void constructor_notBorrowed_clearsBorrowFields() {
    Book_Booklist book = new Book_Booklist("A", "T", false, "PersonTo", "PersonFrom",
        "", "", "", false, 0, null, null, null, new Timestamp(0), false);

    assertEquals("", book.getBorrowedTo());
    assertEquals("", book.getBorrowedFrom());
  }

  // When borrowed=true, borrowedTo/From must be preserved
  @Test
  void constructor_borrowed_keepsBorrowFields() {
    Book_Booklist book = new Book_Booklist("A", "T", true, "PersonTo", "PersonFrom",
        "", "", "", false, 0, null, null, null, new Timestamp(0), false);

    assertEquals("PersonTo", book.getBorrowedTo());
    assertEquals("PersonFrom", book.getBorrowedFrom());
  }

  // Short constructor must delegate correctly with borrowed=false
  @Test
  void shortConstructor_setsDefaults() {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    Book_Booklist book = new Book_Booklist("Author", "Title", "Note", "Series", "1",
        true, 3, null, null, null, now, false);

    assertEquals("Author", book.getAuthor());
    assertFalse(book.isBorrowed());
    assertEquals("", book.getBorrowedTo());
    assertEquals("", book.getBorrowedFrom());
  }

  // toString must return "author    |    title"
  @Test
  void toString_format() {
    Book_Booklist book = createBook("Tolkien", "Der Hobbit");
    assertEquals("Tolkien    |    Der Hobbit", book.toString());
  }

  // Books with same author+title must be equal and have same hashCode
  @Test
  void equals_sameAuthorAndTitle() {
    Book_Booklist a = createBook("Author", "Title");
    Book_Booklist b = createBook("Author", "Title");
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
  }

  // Different author must make books unequal
  @Test
  void equals_differentAuthor() {
    assertNotEquals(createBook("Author1", "Title"), createBook("Author2", "Title"));
  }

  // Different title must make books unequal
  @Test
  void equals_differentTitle() {
    assertNotEquals(createBook("Author", "Title1"), createBook("Author", "Title2"));
  }

  // Comparing with null must return false
  @Test
  void equals_null() {
    assertNotEquals(null, createBook("A", "T"));
  }

  // Comparing with a different class must return false
  @Test
  void equals_differentClass() {
    assertNotEquals("A | T", createBook("A", "T"));
  }

  // Same instance must be equal to itself
  @Test
  void equals_sameInstance() {
    Book_Booklist book = createBook("A", "T");
    assertEquals(book, book);
  }

  // Valid 13-digit ISBN must be accepted and stored
  @Test
  void setIsbn_validIsbn13() {
    Book_Booklist book = createBook("A", "T");
    assertTrue(book.setIsbn("9781234567890", false));
    assertEquals("9781234567890", book.getIsbn());
  }

  // Valid 10-digit ISBN must be accepted and stored
  @Test
  void setIsbn_validIsbn10() {
    Book_Booklist book = createBook("A", "T");
    assertTrue(book.setIsbn("1234567890", false));
    assertEquals("1234567890", book.getIsbn());
  }

  // Dashes must be stripped before validation (e.g. "978-1-234-56789-0")
  @Test
  void setIsbn_withDashes() {
    Book_Booklist book = createBook("A", "T");
    assertTrue(book.setIsbn("978-1-234-56789-0", false));
    assertEquals("9781234567890", book.getIsbn());
  }

  // Empty string is a valid ISBN (means "no ISBN")
  @Test
  void setIsbn_blank() {
    Book_Booklist book = createBook("A", "T");
    assertTrue(book.setIsbn("", false));
    assertEquals("", book.getIsbn());
  }

  // ISBN with wrong digit count must be rejected; field must stay unchanged
  @Test
  void setIsbn_invalidFormat() {
    Book_Booklist book = createBook("A", "T");
    book.setIsbn("9781234567890", false);
    assertFalse(book.setIsbn("12345", false));
    assertEquals("9781234567890", book.getIsbn());
  }

  // Null ISBN must be rejected
  @Test
  void setIsbn_null() {
    Book_Booklist book = createBook("A", "T");
    assertFalse(book.setIsbn(null, false));
  }

  // Letters must be rejected even if the length matches
  @Test
  void setIsbn_letters() {
    Book_Booklist book = createBook("A", "T");
    assertFalse(book.setIsbn("abcdefghij", false));
  }

  // All simple getters/setters must store and retrieve values correctly
  @Test
  void settersAndGetters() {
    Book_Booklist book = createBook("A", "T");

    book.setAuthor("New Author");
    assertEquals("New Author", book.getAuthor());

    book.setTitle("New Title");
    assertEquals("New Title", book.getTitle());

    book.setNote("Note");
    assertEquals("Note", book.getNote());

    book.setSeries("Series");
    assertEquals("Series", book.getSeries());

    book.setSeriesVol("5");
    assertEquals("5", book.getSeriesVol());

    book.setEbook(true);
    assertTrue(book.isEbook());

    book.setBid(42);
    assertEquals(42, book.getBid());

    book.setBorrowed(true);
    assertTrue(book.isBorrowed());

    book.setBorrowedTo("Person");
    assertEquals("Person", book.getBorrowedTo());

    book.setBorrowedFrom("Other");
    assertEquals("Other", book.getBorrowedFrom());
  }
}
