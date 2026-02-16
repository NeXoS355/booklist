package data;

import gui.Mainframe;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DBUpdaterTest {

  private Connection con;

  @BeforeAll
  static void initLogger() {
    if (Mainframe.logger == null) {
      Mainframe.logger = LogManager.getLogger(DBUpdaterTest.class);
    }
  }

  @BeforeEach
  void setUp() throws SQLException {
    con = DriverManager.getConnection("jdbc:sqlite::memory:");
    Database.createTable(con);
  }

  @AfterEach
  void tearDown() throws SQLException {
    if (con != null && !con.isClosed()) {
      con.close();
    }
  }

  // checkVersion must return the version string from the versions table
  @Test
  void checkVersion_returnsInsertedVersion() {
    assertEquals("4.0.0", DBUpdater.checkVersion(con));
  }

  // Empty versions table must return an empty string
  @Test
  void checkVersion_emptyTable() throws SQLException {
    try (Statement st = con.createStatement()) {
      st.execute("DELETE FROM versions");
    }
    assertEquals("", DBUpdater.checkVersion(con));
  }

  // checkUpdate with current version must not throw
  @Test
  void checkUpdate_currentVersion_doesNotThrow() {
    assertDoesNotThrow(() -> DBUpdater.checkUpdate(con));
  }

  // checkUpdate with an unknown version must not throw (logs a warning)
  @Test
  void checkUpdate_unknownVersion_doesNotThrow() throws SQLException {
    try (PreparedStatement st = con.prepareStatement("UPDATE versions SET version = ?")) {
      st.setString(1, "99.99.99");
      st.executeUpdate();
    }
    assertDoesNotThrow(() -> DBUpdater.checkUpdate(con));
  }
}
