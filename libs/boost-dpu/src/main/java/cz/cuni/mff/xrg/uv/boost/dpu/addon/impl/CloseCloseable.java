package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.ExecutableAddon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import eu.unifiedviews.dpu.DPUContext;
import java.util.LinkedList;
import java.util.List;

/**
 * At the end of execution close given {@link Cloneable} classes. This can be handy especially if multiple
 * resources that need to be close are used at once. An resource try-catch should be considerer as an
 * alternative to this add-on.
 *
 * If close operation fail then send a warning event. The given resources/objects are always closed no mater
 * the user code execution ending (exception, return, error event).
 *
 * Usage sample:
 * <pre>
 * {@code
 *  FilesDataUnit.Iteration iter;
 *  try {
 *      iter = inFilesToLoad.getIteration();
 *      getAddon(CloseCloseable.class).add(iter);
 *  } catch (DataUnitException ex) {
 *      // Exception handling.
 *  }
 *  // Work with iterator here.
 * }
 * </pre>
 *
 * @see cz.cuni.mff.xrg.uv.boost.dpu.addonAddon
 * @author Škoda Petr
 */
public class CloseCloseable implements ExecutableAddon {

    /**
     * List of resources to close.
     */
    private final List<AutoCloseable> toCloseList = new LinkedList<>();

    /**
     * DPU's context.
     */
    private DpuAdvancedBase.Context context;

    /**
     * 
     * @param toClose Object to close at the end of DPU's execution.
     */
    public void add(AutoCloseable toClose) {
        if (toClose != null) {
            toCloseList.add(toClose);
        }
    }

    @Override
    public void init(DpuAdvancedBase.Context context) {
        this.context = context;
    }

    @Override
    public void execute(ExecutionPoint execPoint) {
        if (execPoint != ExecutionPoint.POST_EXECUTE) {
            return;
        }
        // Close all.
        for (AutoCloseable item : toCloseList) {
            try {
                item.close();
            } catch (Exception ex) {
                context.getDpuContext().sendMessage(DPUContext.MessageType.WARNING, "Close failed", "", ex);
            }
        }
    }

}
