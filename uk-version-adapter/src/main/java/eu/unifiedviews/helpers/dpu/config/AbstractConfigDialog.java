package eu.unifiedviews.helpers.dpu.config;

/**
 *
 * @author Škoda Petr
 */
public abstract class AbstractConfigDialog<C> extends eu.unifiedviews.dpu.config.vaadin.AbstractConfigDialog<C> {

    public abstract void setContext(ConfigDialogContext newContext);
    
    /**
     * Set context to the dialog. This method is called only once
     * before any other method.
     *
     * @param newContext
     */
    @Override
    public void setContext(eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext newContext) {
        this.setContext(new ConfigDialogContextImpl(newContext));
    }

}