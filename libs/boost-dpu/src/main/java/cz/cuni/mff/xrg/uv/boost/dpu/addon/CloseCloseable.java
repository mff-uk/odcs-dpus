package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
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
    public boolean preAction(DPUContext context, ConfigManager configManager) {
        return true;
    }

    @Override
    public void postAction(DPUContext context, ConfigManager configManager) {
        // close all
        for (AutoCloseable item : toCloseList) {
            try {
                item.close();
            } catch (Exception ex) {
                context.sendMessage(DPUContext.MessageType.ERROR, "Close failed", "", ex);
            }
        }
    }

}
