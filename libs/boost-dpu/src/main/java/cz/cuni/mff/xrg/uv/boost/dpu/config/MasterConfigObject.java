package cz.cuni.mff.xrg.uv.boost.dpu.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to store multiple string values and string keys.
 *
 * @author Škoda Petr
 */
public class MasterConfigObject {

    private Map<String, String> configurations = new HashMap<>();
    
    public Map<String, String> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<String, String> configurations) {
        this.configurations = configurations;
    }

}
