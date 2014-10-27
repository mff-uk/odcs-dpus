package cz.cuni.mff.xrg.uv.boost.dpu.config;

import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG> Final configuration type we can convert to.
 */
public class ConfigHistory<CONFIG> {

    private static final Logger LOG = LoggerFactory.getLogger(
            ConfigHistory.class);

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

    CONFIG parse(String config, SerializationXmlGeneral serializer)
            throws SerializationXmlFailure, ConfigException {
        // Checkif it's not the last class (configuration).
        final String finalClassName = getClassName(finalClass);
        if (config.contains(finalClassName)) {
            // It's just convert and retunr.
            return serializer.convert(finalClass, config);
        }
        // Let's enter the past ..
        if (endOfHistory == null) {
            LOG
                    .error("Can't parse config for ({}), there is no history, value is: {}", finalClassName,
                            config);
            throw new ConfigException("Can't parse given object");
        }

        Object object = null;
        ConfigHistoryEntry<?, ?> current = endOfHistory;
        // Search for mathicng class in history.
        do {
            if (config.contains(current.alias)) {
                object = serializer.convert(current.configClass, config);
                break;
            }
            current = current.previous;
        } while (current != null);
        // Check that we have something.
        if (object == null) {
            throw new ConfigException("Can't parse given object");
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

    private static String getClassName(Class<?> clazz) {
        // Get class name and update it into way in which xStream use it
        // so we can search in string directly.
        String className = clazz.getCanonicalName().replace("_", "__");
        if (clazz.getEnclosingClass() != null) {
            // Change last "." into "_-" used for sub classes - addon configurations.
            int lastDot = className.lastIndexOf(".");
            className = className.substring(0, lastDot) + "_-" + className.substring(lastDot + 1);
        }
        return className;
    }

    /**
     * Call {@link #create(java.lang.Class, java.lang.String)} with alias equals to given class name.
     *
     * @param <T>
     * @param <S>
     * @param clazz
     * @return
     */
    public static <T, S extends VersionedConfig<T>> ConfigHistoryEntry<S, T> create(Class<S> clazz) {
        return create(clazz, getClassName(clazz));
    }

    /**
     * Create new history evidence for given class with given alias.
     *
     * @param <T>
     * @param <S>
     * @param clazz
     * @param alias
     * @return
     */
    public static <T, S extends VersionedConfig<T>> ConfigHistoryEntry<S, T> create(Class<S> clazz,
            String alias) {
        return new ConfigHistoryEntry<>(alias, clazz, null);
    }

    /**
     * Create representation for configuration class without history.
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> ConfigHistory<T> createNoHistory(Class<T> clazz) {
        return new ConfigHistory<>(null, clazz);
    }

}
