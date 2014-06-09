package cz.cuni.mff.xrg.odcs.extractor.url;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsExtractor
public class Main extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	@OutputDataUnit(name = "output")
	public FileDataUnit output;	
	
	public Main() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		final String [] filePath = config.getTarget().split("/");
		for (int i = 0; i < filePath.length - 1; i++) {
			if (!filePath[i].isEmpty()) {
				dir = dir.addNewDirectory(filePath[i]);
			}
		} 
		final File outFile = dir.addNewFile(filePath[filePath.length - 1]).asFile();
		final URL url = config.getURL();
		
		if (url == null) {
			context.sendMessage(MessageType.ERROR, "Source URL not specified.");
		}
		
		try {
			FileUtils.copyURLToFile(url, outFile);
			// ok we have downloaded the file .. 
			return;
		} catch (IOException ex) {
			LOG.error("Download failed.", ex);
		}		
		
		// try again ?
		int retryCount = config.getRetryCount();
		while(retryCount != 0) {
			// sleep for a while
			try {
				Thread.sleep(config.getRetryDelay());
			} catch (InterruptedException ex) {				
			}			
			// try to download
			try {
				FileUtils.copyURLToFile(url, outFile);
				return;
			} catch (IOException ex) {
				LOG.error("Download failed.", ex);
			}
			// update retry counter
			if (retryCount > 0) {
				retryCount--;
			}
			// check for cancelation
			if (context.canceled()) {
				context.sendMessage(MessageType.INFO, "DPU has been canceled.");
				return;
			}
		}	
		context.sendMessage(MessageType.ERROR, "Failed to download file.");
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

}
