package cz.cuni.mff.xrg.odcs.transformer.merger.file;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.NonConfigurableBase;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;

@AsTransformer
public class Main extends NonConfigurableBase {
	
	@InputDataUnit(name = "input")
	public FileDataUnit fileInput;
	
	@OutputDataUnit(name = "output")
	public FileDataUnit fileOutput;
	
	public Main() {	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		// merge file
		if (fileInput != null && fileOutput != null){
			fileOutput.getRootDir().addAll(fileInput.getRootDir());
		}
	}
	
}
