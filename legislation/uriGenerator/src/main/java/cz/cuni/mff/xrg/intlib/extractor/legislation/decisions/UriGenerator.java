package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.IntLibLink;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.module.utils.AddTripleWorkaround;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@AsTransformer
public class UriGenerator extends ConfigurableBase<UriGeneratorConfig> implements ConfigDialogProvider<UriGeneratorConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            UriGenerator.class);

    public UriGenerator() {
        super(UriGeneratorConfig.class);
    }
    @InputDataUnit
    public RDFDataUnit rdfInput;
    @OutputDataUnit
    public RDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<UriGeneratorConfig> getConfigurationDialog() {
        return new UriGeneratorDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {

  log.info("\n ****************************************************** \n STARTING URI GENERATOR \n *****************************************************");
        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();


        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        
        if (config.getStoredXsltFilePath().isEmpty()) {
                     log.error("Configuration file is missing, the processing will NOT continue");
                     context.sendMessage(MessageType.ERROR, "Configuration file is missing, the processing will NOT continue");
        }

//        //prepare access to resources of the jar file
//        //get path JAR file, so that resources (such as perl script can be read)
//        //File jarPath = context.getJarPath();
//        String jarPathString = null;
//        try {
//            jarPathString = context.getJarPath().getCanonicalPath();
//        } catch (IOException ex) {
//            log.error("Cannot get path to the jar file with jTagger resources");
//            log.debug(ex.getLocalizedMessage());
//        }
//
//        //to get unzipped version of JAR
//        String unzipedJarPathString = jarPathString.substring(0, jarPathString.lastIndexOf(".jar"));
//        String pathToResources = "src" + File.separator + "main" + File.separator + "resources";
//        //extract jar file to get to the resources? remove temp hack later when setting path for JTagger
//        log.debug("About to unzip {} to {} so that resources in JAR are accessible", jarPathString, unzipedJarPathString);
//        try {
//            unzip(jarPathString, unzipedJarPathString);
//        } catch (ZipException ex) {
//            log.error("Unzip error, {}", ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            log.error("Error:: " + ex.getLocalizedMessage());
//        }


        //prepare inputs, call xslt for each input
        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
        log.debug("Query for getting input files: {}", query);
        //get the return values
        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);

        //log.info(executeSelectQueryAsTuples.asList().)
        int i = 0;
        try {
            
            while (executeSelectQueryAsTuples.hasNext()) {

                i++;
                //process the inputs
                BindingSet solution = executeSelectQueryAsTuples.next();
                Binding b = solution.getBinding("o");
                String fileContent = b.getValue().toString();
                String subject = solution.getBinding("s").getValue().toString();
                log.info("Processing new file for subject {}", subject);
                //log.debug("Processing file {}", fileContent);


                String inputFilePath = pathToWorkingDir + File.separator + String.valueOf(i) + ".xml";

                //store the input content to file, inputs are xml files!
                File file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath);
                if (file == null) {
                    log.warn("Problem processing object for subject {}", subject);
                    continue;
                }
                
                //run URI Generator
                 String outputURIGeneratorFilename = pathToWorkingDir + File.separator + "outURIGen" + File.separator + String.valueOf(i) + ".xml";
                 DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGen" + File.separator);
           
              
                 
//                 //where the resources within unzipped jar files are located.
//                 String unzipedJarPathStringResources = unzipedJarPathString + File.separator + pathToResources;
//                 //config for URI generator
//                 String configURiGen = unzipedJarPathStringResources + File.separator + "uriGenConfig.xml";
                 
                 
                 
                 runURIGenerator(inputFilePath, outputURIGeneratorFilename, config.getStoredXsltFilePath(), context) ;
            
                //check output
                if (!outputGenerated(outputURIGeneratorFilename)) {
                    continue;
                }
                

              





log.info("URI generator successfully executed, creating output");
              
                 //OUTPUT
               String outputString = DataUnitUtils.readFile(outputURIGeneratorFilename);
                
               Resource subj = rdfOutput.createURI(subject);
               URI pred = rdfOutput.createURI(config.getOutputPredicate());
               Value obj = rdfOutput.createLiteral(outputString); 
            
               
               String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
               
               DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
               String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".ttl";
            
               
               //String tempFileLoc = pathToWorkingDir + File.separator + String.valueOf(i) + "out.ttl";
         
                
               DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
               rdfOutput.addFromTurtleFile(new File(tempFileLoc));
               
               //log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
                
               log.info("Output successfully created");
               
               if (context.canceled()) {
                    log.info("DPU cancelled");
                    return;
               }

            }
        } catch (QueryEvaluationException ex) {
              context.sendMessage(MessageType.ERROR, "Problem evaluating the query to obtain files to be processed. Processing ends.", ex.getLocalizedMessage());
            log.error("Problem evaluating the query to obtain values of the {} literals. Processing ends.", config.getInputPredicate());
            log.debug(ex.getLocalizedMessage());
        }
        
        log.info("Processed {} files - values of predicate {}", i, config.getInputPredicate());











    }

      private void runURIGenerator(String file, String output, String configURiGen, DPUContext context) {
                    //log.info("About to run URI generator for {}", file);
                    IntLibLink.processFiles(file, output, configURiGen,context);


                }
    

    private boolean outputGenerated(String output) {
        File f = new File(output);
        if (!f.exists()) {
            log.warn("File {} was not created", output);
            log.warn("Skipping rest of the steps for the given file");
            return false;
        } else {
            log.info("File {} was generated as result of URI generator",
                    output);
            return true;
        }
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

    private String removeTrailingQuotes(String fileContent) {
        
        if (fileContent.startsWith("\"")) {
            fileContent = fileContent.substring(1);
        }
        if (fileContent.endsWith("\"")) {
            fileContent = fileContent.substring(0, fileContent.length()-1);
        }
        return fileContent;
    }
}
