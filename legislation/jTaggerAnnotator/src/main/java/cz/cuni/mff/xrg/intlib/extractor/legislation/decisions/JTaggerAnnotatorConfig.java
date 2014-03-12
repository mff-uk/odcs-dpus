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

     //mode in which jTaggerIsWorking
	//nscr, uscr
	private String mode = "nscr";

	JTaggerAnnotatorConfig(String mode) {
		this.mode = mode;
	}

	public JTaggerAnnotatorConfig() {
	}

	public String getOutputPredicate() {
		return outputPredicate;
	}

	public String getInputPredicate() {
		return inputPredicate;
	}

	public String getMode() {
		return mode;
	}

	public void setInputPredicate(String inputPredicate) {
		this.inputPredicate = inputPredicate;
	}

	public void setOutputPredicate(String outputPredicate) {
		this.outputPredicate = outputPredicate;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
