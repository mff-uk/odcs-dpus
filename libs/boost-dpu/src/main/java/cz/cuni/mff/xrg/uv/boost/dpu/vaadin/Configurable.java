package cz.cuni.mff.xrg.uv.boost.dpu.vaadin;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;

/**
 * Interface for configurable {@link Addon}.
 *
 * <strong>Configuration class must be static and with nonparametric constructor!</strong>
 * 
 * @author Škoda Petr
 * @param <CONFIG>
 */
public interface Configurable<CONFIG> extends AutoInitializer.Initializable {

    /**
     * 
     * @return Class of used configuration class.
     */
    Class<CONFIG> getConfigClass();

    /**
     * 
     * @return Caption that is used for {@link AbstractAddonDialog}, ie. name of respective Tab.
     */
    String getDialogCaption();

    /**
     * 
     * @return Respective configuration dialog.
     */
    AbstractAddonDialog<CONFIG> getDialog();

}
