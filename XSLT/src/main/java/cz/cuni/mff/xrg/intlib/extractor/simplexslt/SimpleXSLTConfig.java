package cz.cuni.mff.xrg.intlib.extractor.simplexslt;


import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;
import cz.cuni.xrg.intlib.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig extends DPUConfigObjectBase {
    
     public static final String XML_VALUE_PREDICATE = "http://linked.opendata.cz/ontology/odcs/xmlValue";
 
    
    private String xslTemplate = "";
     private  String inputPredicate = XML_VALUE_PREDICATE;
       private  String outputPredicate = XML_VALUE_PREDICATE;
       private String xslTemplateFileName ="";
     
       public enum OutputType {
         RDFXML,
         TTL,
         Literal
                 
         
     }
     private OutputType outputType = OutputType.Literal;
     
     private String outputXSLTMethod = "text"; //text/xml/..

    public SimpleXSLTConfig(String xslT, String xslTFileName, String ip ,OutputType ot, String op, String outputXSLTMeth ) {
        this.xslTemplate = xslT;
        this.xslTemplateFileName = xslTFileName;
        this.inputPredicate = ip;
        this.outputPredicate = op;
        this.outputType = ot;
        this.outputXSLTMethod = outputXSLTMeth;
    }
    
     public SimpleXSLTConfig(String xslT, String xslTFileName, String ip ,OutputType ot, String outputXSLTMeth) {
        this.xslTemplate = xslT;
        this.xslTemplateFileName = xslTFileName;
        this.inputPredicate = ip;
        this.outputType = ot;
        this.outputXSLTMethod = outputXSLTMeth;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public SimpleXSLTConfig() {

    }

    public String getOutputXSLTMethod() {
        return outputXSLTMethod;
    }

    
    
    public String getXslTemplateFileName() {
        return xslTemplateFileName;
    }
    
    public String getInputPredicate() {
        return inputPredicate;
    }
     public String getOutputPredicate() {
        return outputPredicate;
    }

    public String getXslTemplate() {
        return xslTemplate;
    }

   
    

}
