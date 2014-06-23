package cz.cuni.mff.xrg.uv.transformer.filter.file;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

public class Configuration extends DPUConfigObjectBase {
	
	private String filter = ".*";

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
}
