package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;
import java.util.List;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class AdvancedVaadinDialogBase<CONFIG>
    extends AbstractConfigDialog<MasterConfigObject> {

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

    private final TabSheet tabSheet;

    public AdvancedVaadinDialogBase(Class<CONFIG> configClass, List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory.serializationXmlGeneral();
        this.configClass = configClass;
        this.configManager = new ConfigManager(new MasterConfigObject(),
                serializationXml);
        //
        // gui
        //
        tabSheet = new TabSheet();        
        super.setCompositionRoot(tabSheet);
    }

    @Override
    protected void setCompositionRoot(Component compositionRoot) {
        tabSheet.addTab(compositionRoot, "DPU configuration");
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
            // parseconfiguration
            final MasterConfigObject masterConfig
                    = serializationXml.convert(MasterConfigObject.class, conf);
            // wrap inside ConfigManager
            configManager = new ConfigManager(masterConfig, serializationXml);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Conversion failed.", ex);
        }
        //
        // configure DPU
        //
        CONFIG dpuConfig = configManager.get(DpuAdvancedBase.DPU_CONFIG_NAME,
                    configClass);
        setConfiguration(dpuConfig);

        // TODO set other configurations

    }

    @Override
    public String getConfig() throws DPUConfigException {
        CONFIG dpuConfig = getConfiguration();

        ConfigManager configManager = new ConfigManager(new MasterConfigObject(),
                serializationXml);

        configManager.set(dpuConfig, DpuAdvancedBase.DPU_CONFIG_NAME);

        // TODO get other configurations

        try {
            return serializationXml.convert(configManager.getMasterConfig());
        } catch(SerializationXmlFailure ex) {
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
