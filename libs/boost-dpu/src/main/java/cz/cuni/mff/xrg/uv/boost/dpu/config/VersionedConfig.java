package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 * Interface for versioned configuration.
 *
 * @author Škoda Petr
 * @param <CONFIG> Last version of configuration.
 */
public interface VersionedConfig<CONFIG> {

    /**
     *
     * @return Last version of configuration with current settings.
     */
    CONFIG toNextVersion();

}
