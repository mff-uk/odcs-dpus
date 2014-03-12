package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class SimpleXSLTConfig extends DPUConfigObjectBase {

    private String xslTemplate = "";
    
    //INPUT PREDICATE IN RDF DATA UNIT HOLDING the files to be processed
    private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;  //input is always XML
    private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
    private final String paramsPredicate = "http://linked.opendata.cz/ontology/odcs/xsltParam";
    private final String paramsPredicateName = "http://purl.org/dc/terms/title";
    private final String paramsPredicateValue = "http://www.w3.org/1999/02/22-rdf-syntax-ns#value"; //rdf:value
    private String xslTemplateFileNameShownInDialog = "";
    private String escapedString = "\"\"\":&quote;&quote;&quote; "; // "<:&lt; >:&gt; \":&guote; \\*:&#42; \\\\:&#92;";  //preset mappings
    private int numberOfTriesToConnect = -1;

	public String getXslTemplateFileNameShownInDialog() {
		return xslTemplateFileNameShownInDialog;
	}

	public void setXslTemplateFileNameShownInDialog(
			String xslTemplateFileNameShownInDialog) {
		this.xslTemplateFileNameShownInDialog = xslTemplateFileNameShownInDialog;
	}

	public void setXslTemplate(String xslTemplate) {
		this.xslTemplate = xslTemplate;
	}

	public void setInputPredicate(String inputPredicate) {
		this.inputPredicate = inputPredicate;
	}

	public void setOutputPredicate(String outputPredicate) {
		this.outputPredicate = outputPredicate;
	}

	public void setEscapedString(String escapedString) {
		this.escapedString = escapedString;
	}

	public void setNumberOfTriesToConnect(int numberOfTriesToConnect) {
		this.numberOfTriesToConnect = numberOfTriesToConnect;
	}

	public void setOutputType(
			OutputType outputType) {
		this.outputType = outputType;
	}

	public void setOutputXSLTMethod(String outputXSLTMethod) {
		this.outputXSLTMethod = outputXSLTMethod;
	}
    
//    //not used, but needed for backward compatibility
//    private String storedXsltFilePath = "";

    //rdfa.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quote;").replaceAll("\\*", "&#42;").replaceAll("\\\\", "&#92;");
    public enum OutputType {

        RDFXML,
        TTL,
        Literal
    }
    private OutputType outputType = OutputType.Literal;
    private String outputXSLTMethod = "text"; //text/xml/..
    
    //INPUT PREDICATE IN RDF DATA UNIT HOLDING URI which should be used as a subject for the given file 
    //when OutputType.Literal is used
    public static final String DATA_UNIT_RESULTING_SUBJECT_PREDICATE = "http://linked.opendata.cz/ontology/odcs/resultingSubject";
    //predicate holding path to the file data unit. 
    //Object is used to map subject with the particular file in file data unit. 
    //Subject is an arbitrary URI, which may be used to define other properties of the file in the file data unit. 
    public static final String FILE_DATAUNIT_PATH = "http://linked.opendata.cz/ontology/odcs/dataunit/file/filePath";

    public String getResultingSubjectPredicate() {
        return DATA_UNIT_RESULTING_SUBJECT_PREDICATE;
    }
    
    public String getFilePathPredicate() {
        return FILE_DATAUNIT_PATH;
    }

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

	public static String getDATA_UNIT_RESULTING_SUBJECT_PREDICATE() {
		return DATA_UNIT_RESULTING_SUBJECT_PREDICATE;
	}

	public static String getFILE_DATAUNIT_PATH() {
		return FILE_DATAUNIT_PATH;
	}
		
}
