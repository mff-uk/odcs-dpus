package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


import cz.cuni.xrg.intlib.commons.module.config.DPUConfigObjectBase;
import cz.cuni.xrg.intlib.commons.ontology.OdcsTerms;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class UriGeneratorConfig extends DPUConfigObjectBase {
    
 
     
      private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
     
     private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public String getInputPredicate() {
        return inputPredicate;
    }
     
    
  
    
    

}
