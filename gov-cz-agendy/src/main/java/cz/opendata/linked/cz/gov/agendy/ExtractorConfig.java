package cz.opendata.linked.cz.gov.agendy;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private static final long serialVersionUID = -5577275030298541080L;

	private String outputFileName = "agendy.ttl";
	
	private boolean rewriteCache = false;
		
	private int timeout = 10000;

	private int interval = 0;

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public boolean isRewriteCache() {
		return rewriteCache;
	}

	public void setRewriteCache(boolean rewriteCache) {
		this.rewriteCache = rewriteCache;
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

}
