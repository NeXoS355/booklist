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
	
	
	public static BookListModel BookEinträge;
	public static WishlistListModel WishlistEinträge;

	  @Test
	  public void testBookList() {
		  BookEinträge = new BookListModel();
		Book_Booklist book = null;
		try {
			book = new Book_Booklist("Testautor", "Testtitel", true, "", "", "", "",
						"",false, null, null,null, new Timestamp(System.currentTimeMillis()), false);
			BookEinträge.add(book);
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
	    
	    assertEquals(1, BookEinträge.getSize());
	    BookEinträge.delete(book);
	    assertEquals(0, BookEinträge.getSize());
	  }
	  
	  
	  @Test
	  public void testWishlist() {
		  WishlistEinträge = new WishlistListModel();
		Book_Wishlist book = null;
		try {
			book = new Book_Wishlist("Testautor", "Testtitel", "", "", "",new Timestamp(System.currentTimeMillis()), false);
			WishlistEinträge.add(book);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    assertEquals("Testautor", book.getAutor());
	    assertEquals("Testtitel", book.getTitel());
	    
	    book.setAutor("EditTest");
	    assertEquals("EditTest", book.getAutor());
	    
	    assertEquals(1, WishlistEinträge.getSize());
	    WishlistEinträge.delete(book);
	    assertEquals(0, WishlistEinträge.getSize());
	  }

}
