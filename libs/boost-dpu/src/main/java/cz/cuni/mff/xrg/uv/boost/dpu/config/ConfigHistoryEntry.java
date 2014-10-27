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

    ConfigHistoryEntry(String alias, Class<SOURCE> configClass, ConfigHistoryEntry<?, SOURCE> previous) {
        this.alias = alias;
        this.configClass = configClass;
        this.previous = previous;
    }

    /**
     *
     * @param <TARGET>
     * @param <DEST>
     * @param clazz Configuration class.
     * @return
     */
    public <TARGET, DEST extends VersionedConfig<TARGET>> ConfigHistoryEntry<DEST, TARGET> add(Class<DEST> clazz) {
        return add(clazz, clazz.getCanonicalName().replaceAll("_", "__"));
    }

    /**
     * This method is not implemented properly yet as {@link ConfigHistory} does not support aliases properly.
     *
     * @param <TARGET>
     * @param <DEST>
     * @param clazz Configuration class.
     * @param alias Alias used for this configuration class.
     * @return
     */
    public <TARGET, DEST extends VersionedConfig<TARGET>> ConfigHistoryEntry<DEST, TARGET> add(Class<DEST> clazz, String alias) {
        return new ConfigHistoryEntry<>(alias, clazz, (ConfigHistoryEntry<?, DEST>) this);
    }

    /**
     *
     * @param clazz Current configuration class.
     * @return
     */
    public ConfigHistory<DEST> addCurrent(Class<DEST> clazz) {
        return new ConfigHistory<>(this, clazz);
    }

}
