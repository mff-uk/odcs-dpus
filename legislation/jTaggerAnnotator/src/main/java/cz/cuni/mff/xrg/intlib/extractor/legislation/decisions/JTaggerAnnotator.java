package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.JTagger;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.JTaggerResult;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.LineJoiner;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;

import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.dpu.DPU;
import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
import cz.cuni.xrg.intlib.commons.dpu.DPUException;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsTransformer;
import cz.cuni.xrg.intlib.commons.dpu.annotation.InputDataUnit;
import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.module.utils.AddTripleWorkaround;
import cz.cuni.xrg.intlib.commons.module.utils.DataUnitUtils;
import cz.cuni.xrg.intlib.commons.ontology.OdcsTerms;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Literal;
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
public class JTaggerAnnotator extends ConfigurableBase<JTaggerAnnotatorConfig> implements ConfigDialogProvider<JTaggerAnnotatorConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            JTaggerAnnotator.class);

    public JTaggerAnnotator() {
        super(JTaggerAnnotatorConfig.class);
    }
    @InputDataUnit
    public RDFDataUnit rdfInput;
    @OutputDataUnit
    public RDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<JTaggerAnnotatorConfig> getConfigurationDialog() {
        return new JTaggerAnnotatorDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {

      log.info("\n ****************************************************** \n STARTING JTAGGER ANNOTATOR \n *****************************************************");
  
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
        String unzipedJarPathString = jarPathString.substring(0, jarPathString.lastIndexOf(".jar"));
        String pathToResources = "src" + File.separator + "main" + File.separator + "resources";
        //extract jar file to get to the resources? remove temp hack later when setting path for JTagger
        log.info("About to unzip {} to {} so that resources in JAR are accessible", jarPathString, unzipedJarPathString);
        try {
            unzip(jarPathString, unzipedJarPathString);
        } catch (ZipException ex) {
            log.error("Unzip error, {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            log.error("Error:: " + ex.getLocalizedMessage());
        }

        //adjust permissions so that we can execute binaries of jTagger
        try {
            //chmod +x hunpos-tag, hunpos-train
            ///Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1/src/main/resources/tagger/hunpos-tag
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            log.info("Executing: chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-train");
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }

        //prepare inputs, call xslt for each input
        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
        log.debug("Query for getting input files: {}", query);
        //get the return values
        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);

        try {
            int i = 0;
            while (executeSelectQueryAsTuples.hasNext()) {

                i++;
                
                //TODO temp hack 
                if (i > 3) break;
                //process the inputs
                BindingSet solution = executeSelectQueryAsTuples.next();
                Binding b = solution.getBinding("o");
                String fileContent = b.getValue().toString();
                String subject = solution.getBinding("s").getValue().toString();
                log.info("Processing new file for subject {}", subject);
                log.debug("Processing file {}", fileContent);


                String inputFilePath = pathToWorkingDir + File.separator + String.valueOf(i) + ".txt";

                //store the input content to file, inputs are xml files!
                File file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath, Charset.forName("Cp1250"));
                if (file == null) {
                    log.warn("Problem processing object for subject {}", subject);
                    continue;
                }

                //run jTagger
                String outputJTaggerFilename = pathToWorkingDir + File.separator + "outJTagger" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outJTagger" + File.separator);
                runJTagger(inputFilePath, outputJTaggerFilename, unzipedJarPathString, pathToWorkingDir);

                //check output
                if (!outputGenerated(outputJTaggerFilename)) {
                    continue;
                }


                //////////////////////
                //add meta and content elements to body
                //////////////////////
                String outputMetadataElement = pathToWorkingDir + File.separator + "outMetaElem" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outMetaElem" + File.separator);

                addMetaAndContentElements(outputJTaggerFilename, outputMetadataElement);

                //check output
                if (!outputGenerated(outputMetadataElement)) {
                    continue;
                }


                //////////////////////
                //add paragraph elements
                //////////////////////
                log.info("About add paragraphs elements");
                String outputURIGeneratorFilenameWithParagraphs = pathToWorkingDir + File.separator + "outURIGenPara" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGenPara");
            
                runParagraphAdjustment(outputMetadataElement, outputURIGeneratorFilenameWithParagraphs);

                //check output
                if (!outputGenerated(outputURIGeneratorFilenameWithParagraphs)) {
                    continue;
                }






              
                 //OUTPUT
               String outputString = DataUnitUtils.readFile(outputURIGeneratorFilenameWithParagraphs);
                
               Resource subj = rdfOutput.createURI(subject);
                URI pred = rdfOutput.createURI(OdcsTerms.DATA_UNIT_XML_VALUE_PREDICATE);
               //TODO config has still textVal, why???
                //URI pred = rdfOutput.createURI(config.getOutputPredicate());
               Value obj = rdfOutput.createLiteral(outputString); 
            
               
               String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
               
               DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
               String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".ttl";
            
               
               //String tempFileLoc = pathToWorkingDir + File.separator + String.valueOf(i) + "out.ttl";
         
                
               DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
               rdfOutput.addFromTurtleFile(new File(tempFileLoc));
               
               log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
                
                
                
     




            }
        } catch (QueryEvaluationException ex) {
            log.error(ex.getLocalizedMessage());
            log.info("Further files are not annotated");
        }











    }

    private void runParagraphAdjustment(String outputURIGeneratorFilename, String outputURIGeneratorFilenameWithParagraphs) {



        String input_text = null;
        input_text = DataUnitUtils.readFile(outputURIGeneratorFilename, StandardCharsets.UTF_8);



        String output = addParagraphElements(input_text);

        PrintWriter out = null;
        try {
            out = new PrintWriter(outputURIGeneratorFilenameWithParagraphs, "UTF-8");
        } catch (FileNotFoundException ex) {
            log.error(ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getLocalizedMessage());
        }
        out.write(output);
        out.flush();
        out.close();


    }

    private void addMetaAndContentElements(String inputFilePath, String outputFileName) {

        String input_text = null;
        input_text = DataUnitUtils.readFile(inputFilePath, StandardCharsets.UTF_8);



        String output = addMetaAndContentElementsWorker(input_text);

        PrintWriter out = null;
        try {
            out = new PrintWriter(outputFileName, "UTF-8");
        } catch (FileNotFoundException ex) {
            log.error(ex.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getLocalizedMessage());
        }
        out.write(output);
        out.flush();
        out.close();

    }

    private String addMetaAndContentElementsWorker(String input_string) {


        if (input_string == null) {
            log.warn("Input string is null, returning empty string from add Paragraph Elements");
            return "";
        }

        //add metadata elem <body><metadata>... </metadata> USNESENI...
        int indexBody = input_string.indexOf("<body>") + "<body>".length();
        String before = input_string.substring(0, indexBody);

        String toBeProcessedTemp = input_string.substring(indexBody);
        
        String output = "";
        String content = toBeProcessedTemp;
        
        if (toBeProcessedTemp.indexOf("U S N E S E N Í") < 0) {
            log.warn("Label U S N E S E N Í was not found in the processed decision, creation of metadata section will not work properly");
        }
        else {
            String metadata = toBeProcessedTemp.substring(0, toBeProcessedTemp.indexOf("U S N E S E N Í"));
            output = "<meta>" + metadata + "</meta>";
            content = toBeProcessedTemp.substring(toBeProcessedTemp.indexOf("U S N E S E N Í"));
        }

       




        //add content elem </metadata> <content>USNESENI... </content></body>
        String toBeProcessed2 = content.substring(0, content.indexOf("</body>"));
        String output2 = "<content>" + toBeProcessed2 + "</content>";

        String after = content.substring(content.indexOf("</body>"));




        //log.info("Para orig {}", input_string);
        log.debug("****Para before {}", before);
        log.debug("****Para output {}", output);
        log.debug("****Para output2 {}", output2);
        log.debug("*****Para after {}", after);
        //log.info("Para all {}", before + output + after);


        //to remove \n from lines which do not end with "." (line in the paragraph) and new line char (empty line between paragraphs)
        //If they end with dots, \n is not removed if the new line character is followed by space, tab or next new line character, 
        //because this denotes new paragraph and in this case new line char is ok.  
        //output = output.replaceAll("([^.\n])\n([^\\s])", "$1$2");
        log.debug("Para NEW: " + before + output + output2 + after);
        return before + output + output2 + after;
    }

    //add paragraph elements wrapping paragraphs within <body> section
    private String addParagraphElements(String input_string) {

        if (input_string == null) {
            log.warn("Input string is null, returning empty string from add Paragraph Elements");
            return "";
        }

        //add paragraphs only to the content of the body elem:
        int indexContent = input_string.indexOf("<content>") + "<content>".length();
        String before = input_string.substring(0, indexContent);
        String toBeProcessedTemp = input_string.substring(indexContent);
        //String toBeProcessed = input_string.substring(indexBody, input_string.indexOf("</body>") - indexBody);
        String toBeProcessed = toBeProcessedTemp.substring(0, toBeProcessedTemp.indexOf("</content>"));
        String after = toBeProcessedTemp.substring(toBeProcessedTemp.indexOf("</content>"));



        //to ensure that empty lines are really empty (no hidden spaces, tabs, ..)
        String output = toBeProcessed.replaceAll("([^\n]+)\n", "\n<paragraph>$1</paragraph>\n");

        //log.info("Para orig {}", input_string);
        log.info("****Para before {}", before);
        log.info("****Para output {}", toBeProcessed);
        log.info("****Para after {}", after);
        log.info("*****With Para {}", output);
        //log.info("Para all {}", before + output + after);


        //to remove \n from lines which do not end with "." (line in the paragraph) and new line char (empty line between paragraphs)
        //If they end with dots, \n is not removed if the new line character is followed by space, tab or next new line character, 
        //because this denotes new paragraph and in this case new line char is ok.  
        //output = output.replaceAll("([^.\n])\n([^\\s])", "$1$2");

        return before + output + after;



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

    private void runJTagger(String inputFile, String outputJTaggerFilename, String jarPathString, String pathToWorkingDir) {
        log.info("Jtagger is about to be run for file {}, path to unpacked jar with resources: {} ", inputFile, jarPathString);
        try {
            //process one file in the filesystem
            ///Users/tomasknap/Documents/PROJECTS/TACR/judikaty/data from web/nejvyssiSoud/rozhodnuti-3_Tdo_348_2013.txt

            //String inputFile = "/Users/tomasknap/Documents/PROJECTS/TACR/judikaty/data from web/nejvyssiSoud/rozhodnuti-3_Tdo_348_2013.txt";

            FileInputStream fis = new FileInputStream(inputFile);
            InputStreamReader inp = new InputStreamReader(fis, "cp1250");
            //InputStreamReader inp = new InputStreamReader(fis, "UTF-8");
            BufferedReader reader = new BufferedReader(inp);
            String input_text = "";
            String line = null;
            while ((line = reader.readLine()) != null) {
                input_text += line + "\n";
            }

            log.debug("About to join Lines");
            // Opravim riadkovanie
            input_text = LineJoiner.joinLines(input_text);

            log.debug("About to process files");
            // Volam JTagger
            //JTagger.setPath(jarPath.getCanonicalPath());
            //TODO temp hack - problem reading JAR content:
            //JTagger.setPath("/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1");
            //JTagger.setPath(jarPathString, pathToWorkingDir);
            JTagger.setPath(jarPathString);
            log.info("Path to extracted jar file: {}", jarPathString);
            log.info("Path to working dir: {}", pathToWorkingDir);
            JTaggerResult res = JTagger.processFile(input_text, "nscr");
            //log.info("File {} processed", inputFile);
            //log.debug(res.getXml());

            //store result to a file
            PrintWriter out = new PrintWriter(outputJTaggerFilename, "UTF-8");
            out.print(res.getXml());
            out.flush();
            out.close();
            //log.info("File {} was generated as result of JTagger", outputJTaggerFilename);

        } catch (Exception e) {
            log.error("JTagger error {}", e.getLocalizedMessage());

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
