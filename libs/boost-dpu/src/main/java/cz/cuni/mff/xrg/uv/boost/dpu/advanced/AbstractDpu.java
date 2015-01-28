package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.*;
import cz.cuni.mff.xrg.uv.boost.dpu.config.*;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AbstractVaadinDialog;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationUtils;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFailure;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

/**
 * Base class for DPUs.
 *
 * @author Škoda Petr
 * @param <CONFIG> Type of DPU's configuration object.
 */
public abstract class AbstractDpu<CONFIG> implements DPU, DPUConfigurable,
        ConfigDialogProvider<MasterConfigObject> {

    /**
     * Holds all information from {@link AbstractDpu} to make manipulation with them easier.
     */
    public class ExecutionContext extends Context<CONFIG> {

        /**
         * Owner DPU instance.
         */
        private final AbstractDpu<CONFIG> dpu;

        /**
         * Execution context.
         */
        private final DPUContext dpuContext;

        /**
         * DPU's configuration.
         */
        private CONFIG config = null;

        /**
         * Cause given DPU initialization. Must not be called in constructor!
         *
         * @param dpuClass
         * @param dpuInstance
         * @param dpuContext
         * @throws DPUException
         */
        public ExecutionContext(AbstractDpu<CONFIG> dpuInstance, DPUContext dpuContext)
                throws DPUException {
            super((Class<AbstractDpu<CONFIG>>) dpuInstance.getClass(), dpuInstance);
            this.dpuContext = dpuContext;
            this.dpu = dpuInstance;
        }

        public CONFIG getConfig() {
            return config;
        }

        public void setConfig(CONFIG config) {
            this.config = config;
        }

        public DPUContext getDpuContext() {
            return dpuContext;
        }

        public AbstractDpu<CONFIG> getDpu() {
            return dpu;
        }

    }

    /**
     * Name used for DPU's configuration class.
     */
    public static final String DPU_CONFIG_NAME = "dpu_config";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDpu.class);

    /**
     * Holds all variables of this class.
     */
    private ExecutionContext ctx = null;

    /**
     * History of configuration.
     */
    private ConfigHistory<CONFIG> configHistory = null;

    protected CONFIG config;

    /**
     * Used to hold configuration between {@link #configure(java.lang.String)} and
     * {@link #execute(eu.unifiedviews.dpu.DPUContext)} where context is created.
     */
    private String configAsString;

    protected DPUContext context;

    /**
     * Class of user dialog.
     */
    protected Class<AbstractVaadinDialog<CONFIG>> dialogClass;

    /**
     *
     * @param configHistory
     */
    public <DIALOG extends AbstractVaadinDialog<CONFIG>> AbstractDpu(Class<DIALOG> dialogClass,
            ConfigHistory<CONFIG> configHistory) {
        this.dialogClass = (Class<AbstractVaadinDialog<CONFIG>>) dialogClass;
        this.configHistory = configHistory;
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        // Initialize master context -> create add-ons.
        this.ctx = new ExecutionContext(this, context);
        // Set master configuration and initialize ConfigTransformer.
        this.ctx.init(configAsString);
        // ConfigTransformer are ready from setConfiguration method -> get DPU configuration.
        try {
            this.ctx.config = (CONFIG) this.ctx.getConfigManager().get(
                    DPU_CONFIG_NAME, this.ctx.getConfigHistory());
        } catch (ConfigException ex) {
            throw new DPUException("Configuration preparation failed.", ex);
        }
        // Set variables for DPU.
        this.config = this.ctx.config;
        this.context = this.ctx.dpuContext;
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
        executeDpu = executeAddons(Addon.ExecutionPoint.PRE_EXECUTE);
        // Main execution for user code.
        try {
            if (executeDpu) {
                LOG.info("innerExecute:start");
                innerExecute();
                LOG.info("innerExecute:end");
            }
        } catch (DPUException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (DataUnitException ex) {
            context.sendMessage(MessageType.ERROR, "Problem with data unit.", "", ex);
        } catch (RuntimeException ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (Exception ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed", "DPU throws DPUException.", ex);
        } catch (Throwable ex) {
            LOG.error("DPU throws Throwable.", ex);
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
        executeAddons(Addon.ExecutionPoint.POST_EXECUTE);
    }

    @Override
    public void configure(String config) throws DPUConfigException {
        this.configAsString = config;
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            // Get default configuraiton.
            final MasterConfigObject defaultConfig = createDefaultMasterConfig();
            // Serialize into string and return.
            return this.ctx.getSerializationXml().convert(defaultConfig);
        } catch (SerializationFailure | SerializationXmlFailure ex) {
            throw new DPUConfigException("Config serialization failed.", ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        final AbstractConfigDialog<MasterConfigObject> dialog;
        try {
            dialog = dialogClass.newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Can't create dialog.", ex);
        }
        return dialog;

    }

    /**
     * Create default configuration for DPU.
     *
     * @return {@link MasterConfigObject} with default DPU configuration.
     * @throws SerializationXmlFailure
     */
    private MasterConfigObject createDefaultMasterConfig()
            throws SerializationXmlFailure, SerializationFailure {
        // Get string representation of DPU's config class.
        final CONFIG dpuConfig = (CONFIG) SerializationUtils.createInstance(
                this.ctx.getConfigHistory().getFinalClass());
        final String dpuConfigStr = this.ctx.getSerializationXml().convert(dpuConfig);
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
    private boolean executeAddons(Addon.ExecutionPoint execPoint) {
        boolean result = true;
        for (Addon addon : this.ctx.getAddons()) {
            if (addon instanceof Addon.Executable) {
                final Addon.Executable execAddon = (Addon.Executable) addon;
                try {
                    LOG.debug("Executing '{}' with on '{}' point", execAddon.getClass().getSimpleName(),
                            execPoint.toString());
                    execAddon.execute(execPoint);
                } catch (AddonException ex) {
                    ContextUtils.sendError(this.ctx.dpuContext, "Addon execution failed",
                            ex, "Addon: s", addon.getClass().getSimpleName());
                    result = false;
                }
            }
        }
        return result;
    }

    public ConfigHistory<CONFIG> getConfigHistory() {
        return this.configHistory;
    }

    /**
     * Is called before {@link #innerExecute()}. If this method throws DPU execution is immediately stopped.
     * No other function (not Add-on) is executed.
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
