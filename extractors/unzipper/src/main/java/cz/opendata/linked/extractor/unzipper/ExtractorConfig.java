package cz.opendata.linked.extractor.unzipper;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 * Put your DPU's configuration here.
 * 
 * You can optionally implement {@link #isValid()} to provide possibility
 * to validate the configuration.
 * 
 * <b>This class must have default (parameter less) constructor!</b>
 */
public class ExtractorConfig extends DPUConfigObjectBase {

	private static final long serialVersionUID = 2045770110752042989L;

	private String zipFileURL = null;
	
	public ExtractorConfig() {
		
	}
	
	public ExtractorConfig(String zipFileURL) {	
		this.zipFileURL = zipFileURL;		
	}
    
	public String getZipFileURL() {
		return this.zipFileURL;
	}

	public void setZipFileURL(String zipFileURL) {
		this.zipFileURL = zipFileURL;
	}
	
}
