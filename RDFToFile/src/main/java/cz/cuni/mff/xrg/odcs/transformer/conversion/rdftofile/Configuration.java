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
		
}
