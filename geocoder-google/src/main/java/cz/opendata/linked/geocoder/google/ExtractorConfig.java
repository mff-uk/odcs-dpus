package cz.opendata.linked.geocoder.google;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private int limit = 2400;
	
	private int interval = 1000;
	
    private int hoursToCheck = 24;

    private boolean generateMapUrl = false;
	
	@Override
    public boolean isValid() {
		return true;
    }

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getHoursToCheck() {
		return hoursToCheck;
	}

	public void setHoursToCheck(int hoursToCheck) {
		this.hoursToCheck = hoursToCheck;
	}

    public boolean isGenerateMapUrl() {
        return generateMapUrl;
    }

    public void setGenerateMapUrl(boolean generateMapUrl) {
        this.generateMapUrl = generateMapUrl;
    }

}
