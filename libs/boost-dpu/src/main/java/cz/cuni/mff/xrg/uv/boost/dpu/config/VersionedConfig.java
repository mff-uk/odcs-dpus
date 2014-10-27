package cz.cuni.mff.xrg.uv.boost.dpu.config;

/**
 * Interface for versioned configuration.
 *
 * @author Škoda Petr
 * @param <CONFIG> Configuration to which this configuration can be converted (updated) to.
 */
public interface VersionedConfig<CONFIG> {

    /**
     *
     * @return Next version of configuration with "same" settings.
     */
    CONFIG toNextVersion();

}
