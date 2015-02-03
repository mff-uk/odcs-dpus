package cz.cuni.mff.xrg.uv.boost.dpu.context;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.openrdf.model.impl.ValueFactoryImpl;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.vaadin.Configurable;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXml;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFactory;
import eu.unifiedviews.dpu.DPUException;

/**
 * Base class for context.
 *
 * @author Škoda Petr
 * @param <CONFIG> Last configuration class.
 * @param <ONTOLOGY> Ontology class.
 */
public class Context<CONFIG, ONTOLOGY  extends OntologyDefinition> implements AutoInitializer.FieldSetListener {

    /**
     * Respective DPU class.
     */
    private final Class<AbstractDpu<CONFIG, ONTOLOGY>> dpuClass;

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

    /**
     * Class used to initialize given DPU.
     */
    private final AutoInitializer initializer;

    /**
     * Ontology definition for current DPU.
     */
    private final ONTOLOGY ontology;

    /**
     * Module for localization support.
     */
    private final Localization localization = new Localization();

    /**
     * Set base fields and create
     *
     * @param <T>
     * @param dpuClass
     * @param dpuInstance Can be null, in such case temporary instance is created.
     * @param ontology
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public Context(Class<AbstractDpu<CONFIG, ONTOLOGY>> dpuClass, AbstractDpu<CONFIG, ONTOLOGY> dpuInstance) {
        // Prepare DPU instance, just to get some classes.
        try {
            if (dpuInstance == null) {
                dpuInstance = dpuClass.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Can't create DPU instance for purpose of scanning.", ex);
        }
        // Set properties.
        this.dpuClass = dpuClass;
        this.dialog = true;
        this.configHistory = dpuInstance.getConfigHistoryHolder();
        this.serializationXml = SerializationXmlFactory.serializationXml();
        // Create ontology instance.
        try {
            this.ontology = dpuInstance.getOntologyHolder().newInstance();
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException("Can't create ontologz class.", ex);
        }
        // Prepare initializer.
        this.initializer = new AutoInitializer(dpuInstance);        
        // Prepare configuration manager - without addons, configuration transformers etc ..
        this.configManager = new ConfigManager(this.serializationXml);
    }

    /**
     * Initialize fields and set given configuration.
     *
     * @param configAsString If null no configuration is set. Used by dialog as there the configuration is set
     *                       later.
     * @throws DPUException
     */
    protected final void init(String configAsString, Locale locale, ClassLoader classLoader)
            throws DPUException {
        // Initialize localization.
        this.localization.setLocale(locale, classLoader);
        // Start with ontology initialization.
        this.ontology.init(ValueFactoryImpl.getInstance());
        // Init DPU use callback to get info about Addon, ConfigTransformer, ConfigurableAddon.
        initializer.addCallback(this);
        initializer.preInit();
        // Add initialized config transformers.
        this.configManager.addTransformers(configTransformers);
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
        if (value instanceof Addon) {
            addons.add((Addon) value);
        }
        if (value instanceof ConfigTransformer) {
            configTransformers.add((ConfigTransformer) value);
        }
        if (value instanceof Configurable) {
            configurable.add((Configurable) value);
        }
    }

    /**
     * Can be used before complete context initialization.
     *
     * @return
     */
    public Class<AbstractDpu<CONFIG, ONTOLOGY>> getDpuClass() {
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

    public ONTOLOGY getOntology() {
        return ontology;
    }

    /**
     * Return class with given type stored in context.
     *
     * @param <T>
     * @param clazz
     * @return Null if no {@link Addon} of given type exists.
     */
    @SuppressWarnings("unchecked")
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

    public Localization getLocalization() {
        return localization;
    }

    /**
     *
     * @return Return new instance of {@link UserContext} that wrap this context.
     */
    public UserContext asUserContext() {
        return new UserContext(this);
    }

}
