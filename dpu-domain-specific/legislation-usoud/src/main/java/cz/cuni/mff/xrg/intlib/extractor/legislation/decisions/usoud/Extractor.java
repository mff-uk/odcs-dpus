package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils.USoudHTTPRequests;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils.FileRecord;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.slf4j.LoggerFactory;

/**
 * Extracts decisions from nsoud:
 * 
 * TODO: convert doc to txt
 * TODO adjust fileName, so that expression may be constructed, generuje txt convertor nejake names?
 * 
 * TODO: jak ziskat data pro zvolene od-do -> problem se strankou nalus.usoud.cz - nejde vybrat data!
 *
 * @author tomasknap
 */
@DPU.AsExtractor
public class Extractor extends DpuAdvancedBase<ExtractorConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            Extractor.class);
    
    private String dateFrom;
	
    private String dateTo;

   public Extractor(){
		super(ExtractorConfig.class, AddonInitializer.noAddons());
	}
	
    @DataUnit.AsOutput(name = "output")
	public WritableFilesDataUnit outputFiles;
    
          @Override
        public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
            return new ExtractorDialog();
        }

     @Override
        protected void innerExecute() throws DPUException, DataUnitException {
             
        log.info("DPU is running ...");

//        final SimpleRdfWrite rdfOutputWrap = SimpleRdfFactory.create(rdfOutput, context);
//        final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();
		
        //log.info("\n ****************************************************** \n STARTING UNZIPPER \n *****************************************************");

        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();


        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        
        
         //prepare access to resources of the jar file
        //get path JAR file, so that resources (such as perl script can be read)
        //File jarPath = context.getJarPath();
        String jarPathString = null;
        try {
            jarPathString = context.getJarPath().getCanonicalPath();
        } catch (IOException ex) {
            log.error("Cannot get path to the jar file with jTagger resources");
            log.debug(ex.getLocalizedMessage());
        }

        //to get unzipped version of JAR
         String unzipedJarPathString = jarPathString;
        if (jarPathString.lastIndexOf(".jar") > -1) {
            unzipedJarPathString = jarPathString.substring(0, jarPathString.lastIndexOf(".jar"));
            
               //extract jar file to get to the resources? remove temp hack later when setting path for JTagger
            log.debug("About to unzip {} to {} so that resources in JAR are accessible", jarPathString, unzipedJarPathString);
            try {
                unzip(jarPathString, unzipedJarPathString);
            } catch (ZipException ex) {
                log.error("Unzip error, {}", ex.getLocalizedMessage());
            } catch (IOException ex) {
                log.error("Error:: " + ex.getLocalizedMessage());
            }
            
        }
       

        String pathToResources = "src" + File.separator + "main" + File.separator + "resources";
        //adjust permissions so that we can execute binaries of jTagger
        try {
            //chmod +x hunpos-tag, hunpos-train
            ///Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1/src/main/resources/tagger/hunpos-tag
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + "catdoc");
            //log.debug("Executing: chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            //Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-train");
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        
        
        
        
        
        //get cache:
        //File globalDirectory = context.getGlobalDirectory();
     
        
        USoudHTTPRequests util = new USoudHTTPRequests(context, unzipedJarPathString, config);
        
        //*****************************
        //get data (zipped file) from target URL

        List<FileRecord> filePaths = util.downloadData();
        
        

        //*****************************
        //OUTPUT
        int i = 0;
        for (FileRecord fileRecord : filePaths)  {
            i++;

            String filePath = fileRecord.getFilePath();
            String expressionURI = fileRecord.getExpression();
            File newFile = new File(filePath);

            //process each extracted file
            log.info("Processing file {}", newFile.toURI().toASCIIString());
            
            
            
               //add file to the output (symbolic name is newSubject
            outputFiles.addExistingFile(expressionURI, newFile.toURI().toASCIIString());
            
            //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
            String outputVirtualPath = newFile.getName();
            VirtualPathHelpers.setVirtualPath(outputFiles, expressionURI, outputVirtualPath);
                     
            log.debug("Adding new file with sn {} and path{}", expressionURI, newFile.toURI().toASCIIString());
            
            

            if (context.canceled()) {
                log.info("DPU cancelled");
                return;
            }

                
                
                

        }


        //LOG.info("Processed {} files", i);
        context.sendMessage(DPUContext.MessageType.INFO, "Processed " + i +" files");


    }
    
     public static void printProcessErrorOutput(Process process) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errors = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                errors.append(line);
            }
            if (errors.length() > 0) {
                log.warn(errors.toString());
            }
            in.close();

//            in = new BufferedReader(new InputStreamReader(process.getInputStream()));
//           StringBuilder notes = new StringBuilder();
//        
//            while ((line = in.readLine()) != null) {
//                notes.append(line); notes.append("\n");
//            }
//            log.debug(notes.toString());
//            in.close();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }
    }
     
     
     public static String fromStream(InputStream in, Charset charset) throws IOException
{
    BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset));
    StringBuilder out = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    String line;
    while ((line = reader.readLine()) != null) {
        out.append(line);
        out.append(newLine);
    }
    return out.toString();
}

  
    
      private static void unzip(String source, String destination) throws IOException, ZipException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                log.error("Zip encrypted");
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            log.error("Error {}", e.getLocalizedMessage());
        }
    }

  

}
