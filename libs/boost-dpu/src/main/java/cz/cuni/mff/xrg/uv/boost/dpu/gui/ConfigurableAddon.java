package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;

/**
 * Interface for configurable {@link Addon}.
 *
 * <strong>Configuration class must be static and with nonparametric constructor!</strong>
 * 
 * @author Škoda Petr
 * @param <CONFIG>
 */
public interface ConfigurableAddon<CONFIG> extends Addon {

    /**
     * Initialise add-on with configuration context. Used from DPU dialog. This function should not throw!
     *
     * @param context
     */
    void init(AdvancedVaadinDialogBase.Context context);

    /**
     * 
     * @return Class of used configuration class.
     */
    Class<CONFIG> getConfigClass();

    /**
     * 
     * @return Caption that is used for {@link AddonVaadinDialogBase}, ie. name of respective Tab.
     */
    String getDialogCaption();

    /**
     * 
     * @return Respective configuration dialog.
     */
    AddonVaadinDialogBase<CONFIG> getDialog();

}
