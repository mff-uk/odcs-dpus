package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
    
    public static List<AddonInfo> create(Addon addon) {
        final List<AddonInfo> result = new LinkedList<>();
        // convert
        result.add(new AddonInfo(addon));
        return result;
    }

    public static List<AddonInfo> create(Addon addon1, Addon addon2) {
        final List<AddonInfo> result = new LinkedList<>();
        // convert
        result.add(new AddonInfo(addon1));
        result.add(new AddonInfo(addon2));
        return result;
    }


    public static List<AddonInfo> create(Addon[] addons) {
        final List<AddonInfo> result = new LinkedList<>();
        // convert
        for (Addon item : addons) {
            result.add(new AddonInfo(item));
        }
        return result;
    }

}
