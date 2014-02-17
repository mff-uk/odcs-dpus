package cz.opendata.linked.extractor.unzipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
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

@AsExtractor
public class Extractor extends ConfigurableBase<ExtractorConfig> implements
		ConfigDialogProvider<ExtractorConfig> {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            Extractor.class);
	
	@InputDataUnit(name = "zipFile", optional=true)
	public FileDataUnit zipFileInput;
	
	@OutputDataUnit(name = "extractedFiles")
	public FileDataUnit filesOutput;
	
	public Extractor() {
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException,
			DataUnitException {
				
		//	get working directory
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();

        //	get path to the working directory
        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
        	context.sendMessage(MessageType.ERROR, "Problem when getting path to the working directory: " + ex.getLocalizedMessage());
        	return;
        }

        //	get zipFile
        
        String zipFile = pathToWorkingDir + File.separator + "files.zip";
        String extractedFiles = pathToWorkingDir + File.separator + "extracted_files";

        if ( zipFileInput == null )	{
        	
        	String urlWithZip = this.config.getZipFileURL();
        	if ( urlWithZip == null || "".equals(urlWithZip) )	{
        		context.sendMessage(MessageType.ERROR, "Empty URL supplied.");
            	return;
        	}
        	
            URL url = null;
            FileOutputStream fos = null;
            try {
            	url = new URL(urlWithZip);
            	context.sendMessage(MessageType.INFO, "Downloading ZIP file " + urlWithZip);
            	ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            	fos = new FileOutputStream(zipFile);
            	fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (MalformedURLException ex) {
            	context.sendMessage(MessageType.ERROR, "Malformed URL: " + ex.getLocalizedMessage());
            	return;
            } catch (IOException ex) {
            	context.sendMessage(MessageType.ERROR, "Error when storing downloaded ZIP file: " + ex.getLocalizedMessage());
            	return;
            } finally	{
            	if ( fos != null )	{
            		try	{
            			fos.close();
            		} catch	(IOException ex)	{
            			context.sendMessage(MessageType.ERROR, "Problem with closing the output stream for reading NDF-RT_XML_Inferred.zip");
            			return;
            		}
            	}
            }
        
            //	extract files from zipFile
            context.sendMessage(MessageType.INFO, "Extracting files from ZIP file to " + extractedFiles, zipFile);
            try {
                unzip(zipFile, extractedFiles);              
            } catch (IllegalArgumentException ex) {
                 context.sendMessage(MessageType.ERROR, "It was not possible to extract files from " + urlWithZip, "The original problem was: " + ex.getLocalizedMessage());
                 return;
            }
            
        } else {
        	
        	DirectoryHandler root = zipFileInput.getRootDir();
        	for (Handler handler : root) {
        		if (handler instanceof FileHandler) {
        			FileHandler file = (FileHandler) handler;
        			try {
                        unzip(file.getRootedPath(), extractedFiles);              
                    } catch (IllegalArgumentException ex) {
                         context.sendMessage(MessageType.ERROR, "It was not possible to extract files from " + file.getRootedPath(), "The original problem was: " + ex.getLocalizedMessage());
                         return;
                    }
        		}
        	}
        	
        }
        
        DirectoryHandler dir = filesOutput.getRootDir();
        dir.addExistingDirectory(new File(extractedFiles), new OptionsAdd());

       	if (context.canceled()) {
       		log.info("DPU cancelled");
       		return;
       	}
        
	}
	
	private static void unzip(String source, String destination) throws IllegalArgumentException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                throw new IllegalArgumentException("ZIP file " + source + " is encrypted.");
            }
            zipFile.extractAll(destination);           
        } catch (ZipException e) {
            throw new IllegalArgumentException("ZIP file " + source + " could not be unzipped: " + e.getLocalizedMessage());
        }
    }

}
