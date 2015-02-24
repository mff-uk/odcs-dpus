package eu.unifiedviews.intlib.senddatatorextractor;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsLoader
public class IntlibSendDataToRextractor extends DpuAdvancedBase<IntlibSendDataToRextractorConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(IntlibSendDataToRextractor.class);
        
        @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;
		
	public IntlibSendDataToRextractor() {
		super(IntlibSendDataToRextractorConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        LOG.info("DPU is running ...");
        
           RextractorClientSender rc = new RextractorClientSender(config.getTargetRextractorServer());
        
           // RepositoryConnection connection = null;
        try {
            
            //FIRST ITERATION to process xml files and put them on the output
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }

            //iterate over files 
             int processedAll = 0;
             int processedSuccessfully = 0;
             int processedWithError = 0;
            while (filesIteration.hasNext()) {

                processedAll++; 
                FilesDataUnit.Entry entry = filesIteration.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());

                
                //get URI from resource
                String fileURIString = entry.getFileURIString();
                LOG.debug("File URI is: {}", fileURIString);
                    
                String filePath = fileURIString.substring("file:".length());
                
                
               
                    Path p = Paths.get(filePath);
                   LOG.debug("Path to file: {}", p.toString());
                   String decodedPathString;
                    try {
                        decodedPathString = URLDecoder.decode(p.toString(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                          LOG.error("Problem decoding file name {}, {}", p.toString(), ex.getLocalizedMessage());
                          LOG.info("This entry is skipped");
                          processedWithError++;
                          continue;
                    }
                   Path decodedPath = Paths.get(decodedPathString);
                   LOG.debug("Decoded path to file: {}", decodedPath.toString());
//                   object = new String(Files.readAllBytes(decodedPath), StandardCharsets.UTF_8);
                
               
                int resultCode = rc.sendFile(entry.getSymbolicName(), decodedPathString);
                if (resultCode == 1) {
                    LOG.warn("Problem submitting file to rextractor queue - possible cause - the file was already submitted to the queue");
                          LOG.info("This entry is skipped");
                          processedWithError++;
                          continue;
                }
                    
                processedSuccessfully++;
                
                //TODO hack
                //break;
                
              
//                
//                    //get URI from resource
//                    String fileURIString = entry.getFileURIString();
//                    LOG.debug("File URI is: {}", fileURIString);
//                    
//                    String filePath = fileURIString.substring("file:".length());
//                    
//                                
                
//               //OUTPUT
//               filesOutput.addExistingFile(newSymbolicName, entry.getFileURIString());
//                
//                //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
//               String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
//               if (outputVirtualPath != null) {
//                VirtualPathHelpers.setVirtualPath(filesOutput, newSymbolicName, outputVirtualPath);
//               }
//               
//                
//                LOG.info("Output created successfully, sn: {}, file: {}", newSymbolicName, entry.getFileURIString());
                
                


            }
            
            context.sendMessage(DPUContext.MessageType.INFO, "Successfully processed " + processedSuccessfully + " / " + processedAll);
             context.sendMessage(DPUContext.MessageType.INFO, "Processed with error " + processedWithError + " / " + processedAll);
            
             } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibSendDataToRextractorVaadinDialog();
    }
	
}
