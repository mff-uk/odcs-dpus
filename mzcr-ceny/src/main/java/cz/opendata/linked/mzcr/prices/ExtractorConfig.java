package cz.opendata.linked.mzcr.prices;

import java.util.Calendar;

import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig implements DPUConfigObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5577275030298541080L;

	public int timeout = 10000;

	public int interval = 2000;
	
	public boolean rewriteCache = false;


	@Override
    public boolean isValid() {
        return true;
    }

}
