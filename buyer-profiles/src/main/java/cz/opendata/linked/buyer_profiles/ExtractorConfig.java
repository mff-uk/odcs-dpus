package cz.opendata.linked.buyer_profiles;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private static final long serialVersionUID = 3509477277481754571L;

	private boolean rewriteCache = false;
	
	private boolean accessProfiles = true;
	
	private int timeout = 10000;
	
	private int interval = 0;
	
	private boolean currentYearOnly = false;

	public boolean isRewriteCache() {
		return rewriteCache;
	}

	public void setRewriteCache(boolean rewriteCache) {
		this.rewriteCache = rewriteCache;
	}

	public boolean isAccessProfiles() {
		return accessProfiles;
	}

	public void setAccessProfiles(boolean accessProfiles) {
		this.accessProfiles = accessProfiles;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public boolean isCurrentYearOnly() {
		return currentYearOnly;
	}

	public void setCurrentYearOnly(boolean currentYearOnly) {
		this.currentYearOnly = currentYearOnly;
	}
	
}
