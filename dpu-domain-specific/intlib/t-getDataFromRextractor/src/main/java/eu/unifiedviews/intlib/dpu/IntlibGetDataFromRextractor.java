package eu.unifiedviews.intlib.getdatafromrextractor;

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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsExtractor
public class IntlibGetDataFromRextractor extends DpuAdvancedBase<IntlibGetDataFromRextractorConfig_V1> {

	private static final Logger log = LoggerFactory.getLogger(IntlibGetDataFromRextractor.class);

        
        
        
         @DataUnit.AsOutput(name = "fileOutput")
    public WritableFilesDataUnit filesOutput;
		
	public IntlibGetDataFromRextractor() {
		super(IntlibGetDataFromRextractorConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        log.info("DPU is running ...");
            
        File workingDir = context.getWorkingDir();
        
        RextractorClient rc = new RextractorClient(config.getTargetRextractorServer());
        
        //prepare dateFrom, dateTo if the option "last7days" is checked
        String dateFrom;
        String dateTo;
        if (config.isLast7Days()) {
            //compute start
           dateTo= getCurrentTimeStamp();
           dateFrom = getCurrentTimeStampPlusXDays(-3);

            
        } else {
            dateFrom = config.getDateFrom();
            dateTo = config.getDateTo();
        }
        context.sendMessage(DPUContext.MessageType.INFO, "Downloading data for date range: " + dateFrom + " - " + dateTo);
        
        //get list of files
        List<File> files = null;
            try {
                files = rc.prepareFiles(workingDir.getCanonicalPath().toString(), dateFrom, dateTo);
            } catch (IOException ex) {
                log.error("Problem preparing files: {}", ex.getLocalizedMessage());
        }
              
        //add each file to the output
               int i = 0;
            int processedSuccessfully = 0;
        for (File f : files) {
            i++;
         try {
                filesOutput.addExistingFile(f.getName(), f.toURI().toASCIIString());
                VirtualPathHelpers.setVirtualPath(filesOutput, f.getName(), f.getName());
                processedSuccessfully++;
            } catch (DataUnitException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Problem with DataUnit", null, ex);
            }
        }
        context.sendMessage(DPUContext.MessageType.INFO, "Successfully prepared " + processedSuccessfully +" files from downloaded " + i + " files");
        
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibGetDataFromRextractorVaadinDialog();
    }
    
    
    private String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
}

    private Date addDays(Date d, int days)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DATE, days);
        
        //d.setTime( c.getTime().getTime() );
        Date newDate = new Date(c.getTime().getTime());
        return newDate;
    }   
    
    private String getCurrentTimeStampPlusXDays(int daysAdded) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");//dd/MM/yyyy
        Date now = new Date();
        Date newDate = addDays(now, daysAdded);
        String strDate = sdfDate.format(newDate);
        return strDate;
        
    }
    
    
    
   	
}
