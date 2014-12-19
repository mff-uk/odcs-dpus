package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.*;
import cz.cuni.mff.xrg.uv.boost.dpu.config.*;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
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
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for DPUs.
 *
 * @author Škoda Petr
 * @param <CONFIG> Type of DPU's configuration object.
 */
public abstract class DpuAdvancedBase<CONFIG>
        implements DPU, DPUConfigurable, ConfigDialogProvider<MasterConfigObject> {

    /**
     * Holds all information from {@link DpuAdvancedBase} to make manipulation with them easier.
     */
    public class Context {

        /**
         * DPU it self, ie. this.
         */
        final DPU dpu;

        /**
         * Execution context.
         */
        DPUContext dpuContext;

        /**
         * Configuration manager.
         */
        final ConfigManager configManager;

        /**
         * Serialisation service for root configuration.
         */
        final SerializationXmlGeneral serializationXml;

        /**
         * List of used ad-dons.
         */
        final List<AddonInitializer.AddonInfo> addons;

        /**
         * DPU's configuration.
         */
        CONFIG config = null;

        /**
         * History of configuration class, if set used instead of {@link #configClass}.
         */
        final ConfigHistory<CONFIG> configHistory;

        public Context(DPU dpu, List<AddonInitializer.AddonInfo> addons, ConfigHistory<CONFIG> configHistory) {
            this.dpu = dpu;
            this.serializationXml = SerializationXmlFactory.serializationXmlGeneral();
            // This alias is also set in AdvamcedVaadinDialogBase, they muset be tha same!
            this.serializationXml.addAlias(MasterConfigObject.class, "MasterConfigObject");
            this.addons = addons;
            this.configHistory = configHistory;
            // Create config manager.
            List<ConfigTransformerAddon> configAddons = new ArrayList<>(2);
            for (AddonInitializer.AddonInfo item : addons) {
                if (item.getAddon() instanceof ConfigTransformerAddon) {
                    configAddons.add((ConfigTransformerAddon) item.getAddon());
                }
            }
            this.configManager = new ConfigManager(this.serializationXml, configAddons);
        }

        public DPU getDpu() {
            return dpu;
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

    /**
     * Name used for DPU's configuration class.
     */
    public static final String DPU_CONFIG_NAME = "dpu_config";

    private static final Logger LOG = LoggerFactory.getLogger(DpuAdvancedBase.class);

    /**
     * Holds all variables of this class.
     */
    final Context masterContext;

    /**
     * Used to make {@link DPUContext} accessible to DPUs.
     */
    protected DPUContext context;

    /**
     * Used to make configuration accessible to DPUs.
     */
    protected CONFIG config;

    /**
     * If true then add-ons will not be executed.
     */
    private boolean executeAddons = false;

    /**
     *
     * @param configClass
     * @param addons Must not be null!
     */
    public DpuAdvancedBase(Class<CONFIG> configClass, List<AddonInitializer.AddonInfo> addons) {
        this.masterContext = new Context(this, addons, ConfigHistory.createNoHistory(configClass));
    }

    /**
     *
     * @param configHistory
     * @param addons Must not be null!
     */
    public DpuAdvancedBase(ConfigHistory<CONFIG> configHistory, List<AddonInitializer.AddonInfo> addons) {
        this.masterContext = new Context(this, addons, configHistory);
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        // Set master context.
        this.masterContext.dpuContext = context;
        // Initialize add-ons.
        try {
            for (AddonInitializer.AddonInfo addon : this.masterContext.addons) {
                addon.getAddon().init(masterContext);
            }
        } catch (AddonException ex) {
            throw new DPUException("Addon initialization failed.", ex);
        }
        // Prepare configuration.
        try {
            this.masterContext.config = this.masterContext.configManager.get(
                    DPU_CONFIG_NAME, this.masterContext.configHistory);
        } catch (ConfigException ex) {
            throw new DPUException("Configuration preparation failed.", ex);
        }
        // Set values for DPU.
        this.config = this.masterContext.config;
        this.context = this.masterContext.dpuContext;
        // Execute DPU's code - innerInit.
        try {
            LOG.info("innerInit:start");
            innerInit();
            LOG.info("innerInit:end");
        } catch (DataUnitException ex) {
            throw new DPUException("Problem in innerInit().", ex);
        } catch (DPUException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new DPUException("innerInit() throws unexpected exception.", ex);
        }
        // {@link Addon}'s execution point.
        boolean executeDpu = true;
        if (!executeAddons) {
            executeDpu = executeAddons(ExecutableAddon.ExecutionPoint.PRE_EXECUTE);
        }
        // Main execution for user code.
        try {
            if (executeDpu) {
                LOG.info("innerExecute:start");
                innerExecute();
                LOG.info("innerExecute:end");
            }
            // TODO Use exception holder here to hold and throw the first exception ..
        } catch (DPUException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (DataUnitException ex) {
            context.sendMessage(MessageType.ERROR, "Problem with data unit.", "", ex);
        } catch (RuntimeException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (Exception ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable. See logs for more details.");
        }
        // Execute DPU's code - innerCleanUp.
        try {
            LOG.info("innerCleanUp:start");
            innerCleanUp();
            LOG.info("innerCleanUp:stop");
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable in innerCleanUp method. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerCleanUp!", ex);
        }
        // {@link Addon}'s execution point.
        if (!executeAddons) {
            executeAddons(ExecutableAddon.ExecutionPoint.POST_EXECUTE);
        }
    }

    @Override
    public void configure(String config) throws DPUConfigException {
        this.masterContext.configManager.setMasterConfig(config);
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            // Get default configuraiton.
            final MasterConfigObject defaultConfig = createDefaultMasterConfig();
            // Serialize into string and return.
            return this.masterContext.serializationXml.convert(defaultConfig);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Config serialization failed.", ex);
        }
    }

    /**
     * Create default configuration for DPU.
     *
     * @return {@link MasterConfigObject} with default DPU configuration.
     * @throws SerializationXmlFailure
     */
    private MasterConfigObject createDefaultMasterConfig() throws SerializationXmlFailure {
        // Get string representation of DPU's config class.
        final CONFIG dpuConfig = this.masterContext.serializationXml.createInstance(
                this.masterContext.configHistory.getFinalClass());
        final String dpuConfigStr = this.masterContext.serializationXml.convert(dpuConfig);
        // Prepare master config.
        final MasterConfigObject newConfigObject = new MasterConfigObject();
        newConfigObject.getConfigurations().put(DPU_CONFIG_NAME, dpuConfigStr);
        return newConfigObject;
    }

    /**
     * Execute all {@link ExecutableAddon}s of this DPU.
     *
     * @param execPoint
     * @return If exception is thrown then return false.
     */
    private boolean executeAddons(ExecutableAddon.ExecutionPoint execPoint) {
        boolean result = true;
        for (AddonInitializer.AddonInfo addon : this.masterContext.addons) {
            if (addon.getAddon() instanceof ExecutableAddon) {
                final ExecutableAddon execAddon = (ExecutableAddon) addon.getAddon();
                try {
                    LOG.debug("Executing '{}' with on '{}' point", execAddon.getClass().getSimpleName(),
                            execPoint.toString());
                    execAddon.execute(execPoint);
                } catch (AddonException ex) {
                    SendMessage.sendMessage(context, ex, addon.getClass().getSimpleName());
                    result = false;
                }
            } else {
                // Non executable addons are ignored.
            }
        }
        return result;
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
     * Call this method if DPU is running in test. If called add-ons will not be executed!
     */
    public void setTestMode() {
        this.executeAddons = true;
    }

    /**
     * Is called before {@link #innerExecute()}. If this method throws DPU execution is immediately 
     * stopped. No other function (not Add-on) is executed.
     *
     * DPU's configuration is already accessible.
     *
     * @throws eu.unifiedviews.dpu.DPUException
     * @throws DataUnitException
     */
    protected void innerInit() throws DPUException, DataUnitException {
        // Do nothing implementation.
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
        // Do nothing implementation.
    }

}
