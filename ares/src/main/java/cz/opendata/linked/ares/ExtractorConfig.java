package cz.opendata.linked.ares;

import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;
import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719241993054209502L;

    public int PerDay = 4900;
    
    public int hoursToCheck = 12;
    
    public int timeout = 10000;
    
    public int interval = 0;
    
    public boolean sendCache = false;
    
    public boolean or_stdadr = true; 
    
    public boolean bas_puvadr = true;
    
    public boolean bas_active = false;
	
	@Override
    public boolean isValid() {
        return PerDay > 1 && hoursToCheck > 0 && hoursToCheck < 25;
    }

}
