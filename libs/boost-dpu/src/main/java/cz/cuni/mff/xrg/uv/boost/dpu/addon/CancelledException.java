package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 * Add-on should throw this exception if the execution has been cancelled in add-on code.
 *
 * @author Škoda Petr
 */
public class CancelledException extends AddonException {

    public CancelledException() {
        super("Execution canceleld by user.");
    }

}
