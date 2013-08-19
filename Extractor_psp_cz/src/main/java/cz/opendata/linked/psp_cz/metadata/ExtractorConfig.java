package cz.opendata.linked.psp_cz.metadata;

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
	private static final long serialVersionUID = -5577275030298541080L;

	public int Start_year = 1918;

	public int End_year = 1918;
        
	public String outputFileName = "sbirka.ttl";

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
