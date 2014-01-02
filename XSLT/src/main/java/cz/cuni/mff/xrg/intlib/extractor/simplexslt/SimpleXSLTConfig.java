package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
import org.slf4j.LoggerFactory;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig extends DPUConfigObjectBase {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SimpleXSLTConfig.class);
    private String xslTemplate = "";
    private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;  //input is always XML
    private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
    private final String paramsPredicate = "http://linked.opendata.cz/ontology/odcs/xsltParam";
    private final String paramsPredicateName = "http://purl.org/dc/terms/title";
    private final String paramsPredicateValue = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value"; //rdf:value
    private String xslTemplateFileNameShownInDialog = "";
    private String escapedString = "\"\"\":&quote;&quote;&quote; "; // "<:&lt; >:&gt; \":&guote; \\*:&#42; \\\\:&#92;";  //preset mappings
    private int numberOfTriesToConnect = -1;
    
    //not used, but needed for backward compatibility
    private String storedXsltFilePath = "";

    //rdfa.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quote;").replaceAll("\\*", "&#42;").replaceAll("\\\\", "&#92;");
    public enum OutputType {

        RDFXML,
        TTL,
        Literal
    }
    private OutputType outputType = OutputType.Literal;
    private String outputXSLTMethod = "text"; //text/xml/..

    public SimpleXSLTConfig() {
    }

    public SimpleXSLTConfig(String xslT, String xslTFileName, OutputType ot, String op, String outputXSLTMeth, String escaped, int parsedNumberOfTries) {

        this(xslT, xslTFileName, ot, outputXSLTMeth, escaped, parsedNumberOfTries);
        this.outputPredicate = op;

    }

    public SimpleXSLTConfig(String xslT, String xslTFileName, OutputType ot, String outputXSLTMeth, String escaped, int parsedNumberOfTries) {
        this.xslTemplate = xslT;
        this.xslTemplateFileNameShownInDialog = xslTFileName;
        this.outputType = ot;
        this.outputXSLTMethod = outputXSLTMeth;
        this.escapedString = escaped;
        this.numberOfTriesToConnect = parsedNumberOfTries;





    }

    public int getNumberOfTriesToConnect() {
        return numberOfTriesToConnect;
    }

    public String getParamsPredicate() {
        return paramsPredicate;
    }

    public String getParamsPredicateName() {
        return paramsPredicateName;
    }

    public String getParamsPredicateValue() {
        return paramsPredicateValue;
    }

    public OutputType getOutputType() {
        return outputType;
    }

    public String getEscapedString() {
        return escapedString;
    }

    public String getOutputXSLTMethod() {
        return outputXSLTMethod;
    }

    public String getXslTemplateFileName() {
        return xslTemplateFileNameShownInDialog;
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
