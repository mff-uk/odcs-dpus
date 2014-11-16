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
import java.util.ArrayList;
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
        
        RextractorClient rc = new RextractorClient();
        
        //get list of files
        List<File> files = null;
            try {
                files = rc.prepareFiles(workingDir.getCanonicalPath().toString(), config.getDateFrom(), config.getDateTo());
            } catch (IOException ex) {
                log.error("Problem preparing files: {}", ex.getLocalizedMessage());
        }
              
        //add each file to the output
        for (File f : files) {
         try {
                filesOutput.addExistingFile(f.getName(), f.toURI().toASCIIString());
                VirtualPathHelpers.setVirtualPath(filesOutput, f.getName(), f.getName());
            } catch (DataUnitException ex) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Problem with DataUnit", null, ex);
            }
        }
        
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibGetDataFromRextractorVaadinDialog();
    }
    
    
    
   	
}
