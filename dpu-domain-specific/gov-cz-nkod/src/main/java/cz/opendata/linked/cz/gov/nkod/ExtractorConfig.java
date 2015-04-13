package cz.opendata.linked.cz.gov.nkod;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {

    private boolean rewriteCache = true;
    
    private int timeout = 40000;
    
    private int interval = 0;

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
