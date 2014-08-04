package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer.AddonInfo;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXml;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public abstract class NonConfigurableBase implements DPU, DPUConfigurable {

    private static final Logger LOG = LoggerFactory.getLogger(
            NonConfigurableBase.class);

    /**
     * Execution context.
     */
    protected DPUContext context;

    /**
     * Configuration manager.
     */
    protected ConfigManager configManager = new ConfigManager();

    /**
     * Serialisation service for root configuration.
     */
    private final SerializationXml<MasterConfigObject> serializationService;

    private final List<AddonInfo> addons;
    
    public NonConfigurableBase(List<AddonInfo> addons) {
        this.serializationService = SerializationXmlFactory.serializationXml(
                MasterConfigObject.class, "masterConfig");
        this.addons = addons;
    }

    @Override
    public void execute(DPUContext context) {
        // set context
        this.context = context;
     
        // execute
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
        
        boolean executeDpu = true;
        for (AddonInfo item : addons) {
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
        
        try {
            // executed DPU
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

        try {
            LOG.info("innerExecute:start");
            innerCleanUp();
            LOG.info("innerExecute:start");
        } catch (Throwable ex) {
            context.sendMessage(MessageType.ERROR, "DPU Failed",
                    "DPU throws Throwable in innerCleanUp method. See logs for more details.");
            LOG.error("Throwable has ben thrown from innerCleanUp!", ex);
        }
        
        for (AddonInfo item : addons) {
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
                    = serializationService.convert(config);
            // wrap inside ConfigManager
            final SerializationXml serialize = null;
            configManager = new ConfigManager(masterConfig, serialize);
        } catch (SerializationXmlFailure ex) {
            // use default
            configManager = new ConfigManager();
            throw new DPUConfigException("Conversion failed.", ex);
        }
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        try {
            return serializationService.convert(new MasterConfigObject());
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Config serialization failed.", ex);
        }
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
