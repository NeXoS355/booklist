package gui;

import application.UpdateService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MainframeParseVersionTest {

  // Standard 3-part versions: major*1000000 + minor*1000 + patch
  @Test
  void parseVersion_standard() {
    assertEquals(1000000, UpdateService.parseVersion("1.0.0"));
    assertEquals(4001000, UpdateService.parseVersion("4.1.0"));
    assertEquals(3002008, UpdateService.parseVersion("3.2.8"));
  }

  // Multi-digit parts must be handled correctly
  @Test
  void parseVersion_highNumbers() {
    assertEquals(10020030, UpdateService.parseVersion("10.20.30"));
  }

  // A single number without dots must parse as-is
  @Test
  void parseVersion_singleDigit() {
    assertEquals(5, UpdateService.parseVersion("5"));
  }

  // Two-part version (no patch) must still work correctly
  @Test
  void parseVersion_twoPartVersion() {
    assertEquals(4001, UpdateService.parseVersion("4.1"));
  }

  // Newer versions must always produce a higher integer than older ones
  @Test
  void parseVersion_ordering() {
    assertTrue(UpdateService.parseVersion("4.1.0") > UpdateService.parseVersion("4.0.1"));
    assertTrue(UpdateService.parseVersion("4.0.1") > UpdateService.parseVersion("3.2.8"));
    assertTrue(UpdateService.parseVersion("10.0.0") > UpdateService.parseVersion("9.9.9"));
    assertTrue(UpdateService.parseVersion("3.10.0") > UpdateService.parseVersion("3.9.1"));
    assertTrue(UpdateService.parseVersion("4.0.0") > UpdateService.parseVersion("3.10.2"));
  }

  // Non-numeric or empty input must throw NumberFormatException
  @Test
  void parseVersion_invalidThrowsException() {
    assertThrows(NumberFormatException.class, () -> UpdateService.parseVersion("abc"));
    assertThrows(NumberFormatException.class, () -> UpdateService.parseVersion(""));
  }
}
