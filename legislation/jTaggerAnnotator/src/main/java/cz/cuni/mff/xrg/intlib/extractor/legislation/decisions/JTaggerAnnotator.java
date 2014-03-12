package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.JTagger;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.JTaggerResult;
import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.jTaggerCode.LineJoiner;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.odcs.commons.ontology.OdcsTerms;
import cz.cuni.mff.xrg.odcs.rdf.help.OrderTupleQueryResult;


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

        String pathToResources = "src" + File.separator + "main" + File.separator + "resources";
        
        //to get unzipped version of JAR
        String unzipedJarPathString = "";
        if (jarPathString.lastIndexOf(".jar") > 0) {
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

        //adjust permissions so that we can execute binaries of jTagger
        try {
            //chmod +x hunpos-tag, hunpos-train
            ///Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1/src/main/resources/tagger/hunpos-tag
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            log.debug("Executing: chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-train");
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }

        //prepare inputs, call xslt for each input
        //String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
         String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o} ORDER BY ?s ?o";
        log.debug("Query for getting input files: {}", query);
        //get the return values
        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
        //        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
        //OrderTupleQueryResult executeSelectQueryAsTuples = rdfInput.executeOrderSelectQueryAsTuples(query);
        OrderTupleQueryResult executeSelectQueryAsTuples = rdfInput.executeOrderSelectQueryAsTuples(query);

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


                String inputFilePath = pathToWorkingDir + File.separator + "input" + File.separator + String.valueOf(i) + ".txt";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "input" + File.separator);
         
                //TODO temp hack because there is a problem with perl scritp
                if (subject.contains("5-Tdo-271-2013")) {
                    log.warn("Skipping {}", subject);
                    continue;
                }
                
                File file;
                if (config.getMode().equals("nscr")) {
                    //store the input content to file, inputs are xml files!
                    file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath, Charset.forName("Cp1250"));
                } else if (config.getMode().equals("uscr")){
                    file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath, Charset.forName("UTF-8"));

                }
                else {
                    throw new DPUException("Unsupported Mode " + config.getMode());
                }
                    
                if (file == null) {
                    log.warn("Problem processing object for subject {}", subject);
                    continue;
                }

                //run jTagger
                String outputJTaggerFilename = pathToWorkingDir + File.separator + "outJTagger" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outJTagger" + File.separator);
                if (config.getMode().equals("nscr")) {
                    runJTagger(inputFilePath, outputJTaggerFilename, unzipedJarPathString, pathToWorkingDir, Charset.forName("Cp1250"));
                } 
                else if (config.getMode().equals("uscr")){
                    runJTagger(inputFilePath, outputJTaggerFilename, unzipedJarPathString, pathToWorkingDir, Charset.forName("UTF-8"));

                }
                else {
                    throw new DPUException("Unsupported Mode " + config.getMode());
                }

                //check output
                if (!outputGenerated(outputJTaggerFilename)) {
                    continue;
                }

                 log.info("Jtagger annotator finished successfully");

                //////////////////////
                //add meta and content elements to body
                //////////////////////
                String outputMetadataElement = pathToWorkingDir + File.separator + "outMetaElem" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outMetaElem" + File.separator);
                
                try {
                    addMetaAndContentElements(outputJTaggerFilename, outputMetadataElement,config.getMode());
                } catch(MetadataCreationException me) {
                    log.error("Problem when adding meta section, skipping file {}", subject);
                    log.debug(me.getLocalizedMessage());
                    continue;
                }

                //check output
                if (!outputGenerated(outputMetadataElement)) {
                    continue;
                }


                //////////////////////
                //add paragraph elements
                //////////////////////
                log.debug("About add paragraphs elements");
                String outputURIGeneratorFilenameWithParagraphs = pathToWorkingDir + File.separator + "outURIGenPara" + File.separator + String.valueOf(i) + ".xml";
                DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGenPara");
            
                runParagraphAdjustment(outputMetadataElement, outputURIGeneratorFilenameWithParagraphs);

                //check output
                if (!outputGenerated(outputURIGeneratorFilenameWithParagraphs)) {
                    continue;
                }

                   log.info("Metadata and paragraph elements generated, about to create output");
               
                
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
                
               log.info("Output created successfully");
                
     
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

        
        log.info("\n ****************************************************** \n FINISHING JTAGGER ANNOTATOR \n *****************************************************");











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

    private void addMetaAndContentElements(String inputFilePath, String outputFileName, String mode) throws MetadataCreationException {

        String input_text = null;
        input_text = DataUnitUtils.readFile(inputFilePath, StandardCharsets.UTF_8);



        String output = addMetaAndContentElementsWorker(input_text, mode);
        if (output == null) return; 

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

    private String addMetaAndContentElementsWorker(String input_string, String mode) throws MetadataCreationException {


        if (input_string == null) {
            log.warn("Input string is null, returning empty string from add Paragraph Elements");
            return "";
        }

        if (mode.equals("nscr")) {
            //NSOUD:
            //add metadata elem <body><metadata>... </metadata> USNESENI...
            int indexBody = input_string.indexOf("<body>") + "<body>".length();
            String before = input_string.substring(0, indexBody);

            String toBeProcessedTemp = input_string.substring(indexBody);

            String output = "";
            String content = toBeProcessedTemp;

            String label = "Kategorie rozhodnutí : ";
            if ((toBeProcessedTemp.indexOf(label) < 0)) {
                log.info("Label \"Kategorie rozhodnutí : \" was not found in the processed decision.");

                label = "Kategorie rozhodnutí: "; //try without the space before colon
                if ((toBeProcessedTemp.indexOf(label) < 0)) {
                    log.info("Label \"Kategorie rozhodnutí: \" was not found in the processed decision.");

                    label = "Kategorie rozhodnutí :"; //try without the space after the colon
                    if ((toBeProcessedTemp.indexOf(label) < 0)) {
                        log.info("Label \"Kategorie rozhodnutí :\" was not found in the processed decision.");

                        label = "Kategorie rozhodnutí:"; //try without the space before and after the colon
                        if ((toBeProcessedTemp.indexOf(label) < 0)) {
                            log.info("Label \"Kategorie rozhodnutí:\" was not found in the processed decision.");

                            log.warn("Label \"Kategorie rozhodnutí\" was not found in the processed decision, creation of metadata section will not work properly");
                            throw new MetadataCreationException("Meta element not created");
                        }

                    }

                }


            }



            String metadata = toBeProcessedTemp.substring(0, toBeProcessedTemp.indexOf(label)+label.length()+2); //to accommodate label plus the category letter to metadata.
            output = "<meta>" + addElemsToMetadata(metadata) + "</meta>";
            content = toBeProcessedTemp.substring(toBeProcessedTemp.indexOf(label)+label.length()+2);


            //add content elem </metadata> <content>USNESENI... </content></body>
            String toBeProcessed2 = content.substring(0, content.indexOf("</body>"));
            String output2 = "<content>" + toBeProcessed2 + "</content>";

            String after = content.substring(content.indexOf("</body>"));

            return before + output + output2 + after;
            
        }
        else if (mode.equals("uscr")) {
            //USOUD:
            //metadata element already prepared, just add content element! 
            
            
            int indexMeta= input_string.indexOf("</metadata>") + "</metadata>".length();
            String before = input_string.substring(0, indexMeta);

            String content = input_string.substring(indexMeta);
           
            //TODO remove the text before the body starts (header from the original content file) 
           
            String toBeProcessed = content.substring(0, content.indexOf("</body>"));
            String output2 = "<content>" + toBeProcessed + "</content>";

            String after = content.substring(content.indexOf("</body>"));

            return before + output2 +  after;
        }
        else {
            log.error("Wrong unsupported mode!!");
            return null;
        }
        
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
//        log.debug("****Para before {}", before);
//        log.debug("****Para output {}", toBeProcessed);
//        log.debug("****Para after {}", after);
//        log.debug("*****With Para {}", output);
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
            log.debug("File {} was generated as result of URI generator",
                    output);
            return true;
        }
    }

    private void runJTagger(String inputFile, String outputJTaggerFilename, String jarPathString, String pathToWorkingDir, Charset charset) {
        log.info("Jtagger is about to be run for file {}, path to unpacked jar with resources: {} ", inputFile, jarPathString);
        try {
            //process one file in the filesystem
            ///Users/tomasknap/Documents/PROJECTS/TACR/judikaty/data from web/nejvyssiSoud/rozhodnuti-3_Tdo_348_2013.txt

            //String inputFile = "/Users/tomasknap/Documents/PROJECTS/TACR/judikaty/data from web/nejvyssiSoud/rozhodnuti-3_Tdo_348_2013.txt";

            FileInputStream fis = new FileInputStream(inputFile);
            InputStreamReader inp = new InputStreamReader(fis, charset);//"cp1250");
            BufferedReader reader = new BufferedReader(inp);
            String input_text = "";
            String line = null;
            while ((line = reader.readLine()) != null) {
                input_text += line + "\n";
            }

            log.debug("About to join Lines");
            // Opravim riadkovanie
            input_text = LineJoiner.joinLines(input_text, config.getMode());
            if (input_text == null) {
                log.error("Problem processing file. Skipping this file.");
                return;
            }

            log.debug("About to process files");
            // Volam JTagger
            //JTagger.setPath(jarPath.getCanonicalPath());
            //TODO temp hack - problem reading JAR content:
            //JTagger.setPath("/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1");
            //JTagger.setPath(jarPathString, pathToWorkingDir);
            JTagger jtagger = new JTagger();
            
            jtagger.setPath(jarPathString);
           
            log.debug("Path to extracted jar file: {}", jarPathString);
             jtagger.setWorkingDir(pathToWorkingDir);
            log.debug("Path to working dir: {}", pathToWorkingDir);
            JTaggerResult res = jtagger.processFile(input_text, config.getMode());
          

            //store result to a file
            PrintWriter out = new PrintWriter(outputJTaggerFilename, "UTF-8");
            out.print(res.getXml());
            out.flush();
            out.close();

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
     
     
    //denote particular metadata with extra elements - used for reasoneToAppeal and concernedSources
    private String addElemsToMetadata(String metadata) {
        
         StringBuilder res = new StringBuilder();
        
         if (metadata.indexOf("Důvod dovolání :") > -1) {
            //duvod dovolani is present
            res.append(metadata.substring(0,metadata.indexOf("Důvod dovolání :")));
            res.append(" <reasons> " + metadata.substring(metadata.indexOf("Důvod dovolání :"),metadata.indexOf("Spisová značka :"))  + " </reasons> \n");
            
            if ((metadata.indexOf("Dotčené předpisy :") > -1)) {
                //dotcene predpisy is present
                res.append(metadata.substring(metadata.indexOf("Spisová značka :"), metadata.indexOf("Dotčené předpisy :")));
            }
            else {
                //no dotcene predpisy
                 log.info("Not found label: Dotčené předpisy : ");
                res.append(metadata.substring(metadata.indexOf("Spisová značka :"))); //append all and end
                return res.toString();
            }
     
         } else {
             log.info("Not found label: Důvod dovolání : ");
             if ((metadata.indexOf("Dotčené předpisy :") > -1)) {
                 //dotcene predpisy is present
                res.append(metadata.substring(0,metadata.indexOf("Dotčené předpisy :"))); //append everything till dotcene predpisy
                
                
             }
             else {
                  log.info("Not found label: Dotčené předpisy : ");
               
                return metadata;
             }
         }
         
         res.append(" <concernedSources> " + metadata.substring(metadata.indexOf("Dotčené předpisy :"),metadata.indexOf("Kategorie rozhodnutí :"))  + " </concernedSources> \n");
         res.append(metadata.substring(metadata.indexOf("Kategorie rozhodnutí :")));
        
     
        
        return res.toString();
        
        
        
    }
}
