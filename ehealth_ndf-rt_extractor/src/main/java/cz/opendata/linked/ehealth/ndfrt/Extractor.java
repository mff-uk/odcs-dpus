package cz.opendata.linked.ehealth.ndfrt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.*;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;

@AsExtractor
public class Extractor extends ConfigurableBase<ExtractorConfig> implements
		ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);
	
	@OutputDataUnit(name = "XMLNDFRT")
	public WritableRDFDataUnit rdfOutput;

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
        String pathToWorkingDir;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
        	context.sendMessage(MessageType.ERROR, "Problem when getting path to the working directory: " + ex.getLocalizedMessage());
        	return;
        }

        //get http://evs.nci.nih.gov/ftp1/NDF-RT/NDFRT_Public_All.zip
        String ndfrtZipFile = pathToWorkingDir + File.separator + "data.zip";
        String ndfrtFiles = pathToWorkingDir + File.separator + "unzipped";

        String urlWithZip = "http://evs.nci.nih.gov/ftp1/NDF-RT/NDF-RT_XML_Inferred.zip";
        URL url;
        FileOutputStream fos = null;
        try {
        	url = new URL(urlWithZip);
        	context.sendMessage(MessageType.INFO, "Downloading NDF-RT_XML_Inferred.zip");
        	ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        	fos = new FileOutputStream(ndfrtZipFile);
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

        //unzip http://evs.nci.nih.gov/ftp1/NDF-RT/NDFRT_Public_All.zip
        context.sendMessage(MessageType.INFO, "Unzipping NDF-RT_XML_Inferred.zip", ndfrtZipFile);
        try {
            unzip(ndfrtZipFile, ndfrtFiles);              
         } catch (IllegalArgumentException ex) {
             context.sendMessage(MessageType.ERROR, "It was not possible to unzip NDF-RT_XML_Inferred.zip.", "The original problem was: " + ex.getLocalizedMessage());
             return;
        }
        
        //	read NDFRT_Public_YYYY.MM.DD_TDE.xml file
        String ndfrtFileContent = null;
        String ndfrtFileName = null;
        boolean dirFound = false;
    	boolean fileFound = false;
        for (File dir : (new File(ndfrtFiles)).listFiles()) {
        	
        	//	open NDFRT_Public_YYYY.MM.DD directory
        	if ( dir.isDirectory() && dir.getName().matches("NDFRT_Public_[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}") )	{
        	
        		dirFound = true;
        		
        		//	find  NDFRT_Public_YYYY.MM.DD_TDE.xml
        		for (File file : dir.listFiles()) {
        		
        			if ( file.isFile() && file.getName().matches("NDFRT_Public_[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}_TDE_inferred\\.xml") )	{
        				fileFound = true;
        				ndfrtFileName = file.getName();
        				
        	            try {
        	            	ndfrtFileContent =  DataUnitUtils.readFile(file.getCanonicalPath().toString(), Charset.forName("UTF-8"));
        	            } catch (IOException ex) {
        	            	context.sendMessage(MessageType.ERROR, "It was not possible to read NDFRT_Public_YYYY.MM.DD_TDE_inferred.xml.", "The original problem was: " + ex.getLocalizedMessage());
        	            	return;
        	            }        	            
        	            break;
        			}
        		}
        		break;
        	}
        }

        if ( dirFound == false )	{
        	context.sendMessage(MessageType.ERROR, "Directory NDFRT_Public_YYYY.MM.DD was not found.");
        	return;
        }
        if ( fileFound == false )	{
        	context.sendMessage(MessageType.ERROR, "File NDFRT_Public_YYYY.MM.DD_TDE_inferred.xml was not found.");
        	return;
        }
        if ( ndfrtFileContent == null || "".equals(ndfrtFileContent) )	{
        	context.sendMessage(MessageType.ERROR, "File NDFRT_Public_YYYY.MM.DD_TDE_inferred.xml was found but it is empty.");
        	return;
        }        
		
		SimpleRdfWrite rdfOutputWrap = new SimpleRdfWrite(rdfOutput, context);
		// add triple into the pository
		final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();
		Resource subj = valueFactory.createURI(config.getNDFRTPrefix() + ndfrtFileName.substring(0, ndfrtFileName.length()-4));
		URI pred = valueFactory.createURI(OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE);
		Value obj = valueFactory.createLiteral(ndfrtFileContent);
			
		rdfOutputWrap.add(subj, pred, obj);
		
       	LOG.debug("Result was added to output data unit as turtle data containing one triple");

       	if (context.canceled()) {
       		LOG.info("DPU cancelled");
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
 