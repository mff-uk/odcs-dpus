package cz.cuni.mff.xrg.odcs.transformer.conversion.rdftofile;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;

/**
 * DPU's configuration class.
 * 
 * @author Škoda Petr
 */
public class Configuration extends DPUConfigObjectBase {

	private RDFFormatType RDFFileFormat;

	private String fileName = "data";

	private boolean genGraphFile = true;

	private String graphUri = "";

	public Configuration() {
		this.RDFFileFormat = RDFFormatType.TTL;
	}

	public RDFFormatType getRDFFileFormat() {
		return RDFFileFormat;
	}

	public void setRDFFileFormat(RDFFormatType RDFFileFormat) {
		this.RDFFileFormat = RDFFileFormat;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isGenGraphFile() {
		return genGraphFile;
	}

	public void setGenGraphFile(boolean genGraphFile) {
		this.genGraphFile = genGraphFile;
	}

	public String getGraphUri() {
		return graphUri;
	}

	public void setGraphUri(String graphUri) {
		this.graphUri = graphUri;
	}

}
