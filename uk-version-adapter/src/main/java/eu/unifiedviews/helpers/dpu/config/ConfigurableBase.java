package eu.unifiedviews.helpers.dpu.config;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.dpu.config.DPUConfigurable;

/**
 * Convenience base class for configurable DPUs. Every DPU may either extend
 * this class or directly implement {@link DPUConfigurable} interface.
 * 
 * @author Petyr
 * @author Tomas Knap
 * @param <C>
 */
public abstract class ConfigurableBase<C>
        implements DPUConfigurable, DPU {

    /**
     * Object configuration.
     */
    protected C config;

    /**
     * Container for configuration de/serialization.
     */
    private final ConfigWrap<C> configWrap;

    /**
     * Initialize the {@link ConfigurableBase} for given configuration class.
     * 
     * @param configClass
     *            Configuration class.
     */
    public ConfigurableBase(Class<C> configClass) {
        this.configWrap = new ConfigWrap<>(configClass);
        this.config = this.configWrap.createInstance();
    }

    @Override
    public void configure(String configString) throws DPUConfigException {
        if (configString != null) {
            // set configuration for configWrap
            C newConfig = configWrap.deserialize(configString);
            configureDirectly(newConfig);
        }
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        return configWrap.serialize(config);
    }

    /**
     * Validate given configuration and if it's valid then configure the DPU.
     * Can be used to set null configuration too.
     * 
     * @param newConfig
     *            New configuration.
     * @throws DPUConfigException
     *             In case of invalid configuration.
     */
    public void configureDirectly(C newConfig) throws DPUConfigException {
        if (newConfig != null) {
            config = newConfig;
        } else {
            throw new DPUConfigException("Invalid configuration.");
        }
    }

}
