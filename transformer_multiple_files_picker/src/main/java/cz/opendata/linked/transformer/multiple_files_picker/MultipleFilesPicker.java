package cz.opendata.linked.transformer.multiple_files_picker;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.*;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;

@AsTransformer
public class MultipleFilesPicker extends ConfigurableBase<MultipleFilesPickerConfig> implements
		ConfigDialogProvider<MultipleFilesPickerConfig> {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            MultipleFilesPicker.class);
	
	@InputDataUnit(name = "inputFiles")
	public FileDataUnit inputFiles;
	
	@OutputDataUnit(name = "pickedFiles")
	public FileDataUnit pickedFiles;
	
	public MultipleFilesPicker() {
		super(MultipleFilesPickerConfig.class);
	}

	@Override
	public AbstractConfigDialog<MultipleFilesPickerConfig> getConfigurationDialog() {
		return new MultipleFilesPickerDialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {
				

		String path = this.config.getPath();
		if ( path == null || "".equals(path) )	{
			context.sendMessage(MessageType.ERROR, "No path to pick a file has been provided.");
        	return;
		}
		
		
		String fileNamePattern;
		if (path.contains("/"))	{
			fileNamePattern = path.substring(path.lastIndexOf('/')+1);
		} else {
			fileNamePattern = path;
		}
		
		try	{
			Pattern.compile(fileNamePattern);
		} catch (PatternSyntaxException e)	{
			context.sendMessage(MessageType.ERROR, "Invalid regexp provided as a file name pattern.");
        	return;
		}
		
		DirectoryHandler inputRootHandler = inputFiles.getRootDir();

		ArrayList<File> pickedFilesPhysical = new ArrayList<>();
		int filesCounter = 0;
		for (Handler handler : inputRootHandler) {
    		if (handler instanceof FileHandler) {
    			filesCounter++;
    			FileHandler file = (FileHandler) handler;
    			String fileName = file.getName();
    			if ( fileName.matches(fileNamePattern) )	{
    				pickedFilesPhysical.add(file.asFile());
    			}
    		}
    	}
		log.debug(filesCounter + " files found on the input.");
		if ( pickedFilesPhysical.isEmpty())	{
			log.warn("No files found in the input file data unit on the base of the pattern " + path);
        	return;
		}
		
        DirectoryHandler outputRootHandler = pickedFiles.getRootDir();
        for (File pickedFilePhysical : pickedFilesPhysical) {
        	outputRootHandler.addExistingFile(pickedFilePhysical, new OptionsAdd(false, true));
		}

       	if (context.canceled()) {
       		log.info("DPU cancelled");
       	}
        
	}

}
