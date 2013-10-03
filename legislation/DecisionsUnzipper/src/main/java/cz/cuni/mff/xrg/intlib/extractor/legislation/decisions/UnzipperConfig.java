package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


import com.vaadin.ui.Calendar;
import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class UnzipperConfig extends DPUConfigObjectBase {
    
    
     private String dateFrom = "18/09/2013";
    private String dateTo = "18/09/2013";
    private boolean currentDay = true;

 
    
  
     private int maxExtractedDecisions = 3000;
     public static final String decisionPrefix = "http://linked.opendata.cz/resource/legislation/cz/decision/";
     
     private String outputPredicate;

    public boolean isCurrentDay() {
        return currentDay;
    }
     
    public String getOutputPredicate() {
        return outputPredicate;
    }

    public int getMaxExtractedDecisions() {
        return maxExtractedDecisions;
    }
     
    
  
    /**
     *
     * @param dateTo
     * @param dateFrom
     */
    public UnzipperConfig(String dateFrom, String dateTo, int maxNumberOfDecisions, boolean currentDay) {
        this.outputPredicate = OdcsTerms.DATA_UNIT_TEXT_VALUE_PREDICATE;
        this.dateTo = dateTo;
        this.dateFrom = dateFrom;
        this.maxExtractedDecisions = maxNumberOfDecisions;
        this.currentDay = currentDay;
    }
    
    /**
     *
     * @param dateTo
     * @param dateFrom
     */
    public UnzipperConfig(String dateFrom, String dateTo) {
        this.outputPredicate = OdcsTerms.DATA_UNIT_TEXT_VALUE_PREDICATE;
        this.dateTo = dateTo;
        this.dateFrom = dateFrom;
    }
    
    public UnzipperConfig() {
        
    }

    public String getDecisionPrefix() {
        return decisionPrefix;
    }
    
   
    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTO() {
        return dateTo;
    }
   
    

}
