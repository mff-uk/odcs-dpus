package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.*;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class DpuAdvancedBase<CONFIG>
        implements DPU, DPUConfigurable, ConfigDialogProvider<MasterConfigObject> {

    public static final String DPU_CONFIG_NAME = "dpu_config";

    private static final Logger LOG = LoggerFactory.getLogger(
            DpuAdvancedBase.class);

    /**
     * Execution context.
     */
    protected DPUContext context;

    /**
     * Configuration manager.
     */
    private ConfigManager configManager = null;

    /**
     * Serialisation service for root configuration.
     */
    private final SerializationXmlGeneral serializationXml;

    /**
     * List of used ad-dons.
     */
    private final List<AddonInitializer.AddonInfo> addons;

    /**
     * DPU's configuration.
     */
    protected CONFIG config;

    /**
     * DPU's configuration class.
     */
    private final Class<CONFIG> configClass;

    /**
     * History of configuration class, if set used instead of
     * {@link #configClass}.
     */
    private final ConfigHistory<CONFIG> configHistory;

    public DpuAdvancedBase(Class<CONFIG> configClass, List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory
                .serializationXmlGeneral();
        this.addons = addons;
        this.configClass = configClass;
        this.configHistory = null;
    }

    public DpuAdvancedBase(ConfigHistory<CONFIG> configHistory, List<AddonInitializer.AddonInfo> addons) {
        this.serializationXml = SerializationXmlFactory
                .serializationXmlGeneral();
        this.addons = addons;
        this.configClass = configHistory.getFinalClass();
        this.configHistory = configHistory;
    }

    @Override
    public void execute(DPUContext context) {
        // set context
        this.context = context;
        //
        // prepare configuration
        //
        try {
            if (configHistory == null) {
                // no history for configuration
                config = configManager.get(DPU_CONFIG_NAME, configClass);
            } else {
                config = configManager.get(DPU_CONFIG_NAME, configHistory);
            }
        } catch (ConfigException ex) {
            context.sendMessage(MessageType.ERROR,
                    "Configuration prepareation failed.", "", ex);
            return;
        }
        //
        // execute - innerInit
        //
        try {
            LOG.info("innerInit:start");
            innerInit();
            LOG.info("innerInit:end");
        } catch (DataUnitException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU failed to initilize.", ex);
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerInit!", ex);
        }
        //
        // execute - Addon.preAction
        //
        boolean executeDpu = true;
        for (AddonInitializer.AddonInfo item : addons) {
            try {
                if (item.getAddon().preAction(context, configManager)) {
                    // ok continue
                } else {
                    // failed
                    context.sendMessage(MessageType.ERROR, "Addon failed: "
                            + item.getClass().getSimpleName());
                    executeDpu = false;
                }
            } catch (RuntimeException ex) {
                context.sendMessage(MessageType.ERROR,
                        "Addon.preAction throws: "
                        + item.getClass().getSimpleName(), "", ex);
                executeDpu = false;
            }
        }
        //
        // execute - user code
        //
        try {
            if (executeDpu) {
                LOG.info("innerExecute:start");
                innerExecute();
                LOG.info("innerExecute:end");
            }
        } catch (DPUException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws DPUException.", ex);
        } catch (RuntimeException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws DPUException.", ex);
        } catch (Exception ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws DPUException.", ex);
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerExecute!", ex);
        }
        //
        // execute - innerCleanUp
        //
        try {
            LOG.info("innerExecute:start");
            innerCleanUp();
            LOG.info("innerExecute:start");
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable in innerCleanUp method. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerCleanUp!", ex);
        }
        //
        // execute - Addon.postAction
        //
        for (AddonInitializer.AddonInfo item : addons) {
            try {
                item.getAddon().postAction(context, configManager);
            } catch (RuntimeException ex) {
                context.sendMessage(MessageType.ERROR,
                        "Addon.postAction throws: "
                        + item.getClass().getSimpleName(), "", ex);
            }
        }
    }

    @Override
    public void configure(String config) throws DPUConfigException {
        try {
            // parseconfiguration
            final MasterConfigObject masterConfig
                    = serializationXml.convert(MasterConfigObject.class, config);
            // wrap inside ConfigManager
            configManager = new ConfigManager(masterConfig, serializationXml);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Conversion failed.", ex);
        }
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            // get default
            MasterConfigObject defaultConfig = createDefaultMasterConfig();
            // convert to string
            return serializationXml.convert(defaultConfig);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Config serialization failed.", ex);
        }
    }

    /**
     *
     * @return {@link MasterConfigObject} with default DPU configuration.
     * @throws SerializationXmlFailure
     */
    private MasterConfigObject createDefaultMasterConfig() throws SerializationXmlFailure {
        // get string representation
        final CONFIG dpuConfig = serializationXml.createInstance(configClass);
        final String dpuConfigStr = serializationXml.convert(dpuConfig);
        // prepare master config
        final MasterConfigObject newConfigObject = new MasterConfigObject();
        newConfigObject.getConfigurations().put(DPU_CONFIG_NAME, dpuConfigStr);
        return newConfigObject;
    }

    /**
     * Return first {@link Addon} with given class.
     *
     * @param <T>
     * @param clazz
     * @return Null if no {@link Addon} of given type exists.
     */
    protected <T extends Addon> T getAddon(Class<T> clazz) {
        for (AddonInitializer.AddonInfo info : addons) {
            if (info.getAddon().getClass() == clazz) {
                return (T) info.getAddon();
            }
        }
        return null;
    }

    /**
     * Is called before {@link #innerExecute()}.
     *
     * @throws DataUnitException
     */
    protected void innerInit() throws DataUnitException {

    }

    /**
     * Execute user DPU code.
     *
     * @throws DPUException
     */
    protected abstract void innerExecute() throws DPUException;

    /**
     * Is called after the {@link #innerExecute()} ends.
     * {@link Addon#postAction(eu.unifiedviews.dpu.DPUContext, cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager)}
     * methods are called after this method.
     */
    protected void innerCleanUp() {

    }

}
