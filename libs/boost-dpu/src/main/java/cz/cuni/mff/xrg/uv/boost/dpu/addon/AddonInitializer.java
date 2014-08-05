package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import java.util.Collections;
import java.util.List;

/**
 * Provides functions for easy ad-don list initialisation.
 *
 * @author Škoda Petr
 */
public class AddonInitializer {
 
    public static class AddonInfo {
    
        String name;
        
        Integer index;
        
        Addon addon;

        public String getName() {
            return name;
        }

        public Integer getIndex() {
            return index;
        }

        public Addon getAddon() {
            return addon;
        }
        
    }
    
    private AddonInitializer() {
        
    }
    
    public static List<AddonInfo> noAddons() {
        return Collections.EMPTY_LIST;
    }
    
}
