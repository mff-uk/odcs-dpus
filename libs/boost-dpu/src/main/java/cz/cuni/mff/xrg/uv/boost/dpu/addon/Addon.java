package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;

/**
 * Base common interface for add-ons.
 *
 * @author Škoda Petr
 */
public interface Addon {

    /**
     * Initialise add-on with context.
     * 
     * @param context
     * @throws cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException
     */
    public void init(DpuAdvancedBase.Context context) throws AddonException;

}
