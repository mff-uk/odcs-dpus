package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.ConfigTransformerAddon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import static cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase.DPU_CONFIG_NAME;

/**
 * Base class for DPU's configuration dialogs.
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class AdvancedVaadinDialogBase<CONFIG> extends AbstractConfigDialog<MasterConfigObject> {

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedVaadinDialogBase.class);

    /**
     * Holds information stored in dialog.
     */
    public class Context {

        /**
         * Owner dialog.
         */
        private final AdvancedVaadinDialogBase dialog;

        /**
         * Core context.
         */
        private ConfigDialogContext originalDialogContext;

        /**
         * History of configuration class, if set used instead of {@link #configClass}.
         */
        private final ConfigHistory<CONFIG> configHistory;

        /**
         * List of all add-ons.
         */
        private final List<Addon> addons = new LinkedList<>();

        /**
         * List of add-on dialogs.
         */
        private final List<AddonVaadinDialogBase> addonDialogs = new LinkedList<>();

        public Context(AdvancedVaadinDialogBase dialog, ConfigHistory<CONFIG> configHistory,
                List<AddonInitializer.AddonInfo> addonsInfo) {
            this.dialog = dialog;
            this.configHistory = configHistory;
            for (AddonInitializer.AddonInfo addonInfo : addonsInfo) {
                addons.add(addonInfo.getAddon());
            }
        }

        public AdvancedVaadinDialogBase getDialog() {
            return dialog;
        }

        public ConfigDialogContext getOriginalDialogContext() {
            return originalDialogContext;
        }

        public ConfigHistory<CONFIG> getConfigHistory() {
            return configHistory;
        }

        public List<Addon> getAddons() {
            return addons;
        }

        public List<AddonVaadinDialogBase> getAddonDialogs() {
            return addonDialogs;
        }

    }

    /**
     * Serialization service for root configuration.
     */
    private final SerializationXmlGeneral serializationXml;

    /**
     * Configuration manager.
     */
    private ConfigManager configManager = null;

    /**
     * Main tab sheet.
     */
    private final TabSheet tabSheet = new TabSheet();

    /**
     * Currently set main sheet.
     */
    private Tab mainTab = null;

    /**
     * Store value of last set configuration.
     */
    private String lastSetConfiguration = null;

    /**
     * Dialog's originalDialogContext.
     */
    private final Context context;

    public AdvancedVaadinDialogBase(Class<CONFIG> configClass, List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory.serializationXmlGeneral();
        // This alias is also set in DpuAdvancedBase, they muset be tha same!
        this.serializationXml.addAlias(MasterConfigObject.class, "MasterConfigObject");
        this.context = new Context(this, ConfigHistory.createNoHistory(configClass), addons);
        // Create config manager and initialize addons.
        List<ConfigTransformerAddon> configAddons = new ArrayList<>(2);
        for (Addon addon : this.context.addons) {
            if (addon instanceof ConfigTransformerAddon) {
                configAddons.add((ConfigTransformerAddon) addon);
            }
            if (addon instanceof ConfigurableAddon) {
                ((ConfigurableAddon)addon).init(this.context);
            }
        }
        this.configManager = new ConfigManager(serializationXml, configAddons);
        // Build main layout.
        buildMainLayout();
    }

    public AdvancedVaadinDialogBase(ConfigHistory<CONFIG> configHistory,
            List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory.serializationXmlGeneral();
        // This alias is also set in DpuAdvancedBase, they muset be tha same!
        this.serializationXml.addAlias(MasterConfigObject.class, "MasterConfigObject");
        this.context = new Context(this, configHistory, addons);
        // Create config manager and initialize addons.
        List<ConfigTransformerAddon> configAddons = new ArrayList<>(2);
        for (Addon addon : this.context.addons) {
            if (addon instanceof ConfigTransformerAddon) {
                configAddons.add((ConfigTransformerAddon) addon);
            }
            if (addon instanceof ConfigurableAddon) {
                ((ConfigurableAddon)addon).init(this.context);
            }
        }
        this.configManager = new ConfigManager(serializationXml, configAddons);        
        // Build main layout.
        buildMainLayout();
    }

    /**
     * Build main layout and add dialogs for add-ons.
     *
     * @param addons
     */
    private void buildMainLayout() {
        setSizeFull();
        tabSheet.setSizeFull();
        // Prepare add-ons.
        for (Addon addon : this.context.addons) {
            if (addon instanceof ConfigurableAddon) {
                final ConfigurableAddon addonWithDialog = (ConfigurableAddon) addon;
                final AddonVaadinDialogBase dialog = addonWithDialog.getDialog();
                if (dialog == null) {
                    LOG.error("Dialog is ignored as it's null: {}", addonWithDialog.getDialogCaption());
                } else {
                    dialog.buildLayout();
                    addTab(dialog, addonWithDialog.getDialogCaption());
                    this.context.addonDialogs.add(dialog);
                }
            }
        }
        super.setCompositionRoot(tabSheet);
    }

    @Override
    protected void setCompositionRoot(Component compositionRoot) {
        if (mainTab != null && mainTab.getComponent().equals(compositionRoot)) {
            // Already set, just update selected tab index.
            tabSheet.setSelectedTab(0);
            return;
        }
        final Tab newTab = tabSheet.addTab(compositionRoot, "DPU configuration");
        // Remove old one if set, and set new as a master tab (tab with DPU's configuration).
        if (mainTab != null) {
            tabSheet.removeTab(mainTab);
        }
        mainTab = newTab;
        tabSheet.setTabPosition(newTab, 0);
        tabSheet.setSelectedTab(0);
    }

    /**
     *
     * @param component Tab to add.
     * @param caption   Tab name.
     */
    protected void addTab(Component component, String caption) {
        final Tab newTab = tabSheet.addTab(component, caption);
    }

    @Override
    public void setContext(ConfigDialogContext newContext) {
        this.context.originalDialogContext = newContext;
    }

    /**
     *
     * @return Dialog originalDialogContext.
     */
    protected ConfigDialogContext getContext() {
        return this.context.originalDialogContext;
    }

    @Override
    public void setConfig(String conf) throws DPUConfigException {
        configManager.setMasterConfig(conf);
        // Configure DPU's dialog.
        final CONFIG dpuConfig = configManager.get(DPU_CONFIG_NAME, this.context.configHistory);
        setConfiguration(dpuConfig);
        // Configura add-ons.
        for (AddonVaadinDialogBase dialogs : this.context.addonDialogs) {
            dialogs.loadConfig(configManager);
        }
        // Update last configuration.
        this.lastSetConfiguration = conf;
    }

    @Override
    public String getConfig() throws DPUConfigException {
        // Clear config mamanger.
        configManager.setMasterConfig(new MasterConfigObject());
        // Get configuration from DPU.
        CONFIG dpuConfig = getConfiguration();
        configManager.set(dpuConfig, DpuAdvancedBase.DPU_CONFIG_NAME);
        // Get configuration from addons.
        for (AddonVaadinDialogBase dialogs : this.context.addonDialogs) {
            dialogs.storeConfig(configManager);
        }
        // Convert all into a string.
        try {
            return serializationXml.convert(configManager.getMasterConfig());
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Conversion failed.", ex);
        }
    }

    @Override
    public String getToolTip() {
        return "";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean hasConfigChanged() {
        // We utilize string form of configuration to decide it the configuration has changed or not.
        // This could be done probably better, but not in general case.
        final String configString;
        try {
            configString = getConfig();
        } catch (DPUConfigException ex) {
            // Exception according to definition return false.
            LOG.warn("Dialog configuration is invalid. It's assumed unchanged: ", ex);
            return false;
        } catch (Throwable ex) {
            LOG.warn("Unexpected exception. Configuration is assumed to be unchanged.", ex);
            return false;
        }

        if (this.lastSetConfiguration == null) {
            return configString == null;
        } else {
            return this.lastSetConfiguration.compareTo(configString) != 0;
        }
    }

    /**
     * Set DPU's configuration for dialog.
     *
     * @param conf
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract void setConfiguration(CONFIG conf) throws DPUConfigException;

    /**
     * Get DPU's dialog configuration.
     *
     * @return
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract CONFIG getConfiguration() throws DPUConfigException;

}
