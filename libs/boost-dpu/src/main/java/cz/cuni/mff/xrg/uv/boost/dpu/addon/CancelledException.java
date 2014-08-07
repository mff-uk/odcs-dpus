package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 *
 * @author Škoda Petr
 */
public class CancelledException extends AddonException {

    public CancelledException() {
        super("Execution canceleld by user.");
    }

}
