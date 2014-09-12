package cz.opendata.linked.cz.mzp.ippc;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {
    
    private boolean rewriteCache = false;
        
    private int timeout = 10000;

    private int interval = 500;
    
    private int maxattempts = 10;
    
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

    public int getMaxattempts() {
        return maxattempts;
    }

    public void setMaxattempts(int maxattempts) {
        this.maxattempts = maxattempts;
    }

}
