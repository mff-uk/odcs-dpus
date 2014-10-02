package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;

/**
 * <strong>Configuration class must be static and with
 * nonparametric constructor!</strong>
 * 
 * @author Škoda Petr
 * @param <CONFIG>
 */
public interface ConfigurableAddon<CONFIG> extends Addon {

    /**
     * 
     * @return Caption that is used for {@link AddonVaadinDialogBase}.
     */
    public String getDialogCaption();

    /**
     * 
     * @return Respective configuration dialog.
     */
    public AddonVaadinDialogBase<CONFIG> getDialog();

}
