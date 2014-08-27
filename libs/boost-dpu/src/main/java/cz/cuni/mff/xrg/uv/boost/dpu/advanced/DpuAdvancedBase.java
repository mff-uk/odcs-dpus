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

    /**
     * Holds all information from {@link DpuAdvancedBase} to make manipulation
     * with them easier.
     */
    public class Context {

        /**
         * Execution context.
         */
        private DPUContext dpuContext;

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
        private CONFIG config;

        /**
         * History of configuration class, if set used instead of
         * {@link #configClass}.
         */
        private final ConfigHistory<CONFIG> configHistory;

        public Context(List<AddonInitializer.AddonInfo> addons,
                ConfigHistory<CONFIG> configHistory) {
            this.serializationXml = SerializationXmlFactory
                    .serializationXmlGeneral();
            this.serializationXml.addAlias(MasterConfigObject.class,
                    "MasterConfigObject");            
            this.addons = addons;
            this.configHistory = configHistory;
        }

        public DPUContext getDpuContext() {
            return dpuContext;
        }

        public ConfigManager getConfigManager() {
            return configManager;
        }

        public SerializationXmlGeneral getSerializationXml() {
            return serializationXml;
        }

        public List<AddonInitializer.AddonInfo> getAddons() {
            return addons;
        }

        public CONFIG getConfig() {
            return config;
        }

        public ConfigHistory<CONFIG> getConfigHistory() {
            return configHistory;
        }

    }

    public static final String DPU_CONFIG_NAME = "dpu_config";

    private static final Logger LOG = LoggerFactory.getLogger(
            DpuAdvancedBase.class);

    /**
     * Holds all variables of this class.
     */
    private final Context masterContext;

    /**
     * Used to make {@link DPUContext} accessible to DPUs.
     */
    protected DPUContext context;
    
    /**
     * Used to make configuration accessible to DPUs.
     */
    protected CONFIG config;

    public DpuAdvancedBase(Class<CONFIG> configClass,
            List<AddonInitializer.AddonInfo> addons) {
        this.masterContext = new Context(addons, 
                ConfigHistory.createNoHistory(configClass));
    }

    public DpuAdvancedBase(ConfigHistory<CONFIG> configHistory,
            List<AddonInitializer.AddonInfo> addons) {
        this.masterContext = new Context(addons, configHistory);
    }

    @Override
    public void execute(DPUContext context) {
        // set context
        this.masterContext.dpuContext = context;
        //
        // prepare configuration
        //
        try {
            this.masterContext.config = this.masterContext.configManager.get(
                    DPU_CONFIG_NAME, this.masterContext.configHistory);
        } catch (ConfigException ex) {
            context.sendMessage(MessageType.ERROR,
                    "Configuration prepareation failed.", "", ex);
            return;
        }
        //
        // set values for DPU
        //
        this.config = this.masterContext.config;
        this.context = this.masterContext.dpuContext;
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
        for (AddonInitializer.AddonInfo item : this.masterContext.addons) {
            try {
                if (item.getAddon().preAction(this.masterContext)) {
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
        } catch (DataUnitException ex) {
            context.sendMessage(MessageType.ERROR, "Problem with data unit.",
                    "", ex);
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
            LOG.info("innerCleanUp:start");
            innerCleanUp();
            LOG.info("innerCleanUp:stop");
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable in innerCleanUp method. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerCleanUp!", ex);
        }
        //
        // execute - Addon.postAction
        //
        for (AddonInitializer.AddonInfo item : this.masterContext.addons) {
            try {
                item.getAddon().postAction(this.masterContext);
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
                    = this.masterContext.serializationXml.convert(
                            MasterConfigObject.class, config);
            // wrap inside ConfigManager
            this.masterContext.configManager = new ConfigManager(masterConfig,
                    this.masterContext.serializationXml);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Conversion failed.", ex);
        } catch (java.lang.ClassCastException e) {
            // try direct conversion
            // TODO update : move into addon
            try {
                final CONFIG dpuConfig = masterContext.serializationXml.convert(
                        masterContext.configHistory.getFinalClass(), config);
                final MasterConfigObject masterConfig = new MasterConfigObject();
                masterContext.configManager = new ConfigManager(masterConfig, masterContext.serializationXml);

                if (masterContext.configHistory == null) {
                    masterContext.configManager.set(dpuConfig, DPU_CONFIG_NAME);
                } else {
                    masterContext.configManager.set(dpuConfig, DPU_CONFIG_NAME);
                }
            } catch (SerializationXmlFailure ex) {
                throw new DPUConfigException("Conversion failed for prime class", ex);
            }
        }
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            // get default
            MasterConfigObject defaultConfig = createDefaultMasterConfig();
            // convert to string
            return this.masterContext.serializationXml.convert(defaultConfig);
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
        final CONFIG dpuConfig = this.masterContext.serializationXml.createInstance(
                this.masterContext.configHistory.getFinalClass());
        final String dpuConfigStr = this.masterContext.serializationXml.convert(
                dpuConfig);
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
        for (AddonInitializer.AddonInfo info : this.masterContext.addons) {
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
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    protected abstract void innerExecute() throws DPUException, DataUnitException;

    /**
     * Is called after the {@link #innerExecute()} ends.
     * {@link Addon#postAction(eu.unifiedviews.dpu.DPUContext, cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager)}
     * methods are called after this method.
     */
    protected void innerCleanUp() {

    }

}
