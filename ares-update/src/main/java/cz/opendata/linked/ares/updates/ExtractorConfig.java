package cz.opendata.linked.ares.updates;

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

    public int timeout = 40000;
    
    public int interval = 0;
	
	@Override
    public boolean isValid() {
        return true;
    }

}
