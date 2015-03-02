package eu.unifiedviews.helpers.dpu.localization;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class representing resource bundle containing localization messages
 */
public class Messages {

    private final String BUNDLE_NAME = "resources";

    private final ResourceBundle RESOURCE_BUNDLE;

    public Messages(Locale locale, ClassLoader classLoader) {
        RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, locale, classLoader);
    }

    /**
     * Get the resource bundle string stored under key, formatted using {@link MessageFormat}.
     *
     * @param key resource bundle key
     * @param args parameters to formatting routine
     * @return formatted string, returns "!key!" when the value is not found in bundle
     */
    public String getString(final String key, final Object... args) {
        try {
            return MessageFormat.format(RESOURCE_BUNDLE.getString(key), args);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}