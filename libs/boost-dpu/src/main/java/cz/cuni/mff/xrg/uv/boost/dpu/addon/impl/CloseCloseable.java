package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.ExecutableAddon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import eu.unifiedviews.dpu.DPUContext;
import java.util.LinkedList;
import java.util.List;

/**
 * At the end of execution close given {@link Cloneable} classes.
 *
 * @author Škoda Petr
 */
public class CloseCloseable implements ExecutableAddon {

    private final List<AutoCloseable> toCloseList = new LinkedList<>();

    private DpuAdvancedBase.Context context;

    /**
     * Add class to close.
     * 
     * @param toClose
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
    public boolean execute(ExecutionPoint execPoint) {
        if (execPoint != ExecutionPoint.POST_EXECUTE) {
            return true;
        }

        // close all
        for (AutoCloseable item : toCloseList) {
            try {
                item.close();
            } catch (Exception ex) {
                context.getDpuContext().sendMessage(
                        DPUContext.MessageType.ERROR, "Close failed", "", ex);
            }
        }
        return true;
    }

}
