package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class UriGeneratorConfig {

//	private String inputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;
//
//	private String outputPredicate = OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE;

	private String fileNameShownInDialog = "";

	private String storedXsltFilePath = "";

	public UriGeneratorConfig(String fileNameInDialog, String tempFile) {
		this.fileNameShownInDialog = fileNameInDialog;
		this.storedXsltFilePath = tempFile;
	}

	public UriGeneratorConfig() {

	}

//	public String getOutputPredicate() {
//		return outputPredicate;
//	}
//
//	public String getInputPredicate() {
//		return inputPredicate;
//	}

	public String getfileNameShownInDialog() {
		return fileNameShownInDialog;
	}

	public String getStoredXsltFilePath() {
		return storedXsltFilePath;
	}

	public String getFileNameShownInDialog() {
		return fileNameShownInDialog;
	}

	public void setFileNameShownInDialog(String fileNameShownInDialog) {
		this.fileNameShownInDialog = fileNameShownInDialog;
	}

//	public void setInputPredicate(String inputPredicate) {
//		this.inputPredicate = inputPredicate;
//	}
//
//	public void setOutputPredicate(String outputPredicate) {
//		this.outputPredicate = outputPredicate;
//	}

	public void setStoredXsltFilePath(String storedXsltFilePath) {
		this.storedXsltFilePath = storedXsltFilePath;
	}

}
