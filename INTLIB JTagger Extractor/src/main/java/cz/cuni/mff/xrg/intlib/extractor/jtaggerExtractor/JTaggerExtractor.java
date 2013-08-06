package cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor;

//import cz.cuni.mff.ksi.intlib.jtagger.JTagger;
//import cz.cuni.mff.ksi.intlib.jtagger.JTaggerResult;
//import cz.cuni.mff.ksi.intlib.jtagger.LineJoiner;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.JTagger;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.JTaggerResult;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.jTagger.LineJoiner;
import cz.cuni.mff.xrg.intlib.extractor.jtaggerExtractor.uriGenerator.IntLibLink;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;

import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.commons.extractor.ExtractContext;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.transformer.TransformException;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.enums.FileExtractType;
import cz.cuni.xrg.intlib.rdf.exceptions.RDFException;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataRepository;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
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
public class JTaggerExtractor extends ConfigurableBase<JTaggerExtractorConfig> implements Extract, ConfigDialogProvider<JTaggerExtractorConfig> {

    private static final Logger logger = LoggerFactory.getLogger(JTaggerExtractor.class);

    @Override
    public AbstractConfigDialog<JTaggerExtractorConfig> getConfigurationDialog() {
        return new JTaggerExtractorDialog();
    }

    @Override
    public void extract(ExtractContext context) throws ExtractException {

        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();

        //String pathToWorkingDir = "/tmp/intlib";
        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JTaggerExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (new File(pathToWorkingDir).mkdirs()) {
            logger.debug("Dir {} created", pathToWorkingDir);
        } else {
            logger.warn("Dir {} NOT created, could have already exist", pathToWorkingDir);
        }

        //get path JAR file, so that resources (such as perl script can be read)
        //File jarPath = context.getJarPath();
        String jarPathString = null;
        try {
            jarPathString = context.getJarPath().getCanonicalPath();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JTaggerExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        String unzipedJarPathString = jarPathString.substring(0, jarPathString.lastIndexOf(".jar"));
        String unzipedJarPathStringResources = unzipedJarPathString + "/src/main/resources";
        //extract jar file to get to the resources? remove temp hack later when setting path for JTagger
        logger.debug("About to unzip {} to {} so that resources in JAR are accessible", jarPathString, unzipedJarPathString);
        try {
            unzip(jarPathString, unzipedJarPathString);
        } catch (ZipException ex) {
            logger.error("Unzip error, {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            logger.error(ex.getLocalizedMessage());
        }

        //needed to execute binaries
        try {
            //chmod +x hunpos-tag, hunpos-train
            ///Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1/src/main/resources/tagger/hunpos-tag
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + "/src/main/resources/tagger/hunpos-tag");
            Runtime.getRuntime().exec("chmod +x " + unzipedJarPathString + "/src/main/resources/tagger/hunpos-train");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JTaggerExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }


        //*****************************
        //Prepare OUTPUTS - copy prepare file to output data unit
        RDFDataRepository outputRepository;
        try {
            outputRepository = (RDFDataRepository) context.addOutputDataUnit(DataUnitType.RDF, "output");
        } catch (DataUnitCreateException e) {
            throw new ExtractException("Can't create DataUnit", e);
        }







        //*****************************
        //get data (zipped file) from target URL 

        String tmpCourtFilesZipFile = pathToWorkingDir + "/data.zip";
        String tmpCourtFiles = pathToWorkingDir + "/unzipped";

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

        //my
//        http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&query=
//        %5Bdatum_predani_na_web%5D%3E%3D&start=1&count=15&pohled= 
//        //correct
//        http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&query=
//        %5Bdatum_predani_na_web%5D%3E%3D02%2F07%2F2013%20AND%20%5Bdatum_predani_na_web
//                %5D%3C%3D09%2F07%2F2013&start=1&count=15&pohled=
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
        checkExistanceOf(pathToWorkingDir + "/outJTagger/");
        checkExistanceOf(pathToWorkingDir + "/outURIGen/");
        checkExistanceOf(pathToWorkingDir + "/outXSLT/");

        //config for URI generator
        String configURiGen = unzipedJarPathStringResources + "/uriGenConfig.xml";

        //XSLT template for data creation
        String xsltTemplate = unzipedJarPathStringResources + "/xmlToRDFSimple.xslt";

        for (String file : filesToProcess.list()) {
            logger.info("Processing file: {}", file);

            //run jTagger
            String outputJTaggerFilename = pathToWorkingDir + "/outJTagger/" + file;
            runJTagger(tmpCourtFiles + File.separator + file, outputJTaggerFilename);

            //run URI Generator
            String outputURIGeneratorFilename = pathToWorkingDir + "/outURIGen/" + file;
            runURIGenerator(outputJTaggerFilename, outputURIGeneratorFilename, configURiGen, context);

            //run XSLT
            String outputXSLT = pathToWorkingDir + "/outXSLT/" + file;
            // String template =  //String xsltFile = "/Users/tomasknap/Documents/PROJECTS/TACR/2012_tacr_playground/RDFConvertorJudikaty/xmlToRDFSimple.xslt";

            if (runXSLT(outputURIGeneratorFilename, outputXSLT, xsltTemplate)) {
                logger.info("About to write result {} to output", outputXSLT);
                try {
                    outputRepository.extractFromLocalTurtleFile(outputXSLT);
                    //outputRepository.extractfromFile(FileExtractType.PATH_TO_FILE, outputXSLT, "", "", false, false);
                } catch (RDFException ex) {
                    logger.error("Problems with adding RDF data to output data unit", ex.getLocalizedMessage());
                }
            } else {
                logger.error("Problems with resulting RDF data, skipping this file");
            }
            //TODO hack just one file
            //break;
        }

    }

    private void runJTagger(String inputFile, String outputJTaggerFilename) {
        logger.debug("Jtagger is about to be run for file {}", inputFile);
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
            JTagger.setPath("/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/target/dpu/JTagger_Extractor-0.0.1");
            JTaggerResult res = JTagger.processFile(input_text, "nscr");
            //logger.info("File {} processed", inputFile);
            //logger.debug(res.getXml());

            //store result to a file
            PrintWriter out = new PrintWriter(outputJTaggerFilename, "UTF-8");
            out.print(res.getXml());
            out.flush();
            out.close();
            logger.info("File {} was generated as result of JTagger", outputJTaggerFilename);

        } catch (Exception e) {
            logger.error("JTagger error {}", e.getLocalizedMessage());

        }
    }

    private void runURIGenerator(String file, String output, String configURiGen, ExtractContext context) {
        logger.info("About to run URI generator for {}", file);
        IntLibLink.processFiles(file, output, configURiGen,context);
        
        logger.info("File {} was generated as result of URI generator", output);
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
            logger.warn("Dir {} NOT created, could have already exist", file);
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
}
