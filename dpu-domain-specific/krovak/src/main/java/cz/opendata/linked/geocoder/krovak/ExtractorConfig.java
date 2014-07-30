package cz.opendata.linked.geocoder.krovak;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private int numofrecords = 100;
	
	private int interval = 1000;
	
	private int failInterval = 10000;
	
	private String sessionId = "c34mab45sjcdjf235dxrvarb";

	@Override
    public boolean isValid() {
		return true;
    }

	public int getNumofrecords() {
		return numofrecords;
	}

	public void setNumofrecords(int numofrecords) {
		this.numofrecords = numofrecords;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getFailInterval() {
		return failInterval;
	}

	public void setFailInterval(int failInterval) {
		this.failInterval = failInterval;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
