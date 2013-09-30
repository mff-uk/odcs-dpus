package cz.opendata.linked.ares;

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
	private static final long serialVersionUID = 8719241993054209502L;

    public int PerDay = 4900;
    
    public int hoursToCheck = 12;
    
    public int timeout = 10000;
    
    public int interval = 0;
    
    public boolean sendCache = false;
	
	@Override
    public boolean isValid() {
        return PerDay > 1 && hoursToCheck > 0 && hoursToCheck < 25;
    }

}
