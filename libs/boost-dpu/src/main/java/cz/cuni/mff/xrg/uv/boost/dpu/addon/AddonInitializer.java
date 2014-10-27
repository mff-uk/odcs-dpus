package cz.cuni.mff.xrg.uv.boost.dpu.addon;

import java.util.*;

/**
 * Provides functions for easy add-on list initialisation. Should be used in DPUs constructor.
 *
 * Sample usage:
 * <pre>
 * {@code
 * public MyDpu() {
        super(MyDpuConfig_V1.class, AddonInitializer.create(new CachedFileDownloader()));
 * }
 * }
 * </pre>
 * Same initialisation should be used in DPU as well as in DPU's configuration dialog. Add-ons without
 * configuration does not have to be listed in DPU's configuration, if they are listed then they are ignored.
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

    /**
     * Initialise list of add-on. Use in DPU's constructor.
     *
     * @param addons
     * @return
     */
    public static List<AddonInfo> create(Addon ... addons) {
        final List<AddonInfo> result = new LinkedList<>();
        for (int index = 0; index < addons.length; ++index) {
            result.add(new AddonInfo(addons[index]));
        }
        return result;
    }

}
