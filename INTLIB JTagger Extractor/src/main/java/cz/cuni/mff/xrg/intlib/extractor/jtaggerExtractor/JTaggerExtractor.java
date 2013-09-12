package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor;

//import cz.cuni.mff.ksi.intlib.jtagger.JTagger;
//import cz.cuni.mff.ksi.intlib.jtagger.JTaggerResult;
//import cz.cuni.mff.ksi.intlib.jtagger.LineJoiner;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.JTagger;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.JTaggerResult;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.LineJoiner;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.IntLibLink;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.dpu.DPU;
import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
import cz.cuni.xrg.intlib.commons.dpu.DPUException;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.logging.Level;
import javax.xml.transform.stream.StreamSource;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 * 
 * @author tomasknap
 */
@AsExtractor
public class JTaggerExtractor 
        extends ConfigurableBase<JTaggerExtractorConfig> 
        implements DPU, ConfigDialogProvider<JTaggerExtractorConfig> {

    private static final Logger logger = LoggerFactory.getLogger(JTaggerExtractor.class);

    @OutputDataUnit
    private RDFDataUnit outputRepository;
    
    public JTaggerExtractor(){
            super(JTaggerExtractorConfig.class);
        }
    
    @Override
    public AbstractConfigDialog<JTaggerExtractorConfig> getConfigurationDialog() {
        return new JTaggerExtractorDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException {

        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();

                    
        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        //get path JAR file, so that resources (such as perl script can be read)
        //File jarPath = context.getJarPath();
        String jarPathString = null;
        try {
            jarPathString = context.getJarPath().getCanonicalPath();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JTaggerExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //to get unzipped version of JAR
        String unzipedJarPathString = jarPathString.substring(0, jarPathString.lastIndexOf(".jar"));
        String pathToResources = "src" + File.separator + "main" + File.separator + "resources";
           //extract jar file to get to the resources? remove temp hack later when setting path for JTagger
        logger.info("About to unzip {} to {} so that resources in JAR are accessible", jarPathString, unzipedJarPathString);
        try {
            unzip(jarPathString, unzipedJarPathString);
        } catch (ZipException ex) {
            logger.error("Unzip error, {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            logger.error("Error:: " + ex.getLocalizedMessage());
        }

        //needed to execute binaries
        try {
            //chmod +x hunpos-tag, hunpos-train
            ///Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1/src/main/resources/tagger/hunpos-tag
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            logger.info("Executing: chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-tag");
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + File.separator + pathToResources + File.separator + "tagger" + File.separator + "hunpos-train");
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }


     

        //*****************************
        //get data (zipped file) from target URL 

        String tmpCourtFilesZipFile = pathToWorkingDir + File.separator + "data.zip";
        String tmpCourtFiles = pathToWorkingDir + File.separator + "unzipped";

        String urlWithZip = buildURL(config);

        URL url = null;
        try {
            url = new URL(urlWithZip);

            logger.info("About to download zip file {}", urlWithZip);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(tmpCourtFilesZipFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        } catch (MalformedURLException ex) {
            logger.error("Malformed URL " + ex.getLocalizedMessage());
        } catch (IOException e) {
            logger.error("Error storing zip file " + e.getLocalizedMessage());
        } finally {
            //TODO clean up
        }

        //*****************************
        //UNZIP files
        logger.info("About to unzip {} ", tmpCourtFilesZipFile);
        try {
            unzip(tmpCourtFilesZipFile, tmpCourtFiles);
        } catch (ZipException ex) {
            logger.error("Unzip error, {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }


        //******************************
        //get the Directory with files to be processed and process them
        File filesToProcess = new File(tmpCourtFiles);

        //check existence of directories
        checkExistanceOf(pathToWorkingDir + File.separator + "outJTagger" + File.separator);
        checkExistanceOf(pathToWorkingDir + File.separator + "outURIGen" + File.separator);
         checkExistanceOf(pathToWorkingDir + File.separator + "outXSLT" + File.separator);
        checkExistanceOf(pathToWorkingDir + File.separator + "outURIGenPara" + File.separator);

        
        //where the resources within unzipped jar files are located.
        String unzipedJarPathStringResources = unzipedJarPathString + File.separator + pathToResources;
     
        //config for URI generator
        String configURiGen = unzipedJarPathStringResources + File.separator + "uriGenConfig.xml";

        //XSLT template for data creation
        String xsltTemplate = unzipedJarPathStringResources + File.separator + "xmlToRDFSimple.xslt";
   
        for (String file : filesToProcess.list()) {
            logger.info("Processing file: {}", file);

            //run jTagger
            String outputJTaggerFilename = pathToWorkingDir + File.separator + "outJTagger" + File.separator + file;
            runJTagger(tmpCourtFiles + File.separator + file, outputJTaggerFilename, unzipedJarPathString, pathToWorkingDir);

              //check output
                if (!outputGenerated(outputJTaggerFilename)) {
                    continue;
                }
            
            //run URI Generator
            String outputURIGeneratorFilename = pathToWorkingDir + File.separator + "outURIGen" + File.separator + file;
            runURIGenerator(outputJTaggerFilename, outputURIGeneratorFilename, configURiGen, context);       
            
                //check output
                if (!outputGenerated(outputURIGeneratorFilename)) {
                    continue;
                }
                        
            
            
            //////////////////////
            //add paragraph elements?
            //////////////////////
            logger.info("About add paragraphs elements");
            String outputURIGeneratorFilenameWithParagraphs = pathToWorkingDir + File.separator + "outURIGenPara" + File.separator + file;
            runParagraphAdjustment(outputURIGeneratorFilename,outputURIGeneratorFilenameWithParagraphs,context);
             
            //check output
                if (!outputGenerated(outputURIGeneratorFilenameWithParagraphs)) {
                    continue;
                }
            
            
            //run XSLT
            logger.info("About to run xslt");
            String outputXSLT = pathToWorkingDir + File.separator + "outXSLT" + File.separator + file;
            if (runXSLT(outputURIGeneratorFilenameWithParagraphs, outputXSLT, xsltTemplate)) {
                logger.info("About to write result {} to output", outputXSLT);
                try {
                    outputRepository.addFromTurtleFile(new File(outputXSLT));
                    //outputRepository.extractfromFile(FileExtractType.PATH_TO_FILE, outputXSLT, "", "", false, false);
                } catch (RDFException ex) {
                    logger.error("Problems with adding RDF data to output data unit", ex.getLocalizedMessage());
                }
            } else {
                logger.error("Problems with resulting RDF data, skipping this file");
            }
        }

    }

    private void runJTagger(String inputFile, String outputJTaggerFilename, String jarPathString, String pathToWorkingDir) {
        logger.info("Jtagger is about to be run for file {}, path to unpacked jar with resources: {} ", inputFile, jarPathString);
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

            logger.debug("About to join Lines");
            // Opravim riadkovanie
            input_text = LineJoiner.joinLines(input_text);

            logger.debug("About to process files");
            // Volam JTagger
            //JTagger.setPath(jarPath.getCanonicalPath());
            //TODO temp hack - problem reading JAR content:
            //JTagger.setPath("/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1");
          //JTagger.setPath(jarPathString, pathToWorkingDir);
            JTagger.setPath(jarPathString);
            logger.info("Path to extracted jar file: {}", jarPathString);
             logger.info("Path to working dir: {}", pathToWorkingDir);
            JTaggerResult res = JTagger.processFile(input_text, "nscr");
            //logger.info("File {} processed", inputFile);
            //logger.debug(res.getXml());

            //store result to a file
            PrintWriter out = new PrintWriter(outputJTaggerFilename, "UTF-8");
            out.print(res.getXml());
            out.flush();
            out.close();
            //logger.info("File {} was generated as result of JTagger", outputJTaggerFilename);

        } catch (Exception e) {
            logger.error("JTagger error {}", e.getLocalizedMessage());

        }
    }

    private void runURIGenerator(String file, String output, String configURiGen, DPUContext context) {
        logger.info("About to run URI generator for {}", file);
        IntLibLink.processFiles(file, output, configURiGen,context);
        
        
    }

    private boolean runXSLT(String file, String outputXSLT, String template) {
        logger.info("About to run XSLT on {}", file);
        //RUN Simple XSLT to create RDF
        //String xsltFile = "/Users/tomasknap/Documents/PROJECTS/TACR/2012_tacr_playground/RDFConvertorJudikaty/xmlToRDFSimple.xslt";

        //String outputXSLTFilename = pathToWorkingDir + "/output.ttl";
        File stylesheet = new File(template);
        File inputFile = new File(file);
        File outputFile = new File(outputXSLT);
        boolean res = convertToRDF(inputFile, outputFile, stylesheet);
        logger.info("File {} is output of XSLT", outputXSLT);
        return res;

    }

    private boolean convertToRDF(File inputFile, File outputFile, File stylesheet) {
        Processor proc = new Processor(false);
        XsltCompiler compiler = proc.newXsltCompiler();
        XsltExecutable exp;
        try {
            exp = compiler.compile(new StreamSource(stylesheet));


            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(inputFile));



            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "text");
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            out.setOutputFile(outputFile);

            XsltTransformer trans = exp.load();

            trans.setInitialContextNode(source);
            trans.setDestination(out);
            trans.transform();

            return true;

        } catch (SaxonApiException ex) {
            logger.error(ex.getLocalizedMessage());
            return false;
        } catch (Exception ex) {
            logger.error("Error processing file {}, {}", inputFile, ex.getLocalizedMessage());
            return false;
        }


    }

    private static void unzip(String source, String destination) throws IOException, ZipException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                logger.error("Zip encrypted");
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            logger.error("Error {}", e.getLocalizedMessage());
        }
    }

    private void checkExistanceOf(String file) {
        if (new File(file).mkdirs()) {
            logger.debug("Dir {} created", file);
        } else {
            logger.debug("Dir {} NOT created, could have already exist", file);
        }
    }

    private String buildURL(JTaggerExtractorConfig config) {

        String urlWithZip = "http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&query=%5Bdatum_predani_na_web%5D%3E%3D";

        urlWithZip += config.getDateFrom().replaceAll("/", "%2F");
        if (!config.getDateTO().isEmpty()) {
            urlWithZip += "%20AND%20%5Bdatum_predani_na_web%5D%3C%3D"; //%20
            urlWithZip += config.getDateTO().replaceAll("/", "%2F");
        }
        //urlWithZip += "&start=1&count=15&pohled=";
         urlWithZip += "&start=1&count="+ JTaggerExtractorConfig.maxExtractedDecisions + "&pohled=";
        //urlWithZip += "&SearchMax=1000&Start=1&Count=15&pohled=1";

        logger.info(urlWithZip);
        //String encodedURL = URLEncoder.encode(urlWithZip);
        //logger.info(encodedURL);
        return urlWithZip;


//        String urlWithZip = "http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&";
//        urlWithZip += "query=%5Bdatum_predani_na_web%5D%3E%3D15%2F07%2F2013&start=1&count=15&pohled=";
//        return urlWithZip;

        // return urlWithZip;

    }

    //add paragraph elements wrapping paragraphs within <body> section
    private String addParagraphElemenets(String input_string) {
        
        if (input_string == null) {
            logger.warn("Input string is null, returning empty string from add Paragraph Elements");
            return "";
        }
        
        //add paragraphs only to the content of the body elem:
        int indexBody = input_string.indexOf("<body>")+6;
        String before = input_string.substring(0,indexBody);
        String toBeProcessedTemp = input_string.substring(indexBody);
        //String toBeProcessed = input_string.substring(indexBody, input_string.indexOf("</body>") - indexBody);
        String toBeProcessed = toBeProcessedTemp.substring(0,toBeProcessedTemp.indexOf("</body>"));
        String after = toBeProcessedTemp.substring(toBeProcessedTemp.indexOf("</body>"));
        
       
        
        //to ensure that empty lines are really empty (no hidden spaces, tabs, ..)
        String output = toBeProcessed.replaceAll("([^\n]+)\n", "\n<paragraph>$1</paragraph>\n");
        
        //logger.info("Para orig {}", input_string);
        logger.info("****Para before {}", before);
        logger.info("****Para output {}", toBeProcessed);
        logger.info("****Para after {}", after);
         logger.info("*****With Para {}", output);
        //logger.info("Para all {}", before + output + after);
        
        
        //to remove \n from lines which do not end with "." (line in the paragraph) and new line char (empty line between paragraphs)
        //If they end with dots, \n is not removed if the new line character is followed by space, tab or next new line character, 
        //because this denotes new paragraph and in this case new line char is ok.  
        //output = output.replaceAll("([^.\n])\n([^\\s])", "$1$2");
        
        return before + output + after;
        
    
        
    }

    private void runParagraphAdjustment(String outputURIGeneratorFilename, String outputURIGeneratorFilenameWithParagraphs, DPUContext context) {

      
        
      String input_text = null;
        try {
            input_text = readFile(outputURIGeneratorFilename, StandardCharsets.UTF_8 );
            //      FileInputStream fis = null;
            //
            //
            //            try {
            //                fis = new FileInputStream(outputURIGeneratorFilename);
            //            } catch (FileNotFoundException ex) {
            //                logger.error(ex.getLocalizedMessage());
            //            }
            //            InputStreamReader inp = null;
            //            try {
            //                inp = new InputStreamReader(fis,"UTF-8");
            //            } catch (UnsupportedEncodingException ex) {
            //                logger.error("Encoding {}", ex.getLocalizedMessage());
            //            }
            //
            //            //InputStreamReader inp = new InputStreamReader(fis, "UTF-8");
            //            BufferedReader reader = new BufferedReader(inp);
            //            String input_text = "";
            //            String line = null;
            //            try {
            //                while ((line = reader.readLine()) != null) {
            //                    input_text += line + "\n";
            //                }
            //
            //                ///////
            //            }
            //                logger.error(ex.getLocalizedMessage());
            //            }
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }
            
            
            String output = addParagraphElemenets(input_text);
                              
            PrintWriter out = null;
            try {
                out = new PrintWriter(outputURIGeneratorFilenameWithParagraphs, "UTF-8");
            } catch (FileNotFoundException ex) {
                logger.error(ex.getLocalizedMessage());
            } catch (UnsupportedEncodingException ex) {
            logger.error(ex.getLocalizedMessage());
        }
            out.write(output);
            out.flush();
            out.close();
            
             
//        writer = new PrintWriter("/tmp/jtagger/txt_source/judikatura.zakon.txt", "UTF-8");
//        writer.println(text);
//        writer.close();
            
            //////////////////
    }
    
    static String readFile(String path, Charset encoding) 
  throws IOException 
{
  byte[] encoded = Files.readAllBytes(Paths.get(path));
  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
}
    
    
    private boolean outputGenerated(String output) {
		File f = new File(output);
		if (!f.exists()) {
			logger.warn("File {} was not created", output);
			logger.warn("Skipping rest of the steps for the given file");
			return false;
		} else {
			logger.info("File {} was generated as result of URI generator",
					output);
			return true;
		}
	}

    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
