package cz.opendata.linked.ehealth.ndfrt;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class ExtractorConfig extends DPUConfigObjectBase {

	private static final long serialVersionUID = 2045770110752042989L;

	public final String NDFRTPrefix = "http://linked.opendata.cz/resource/ndfrt/";
	
	public ExtractorConfig() {
	
	}
    
	public String getNDFRTPrefix() {
		return this.NDFRTPrefix;
	}

}
