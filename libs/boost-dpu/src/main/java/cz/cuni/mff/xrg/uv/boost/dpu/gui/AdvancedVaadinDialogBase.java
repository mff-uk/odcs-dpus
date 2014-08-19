package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
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
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase.DPU_CONFIG_NAME;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class AdvancedVaadinDialogBase<CONFIG>
        extends AbstractConfigDialog<MasterConfigObject> {

    private static final Logger LOG = LoggerFactory.getLogger(
            AdvancedVaadinDialogBase.class);

    private ConfigDialogContext context;

    /**
     * Serialisation service for root configuration.
     */
    private final SerializationXmlGeneral serializationXml;

    /**
     * DPU's configuration class.
     */
    private final Class<CONFIG> configClass;

    /**
     * Configuration manager.
     */
    private ConfigManager configManager = null;

    /**
     * History of configuration class, if set used instead of
     * {@link #configClass}.
     */
    private final ConfigHistory<CONFIG> configHistory;

    /**
     * Main tab sheet.
     */
    private final TabSheet tabSheet;

    /**
     * List of configurable addons.
     */
    private final List<AddonDialogBase> addons;

    /**
     * Currently set main sheet.
     */
    private Tab mainTab = null;

    public AdvancedVaadinDialogBase(Class<CONFIG> configClass,
            List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory
                .serializationXmlGeneral();
        this.serializationXml.addAlias(MasterConfigObject.class,
                "MasterConfigObject");
        this.configClass = configClass;
        this.configManager = new ConfigManager(new MasterConfigObject(),
                serializationXml);
        this.configHistory = null;
        // addons - add dialogs
        this.addons = new LinkedList<>();
        this.tabSheet = new TabSheet();
        buildMainLayout(addons);
    }

    public AdvancedVaadinDialogBase(ConfigHistory<CONFIG> configHistory,
            List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory
                .serializationXmlGeneral();
        this.serializationXml.addAlias(MasterConfigObject.class, "MasterConfigObject");
        this.configClass = configHistory.getFinalClass();
        this.configManager = new ConfigManager(new MasterConfigObject(),
                serializationXml);
        this.configHistory = configHistory;
        // addons - add dialogs
        this.addons = new LinkedList<>();
        this.tabSheet = new TabSheet();
        buildMainLayout(addons);
    }

    /**
     * Build main layout and add dialogs for addons.
     *
     * @param addons
     */
    private void buildMainLayout(List<AddonInitializer.AddonInfo> addons) {
        tabSheet.setSizeFull();

        for (AddonInitializer.AddonInfo addonInfo : addons) {
            if (addonInfo.getAddon() instanceof AddonWithVaadinDialog) {
                final AddonWithVaadinDialog addonWithDialog
                        = (AddonWithVaadinDialog) addonInfo.getAddon();
                final AddonDialogBase dialog = addonWithDialog.getDialog();
                if (dialog == null) {
                    LOG.error("Dialog is ignored as it's null: {}",
                            addonWithDialog.getDialogCaption());
                } else {
                    dialog.buildLayout();
                    tabSheet.addTab(dialog, addonWithDialog.getDialogCaption());
                    this.addons.add(dialog);
                }
            }
        }

        super.setCompositionRoot(tabSheet);
    }

    @Override
    protected void setCompositionRoot(Component compositionRoot) {
        if (mainTab != null && mainTab.getComponent().equals(compositionRoot)) {
            // already set
            return;
        }
        final Tab newTab = tabSheet.addTab(compositionRoot, "DPU configuration");
        // remove old one if set
        if (mainTab != null) {
            tabSheet.removeTab(mainTab);
        }
        mainTab = newTab;
        tabSheet.setTabPosition(newTab, 0);
    }

    /**
     * Add new tab under given name.
     * 
     * @param component
     * @param caption
     */
    protected void addTab(Component component, String caption) {
        final Tab newTab = tabSheet.addTab(component, caption);
        tabSheet.setTabPosition(newTab, 1);
    }

    @Override
    public void setContext(ConfigDialogContext newContext) {
        this.context = newContext;
    }

    protected ConfigDialogContext getContext() {
        return this.context;
    }

    @Override
    public void setConfig(String conf) throws DPUConfigException {
        try {
            // parse configuration
            final MasterConfigObject masterConfig
                    = serializationXml.convert(MasterConfigObject.class, conf);
            // wrap inside ConfigManager
            configManager = new ConfigManager(masterConfig, serializationXml);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Conversion failed.", ex);
        } catch (java.lang.ClassCastException e) {
            // try direct conversion
            // TODO update
            try {
                CONFIG dpuConfig;
                if (configHistory == null) {
                    // no history for configuration
                    dpuConfig = serializationXml.convert(configClass, conf);
                } else {
                    dpuConfig = serializationXml.convert(configHistory.getFinalClass(), conf);
                }
                final MasterConfigObject masterConfig = new MasterConfigObject();
                configManager = new ConfigManager(masterConfig, serializationXml);
                
                if (configHistory == null) {
                    configManager.set(dpuConfig, DPU_CONFIG_NAME);
                } else {
                    configManager.set(dpuConfig, DPU_CONFIG_NAME);
                }
            } catch (Exception ex) {
                throw new DPUConfigException("Conversion failed for prime class", ex);
            }
        }
        //
        // configure DPU
        //
        CONFIG dpuConfig;
        if (configHistory == null) {
            // no history for configuration
            dpuConfig = configManager.get(DPU_CONFIG_NAME, configClass);
        } else {
            dpuConfig = configManager.get(DPU_CONFIG_NAME, configHistory);
        }
        setConfiguration(dpuConfig);
        //
        // configure addons
        //
        for (AddonDialogBase dialogs : addons) {
            dialogs.loadConfig(configManager);
        }
    }

    @Override
    public String getConfig() throws DPUConfigException {
        final ConfigManager newConfigManager = new ConfigManager(
                new MasterConfigObject(), serializationXml);
        //
        // get configuration from DPU
        //
        CONFIG dpuConfig = getConfiguration();
        newConfigManager.set(dpuConfig, DpuAdvancedBase.DPU_CONFIG_NAME);
        //
        // get configuration from addons
        //
        for (AddonDialogBase dialogs : addons) {
            dialogs.storeConfig(newConfigManager);
        }
        //
        // convert all into a string
        //
        try {
            return serializationXml.convert(newConfigManager.getMasterConfig());
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
        return false;
    }

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
