package cz.cuni.mff.xrg.uv.transformer.filter.file;

import cz.cuni.mff.xrg.uv.boost.dpu.simple.ConfigurableBase;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

@DPU.AsTransformer
public class Main extends ConfigurableBase<Configuration> {

	@DataUnit.AsInput(name = "input", description = "Files to filter.")
	public FilesDataUnit input;

	@DataUnit.AsOutput(name = "output", description = "Filtered files.")
	public WritableFilesDataUnit output;

	public Main() {
		super(Configuration.class);
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	@Override
	public void execute() throws DataUnitException {
		final FilesDataUnit.Iteration iter = input.getIteration();
        while (iter.hasNext()) {
            FilesDataUnit.Entry entry = iter.next();
            if (entry.getSymbolicName().matches(config.getFilter())) {
                CopyHelpers.copyMetadata(entry.getSymbolicName(), input, output);
            }            
        }
	}
}
