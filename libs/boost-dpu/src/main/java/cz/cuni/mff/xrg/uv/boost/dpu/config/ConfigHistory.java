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

    ConfigHistory(ConfigHistoryEntry<?, CONFIG> endOfHistory, Class<CONFIG> finalClass) {
        this.endOfHistory = endOfHistory;
        this.finalClass = finalClass;
    }

    CONFIG parse(String config, SerializationXmlGeneral serializer) throws SerializationXmlFailure, ConfigException {
        //
        // checkif it's not the last class
        //
        if (config.contains(finalClass.getCanonicalName().replace("_", "__"))) {
            // ok, just convert
            return serializer.convert(finalClass, config);
        }
        //
        // go down to deep past
        //
        Object object = null;
        ConfigHistoryEntry<?,?> current = endOfHistory;
        do {
            if (config.contains(current.alias)) {
                // we got it
                object = serializer.convert(current.configClass, config);
                break;
            }
            // move to next
            current = current.previous;
        } while (current != null);
        
        if (object == null) {
            // we can not work with this object
            throw new ConfigException("Can't parse given object");
        }
        //
        // and back from darkness
        //
        // we have the configuration object, now we must rely on compile time
        // check that we can update to CONFIG
        //
        while (!object.getClass().equals(finalClass)) {
            if (object instanceof VersionedConfig) {
                object = ((VersionedConfig)object).toNextVersion();
            } else {
                // we can convert anymore
                throw new ConfigException("Can't update given configuration to current.");
            }
        }
        return (CONFIG)object;
    }

    /**
     * 
     * @return Final class in configuration class ie. the current configuration.
     */
    public Class<CONFIG> getFinalClass() {
        return finalClass;
    }

    public static <T, S extends VersionedConfig<T>> ConfigHistoryEntry<S, T> create(Class<S> clazz) {
        return create(clazz, clazz.getCanonicalName().replaceAll("_", "__"));
    }

    public static <T, S extends VersionedConfig<T>> ConfigHistoryEntry<S, T> create(Class<S> clazz, String alias) {
        return new ConfigHistoryEntry<>(alias, clazz, null);
    }

}
