package cz.opendata.linked.ares;


/**
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {
    
    private int PerDay = 4900;
    
    private int hoursToCheck = 12;
    
    private int timeout = 10000;
    
    private int interval = 0;
    
    private boolean useCacheOnly = false;
    
    private boolean generateOutput = true;

    private boolean or_stdadr = true; 
    
    private boolean bas_puvadr = true;
    
    private boolean bas_active = false;
    
    private boolean downloadOR = true;
    
    private boolean downloadBasic = true;
    
    private boolean downloadRZP = true;
    
    public boolean isValid() {
        return PerDay > 1 && hoursToCheck > 0 && hoursToCheck < 25;
    }

    public int getPerDay() {
        return PerDay;
    }

    public void setPerDay(int PerDay) {
        this.PerDay = PerDay;
    }

    public int getHoursToCheck() {
        return hoursToCheck;
    }

    public void setHoursToCheck(int hoursToCheck) {
        this.hoursToCheck = hoursToCheck;
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

    public boolean isUseCacheOnly() {
        return useCacheOnly;
    }

    public void setUseCacheOnly(boolean useCacheOnly) {
        this.useCacheOnly = useCacheOnly;
    }

    public boolean isGenerateOutput() {
        return generateOutput;
    }

    public void setGenerateOutput(boolean generateOutput) {
        this.generateOutput = generateOutput;
    }

    public boolean isOr_stdadr() {
        return or_stdadr;
    }

    public void setOr_stdadr(boolean or_stdadr) {
        this.or_stdadr = or_stdadr;
    }

    public boolean isBas_puvadr() {
        return bas_puvadr;
    }

    public void setBas_puvadr(boolean bas_puvadr) {
        this.bas_puvadr = bas_puvadr;
    }

    public boolean isBas_active() {
        return bas_active;
    }

    public void setBas_active(boolean bas_active) {
        this.bas_active = bas_active;
    }

    public boolean isDownloadOR() {
        return downloadOR;
    }

    public void setDownloadOR(boolean downloadOR) {
        this.downloadOR = downloadOR;
    }

    public boolean isDownloadBasic() {
        return downloadBasic;
    }

    public void setDownloadBasic(boolean downloadBasic) {
        this.downloadBasic = downloadBasic;
    }

    public boolean isDownloadRZP() {
        return downloadRZP;
    }

    public void setDownloadRZP(boolean downloadRZP) {
        this.downloadRZP = downloadRZP;
    }    
    
}
