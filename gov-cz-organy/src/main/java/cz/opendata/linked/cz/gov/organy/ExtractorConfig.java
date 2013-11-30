package cz.opendata.linked.cz.gov.organy;

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
	private static final long serialVersionUID = 8719241993054209502L;

    public boolean rewriteCache = true;
	
	public int timeout = 40000;
    
    public int interval = 0;
	
	@Override
    public boolean isValid() {
        return true;
    }

}
