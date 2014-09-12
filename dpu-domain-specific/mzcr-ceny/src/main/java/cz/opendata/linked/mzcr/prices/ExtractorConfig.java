package cz.opendata.linked.mzcr.prices;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {
    
    private static final long serialVersionUID = -5577275030298541080L;

    private int timeout = 10000;

    private int interval = 2000;
    
    private boolean rewriteCache = false;

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

    public boolean isRewriteCache() {
        return rewriteCache;
    }

    public void setRewriteCache(boolean rewriteCache) {
        this.rewriteCache = rewriteCache;
    }

}
