package application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BorrowStatusTest {

  // Verify each enum constant maps to the correct DB string
  @Test
  void getDbValue_returnsCorrectValues() {
    assertEquals("lent", BorrowStatus.LENT_TO.getDbValue());
    assertEquals("borrowed", BorrowStatus.BORROWED_FROM.getDbValue());
    assertEquals("none", BorrowStatus.NONE.getDbValue());
  }

  // Verify reverse lookup from DB string to enum works for all valid values
  @Test
  void fromDbValue_validValues() {
    assertEquals(BorrowStatus.LENT_TO, BorrowStatus.fromDbValue("lent"));
    assertEquals(BorrowStatus.BORROWED_FROM, BorrowStatus.fromDbValue("borrowed"));
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue("none"));
  }

  // Null input should default to NONE
  @Test
  void fromDbValue_nullReturnsNone() {
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue(null));
  }

  // Unknown or empty strings should default to NONE
  @Test
  void fromDbValue_unknownValueReturnsNone() {
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue("unknown"));
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue(""));
  }

  // Lookup uses exact match — wrong case must not match
  @Test
  void fromDbValue_isCaseSensitive() {
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue("Lent"));
    assertEquals(BorrowStatus.NONE, BorrowStatus.fromDbValue("BORROWED"));
  }

  // Converting to DB value and back must return the original enum
  @Test
  void roundTrip_allValues() {
    for (BorrowStatus status : BorrowStatus.values()) {
      assertEquals(status, BorrowStatus.fromDbValue(status.getDbValue()));
    }
  }
}
