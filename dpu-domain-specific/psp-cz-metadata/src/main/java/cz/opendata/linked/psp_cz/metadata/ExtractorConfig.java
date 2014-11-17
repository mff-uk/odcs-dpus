package cz.opendata.linked.psp_cz.metadata;

import java.util.Calendar;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig {
	
	private static final long serialVersionUID = -5577275030298541080L;

	private int Start_year = 1918;

	private int End_year = Calendar.getInstance().get(Calendar.YEAR);
        
	private String outputFileName = "sbirka.ttl";
	
	private boolean rewriteCache = false;
	
	private boolean cachedLists = false;
	
	private int timeout = 10000;

	private int interval = 2000;
	
//	@Override
//    public boolean isValid() {
//        return Start_year <= End_year && Start_year >= 1918 && End_year <= Calendar.getInstance().get(Calendar.YEAR);
//    }

	public int getStart_year() {
		return Start_year;
	}

	public void setStart_year(int Start_year) {
		this.Start_year = Start_year;
	}

	public int getEnd_year() {
		return End_year;
	}

	public void setEnd_year(int End_year) {
		this.End_year = End_year;
	}

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

	public boolean isCachedLists() {
		return cachedLists;
	}

	public void setCachedLists(boolean cachedLists) {
		this.cachedLists = cachedLists;
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
