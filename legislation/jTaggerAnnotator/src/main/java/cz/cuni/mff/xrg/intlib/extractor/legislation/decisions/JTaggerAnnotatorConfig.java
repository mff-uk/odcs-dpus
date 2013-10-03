package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class JTaggerAnnotatorConfig extends DPUConfigObjectBase {
    
 
     
      private String inputPredicate = OdcsTerms.DATA_UNIT_TEXT_VALUE_PREDICATE;
     
     private String outputPredicate = "http://linked.opendata.cz/ontology/odcs/xmlValue"; //OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public String getInputPredicate() {
        return inputPredicate;
    }
     
    
  
    
    

}
