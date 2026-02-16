package gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainframeParseVersionTest {

  // Standard 3-part versions must concatenate to the correct integer
  @Test
  void parseVersion_standard() {
    assertEquals(100, Mainframe.parseVersion("1.0.0"));
    assertEquals(410, Mainframe.parseVersion("4.1.0"));
    assertEquals(328, Mainframe.parseVersion("3.2.8"));
  }

  // Multi-digit parts must be concatenated, not summed
  @Test
  void parseVersion_highNumbers() {
    assertEquals(102030, Mainframe.parseVersion("10.20.30"));
  }

  // A single number without dots must parse as-is
  @Test
  void parseVersion_singleDigit() {
    assertEquals(5, Mainframe.parseVersion("5"));
  }

  // Two-part version (no patch) must still concatenate correctly
  @Test
  void parseVersion_twoPartVersion() {
    assertEquals(41, Mainframe.parseVersion("4.1"));
  }

  // Newer versions must always produce a higher integer than older ones
  @Test
  void parseVersion_ordering() {
    assertTrue(Mainframe.parseVersion("4.1.0") > Mainframe.parseVersion("4.0.1"));
    assertTrue(Mainframe.parseVersion("4.0.1") > Mainframe.parseVersion("3.2.8"));
    assertTrue(Mainframe.parseVersion("10.0.0") > Mainframe.parseVersion("9.9.9"));
  }

  // Non-numeric or empty input must throw NumberFormatException
  @Test
  void parseVersion_invalidThrowsException() {
    assertThrows(NumberFormatException.class, () -> Mainframe.parseVersion("abc"));
    assertThrows(NumberFormatException.class, () -> Mainframe.parseVersion(""));
  }
}
