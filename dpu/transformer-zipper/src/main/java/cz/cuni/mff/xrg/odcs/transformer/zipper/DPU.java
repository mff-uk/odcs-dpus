package cz.cuni.mff.xrg.odcs.transformer.zipper;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.LoggerFactory;

/**
 * Zip content of input data unit into the output data unit.
 * 
 * @author Škoda Petr
 */
@AsTransformer
public class DPU extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(
			DPU.class);
	
	@InputDataUnit(name = "input")
	public FileDataUnit input;
	
	@OutputDataUnit(name = "output")
	public FileDataUnit output;
	
	public DPU() {
		super(Configuration.class);
	}
	
	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		final String [] filePath = config.getFileName().split("/");
		for (int i = 0; i < filePath.length - 1; i++) {
			dir = dir.addNewDirectory(filePath[i]);
		} 
		final File outFile = dir.addNewFile(filePath[filePath.length - 1] + ".zip").asFile();
		
		// iterator over input content
		Iterator<Handler> iter = input.getRootDir().getFlatIterator();
		
		// used to publish the error mesage only for the first time
		boolean firstFailure = true;
		// buffer used to copy data
		byte[] buffer = new byte[4096];
		// zip
		try (FileOutputStream fos = new FileOutputStream(outFile); 
				ZipOutputStream zos = new ZipOutputStream(fos)) {
			
			while (iter.hasNext())  {
				final Handler handler = iter.next();
				if (handler instanceof FileHandler) {
					// ok we can continue
				} else {
					// else skip
					continue;
				}
				// convert into file handler
				FileHandler fileHandler = (FileHandler)handler;

				LOG.debug("Adding file: {}", fileHandler.getRootedPath());
				// we use rooted path to preserve the structure
				ZipEntry ze = new ZipEntry(fileHandler.getRootedPath());
				zos.putNextEntry(ze);
				
				try (FileInputStream in = new FileInputStream(fileHandler.asFile())) {
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} catch (Exception ex) {
					LOG.error("Failed to add file: {}", fileHandler.getRootedPath());
					if (firstFailure) {
						firstFailure = false;
						context.sendMessage(MessageType.ERROR, "Faild to zip all files");
					}
				}
			}
		} catch (FileNotFoundException ex) {
			context.sendMessage(MessageType.ERROR, "File not found", ex.getMessage());
		} catch(IOException ex) {
			context.sendMessage(MessageType.ERROR, "IOException", ex.getMessage());
		}		
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}
	
}
