package eu.unifiedviews.helpers.dpu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * Class which should be used by DPU developer as a base class from which his
 * DPU's configuration dialog is derived.
 *
 * @param <C>
 *            Particular configuration object of the DPU
 */
public abstract class BaseConfigDialog<C>
        extends AbstractConfigDialog<C> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseConfigDialog.class);

    /**
     * Used to convert configuration object into byte array and back.
     */
    private final ConfigWrap<C> configWrap;

    /**
     * Last valid configuration that is in dialog. Is used to detect changes in
     * configuration by function {@link #hasConfigChanged()}.
     */
    private String lastSetConfig;

    /**
     * DPUs context.
     */
    private ConfigDialogContext context;

    /**
     * Initialize {@link BaseConfigDialog} for given configuration class.
     *
     * @param configClass
     *            Configuration class.
     */
    public BaseConfigDialog(Class<C> configClass) {
        this.configWrap = new ConfigWrap<>(configClass);
        this.lastSetConfig = null;
    }

    @Override
    public void setContext(ConfigDialogContext newContext) {
        this.context = newContext;
    }

    @Override
    public void setConfig(String conf) throws DPUConfigException {
        C config;
        try {
            config = configWrap.deserialize(conf);
        } catch (DPUConfigException e) {
            LOG.error("Failed to deserialize configuration, using default instead.");
            // failed to deserialize configuraiton, use default
            config = configWrap.createInstance();
            setConfiguration(config);
            // rethrow
            throw e;
        }

        boolean originalConfigNull = config == null;

        if (originalConfigNull) {
            LOG.warn("The deserialized confirugarion is null, using default instead.");
            // null -> try to use default configuration
            config = configWrap.createInstance();
            if (config == null) {
                throw new DPUConfigException(
                        "Missing configuration and failed to create default."
                                + "No configuration has been loaded into dialog.");
            }
        }

        // in every case set the configuration
        setConfiguration(config);
        lastSetConfig = conf;
    }

    @Override
    public String getConfig() throws DPUConfigException {
        C configuration = getConfiguration();
        // check for validity before saving
        if (configuration == null) {
            throw new DPUConfigException("Configuration dialog return null.");
        }

        lastSetConfig = configWrap.serialize(getConfiguration());
        return lastSetConfig;
    }

    @Override
    public String getToolTip() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean hasConfigChanged() {
        String configString;
        try {
            C config = getConfiguration();
            configString = configWrap.serialize(config);
        } catch (DPUConfigException e) {
            // exception according to definition return false
            LOG.warn("Dialog configuration is invalid. It's assumed unchanged: ",
                    e.getLocalizedMessage());
            return false;
        } catch (Throwable e) {
            LOG.warn("Unexpected exception. Configuration is assumed to be unchanged.", e);
            return false;
        }

        if (lastSetConfig == null) {
            return configString == null;
        } else {
            return lastSetConfig.compareTo(configString) != 0;
        }
    }

    /**
     * @return Dialog's context.
     */
    protected ConfigDialogContext getContext() {
        return this.context;
    }

    /**
     * Set dialog interface according to passed configuration. If the passed
    configuration is invalid DPUConfigException can be thrown.
     *
     * @param conf
     *            Configuration object.
     * @throws DPUConfigException
     */
    protected abstract void setConfiguration(C conf) throws DPUConfigException;

    /**
     * Get configuration from dialog. In case of presence invalid configuration
    in dialog throw DPUConfigException.
     *
     * @return Configuration object.
     * @throws cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigException
     */
    protected abstract C getConfiguration() throws DPUConfigException;

}
