package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;

/**
 * 
 * @author Škoda Petr
 * @param <C>
 */
public interface AddonWithDialog<C> extends Addon {

    /**
     * 
     * @return Caption that is used for {@link VaadinDialogBase}.
     */
    public String getDialogCaption();

    /**
     * Configuration name used to store configuration.
     * 
     * @return
     */
    public String getConfigName();

    /**
     * 
     * @return Respective configuration dialog.
     */
    public VaadinDialogBase<C> getDialog();

}
