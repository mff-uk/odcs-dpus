package cz.opendata.linked.geocoder.nominatim;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private static final long serialVersionUID = 8719241993054209502L;

	private int limit = 2400;
	
	private int interval = 1000;
	
    private int hoursToCheck = 24;
    
    private boolean structured = false;
	
    private boolean stripNumFromLocality = true;
    
    private String country = "";
    
    private boolean useStreet = true;
    
    private boolean useRegion = false;
    
    private boolean useLocality = true;
    
    private boolean usePostalCode = false;

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

	public boolean isStructured() {
		return structured;
	}

	public void setStructured(boolean structured) {
		this.structured = structured;
	}

	public boolean isStripNumFromLocality() {
		return stripNumFromLocality;
	}

	public void setStripNumFromLocality(boolean stripNumFromLocality) {
		this.stripNumFromLocality = stripNumFromLocality;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public boolean isUseStreet() {
		return useStreet;
	}

	public void setUseStreet(boolean useStreet) {
		this.useStreet = useStreet;
	}

	public boolean isUseRegion() {
		return useRegion;
	}

	public void setUseRegion(boolean useRegion) {
		this.useRegion = useRegion;
	}

	public boolean isUseLocality() {
		return useLocality;
	}

	public void setUseLocality(boolean useLocality) {
		this.useLocality = useLocality;
	}

	public boolean isUsePostalCode() {
		return usePostalCode;
	}

	public void setUsePostalCode(boolean usePostalCode) {
		this.usePostalCode = usePostalCode;
	}
    
}
