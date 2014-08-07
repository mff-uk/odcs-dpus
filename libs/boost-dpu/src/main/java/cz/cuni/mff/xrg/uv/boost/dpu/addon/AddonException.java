package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 *
 * @author Škoda Petr
 */
public class AddonException extends Exception {

    public AddonException(String message) {
        super(message);
    }

    public AddonException(String message, Throwable cause) {
        super(message, cause);
    }

}
