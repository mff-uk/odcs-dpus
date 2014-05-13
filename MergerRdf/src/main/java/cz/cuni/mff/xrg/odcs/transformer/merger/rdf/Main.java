package cz.cuni.mff.xrg.odcs.transformer.merger.rdf;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.NonConfigurableBase;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.ManagableRdfDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsTransformer
public class Main extends NonConfigurableBase {

	@InputDataUnit(name = "input")
	public RDFDataUnit rdfInput;
	
	@OutputDataUnit(name = "output")
	public RDFDataUnit rdfOutput;
	
	public Main() {

	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		// merge rdf
		if (rdfInput != null && rdfOutput != null){
			((ManagableRdfDataUnit) rdfOutput).merge(rdfInput);
		}
	}
	
}
