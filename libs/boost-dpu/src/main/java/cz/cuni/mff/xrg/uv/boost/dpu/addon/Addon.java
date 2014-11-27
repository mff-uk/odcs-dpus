package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;

/**
 * Base common interface for add-ons. Add-ons can be used to add additional functionality into DPUs.
 *
 * @author Škoda Petr
 */
public interface Addon {

    /**
     * Initialise add-on with execution context.
     * 
     * @param context
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     */
    void init(DpuAdvancedBase.Context context) throws AddonException;

}
