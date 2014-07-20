package cz.cuni.mff.xrg.uv.configuration.dialog;

/**
 *
 * @author Škoda Petr
 */
public class ConfigException extends Exception {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

}
