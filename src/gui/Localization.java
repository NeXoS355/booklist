package gui;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {
    private static ResourceBundle messages;

    public static void setLocale(Locale locale) {
        messages = ResourceBundle.getBundle("text", locale);
    }

    public static String get(String key) {
        return messages.getString(key);
    }
}
