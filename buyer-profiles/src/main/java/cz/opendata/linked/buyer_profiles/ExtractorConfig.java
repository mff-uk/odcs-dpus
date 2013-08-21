package cz.opendata.linked.buyer_profiles;

import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig implements DPUConfigObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3509477277481754571L;

	public boolean rewriteCache = false;
	
	public boolean accessProfiles = true;
	
	public int timeout = 10000;
	
	public int interval = 2000;
	
	@Override
    public boolean isValid() {
        return true;
    }

}
