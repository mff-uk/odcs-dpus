package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor;

import cz.cuni.xrg.intlib.commons.configuration.Config;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class JTaggerExtractorConfig implements Config {
    
    
    private final String dateTo;
     private final String dateFrom;
    
     public static final int maxExtractedDecisions = 1000;
     
  
    /**
     *
     * @param dateTo
     * @param dateFrom
     */
    public JTaggerExtractorConfig(String dateTo, String dateFrom) {
        this.dateTo = dateTo;
        this.dateFrom = dateFrom;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTO() {
        return dateTo;
    }
    

}
