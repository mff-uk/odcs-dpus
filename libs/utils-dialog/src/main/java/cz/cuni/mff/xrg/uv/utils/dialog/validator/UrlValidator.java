package cz.cuni.mff.xrg.uv.utils.dialog.validator;

import com.vaadin.data.Validator;
import java.net.MalformedURLException;

/**
 * Validate given value to be full URL.
 * 
 * @author Škoda Petr
 */
public class UrlValidator implements Validator {

    /**
     * If true them empty value is considered to be valid URL.
     */
    private boolean emptyAllowed = true;

    public UrlValidator() {
    }

    /**
     *
     * @param emptyAllowed If true then empty value is considered to be a valid URL.
     */
    public UrlValidator(boolean emptyAllowed) {
        this.emptyAllowed = emptyAllowed;
    }

    @Override
    public void validate(Object value) throws InvalidValueException {
        if (value instanceof String) {
            final String valueStr = (String)value;
            // null instance does not pass 'instanceof' test.
            if (emptyAllowed && valueStr.isEmpty()) {
                return;
            }

            try {
                new java.net.URL(valueStr);
            } catch (MalformedURLException ex) {
                throw new InvalidValueException("Invalid uri: " + valueStr);
            }

        }
    }

}
