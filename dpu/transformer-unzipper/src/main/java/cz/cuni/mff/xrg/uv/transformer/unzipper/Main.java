package cz.cuni.mff.xrg.uv.transformer.unzipper;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.NonConfigurableBase;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;
import java.io.File;
import java.util.Iterator;
import net.lingala.zip4j.core.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsTransformer
public class Main extends NonConfigurableBase {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@InputDataUnit(name = "input")
	public FileDataUnit input;

	@OutputDataUnit(name = "output")
	public FileDataUnit output;

	public Main() {
	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {

		Iterator<Handler> iter = input.getRootDir().getFlatIterator();
		while (iter.hasNext()) {
			final Handler handler = iter.next();
			LOG.debug("Handler located {}", handler.getRootedPath());
			if (handler instanceof FileHandler && isZipFile(handler)) {
				// ok, we unpack
				final File file = handler.asFile();
				final String outputName = file.getName();

				if (output.getRootDir().getByName(outputName) != null) {
					// name=directory is already used
					context.sendMessage(MessageType.ERROR, "Unzipper failed",
							String.format("Duplicit zip name '%s'", outputName));
					return;
				}

				final File outputDir = new File(output.getRootDir().asFile(),
						outputName);
				outputDir.mkdirs();

				LOG.debug("Unzipping {} into {}", handler.getRootedPath(),
						outputDir.toString());

				try {
					unzip((FileHandler) handler, outputDir);
				} catch (Exception ex) {
					context.sendMessage(MessageType.ERROR, "Unzipper failed",
							String.format("Failed to unzip '%s'", handler
									.getRootedPath()), ex);
					return;
				}

				LOG.debug("Adding directory ...");

				// add into FileData unit as existing, that force scan for 
				// subdirectories
				output.getRootDir().addExistingDirectory(outputDir,
						new OptionsAdd(true, true));

				LOG.debug("Adding directory ... done");

			}
		}

		if (output.getRootDir().isEmpty()) {
			context.sendMessage(MessageType.WARNING, "No output",
					"The output is empty, run DPU in debug mode and examine logs for more info.");
		}

	}

	/**
	 *
	 * @param handler
	 * @return True if given handler name has 'zip' extension.
	 */
	private boolean isZipFile(Handler handler) {
		final String extension = handler.getName().substring(
				handler.getName().length() - 3);
		LOG.debug("Test for extension for '{}' extension '{}'", 
				handler.getRootedPath(), extension);
		return extension.compareToIgnoreCase("zip") == 0;
	}

	/**
	 * Unzip given source zip file into target directory.
	 *
	 * @param source
	 * @param target
	 */
	private void unzip(FileHandler source, File target) throws Exception {
		ZipFile zipFile = new ZipFile(source.asFile());
		if (zipFile.isEncrypted()) {
			throw new Exception("Zip file is encrypted.");
		}
		zipFile.extractAll(target.toString());
	}

}
