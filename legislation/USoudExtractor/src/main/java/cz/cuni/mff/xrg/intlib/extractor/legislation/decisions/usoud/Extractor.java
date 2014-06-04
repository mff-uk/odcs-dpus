package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.usoud;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils.USoudHTTPRequests;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils.FileRecord;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.module.utils.AddTripleWorkaround;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
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
@AsExtractor
public class Extractor extends ConfigurableBase<ExtractorConfig> implements ConfigDialogProvider<ExtractorConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            Extractor.class);
    
    private String dateFrom;
	
    private String dateTo;

    public Extractor() {
        super(ExtractorConfig.class);
    }
	
    @OutputDataUnit(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {
        return new ExtractorDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {

		final SimpleRdfWrite rdfOutputWrap = new SimpleRdfWrite(rdfOutput, context);
		final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();
		
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

            //process each extracted file
            log.info("Processing file {}", filePath);
            
            //Create output
            String output =  DataUnitUtils.readFile(filePath); //, Charset.forName("Cp1250"));
            if (output == null) {
                log.warn("File {} cannot be read", filePath);
                log.warn("File skipped");
            }

            //use the expression
            Resource subj = valueFactory.createURI(expressionURI);
            URI pred = valueFactory.createURI(config.getOutputPredicate());
            Value obj = valueFactory.createLiteral(output);

            String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
            log.debug("Prepared triple {}", preparedTriple);

            DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
            String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".txt";
            DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
            
            try {
				rdfOutputWrap.extract(new File(tempFileLoc), RDFFormat.TURTLE, null);
                log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
            } catch(OperationFailedException e) {
                log.warn("Error parsing file for subject {}, exception {}", subj, e.getLocalizedMessage());
                log.info("Continues with the next file");
            }

            if (context.canceled()) {
                log.info("DPU cancelled");
                return;
            }

                
                
                

        }


        log.info("Processed {} files", i);






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
