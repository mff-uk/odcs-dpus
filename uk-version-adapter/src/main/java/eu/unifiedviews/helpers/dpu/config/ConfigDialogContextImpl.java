package eu.unifiedviews.helpers.dpu.config;

import java.util.Locale;

/**
 *
 * @author Škoda Petr
 */
public class ConfigDialogContextImpl implements ConfigDialogContext  {

    private final eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext ctx;

    public ConfigDialogContextImpl(eu.unifiedviews.dpu.config.vaadin.ConfigDialogContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public boolean isTemplate() {
        return ctx.isTemplate();
    }

    @Override
    public Locale getLocale() {
        return ctx.getLocale();
    }

}
