package cz.cuni.mff.xrg.uv.boost.dpu.addon;

/**
 *
 * @author Škoda Petr
 */
public interface ExecutableAddon extends Addon {

    public enum ExecutionPoint {
        /**
         * It's called before DPU execution.
         */
        PRE_EXECUTE,
        /**
         * Is executed after used DPU code execution in innerExecute. Return
         * value from execute method is not used.
         */
        POST_EXECUTE
    }

    /**
     *
     * @param execPoint Place where the add-on is executed.
     * @return False if DPU's user code should not be executed.
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     */
    boolean execute(ExecutionPoint execPoint) throws AddonException;

}
