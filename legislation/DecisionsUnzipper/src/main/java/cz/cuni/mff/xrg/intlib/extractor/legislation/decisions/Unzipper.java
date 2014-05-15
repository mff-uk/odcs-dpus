package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.utils.UnzipException;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@AsExtractor
public class Unzipper extends ConfigurableBase<UnzipperConfig> implements ConfigDialogProvider<UnzipperConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(
            Unzipper.class);
    
    private String dateFrom;
    private String dateTo;

    public Unzipper() {
        super(UnzipperConfig.class);
    }
	
    @OutputDataUnit(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<UnzipperConfig> getConfigurationDialog() {
        return new UnzipperDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {

        //log.info("\n ****************************************************** \n STARTING UNZIPPER \n *****************************************************");


        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();


        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
        //*****************************
        //get data (zipped file) from target URL

        String tmpCourtFilesZipFile = pathToWorkingDir + File.separator + "data.zip";
        String tmpCourtFiles = pathToWorkingDir + File.separator + "unzipped";

        String urlWithZip;
        try {
            urlWithZip = buildURL(config,context.getLastExecutionTime());
            context.sendMessage(MessageType.INFO, "Running for dates: " + dateFrom + " - " + dateTo);
        } catch (ConfigException ex) {
            //log.error("Problem when building URL to be downloaded: " + ex.getLocalizedMessage());
            context.sendMessage(MessageType.ERROR, "Problem when building URL to be downloaded: " + ex.getLocalizedMessage());
            return;
        } 
        
        if (urlWithZip.isEmpty()) {
            //nothing to do
            LOG.info("No new files to be extracted");
            return;
        }
        
        if (!new File(tmpCourtFilesZipFile).exists()) {
            URL url;
            try {
                url = new URL(urlWithZip);

                LOG.info("About to download zip file {}", urlWithZip);
                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(tmpCourtFilesZipFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            } catch (MalformedURLException ex) {
                LOG.error("Malformed URL " + ex.getLocalizedMessage());
            } catch (IOException e) {
                LOG.error("Error storing zip file " + e.getLocalizedMessage());
            } finally {
                //TODO clean up
            }

        }

//        //Check that the file exists
//        if (!Files.exists(new File(tmpCourtFilesZipFile).toPath())) {
//             //TODO check whether there are only Saturdays and Sundays. If yes, it is ok that nothing was downloaded
//             context.sendMessage(MessageType.WARNING, "No data to be unzipped. Extractor is ending.", "Possible reason: No data available for the chosen dates: " + dateFrom + " - " + dateTo);
//             return;
//        }
        
        //*****************************
        //UNZIP files
        LOG.info("About to unzip {} ", tmpCourtFilesZipFile);

        try {
            unzip(tmpCourtFilesZipFile, tmpCourtFiles);              
 

        } catch (UnzipException ex) {
            
             //log.warn("Archive was not unzipped, dpu is ending.");
             context.sendMessage(MessageType.WARNING, "No data to be unzipped, DPU is ending.", "It was run for dates: " + dateFrom + " - " + dateTo + ". The original problem was: " + ex.getLocalizedMessage() + ". Possible reason: No data available for the chosen dates");
             return;
        }
      




        //*****************************
        //OUTPUT
        int i = 0;
		
		final SimpleRdfWrite rdfOutputWrap = new SimpleRdfWrite(rdfOutput, context);	
		final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();
		
        for (File file : (new File(tmpCourtFiles)).listFiles()) {
            i++;

            if (i == config.getMaxExtractedDecisions()) {
                LOG.warn("The number of unzipped files is equal to max number of extracted decisions {}, thus certain decisions above this threshold may be thrown away", config.getMaxExtractedDecisions());
                LOG.info("If you need more decisions, please increase the max number of extracted decisions via configuration dialog");
            }

            //process each extracted file
            LOG.info("Processing fle name {}", file.getName());
            try {
                LOG.debug("Processing file with path: {}", file.getCanonicalPath().toString());
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage());
            }

            String output = null;
            try {
                output =  DataUnitUtils.readFile(file.getCanonicalPath().toString(), Charset.forName("Cp1250"));
            } catch (IOException ex) {
                LOG.error("Failed to read file", ex);
            }

            if (output == null) {
                LOG.warn("File {} cannot be read", file.getName());
                LOG.warn("File skipped");
            }
            LOG.debug("File was read, the content of the file: {}", output);


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
            
            //adjust the subject based on the latest adjustment:\
            // add /cz/decision/2010/22-Cdo-4430-2010/cs
            newSubject = newSubject + "/cz/decision/" + year + "/" + decisionID + "/cs";

            Resource subj = valueFactory.createURI(newSubject);
            URI pred = valueFactory.createURI(config.getOutputPredicate());
            Value obj = valueFactory.createLiteral(output);


            String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
            LOG.debug("Prepared triple {}", preparedTriple);

            DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
            String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".txt";
            DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
            
			try {
				rdfOutputWrap.extract(new File(tempFileLoc), RDFFormat.TURTLE, null);
				LOG.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
			} catch (OperationFailedException e) {
				LOG.warn("Error parsing file for subject {}, exception {}", subj, e.getLocalizedMessage());
				LOG.info("Continues with the next file");
			}
			
			if (context.canceled()) {
				LOG.info("DPU cancelled");
				return;
			}
		}
        LOG.info("Processed {} files", i);
    }

    private String buildURL(UnzipperConfig config, Date lastExecutionTime) throws ConfigException {

        String urlWithZip = "http://www.nsoud.cz/Judikatura/judikatura_ns.nsf/zip?openAgent&query=%5Bdatum_predani_na_web%5D%3E%3D";

       dateFrom = config.getDateFrom();
       dateTo = config.getDateTO();

        if (config.isFromLastSuccess()) {
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
            
            //Date dateToday = new Date();
             //String today = dateFormat.format(dateToday);
             
            //get yesterday's date
                Calendar yesterdayCal = Calendar.getInstance(); 
                yesterdayCal.setTime(new Date()); //today
                yesterdayCal.add(Calendar.DATE, -1); //get the previous day
                Date dateYesterday= yesterdayCal.getTime();
                String yesterday = dateFormat.format(dateYesterday);
            
            //last exec 
            Calendar lastExecCal = Calendar.getInstance(); 
                lastExecCal.setTime(lastExecutionTime); 
             
            
            if (lastExecutionTime == null) {
                LOG.warn("No Last execution, processing decisions for yesterday");
                dateFrom = yesterday;
            }
            else  {
                       
                 String last = dateFormat.format(lastExecutionTime);
                 dateFrom = last;
            }
             
            dateTo = yesterday;
             
//            //TODO check that dataFrom is not > dateTo (in terms of days), if yes, end. 
//              if (lastExecutionTime.after(dateYesterday)) { //ale after o den!!
//                    log.info("Nothing to do, data up to yesterday was already processed");
//                    return ""; //no new files to be extracted
//                }
           

            LOG.info("Getting decisions for the dates from {} to {}", dateFrom, dateTo);
        }
        else if (config.isCurrentDay()) {

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
            
             //get yesterday's date
                Calendar c = Calendar.getInstance(); 
                c.setTime(new Date()); //today
                c.add(Calendar.DATE, -1); //get the previous day
                Date dateYesterday= c.getTime();
                String yesterday = dateFormat.format(dateYesterday);
                
//            Date date = new Date();
//            String today = dateFormat.format(date);

            dateFrom = yesterday;
            dateTo = yesterday;

            LOG.info("Getting decisions for the date {}", yesterday);

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

        LOG.debug(urlWithZip);
       
        return urlWithZip;


    }

    private static void unzip(String source, String destination) throws UnzipException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
        
                throw new UnzipException("Zip encrypted");
            }
            zipFile.extractAll(destination);
           
        } catch (ZipException e) {
          
            throw new UnzipException(e.getLocalizedMessage());
        }
    }

//    static String readFile(String path, Charset encoding)
//            throws IOException {
//        byte[] encoded = Files.readAllBytes(Paths.get(path));
//        return encoding.decode(ByteBuffer.wrap(encoded)).toString();
//    }
}
