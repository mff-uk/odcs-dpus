package cz.opendata.linked.cz.ruian;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {
    
//    private static final long serialVersionUID = 8719241993054209502L;

    private boolean rewriteCache = true;
    
    private int timeout = 40000;
    
    private int interval = 0;
    
    private boolean passToOutput = false;
    
    private boolean inclGeoData = false;

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

    public boolean isPassToOutput() {
        return passToOutput;
    }

    public void setPassToOutput(boolean passToOutput) {
        this.passToOutput = passToOutput;
    }

    public boolean isInclGeoData() {
        return inclGeoData;
    }

    public void setInclGeoData(boolean inclGeoData) {
        this.inclGeoData = inclGeoData;
    }    

}
