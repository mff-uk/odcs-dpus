package cz.cuni.mff.xrg.odcs.transformer.merger.rdf;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.NonConfigurableBase;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;

@AsTransformer
public class Main extends NonConfigurableBase {

	@InputDataUnit(name = "rdf")
	public RDFDataUnit rdfInput;
	
	@OutputDataUnit(name = "rdf")
	public WritableRDFDataUnit rdfOutput;
	
	@InputDataUnit(name = "file")
	public FileDataUnit fileInput;
	
	@OutputDataUnit(name = "file")
	public FileDataUnit fileOutput;	
	
	public Main() {

	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		// merge rdf
		if (rdfInput != null && rdfOutput != null){
			rdfOutput.addAll(rdfInput);
		}
		// merge file
		if (fileInput != null && fileOutput != null){
			fileOutput.getRootDir().addAll(fileInput.getRootDir());
		}
	}
	
}
