package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 * Used to report problem in/with {@link Addon}.
 *
 * @author Škoda Petr
 */
public class AddonException extends Exception {

    public AddonException(String message) {
        super(message);
    }

    public AddonException(String format, Object ... params) {
        super(String.format(format, params));
    }

    public AddonException(String message, Throwable cause) {
        super(message, cause);
    }

}
