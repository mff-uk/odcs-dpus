package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import cz.cuni.xrg.intlib.commons.configuration.Config;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig implements Config {
    
    private final String xslTemplate;
     private final String xmlFile;

    public SimpleXSLTConfig(String xslTemplate, String xmlFile) {
        this.xslTemplate = xslTemplate;
        this.xmlFile = xmlFile;
    }

    public String getXmlFile() {
        return xmlFile;
    }

    public String getXslTemplate() {
        return xslTemplate;
    }
    

}
