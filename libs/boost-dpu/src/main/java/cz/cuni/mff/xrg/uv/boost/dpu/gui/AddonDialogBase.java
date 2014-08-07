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
     * DPU's configuration class.
     */
    private final Class<CONFIG> configClass;

    /**
     * History of configuration class, if set used instead of
     * {@link #configClass}.
     */
    private final ConfigHistory<CONFIG> configHistory;

    public AddonDialogBase(ConfigHistory<CONFIG> configHistory) {
        this.configClass = null;
        this.configHistory = configHistory;
    }

    public AddonDialogBase(Class<CONFIG> configClass) {
        this.configClass = configClass;
        this.configHistory = null;
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
        CONFIG dpuConfig;
        if (configHistory == null) {
            // no history for configuration
            dpuConfig = configManager.get(getConfigClassName(), configClass);
        } else {
            dpuConfig = configManager.get(getConfigClassName(), configHistory);
        }
        // config can be null, so we then use the default
        if (dpuConfig == null) {
            // create new class
            dpuConfig = configManager.createNew(configClass != null ?
                    configClass : configHistory.getFinalClass());
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
