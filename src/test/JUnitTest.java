package test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import application.BookListModel;
import application.Book_Booklist;
import application.Book_Wishlist;
import application.WishlistListModel;

class JUnitTest {

	public static BookListModel BookEntries;
	public static WishlistListModel wishlistEntries;

	@Test
	public void testBookList() {
		BookEntries = new BookListModel();
		Book_Booklist book = null;
		book = new Book_Booklist("Testautor", "Testtitel", true, "", "", "", "", "", false, null, null, null,
				new Timestamp(System.currentTimeMillis()), false);
		BookEntries.add(book);
		// Is Book added?
		int anzahl = BookEntries.getSize();
		assertTrue(anzahl > 0);

		// Is Book deleted?
		BookEntries.delete(book);
		assertTrue(BookEntries.getSize() == anzahl - 1);

		// Is Book correctly saved?
		assertEquals("Testautor", book.getAuthor());
		assertEquals("Testtitel", book.getTitle());

		// Is Book editable?
		book.setAuthor("EditTest");
		book.setBorrowed(true);
		book.setBorrowedTo("TestAusleihe");
		assertEquals("EditTest", book.getAuthor());
		assertEquals("TestAusleihe", book.getBorrowedTo());

	}

	@Test
	public void testWishlist() {
		wishlistEntries = new WishlistListModel();
		Book_Wishlist book = null;
		try {
			book = new Book_Wishlist("Testautor", "Testtitel", "", "", "", new Timestamp(System.currentTimeMillis()),
					false);
			wishlistEntries.add(book);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertEquals("Testautor", book.getAuthor());
		assertEquals("Testtitel", book.getTitle());

		book.setAuthor("EditTest");
		assertEquals("EditTest", book.getAuthor());

		assertEquals(1, wishlistEntries.getSize());
		wishlistEntries.delete(book);
		assertEquals(0, wishlistEntries.getSize());
	}

}
