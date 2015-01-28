package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 * Used to chain history of configuration. Can be used in DPU's constructor, sample usage:
 * <pre>
 * {@code
 *  public MyDpu() {
        super(ConfigHistory.create(MyDpuConfig_V1.class).addCurrent(MyDpuConfig_V2.class),
                AddonInitializer.noAddons());
    }
 * }
 * </pre>
 * 
 * @author Škoda Petr
 * @param <SOURCE> Our source configuration we need as input.
 * @param <DEST> Configuration we can update to.
 * @see ConfigHistory
 */
public class ConfigHistoryEntry<SOURCE, DEST> {

    /**
     * Instance of class for configuration class.
     */
    final Class<SOURCE> configClass;

    /**
     * User given class alias.
     * Null if no alias was given.
     */
    final String alias;

    /**
     * Pointer to previous entry in configuration history.
     */
    final ConfigHistoryEntry<?, SOURCE> previous;

    ConfigHistoryEntry(Class<SOURCE> configClass, String alias, ConfigHistoryEntry<?, SOURCE> previous) {
        this.configClass = configClass;
        this.alias = alias;
        this.previous = previous;
    }

    /**
     *
     * @param <TARGET>
     * @param <DEST>
     * @param clazz Configuration class.
     * @return
     */
    public <TARGET, DEST extends VersionedConfig<TARGET>> ConfigHistoryEntry<DEST, TARGET> add(
            Class<DEST> clazz) {
        return new ConfigHistoryEntry<>(clazz, null, (ConfigHistoryEntry<?, DEST>) this);
    }

    /**
     * End the configuration history with current class.
     *
     * @param clazz Current configuration class.
     * @return
     */
    public ConfigHistory<DEST> addCurrent(Class<DEST> clazz) {
        return new ConfigHistory<>(this, clazz);
    }

}
