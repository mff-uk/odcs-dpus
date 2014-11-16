package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 * Interface for add-on that contains code that should be executed during DPU execution.
 *
 * @author Škoda Petr
 */
public interface ExecutableAddon extends Addon {

    public enum ExecutionPoint {
        /**
         * It's called before DPU execution. If exception is thrown here, the DPU's user code is not executed,
         * no other add-on for this point is called. DPU execution fail. For all other points
         * {@link #execute(cz.cuni.mff.xrg.uv.boost.dpu.addon.ExecutableAddon.ExecutionPoint)} is called.
         */
        PRE_EXECUTE,
        /**
         * Is executed after used DPU code execution in innerExecute. If throws then error message is logged
         * ie. DPU's execution fail, but all other add-ons are executed.
         */
        POST_EXECUTE
    }

    /**
     *
     * @param execPoint Place where the add-on is executed.
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException Throw in case of failure.
     */
    void execute(ExecutionPoint execPoint) throws AddonException;

}
