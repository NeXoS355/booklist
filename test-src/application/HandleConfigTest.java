package application;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class HandleConfigTest {

  @BeforeAll
  static void initLogger() {
    if (gui.Mainframe.logger == null) {
      gui.Mainframe.logger = org.apache.logging.log4j.LogManager.getLogger(HandleConfigTest.class);
    }
  }

  // --- generateRandomToken ---

  // Token length must exactly match the requested length
  @Test
  void generateRandomToken_correctLength() {
    assertEquals(64, HandleConfig.generateRandomToken(64).length());
    assertEquals(10, HandleConfig.generateRandomToken(10).length());
    assertEquals(1, HandleConfig.generateRandomToken(1).length());
  }

  // Zero length must produce an empty string
  @Test
  void generateRandomToken_zeroLength() {
    assertEquals("", HandleConfig.generateRandomToken(0));
  }

  // Token must only contain alphanumeric characters (A-Z, a-z, 0-9)
  @Test
  void generateRandomToken_onlyAlphanumeric() {
    String token = HandleConfig.generateRandomToken(1000);
    assertTrue(token.matches("[A-Za-z0-9]+"));
  }

  // Two generated tokens must not be identical (randomness check)
  @Test
  void generateRandomToken_notAlwaysSame() {
    String token1 = HandleConfig.generateRandomToken(64);
    String token2 = HandleConfig.generateRandomToken(64);
    assertNotEquals(token1, token2);
  }

  // --- getInt(props, key, default) ---

  // Valid integer string must be parsed correctly
  @Test
  void getInt_validValue() {
    Properties props = new Properties();
    props.setProperty("key", "42");
    assertEquals(42, HandleConfig.getInt(props, "key", 0));
  }

  // Missing key must return the default value
  @Test
  void getInt_missingKeyReturnsDefault() {
    Properties props = new Properties();
    assertEquals(99, HandleConfig.getInt(props, "missing", 99));
  }

  // Non-numeric string must fall back to default
  @Test
  void getInt_invalidValueReturnsDefault() {
    Properties props = new Properties();
    props.setProperty("key", "abc");
    assertEquals(5, HandleConfig.getInt(props, "key", 5));
  }

  // Leading/trailing whitespace must be trimmed before parsing
  @Test
  void getInt_whitespaceIsTrimmed() {
    Properties props = new Properties();
    props.setProperty("key", "  7  ");
    assertEquals(7, HandleConfig.getInt(props, "key", 0));
  }

  // Negative integers must be parsed correctly
  @Test
  void getInt_negativeValue() {
    Properties props = new Properties();
    props.setProperty("key", "-3");
    assertEquals(-3, HandleConfig.getInt(props, "key", 0));
  }

  // --- getInt(props, key, default, min, max) ---

  // Value within [min, max] must be accepted
  @Test
  void getIntRange_withinRange() {
    Properties props = new Properties();
    props.setProperty("key", "1");
    assertEquals(1, HandleConfig.getInt(props, "key", 0, 0, 2));
  }

  // Value below min must fall back to default
  @Test
  void getIntRange_belowMinReturnsDefault() {
    Properties props = new Properties();
    props.setProperty("key", "-1");
    assertEquals(0, HandleConfig.getInt(props, "key", 0, 0, 2));
  }

  // Value above max must fall back to default
  @Test
  void getIntRange_aboveMaxReturnsDefault() {
    Properties props = new Properties();
    props.setProperty("key", "5");
    assertEquals(0, HandleConfig.getInt(props, "key", 0, 0, 2));
  }

  // Exact boundary values (min and max) must be accepted
  @Test
  void getIntRange_exactMinAndMax() {
    Properties props = new Properties();
    props.setProperty("min", "0");
    props.setProperty("max", "2");
    assertEquals(0, HandleConfig.getInt(props, "min", 1, 0, 2));
    assertEquals(2, HandleConfig.getInt(props, "max", 1, 0, 2));
  }

  // Missing key with range check must return default
  @Test
  void getIntRange_missingKeyReturnsDefault() {
    Properties props = new Properties();
    assertEquals(1, HandleConfig.getInt(props, "missing", 1, 0, 2));
  }
}
