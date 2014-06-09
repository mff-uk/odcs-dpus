package cz.cuni.mff.xrg.odcs.transformer.zipper;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {
	
	private String fileName = "data";

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
		
}
