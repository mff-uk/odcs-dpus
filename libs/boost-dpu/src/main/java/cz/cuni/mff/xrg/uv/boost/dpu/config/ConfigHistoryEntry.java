package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 * Used to chain history of configuration together.
 * 
 * @author Škoda Petr
 * @param <SOURCE> Our source configuration we need as input.
 * @param <DEST> Configuration we can update to.
 */
public class ConfigHistoryEntry<SOURCE, DEST> {

    /**
     * Class alias.
     */
    final String alias;

    /**
     * Instance of class for configuration class.
     */
    final Class<SOURCE> configClass;

    /**
     * Pointer to previous entry.
     */
    final ConfigHistoryEntry<?, SOURCE> previous;

    ConfigHistoryEntry(String alias, Class<SOURCE> configClass,
            ConfigHistoryEntry<?, SOURCE> previous) {
        this.alias = alias;
        this.configClass = configClass;
        this.previous = previous;
    }

    public <TARGET, DEST extends VersionedConfig<TARGET>> ConfigHistoryEntry<DEST, TARGET> add(Class<DEST> clazz) {
        return add(clazz, clazz.getCanonicalName().replaceAll("_", "__"));
    }

    public <TARGET, DEST extends VersionedConfig<TARGET>> ConfigHistoryEntry<DEST, TARGET> add(Class<DEST> clazz, String alias) {
        return new ConfigHistoryEntry<>(alias, clazz, (ConfigHistoryEntry<?, DEST>) this);
    }

    public ConfigHistory<DEST> addCurrent(Class<DEST> clazz) {
        return new ConfigHistory<>(this, clazz);
    }

}
