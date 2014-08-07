package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import eu.unifiedviews.dpu.DPUContext;
import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;

/**
 * At the end of execution close given {@link Cloneable} classes.
 *
 * @author Škoda Petr
 */
public class CloseCloseable implements Addon {

    private final List<Closeable> toCloseList = new LinkedList<>();

    /**
     * Add class to close.
     * 
     * @param toClose
     */
    public void add(Closeable toClose) {
        if (toClose != null) {
            toCloseList.add(toClose);
        }
    }

    @Override
    public boolean preAction(DpuAdvancedBase.Context context) {
        return true;
    }

    @Override
    public void postAction(DpuAdvancedBase.Context context) {
        // close all
        for (AutoCloseable item : toCloseList) {
            try {
                item.close();
            } catch (Exception ex) {
                context.getDpuContext().sendMessage(
                        DPUContext.MessageType.ERROR, "Close failed", "", ex);
            }
        }
    }

}
