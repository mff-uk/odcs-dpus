package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
/**
 *
 * Put your DPU's configuration here.
 *
 */
public class UriGeneratorConfig extends DPUConfigObjectBase {
    
 
     
    private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;

    private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;

    private String fileNameShownInDialog = "";
    private String storedXsltFilePath = "";

    public UriGeneratorConfig(String fileNameInDialog, String tempFile) {
        this.fileNameShownInDialog = fileNameInDialog;
        this.storedXsltFilePath = tempFile;
    }
    
    public UriGeneratorConfig() {
        
    }

    public String getOutputPredicate() {
        return outputPredicate;
    }

    public String getInputPredicate() {
        return inputPredicate;
    }

    public String getfileNameShownInDialog() {
        return fileNameShownInDialog;
    }

    public String getStoredXsltFilePath() {
        return storedXsltFilePath;
    }
     
    
    
  
    
    

}
