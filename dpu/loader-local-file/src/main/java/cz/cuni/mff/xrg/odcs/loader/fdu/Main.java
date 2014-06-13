package cz.cuni.mff.xrg.odcs.loader.fdu;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsLoader;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@AsLoader
public class Main extends ConfigurableBase<Configuration> implements
		ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@InputDataUnit(name = "input")
	public FileDataUnit input;

	public Main() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		// get target
		final File target = new File(config.getDestination());
		// copy
		copyDirectory(input.getRootDir(), target);
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	/**
	 * Copy given directory into given directory.
	 * 
	 * @param handler
	 * @param dir
	 * @throws DPUException 
	 */
	private void copyDirectory(DirectoryHandler handler, File dir) throws DPUException {
		for (Handler item : handler) {
			if (item instanceof FileHandler) {
				copyFile((FileHandler)item, dir);
			} else if (item instanceof DirectoryHandler) {
				final DirectoryHandler subDir = (DirectoryHandler)item;
				// create directory
				final File target = new File(dir, subDir.getName());
				target.mkdirs();
				// copy data
				copyDirectory(subDir, target);
			}
		}
	}

	/**
	 * Copy file represented by given handler into given directory.
	 * 
	 * @param handler
	 * @param dir
	 * @throws DPUException 
	 */
	private void copyFile(FileHandler handler, File dir) throws DPUException {
		final File dest = new File(dir, handler.getName());
		try {
			FileUtils.copyFile(handler.asFile(), dest);
		} catch (IOException ex) {
			LOG.error("Failed to copy file", ex);
			throw new DPUException("Failed to copy file", ex);
		}
	}

}
