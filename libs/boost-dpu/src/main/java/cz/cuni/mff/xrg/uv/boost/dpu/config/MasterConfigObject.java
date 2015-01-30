package cz.cuni.mff.xrg.uv.boost.dpu.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store multiple string values (configurations) under string keys.
 *
 * @author Škoda Petr
 */
public class MasterConfigObject {

    /**
     * Name of master configuration. Used as a master configuration name for
     * {@link cz.cuni.mff.xrg.uv.boost.dpu.addon.ConfigTransformerAddon}.
     */
    public static final String CONFIG_NAME = "master_config_object";

    /**
     * Type name used during serialisation.
     */
    public static final String TYPE_NAME = "MasterConfigObject";

    /**
     * Storage for configurations.
     */
    private Map<String, String> configurations = new HashMap<>();
    
    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
    }

}
