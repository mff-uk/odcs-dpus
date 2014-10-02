package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import java.util.*;

/**
 * Provides functions for easy ad-don list initialisation.
 *
 * @author Škoda Petr
 */
public class AddonInitializer {
 
    public static class AddonInfo {
        
        private final Addon addon;

        AddonInfo(Addon addon) {
            this.addon = addon;
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
    
    public static List<AddonInfo> create(Addon... addons) {
        final List<AddonInfo> result = new LinkedList<>();
        for (int index = 0; index < addons.length; ++index) {
            result.add(new AddonInfo(addons[index]));
        }
        return result;
    }

}
