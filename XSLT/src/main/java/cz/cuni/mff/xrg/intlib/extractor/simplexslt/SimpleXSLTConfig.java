package cz.cuni.mff.xrg.intlib.extractor.simplexslt;


import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig implements DPUConfigObject {
    
    private String xslTemplate = "";
     private  String inputPredicate = "";
       private  String outputPredicate = "";
     
       public enum OutputType {
         RDFXML,
         TTL,
         Literal
                 
         
     }
     private OutputType outputType = OutputType.Literal;

    public SimpleXSLTConfig(String xslTemplate, String ip ,OutputType ot, String op ) {
        this.xslTemplate = xslTemplate;
        this.inputPredicate = ip;
        this.outputPredicate = op;
        this.outputType = ot;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public SimpleXSLTConfig() {

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

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
