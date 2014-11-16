package eu.unifiedviews.intlib.getdatafromrextractor;

/**
 * DPU's configuration class.
 */
public class IntlibGetDataFromRextractorConfig_V1 {

     private String dateFrom = "2014-11-13";
    private String dateTo = "2014-11-13";
    private boolean last7Days = false;
    
    public IntlibGetDataFromRextractorConfig_V1() {

    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    public boolean isLast7Days() {
        return last7Days;
    }

    public void setLast7Days(boolean last7Days) {
        this.last7Days = last7Days;
    }

    
    
}
