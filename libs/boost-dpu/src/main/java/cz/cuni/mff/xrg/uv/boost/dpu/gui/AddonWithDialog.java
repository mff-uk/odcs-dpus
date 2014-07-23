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
     * @return Caption that is used for {@link AdvancedConfigDialogBase}.
     */
    public String getDialogCaption();

    /**
     * 
     * @return Respective configuration dialog.
     */
    public AdvancedConfigDialogBase<C> getDialog();

}
