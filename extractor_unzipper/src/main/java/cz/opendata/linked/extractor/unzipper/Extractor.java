package cz.opendata.linked.extractor.unzipper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
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
import org.slf4j.Logger;



@AsExtractor
public class Extractor extends ConfigurableBase<ExtractorConfig> implements
		ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);
	
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
		//File workingDir = new File("/home/cammeron/Downloads/DPUs-master/extractor_unzipper/temp");

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

//        String extractedFiles = pathToWorkingDir + File.separator + "extracted_files";
        String extractedFiles = pathToWorkingDir;

        if ( zipFileInput == null )	{
        	
        	String urlWithZip = this.config.getZipFileURL();
        	if ( urlWithZip == null || "".equals(urlWithZip) )	{
        		context.sendMessage(MessageType.ERROR, "Empty URL supplied.");
            	return;
        	}

	        String zipFile = pathToWorkingDir + File.separator + urlWithZip.substring(urlWithZip.lastIndexOf("/")+1,urlWithZip.length());
        	
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
            	context.sendMessage(MessageType.ERROR, "Error when storing downloaded compressed file: " + ex.getLocalizedMessage());
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

            try {
                unCompress(zipFile, extractedFiles);
            } catch (IllegalArgumentException ex) {
                 context.sendMessage(MessageType.ERROR, "It was not possible to extract files from " + urlWithZip, "The original problem was: " + ex.getLocalizedMessage());
                 return;
            }
            
        } else {
        	
        	DirectoryHandler root = zipFileInput.getRootDir();
        	for (Handler handler : root) {
        		if (handler instanceof FileHandler) {
        			File file = (File) handler.asFile();
        			try {
                        unCompress(file.getAbsolutePath(), extractedFiles);
                    } catch (IllegalArgumentException ex) {
                         context.sendMessage(MessageType.ERROR, "It was not possible to extract files from " + file.getAbsolutePath(), "The original problem was: " + ex.getLocalizedMessage());
                         return;
                    }
        		}
        	}
        	
        }
        
        DirectoryHandler outputRoot = filesOutput.getRootDir();
        File inputRoot = new File(extractedFiles);
        for (File file : inputRoot.listFiles()) {
    		if (file.isFile()) {
    			outputRoot.addExistingFile(file, new OptionsAdd(false, true));
    		} else if (file.isDirectory())	{
    			outputRoot.addExistingDirectory(file, new OptionsAdd(false, true));
    		}
    	}

       	if (context.canceled()) {
       		LOG.info("DPU cancelled");
       		return;
       	}
        
	}
	
	private void unCompress(String source, String destination) throws IllegalArgumentException {

		try {

			Path path = FileSystems.getDefault().getPath(source);
			String fileType = Files.probeContentType(path);

			LOG.debug("Uncompressing file " + source + " with file type: " + fileType);

			switch(fileType) {
				case "application/zip":
					this.unZip(source, destination);
					break;
				case "application/x-compressed-tar":
					this.unTar(source, destination);
					break;
				default:
					LOG.error("Unsupported mime type " + fileType + " of file: " + source + " - skipping");
					return;
			}

		} catch(IOException e) {
			LOG.error("It was not possible to extract files. " + e.getMessage());
			e.printStackTrace();
		}

    }

	/**
	 * Extract files from zip archive
	 * @param source
	 * @param destination
	 */
	private void unZip(String source, String destination) throws IOException {

		LOG.debug("Extracting files from zip located in " + source + " to " + destination);

		ZipFile zipIn = new ZipFile(source);

		for (Enumeration e = zipIn.getEntries(); e.hasMoreElements(); ) {
			ZipArchiveEntry entry = (ZipArchiveEntry) e.nextElement();

			File destPath = new File(destination, entry.getName());

			if (entry.isDirectory()) {
				this.createDir(destPath);
				continue;
			}

			if (!destPath.getParentFile().exists()){
				this.createDir(destPath.getParentFile());
			}

			BufferedInputStream inputStream = new BufferedInputStream(zipIn.getInputStream(entry));
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destPath));

			try {
				IOUtils.copy(inputStream, outputStream);
			} finally {
				outputStream.close();
				inputStream.close();
			}

		}

	}

	/**
	 * extract files from tar.gz archive
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	private void unTar(String source, String destination) throws IOException {

		LOG.debug("Extracting files from tar.gz located in: " + source + " to " + destination);

		TarArchiveInputStream tarIn;

		tarIn = new TarArchiveInputStream(
					new GzipCompressorInputStream(
						new BufferedInputStream(
							new FileInputStream(source)
						)
					)
				);

		TarArchiveEntry tarEntry;

		while ((tarEntry = (TarArchiveEntry)tarIn.getNextEntry()) != null) {
			File destPath = new File(destination, tarEntry.getName());

			if (tarEntry.isDirectory()) {
				if(!destPath.exists()) {
					this.createDir(destPath);
				}
			} else {

				if (!destPath.getParentFile().exists()){
					this.createDir(destPath.getParentFile());
				}

				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(destPath));
				IOUtils.copy(tarIn, outputStream);

				outputStream.close();
			}
		}
		tarIn.close();
	}

	private void createDir(File dir) {
		if(!dir.mkdirs()) throw new RuntimeException("Couldn´t make dir: " + dir);
	}



}
