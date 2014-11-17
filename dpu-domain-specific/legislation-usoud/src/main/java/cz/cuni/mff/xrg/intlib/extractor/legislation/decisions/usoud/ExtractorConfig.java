package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud;



/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig {
    
    
   
    
     private String dateFrom = "18/09/2013";
    private String dateTo = "18/09/2013";
    private boolean last7Days = false;

     public static final String decisionPrefix = "http://linked.opendata.cz/resource/legislation/cz/decision/";
     
//     private String outputPredicate = OdcsTerms.DATA_UNIT_TEXT_VALUE_PREDICATE;

     
//    public String getOutputPredicate() {
//        return outputPredicate;
//    }

    public ExtractorConfig() {
//        this.outputPredicate = OdcsTerms.DATA_UNIT_TEXT_VALUE_PREDICATE;
    }
//
    public String getDecisionPrefix() {
        return decisionPrefix;
    }
//    
//   
    public String getDateFrom() {
        return dateFrom;
    }

    public String getDateTO() {
        return dateTo;
    }
   
    

}
