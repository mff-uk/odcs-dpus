package cz.cuni.mff.xrg.uv.transformer.filter.file;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsTransformer
public class Main extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@InputDataUnit(name = "input", description = "Files to filter.")
	public FileDataUnit input;

	@OutputDataUnit(name = "output", description = "Filtered files.")
	public FileDataUnit output;

	public Main() {
		super(Configuration.class);
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException {
		// filter files
		Iterator<Handler> iter = input.getRootDir().getFlatIterator();
		while(iter.hasNext()) {
			Handler handler = iter.next();
			if (handler instanceof FileHandler) {
				// it's a file, check name
				if (handler.getRootedPath().matches(config.getFilter())) {
					// ok copy to output
					copyToOutput((FileHandler)handler);
				}
			}			
		}
	}

	/**
	 * Copy given file to output.
	 * 
	 * @param fileHandler
	 * @throws DataUnitException 
	 */
	private void copyToOutput(FileHandler fileHandler) throws DataUnitException {
		final String rootedPath = fileHandler.getRootedPath();
		final int pos = rootedPath.lastIndexOf("/");
		final String dirName = rootedPath.substring(0, pos);
		final String fileName = rootedPath.substring(pos + 1);
		LOG.debug("Copy {} to {} as {}", rootedPath, dirName, fileName);
		
		DirectoryHandler targetDir = secureDirectory(output.getRootDir(), dirName);
		targetDir.add(fileHandler, new OptionsAdd(true));
	}

	/**
	 * Secure existence of given directory.
	 *
	 * @param rootDir
	 * @param rootPath
	 * @return
	 * @throws DataUnitException
	 */
	private DirectoryHandler secureDirectory(DirectoryHandler rootDir,
			String rootPath) throws DataUnitException {
		final String[] path = rootPath.split("/");

		for (String item : path) {
			if (item.isEmpty()) {
				LOG.debug("Skipping empty directory name in {}", rootPath);
				continue;
			}

			Handler handler = rootDir.getByName(item);
			if (handler instanceof DirectoryHandler) {
				rootDir = (DirectoryHandler) handler;
			} else {
				// create new
				rootDir = rootDir.addNewDirectory(item);
			}
		}
		return rootDir;
	}

}
