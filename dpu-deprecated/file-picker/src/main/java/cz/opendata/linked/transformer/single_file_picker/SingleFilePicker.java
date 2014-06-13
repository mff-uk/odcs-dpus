package cz.opendata.linked.transformer.single_file_picker;

import java.io.File;

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
public class SingleFilePicker extends ConfigurableBase<SingleFilePickerConfig> implements
		ConfigDialogProvider<SingleFilePickerConfig> {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            SingleFilePicker.class);
	
	@InputDataUnit(name = "inputFiles")
	public FileDataUnit inputFiles;
	
	@OutputDataUnit(name = "pickedFile")
	public FileDataUnit pickedFile;
	
	public SingleFilePicker() {
		super(SingleFilePickerConfig.class);
	}

	@Override
	public AbstractConfigDialog<SingleFilePickerConfig> getConfigurationDialog() {
		return new SingleFilePickerDialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {
				

		String path = this.config.getPath();
		if ( path == null || "".equals(path) )	{
			context.sendMessage(MessageType.ERROR, "No path to pick a file has been provided.");
        	return;
		}
		
		DirectoryHandler inputRootHandler = inputFiles.getRootDir();

		File pickedFilePhysical = null;		
		for (Handler handler : inputRootHandler) {
    		if (handler instanceof FileHandler) {
    			FileHandler file = (FileHandler) handler;
    			if (path.equals(file.getName()))	{
    				pickedFilePhysical = file.asFile();
    			}
    		}
    	}
		if ( pickedFilePhysical == null )	{
			context.sendMessage(MessageType.ERROR, "Supplied path does not lead to a file.");
        	return;
		}
		
        DirectoryHandler outputRootHandler = pickedFile.getRootDir();
        outputRootHandler.addExistingFile(pickedFilePhysical, new OptionsAdd(false, true));

       	if (context.canceled()) {
       		log.info("DPU cancelled");
       	}
        
	}

}
