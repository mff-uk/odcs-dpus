package cz.cuni.mff.xrg.uv.boost.dpu.context;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.Configurable;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXml;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFactory;
import eu.unifiedviews.dpu.DPUException;

/**
 * Base class for context.
 *
 * @author Škoda Petr
 * @param <CONFIG> Last configuration class.
 */
public class Context<CONFIG> implements AutoInitializer.FieldSetListener {

    /**
     * Respective DPU class.
     */
    private final Class<AbstractDpu<CONFIG>> dpuClass;

    /**
     * True if context is used for dialog.
     */
    private final boolean dialog;

    /**
     * List of used ad-dons.
     */
    private final List<Addon> addons = new LinkedList<>();

    /**
     * List of used configuration transformers.
     */
    private final List<ConfigTransformer> configTransformers = new LinkedList<>();

    /**
     * List of configurable ad-dons. May contains same classes as {@link #addons} and
     * {@link #configTransformers}.
     */
    private final List<Configurable> configurable = new LinkedList<>();

    /**
     * History of configuration class, if set used instead of {@link #configClass}.
     */
    private ConfigHistory<CONFIG> configHistory = null;

    /**
     * Configuration manager.
     */
    private ConfigManager configManager = null;

    /**
     * Serialisation service for root configuration.
     */
    private final SerializationXml serializationXml;

    private final AbstractDpu<CONFIG> dpuInstance;

    /**
     * Set base fields and create
     *
     * @param <T>
     * @param dpuClass
     * @param dpuInstance Can be null, in such case temporary instance is created.
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public Context(Class<AbstractDpu<CONFIG>> dpuClass, AbstractDpu<CONFIG> dpuInstance) {
        // Prepare DPU instance.
        try {
            if (dpuInstance == null) {
                dpuInstance = dpuClass.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Can't create DPU instance for purpose of scanning.");
        }
        // Set properties.
        this.dpuClass = dpuClass;
        this.dialog = true;
        this.configHistory = dpuInstance.getConfigHistory();
        this.serializationXml = SerializationXmlFactory.serializationXml();
        this.dpuInstance = dpuInstance;
    }

    /**
     * Initialize fields and set given configuration.
     *
     * @param configAsString If null no configuration is set. Used by dialog as there the configuration is set
     *                       later.
     * @throws DPUException
     */
    public void init(String configAsString) throws DPUException {
        // Init DPU use callback to get info about Addon, ConfigTransformer, ConfigurableAddon.
        final AutoInitializer initializer = new AutoInitializer(dpuInstance);
        initializer.addCallback(this);
        initializer.preInit();
        // Prepare configuration - it may need to load some classes initilized by AutoInitializer
        this.configManager = new ConfigManager(this.serializationXml, configTransformers);
        try {
            if (configAsString != null) {
                this.configManager.setMasterConfig(configAsString);
            }
        } catch (ConfigException ex) {
            throw new DPUException("Can't configure DPU.", ex);
        }
        // Finish addon initialization.
        initializer.afterInit(this);
    }

    /**
     * Callback to gather ad-dons.
     *
     * @param field
     * @param value
     */
    @Override
    public void onField(Field field, Object value) {
        if (Addon.class.isAssignableFrom(value.getClass())) {
            addons.add((Addon) value);
        }
        if (ConfigTransformer.class.isAssignableFrom(value.getClass())) {
            configTransformers.add((ConfigTransformer) value);
        }
        if (Configurable.class.isAssignableFrom(value.getClass())) {
            configurable.add((Configurable) value);
        }
    }

    /**
     * Can be used before complete context initialization.
     *
     * @return
     */
    public Class<AbstractDpu<CONFIG>> getDpuClass() {
        return dpuClass;
    }

    /**
     * Can be used before complete context initialization.
     *
     * @return
     */
    public boolean isDialog() {
        return dialog;
    }

    /**
     * Can be used before complete context initialization.
     *
     * @return
     */
    public SerializationXml getSerializationXml() {
        return serializationXml;
    }

    public List<Addon> getAddons() {
        return addons;
    }

    public List<ConfigTransformer> getConfigTransformers() {
        return configTransformers;
    }

    public List<Configurable> getConfigurableAddons() {
        return configurable;
    }

    public ConfigHistory<CONFIG> getConfigHistory() {
        return configHistory;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Return class with given type stored in context.
     *
     * @param <T>
     * @param clazz
     * @return Null if no {@link Addon} of given type exists.
     */
    public <T> T getInstance(Class<T> clazz) {
        for (Addon item : addons) {
            if (item.getClass() == clazz) {
                return (T) item;
            }
        }
        for (ConfigTransformer item : configTransformers) {
            if (item.getClass() == clazz) {
                return (T) item;
            }
        }

        for (Configurable item : configurable) {
            if (item.getClass() == clazz) {
                return (T) item;
            }
        }
        return null;
    }

}
