package cz.cuni.mff.xrg.intlib.extractor.simplexslt;


import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig implements DPUConfigObject {
    
    private String xslTemplate;
     private  String xmlFile;

    public SimpleXSLTConfig(String xslTemplate, String xmlFile) {
        this.xslTemplate = xslTemplate;
        this.xmlFile = xmlFile;
    }

    public SimpleXSLTConfig() {

    }
    
    public String getXmlFile() {
        return xmlFile;
    }

    public String getXslTemplate() {
        return xslTemplate;
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    

}
