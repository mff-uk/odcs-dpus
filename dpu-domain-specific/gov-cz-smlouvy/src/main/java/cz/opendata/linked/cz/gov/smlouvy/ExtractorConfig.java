package cz.opendata.linked.cz.gov.smlouvy;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {

    private boolean rewriteCache = true;
    
    private int timeout = 40000;
    
    private int interval = 0;

    private boolean smlouvy = true;
    
    private boolean objednavky = true;

    private boolean plneni = true;

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

    public boolean isSmlouvy() {
        return smlouvy;
    }

    public void setSmlouvy(boolean smlouvy) {
        this.smlouvy = smlouvy;
    }

    public boolean isObjednavky() {
        return objednavky;
    }

    public void setObjednavky(boolean objednavky) {
        this.objednavky = objednavky;
    }

    public boolean isPlneni() {
        return plneni;
    }

    public void setPlneni(boolean plneni) {
        this.plneni = plneni;
    }
    
}
