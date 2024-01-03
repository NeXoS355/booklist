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
	
	
	public static BookListModel BookEintr�ge;
	public static WishlistListModel WishlistEintr�ge;

	  @Test
	  public void testBookList() {
		  BookEintr�ge = new BookListModel();
		Book_Booklist book = null;
		try {
			book = new Book_Booklist("Testautor", "Testtitel", true, "", "", "", "",
						"",false, null, null,null, new Timestamp(System.currentTimeMillis()), false);
			BookEintr�ge.add(book);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertEquals("Testautor", book.getAutor());
	    assertEquals("Testtitel", book.getTitel());
	    
	    book.setAutor("EditTest");
	    book.setAusgeliehen(true);
	    book.setAusgeliehen_an("TestAusleihe");
	    assertEquals("EditTest", book.getAutor());
	    assertEquals("TestAusleihe", book.getAusgeliehen_an());
	    
	    assertEquals(1, BookEintr�ge.getSize());
	    BookEintr�ge.delete(book);
	    assertEquals(0, BookEintr�ge.getSize());
	  }
	  
	  
	  @Test
	  public void testWishlist() {
		  WishlistEintr�ge = new WishlistListModel();
		Book_Wishlist book = null;
		try {
			book = new Book_Wishlist("Testautor", "Testtitel", "", "", "",new Timestamp(System.currentTimeMillis()), false);
			WishlistEintr�ge.add(book);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertEquals("Testautor", book.getAutor());
	    assertEquals("Testtitel", book.getTitel());
	    
	    book.setAutor("EditTest");
	    assertEquals("EditTest", book.getAutor());
	    
	    assertEquals(1, WishlistEintr�ge.getSize());
	    WishlistEintr�ge.delete(book);
	    assertEquals(0, WishlistEintr�ge.getSize());
	  }

}
