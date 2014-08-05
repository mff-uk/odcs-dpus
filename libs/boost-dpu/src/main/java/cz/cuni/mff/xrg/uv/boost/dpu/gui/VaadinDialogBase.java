package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.CustomComponent;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;

/**
 *
 * @author Škoda Petr
 * @param <C>
 */
public abstract class VaadinDialogBase<C>
    extends CustomComponent {

    /**
     * Dialog context.
     */
    ConfigDialogContext context;
    
    /**
     *
     * @return Dialog context.
     */
    protected ConfigDialogContext getContext() {
        return context;
    }

    /**
     * Build and initialise layout.
     */
    public abstract void buildLayout();

    /**
     * Set configuration for dialog.
     *
     * @param conf
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract void setConfiguration(C conf) throws DPUConfigException;

    /**
     * Get dialog configuration.
     *
     * @return
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract C getConfiguration() throws DPUConfigException;

}
