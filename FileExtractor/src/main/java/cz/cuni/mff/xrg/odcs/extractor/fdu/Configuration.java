package cz.cuni.mff.xrg.odcs.extractor.fdu;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {
	
	private String source;
	
	private String target = "/";
	
	private boolean asLink = true;
	
	private boolean includeSubDirs = true;

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public boolean isAsLink() {
		return asLink;
	}

	public void setAsLink(boolean asLink) {
		this.asLink = asLink;
	}

	public boolean isIncludeSubDirs() {
		return includeSubDirs;
	}

	public void setIncludeSubDirs(boolean includeSubDirs) {
		this.includeSubDirs = includeSubDirs;
	}
	
}
