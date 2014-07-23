package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.CustomComponent;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;

/**
 *
 * @author Škoda Petr
 * @param <C>
 */
public abstract class AdvancedConfigDialogBase<C> 
    extends CustomComponent {

    /**
     * Dialog context.
     */
    protected ConfigDialogContext context;
    
    /**
     * Build and initialise layout.
     */
    public abstract void buildLayout();

    /**
     * Set configuration for dialog.
     *
     * @param conf
     * @return
     */
    public abstract boolean setConfiguration(C conf);

    /**
     * Get dialog configuration.
     *
     * @return
     */
    public abstract C getConfiguration();

}
