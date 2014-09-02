package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.CustomComponent;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;

/**
 * Base dialog for {@link AddonWithDialog}s.
 * 
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class AddonDialogBase<CONFIG>
    extends CustomComponent {

    /**
     * Dialog context.
     */
    protected ConfigDialogContext context;
    
    /**
     * History of configuration class, if set used instead of
     * {@link #configClass}.
     */
    private final ConfigHistory<CONFIG> configHistory;

    public AddonDialogBase(ConfigHistory<CONFIG> configHistory) {
        this.configHistory = configHistory;
    }

    public AddonDialogBase(Class<CONFIG> configClass) {
        this.configHistory = ConfigHistory.createNoHistory(configClass);
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
        // config can be null, so we then use the default
        if (dpuConfig == null) {
            // create new class
            dpuConfig = configManager.createNew(configHistory.getFinalClass());
        }
        setConfiguration(dpuConfig);
    }

    /**
     * Store configuration from dialog into manager.
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
