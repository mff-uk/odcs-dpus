package cz.opendata.linked.psp_cz.metadata;

import cz.cuni.xrg.intlib.commons.configuration.Config;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig implements Config {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5577275030298541080L;

	public int Start_year = 1918;

	public int End_year = 2013;
	
	public String outputFileName = "sbirka.ttl";

}
