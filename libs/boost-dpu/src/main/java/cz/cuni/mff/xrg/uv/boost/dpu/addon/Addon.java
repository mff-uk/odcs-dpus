package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import eu.unifiedviews.dpu.DPUContext;

/**
 *
 * @author Škoda Petr
 */
public interface Addon {

    /**
     *
     * @param context
     * @param configManager
     * @return False if DPU's user code should not be executed.
     */
    boolean preAction(DPUContext context, ConfigManager configManager);

    /**
     * Is executed after DPU's user code, or after all
     * {@link #preAction(cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext, cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager)}
     * if some of them return true.
     *
     * @param context
     * @param configManager
     */
    void postAction(DPUContext context, ConfigManager configManager);

}
