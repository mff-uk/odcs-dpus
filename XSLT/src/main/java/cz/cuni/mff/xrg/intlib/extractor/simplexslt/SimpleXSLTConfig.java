package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import cz.cuni.xrg.intlib.commons.configuration.DPUConfigObject;
import cz.cuni.xrg.intlib.commons.module.config.DPUConfigObjectBase;
import cz.cuni.xrg.intlib.commons.ontology.OdcsTerms;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig extends DPUConfigObjectBase {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleXSLTConfig.class);
//     public static final String XML_VALUE_PREDICATE = "http://linked.opendata.cz/ontology/odcs/xmlValue";
    private String xslTemplate = "";
    private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;  //input is always XML
    private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
    private String xslTemplateFileName = "";
    private String escapedString = "<:&lt; >:&gt; \":&guote; \\*:&#42; \\\\:&#92;";  //preset mappings
//    private Map<String, String> escapedChars;
    
    //rdfa.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quote;").replaceAll("\\*", "&#42;").replaceAll("\\\\", "&#92;");

    public enum OutputType {

        RDFXML,
        TTL,
        Literal
    }
    private OutputType outputType = OutputType.Literal;
    private String outputXSLTMethod = "text"; //text/xml/..

    public SimpleXSLTConfig(String xslT, String xslTFileName, /*String ip, */OutputType ot, String op, String outputXSLTMeth, String escaped) {

        this(xslT, xslTFileName, ot, outputXSLTMeth, escaped);
        this.outputPredicate = op;

    }

    public SimpleXSLTConfig(String xslT, String xslTFileName, /*String ip, */OutputType ot, String outputXSLTMeth, String escaped) {
        this.xslTemplate = xslT;
        this.xslTemplateFileName = xslTFileName;
//        this.inputPredicate = ip;
        this.outputType = ot;
        this.outputXSLTMethod = outputXSLTMeth;
        this.escapedString = escaped;
//        escapedChars = new HashMap<>();

       



    }

//    public Map<String, String> getEscapedChars() {
//        return escapedChars;
//    }
//    

    public OutputType getOutputType() {
        return outputType;
    }

    public String getEscapedString() {
        return escapedString;
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
