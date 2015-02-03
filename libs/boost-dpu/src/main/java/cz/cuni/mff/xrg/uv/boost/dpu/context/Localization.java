package cz.cuni.mff.xrg.uv.boost.dpu.context;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * @author eea
 */
public class Localization {

    private final String BUNDLE_NAME = "i18n";

    /**
     * Used resource bundle.
     */
    private ResourceBundle resourceBundle = null;

    /**
     * Set current locale.
     *
     * @param locale
     */
    public void setLocale(Locale locale, ClassLoader classLoader) {
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, classLoader);
    }

    /**
     * Get the resource bundle string stored under key, formatted using {@link MessageFormat}.
     *
     * @param key resource bundle key
     * @param args parameters to formatting routine
     * @return formatted string, returns "!key!" when the value is not found in bundle
     */
    public String getString(final String key, final Object... args) {
        if (resourceBundle == null) {
            throw new RuntimeException("Localization module has not been initialized!");
        }
        try {
            return MessageFormat.format(resourceBundle.getString(key), args);
        } catch (MissingResourceException e) {
            // Fallback for missing values.
            return '!' + key + '!';
        }
    }

}
