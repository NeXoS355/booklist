package gui;

import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.MissingResourceException;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationTest {

  // German locale must load the German resource bundle
  @Test
  void setLocale_german_loadsBundle() {
    Localization.setLocale(Locale.GERMAN);
    assertEquals("Autor", Localization.get("label.author"));
  }

  // English locale must load the English resource bundle
  @Test
  void setLocale_english_loadsBundle() {
    Localization.setLocale(Locale.ENGLISH);
    assertEquals("Author", Localization.get("label.author"));
  }

  // Existing key must return the localized string
  @Test
  void get_existingKey() {
    Localization.setLocale(Locale.GERMAN);
    assertEquals("speichern", Localization.get("label.save"));
  }

  // Non-existent key must throw MissingResourceException
  @Test
  void get_missingKeyThrowsException() {
    Localization.setLocale(Locale.GERMAN);
    assertThrows(MissingResourceException.class, () -> Localization.get("nonexistent.key"));
  }

  // Switching locale must change the returned values
  @Test
  void switchLocale_changesValues() {
    Localization.setLocale(Locale.GERMAN);
    String german = Localization.get("label.save");

    Localization.setLocale(Locale.ENGLISH);
    String english = Localization.get("label.save");

    assertEquals("speichern", german);
    assertEquals("Save", english);
  }

  // Migration-related keys must exist in both locales
  @Test
  void migrationKeys_exist() {
    Localization.setLocale(Locale.GERMAN);
    assertDoesNotThrow(() -> Localization.get("update.migrationDone"));
    assertDoesNotThrow(() -> Localization.get("q.updateMigration"));

    Localization.setLocale(Locale.ENGLISH);
    assertDoesNotThrow(() -> Localization.get("update.migrationDone"));
    assertDoesNotThrow(() -> Localization.get("q.updateMigration"));
  }
}
