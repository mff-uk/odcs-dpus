package cz.opendata.linked.geocoder.google;

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

	public int limit = 2400;
	
	public int interval = 1000;
	
    public int hoursToCheck = 24;
	
	@Override
    public boolean isValid() {
		return true;
    }

}
