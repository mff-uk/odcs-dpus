package cz.cuni.mff.xrg.uv.boost.dpu.utils;

import eu.unifiedviews.dpu.DPUContext;
import java.util.LinkedList;
import java.util.List;

/**
 * Small helper for closing {@link AutoCloseable} classes.
 *
 * @author Škoda Petr
 */
public class CloseAutoCloseable {

    List<AutoCloseable> toCloseList = new LinkedList<>();

    public void add(AutoCloseable toClose) {
        if (toClose != null) {
            toCloseList.add(toClose);
        }
    }

    public void closeAll(DPUContext context) {
        for (AutoCloseable item : toCloseList) {
            try {
                item.close();
            } catch (Exception ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Close failed", "", ex);
            }
        }
    }

}
