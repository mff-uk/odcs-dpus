package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.configuration.Configurable;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitCreateException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitType;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.Repository;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@AsExtractor
public class Unzipper extends ConfigurableBase<UnzipperConfig> implements ConfigDialogProvider<UnzipperConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            Unzipper.class);

    public Unzipper() {
        super(UnzipperConfig.class);
    }
    @OutputDataUnit
    public RDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<UnzipperConfig> getConfigurationDialog() {
        return new UnzipperDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {

        log.info("\n ****************************************************** \n STARTING UNZIPPER \n *****************************************************");


        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();


        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        //*****************************
        //get data (zipped file) from target URL

        String tmpCourtFilesZipFile = pathToWorkingDir + File.separator + "data.zip";
        String tmpCourtFiles = pathToWorkingDir + File.separator + "unzipped";

        String urlWithZip = null;
        try {
            urlWithZip = buildURL(config,context.getLastExecutionTime());
        } catch (ConfigException ex) {
            log.error("DateFrom must be filled");
            context.sendMessage(MessageType.ERROR, "DateFrom must be filled");
            return;
        } 
        
        if (urlWithZip.isEmpty()) {
            //nothing to do
            log.info("No new files to be extracted");
            return;
        }
        


        if (!new File(tmpCourtFilesZipFile).exists()) {

            URL url = null;
            try {
                url = new URL(urlWithZip);

                log.info("About to download zip file {}", urlWithZip);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(tmpCourtFilesZipFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            } catch (MalformedURLException ex) {
                log.error("Malformed URL " + ex.getLocalizedMessage());
            } catch (IOException e) {
                log.error("Error storing zip file " + e.getLocalizedMessage());
            } finally {
                //TODO clean up
            }

        }

        //*****************************
        //UNZIP files
        log.debug("About to unzip {} ", tmpCourtFilesZipFile);

        try {
            if (!unzip(tmpCourtFilesZipFile, tmpCourtFiles)) {
                log.error("Archive was not unzipped, dpu is ending.");
                context.sendMessage(MessageType.ERROR, "Cannot unzip the file");
                return;
            }

        } catch (ZipException ex) {
            log.error("Unzip error, {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }




        //*****************************
        //OUTPUT
        int i = 0;
        for (File file : (new File(tmpCourtFiles)).listFiles()) {
            i++;

            if (i == config.getMaxExtractedDecisions()) {
                log.warn("The number of unzipped files is equal to max number of extracted decisions {}, thus certain decisions above this threshold may be thrown away", config.getMaxExtractedDecisions());
                log.info("If you need more decisions, please increase the max number of extracted decisions via configuration dialog");
            }

            //process each extracted file
            log.info("Processing fle name {}", file.getName());
            try {
                log.debug("Processing file with path: {}", file.getCanonicalPath().toString());
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
            }

            String output = null;
            try {
                output =  DataUnitUtils.readFile(file.getCanonicalPath().toString(), Charset.forName("Cp1250"));
            } catch (IOException ex) {
                Logger.getLogger(Unzipper.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (output == null) {
                log.warn("File {} cannot be read", file.getName());
                log.warn("File skipped");
            }
            log.debug("File was read, the content of the file: {}", output);


            //OUTPUT

            //////////////////////
            //adjust subject of the resulting triple from 
            //http://linked.opendata.cz/resource/legislation/cz/decision/rozhodnuti-11_Td_30_2013.txt to
            //http://linked.opendata.cz/resource/legislation/cz/decision/2011/22-cdo-1661-2011/expression
            //////////////////////
           
            
            String subject = config.getDecisionPrefix() + file.getName();
            String newSubjectPrefix = subject.substring(0, subject.lastIndexOf("/"));
            String decisionID = subject.substring(subject.lastIndexOf("-") + 1, subject.lastIndexOf(".txt")).replaceAll("_", "-");
            //String decisionID = prepare
          
            String year = decisionID.substring(decisionID.lastIndexOf("-") + 1);
            String newSubject = newSubjectPrefix + "/" + year + "/" + decisionID + "/expression";


            Resource subj = rdfOutput.createURI(newSubject);
            URI pred = rdfOutput.createURI(config.getOutputPredicate());
            Value obj = rdfOutput.createLiteral(output);


            String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
            log.debug("Prepared triple {}", preparedTriple);

            DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
            String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".txt";
            DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
            
            try {
                rdfOutput.addFromTurtleFile(new File(tempFileLoc));
                log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);

            } catch(cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException e) {
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

    private String buildURL(UnzipperConfig config, Date lastExecutionTime) throws ConfigException {

        String urlWithZip = "http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&query=%5Bdatum_predani_na_web%5D%3E%3D";

        String dateFrom = config.getDateFrom();
        String dateTo = config.getDateTO();

        if (config.isFromLastSuccess()) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
            
            Date dateToday = new Date();
             String today = dateFormat.format(dateToday);
            
            if (lastExecutionTime == null) {
                log.warn("No Last execution, processing decisions for today");
                dateFrom = today;
            }
            else  {
                
                Calendar c = Calendar.getInstance(); 
                c.setTime(lastExecutionTime); 
                c.add(Calendar.DATE, 1); //add one day
                Date startDate = c.getTime();
                
                if (startDate.after(dateToday)) {
                    log.info("Nothing to do, data for today was already processed");
                    return ""; //no new files to be extracted
                }
               
                 String last = dateFormat.format(startDate);
                 dateFrom = last;
            }
             dateTo = today;
           

            log.info("Getting decisions for the dates from {} to {}", dateFrom, dateTo);
        }
        else if (config.isCurrentDay()) {

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
            Date date = new Date();
            String today = dateFormat.format(date);

            dateFrom = today;
            dateTo = today;

            log.info("Getting decisions for the date {}", today);

        }
       
       



        if (dateFrom.isEmpty()) {
            throw new ConfigException("DateFrom must be filled");
        }

        urlWithZip += dateFrom.replaceAll("/", "%2F");
        if (!dateTo.isEmpty()) {
            urlWithZip += "%20AND%20%5Bdatum_predani_na_web%5D%3C%3D"; //%20
            urlWithZip += dateTo.replaceAll("/", "%2F");
        }
        urlWithZip += "&start=1&count=" + config.getMaxExtractedDecisions() + "&pohled=";

        log.debug(urlWithZip);
        return urlWithZip;


    }

    private static boolean unzip(String source, String destination) throws IOException, ZipException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                log.error("Zip encrypted");
                return false;
            }
            zipFile.extractAll(destination);
            return true;
        } catch (ZipException e) {
            log.error("Error {}", e.getLocalizedMessage());
            return false;
        }
    }

//    static String readFile(String path, Charset encoding)
//            throws IOException {
//        byte[] encoded = Files.readAllBytes(Paths.get(path));
//        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
//    }
}
