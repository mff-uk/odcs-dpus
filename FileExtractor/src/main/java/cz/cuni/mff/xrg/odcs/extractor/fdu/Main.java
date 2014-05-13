package cz.cuni.mff.xrg.odcs.extractor.fdu;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 *
 * @author Škoda Petr
 */
@AsExtractor
public class Main extends ConfigurableBase<Configuration> implements
		ConfigDialogProvider<Configuration> {

	@OutputDataUnit
	public FileDataUnit output;

	public Main() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		OptionsAdd options = new OptionsAdd(config.isAsLink());
		final File source = new File(config.getSource());

		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		final String[] filePath = config.getTarget().split("/", -1);
		for(String item : filePath) {
			if (item.isEmpty()) {
				continue;
			}			
			dir = dir.addNewDirectory(item);
		}

		// get files to add
		Collection<File> files = FileUtils.listFiles(source,
				TrueFileFilter.INSTANCE, FalseFileFilter.INSTANCE);

		if (config.isIncludeSubDirs()) {
			// add subdirectories
			files.addAll(Arrays.asList(
					source.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)));
		}

		// add located files
		for (File item : files) {
			if (item.isDirectory()) {
				dir.addExistingDirectory(item, options);
			} else {
				dir.addExistingFile(item, options);
			}
		}

	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

}
