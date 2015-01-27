package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.CustomComponent;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;

/**
 * Base dialog for {@link ConfigurableAddon}.
 * 
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class AbstractAddonVaadinDialog<CONFIG> extends CustomComponent {

    /**
     * Dialog context.
     * TODO: Use some richer version like in AdvancedDPu.
     */
    protected ConfigDialogContext context;
    
    /**
     * History of configuration class, if set used instead of {@link #configClass}.
     */
    private final ConfigHistory<CONFIG> configHistory;

    public AbstractAddonVaadinDialog(ConfigHistory<CONFIG> configHistory) {
        this.configHistory = configHistory;
    }

    /**
     *
     * @return Dialog context.
     */
    protected ConfigDialogContext getContext() {
        return context;
    }

    /**
     * Load configuration into dialog.
     *
     * @param configManager
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    public void loadConfig(ConfigManager configManager) throws DPUConfigException {
        CONFIG dpuConfig = configManager.get(getConfigClassName(), configHistory);
        // Config can be null, so we then use the default.
        if (dpuConfig == null) {
            // Create new class.
            dpuConfig = configManager.createNew(configHistory.getFinalClass());
        }
        setConfiguration(dpuConfig);
    }

    /**
     * Store configuration from dialog into given {@link ConfigManager}.
     *
     * @param configManager
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    public void storeConfig(ConfigManager configManager) throws DPUConfigException {
        configManager.set(getConfiguration(), getConfigClassName());
    }

    /**
     * Build and initialise layout.
     */
    public abstract void buildLayout();

    /**
     *
     * @return Key under which the configuration is saved.
     */
    protected abstract String getConfigClassName();

    /**
     * Set configuration for dialog.
     *
     * @param conf
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract void setConfiguration(CONFIG conf) throws DPUConfigException;

    /**
     * Get dialog configuration.
     *
     * @return
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract CONFIG getConfiguration() throws DPUConfigException;

}
