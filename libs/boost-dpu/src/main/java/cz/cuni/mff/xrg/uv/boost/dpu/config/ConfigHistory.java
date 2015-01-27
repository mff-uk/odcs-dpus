package cz.cuni.mff.xrg.uv.boost.dpu.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.config.serializer.ConfigSerializer;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG> Final configuration type we can convert to.
 */
public class ConfigHistory<CONFIG> {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigHistory.class);

    /**
     * End of history chain.
     */
    private final ConfigHistoryEntry<?, CONFIG> endOfHistory;

    private final Class<CONFIG> finalClass;

    /**
     *
     * @param endOfHistory
     * @param finalClass   If null then class from endOfHistory is used.
     */
    ConfigHistory(ConfigHistoryEntry<?, CONFIG> endOfHistory, Class<CONFIG> finalClass) {
        this.endOfHistory = endOfHistory;
        if (finalClass == null) {
            // We know this as the only option we can get here is by ConfigHistoryEntry.addCurrent
            // which adds the final version of config ie. CONFIG.
            this.finalClass = (Class<CONFIG>) endOfHistory.configClass;
        } else {
            this.finalClass = finalClass;
        }
    }

    CONFIG parse(String configAsString, List<ConfigSerializer> serializers) throws ConfigException {
        // Be positive, start with the last class = current class.
        CONFIG config = deserialize(configAsString, finalClass, serializers);
        if(config != null) {
            return config;
        }
        // Let's enter the past ..
        if (endOfHistory == null) {
            LOG.error("Can't parse config for ({}), there is no history, value is: {}", finalClass.getName(),
                    config);
            throw new ConfigException("Can't parse given object.");
        }
        Object object = null;
        ConfigHistoryEntry<?, ?> current = endOfHistory;
        // Search for mathicng class in history.
        do {
            object = deserialize(configAsString, current.configClass, serializers);
            if (object != null) {
                break;
            }
            // Go to the next history record.
            current = current.previous;
        } while (current != null);
        // Check that we have something.
        if (object == null) {
            throw new ConfigException("Can't parse given object, no history record has been found.");
        }
        // We have the configuration object and we will update it as we can - call toNextVersion.
        // The compile time check secure that the last conversion return CONFIG object.
        while (!object.getClass().equals(finalClass)) {
            if (object instanceof VersionedConfig) {
                object = ((VersionedConfig) object).toNextVersion();
            } else {
                // We can convert anymore.
                throw new ConfigException("Can't update given configuration to current.");
            }
        }
        return (CONFIG) object;
    }

    /**
     *
     * @return Final class in configuration class ie. the current configuration.
     */
    public Class<CONFIG> getFinalClass() {
        return finalClass;
    }

    /**
     * Call {@link #create(java.lang.Class, java.lang.String)} with alias equals to given class name.
     *
     * @param <T>
     * @param <S>
     * @param clazz
     * @return
     */
    public static <T, S extends VersionedConfig<T>> ConfigHistoryEntry<S, T> history(Class<S> clazz) {
        return new ConfigHistoryEntry<>(clazz, null);
    }

    /**
     * Create representation for configuration class without history.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> ConfigHistory<T> noHistory(Class<T> clazz) {
        return new ConfigHistory<>(null, clazz);
    }

    /**
     *
     * @param <TYPE>
     * @param configAsString
     * @param clazz
     * @param serializers
     * @return Null if object can't be deserialise.
     */
    private <TYPE> TYPE deserialize(String configAsString, Class<TYPE> clazz,
            List<ConfigSerializer> serializers) {
        for (ConfigSerializer serializer : serializers) {
            if (serializer.canDeserialize(configAsString, clazz)) {
                final TYPE object = serializer.deserialize(configAsString, clazz);
                if (object != null) {
                    return object;
                }
            }
        }
        return null;
    }
}
