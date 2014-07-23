package cz.cuni.mff.xrg.uv.boost.dpu.gui;

import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXml;
import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.serialization.xml.SerializationXmlFailure;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class SimpleConfigDialogBase<CONFIG> extends 
        AbstractConfigDialog<CONFIG> {

    private static final Logger LOG = LoggerFactory.getLogger(
            SimpleConfigDialogBase.class);

    private final SerializationXml<CONFIG> serializationService;

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
     * Initializes {@link BaseConfigDialog} for given configuration class.
     *
     * @param configClass Configuration class.
     */
    public SimpleConfigDialogBase(Class<CONFIG> configClass) {
        this.serializationService = SerializationXmlFactory.serializationXml(
                configClass, "dpuConfig");
        this.lastSetConfig = null;
    }

    @Override
    public void setContext(ConfigDialogContext newContext) {
        this.context = newContext;
    }

    @Override
    public void setConfig(String configStr) throws DPUConfigException {
        CONFIG config = null;

        try {
            config = serializationService.convert(configStr);
        } catch (SerializationXmlFailure ex) {
            LOG.warn("Failed to deserialize configuration.", ex);
        }

        if (config == null) {
            try {
                config = serializationService.createInstance();
                LOG.info("Default configuration used instead.");
            } catch (SerializationXmlFailure ex) {
                throw new DPUConfigException(
                        "Failed to create default configuration.", ex);
            }
        }

        setConfiguration(config);
        lastSetConfig = configStr;
    }

    @Override
    public String getConfig() throws DPUConfigException {
        CONFIG config = getConfiguration();
        // check for validity before saving
        if (config == null) {
            throw new DPUConfigException("Configuration dialog return null.");
        }

        try {
            lastSetConfig = serializationService.convert(config);
        } catch (SerializationXmlFailure ex) {
            throw new DPUConfigException("Serialization failed.", ex);
        }
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
        try {
            CONFIG config = getConfiguration();
            String configString = serializationService.convert(config);

            if (lastSetConfig == null) {
                // both null
                return configString == null;
            } else {
                return lastSetConfig.compareTo(configString) == 0;
            }
        } catch (DPUConfigException ex) {
            LOG.warn("Failed to get configuration from dialog.", ex);
            return false;
        } catch (SerializationXmlFailure ex) {
            LOG.warn("Serialization failure.", ex);
        } catch (Throwable t) {
            LOG.warn("Throwable has been thrown", t);
        }      
        LOG.info("Configuration is assumed to be unchanged.");
        return false;
    }

    /**
     * @return Dialog's context.
     */
    protected ConfigDialogContext getContext() {
        return this.context;
    }

    /**
     * Set dialog interface according to passed configuration. If the passed
     * configuration is invalid DPUConfigException can be thrown.
     *
     * @param conf Configuration object.
     * @throws DPUConfigException
     */
    protected abstract void setConfiguration(CONFIG conf) throws DPUConfigException;

    /**
     * Get configuration from dialog. In case of presence invalid configuration
     * in dialog throw DPUConfigException.
     *
     * @return Configuration object.
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    protected abstract CONFIG getConfiguration() throws DPUConfigException;
}
