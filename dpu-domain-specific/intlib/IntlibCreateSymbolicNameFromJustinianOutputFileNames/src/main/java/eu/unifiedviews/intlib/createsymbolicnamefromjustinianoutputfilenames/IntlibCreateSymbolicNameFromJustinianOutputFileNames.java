package eu.unifiedviews.intlib.createsymbolicnamefromjustinianoutputfilenames;

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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class IntlibCreateSymbolicNameFromJustinianOutputFileNames extends DpuAdvancedBase<IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1> {

      @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "filesOutput")
    public WritableFilesDataUnit filesOutput;
    
	private static final Logger LOG = LoggerFactory.getLogger(IntlibCreateSymbolicNameFromJustinianOutputFileNames.class);
		
	public IntlibCreateSymbolicNameFromJustinianOutputFileNames() {
		super(IntlibCreateSymbolicNameFromJustinianOutputFileNamesConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        LOG.info("DPU is running ...");
        
        
         // RepositoryConnection connection = null;
        try {
            
            //FIRST ITERATION to process xml files and put them on the output
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }

            //iterate over files 
            int processedSuccessfully = 0;
            while (filesIteration.hasNext()) {

                FilesDataUnit.Entry entry = filesIteration.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());

                String sn = entry.getSymbolicName();
               
               //create new symbolic name from the existing, so that it is in the proper form
                   //build new symbolic name (containing expression URI)
                   String newSymbolicName = null; 
                   newSymbolicName = createNewSymbolicName(sn);
                                     
                                     
                    
                    if (newSymbolicName == null) {
                        LOG.error("Expression URI not parsed successfully, skipped");
                        continue;
                    }
                    LOG.debug("Expression URI is {}", newSymbolicName);
                    
                
                
                    //OUTPUT
                    filesOutput.addExistingFile(newSymbolicName, entry.getFileURIString());

                     //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
                    String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
                    if (outputVirtualPath != null) {
                     VirtualPathHelpers.setVirtualPath(filesOutput, newSymbolicName, outputVirtualPath);
                    }
               
                
                   LOG.info("Output created successfully, sn: {}, file: {}", newSymbolicName, entry.getFileURIString());
                     processedSuccessfully ++;
                
                       if (context.canceled()) {
                        LOG.info("DPU cancelled");
                        return;
                }
                


            }
            context.sendMessage(DPUContext.MessageType.INFO, "Successfully processed " + processedSuccessfully +" files");
            
             } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }
        
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibCreateSymbolicNameFromJustinianOutputFileNamesVaadinDialog();
    }

    
    private String createNewSymbolicName(String sn) {
        
          //input: Export_HTML\predpisy\1992\0357\pr0357-1992_0420-2003.xml
          //desired output: http://linked.opendata.cz/resource/legislation/cz/act/2002/47-2002/expression/cz/act/2004/690-2004/cs 
    
        String resultingURI = "http://linked.opendata.cz/resource/legislation/cz/act/";
     
        
        
        Pattern pattern = Pattern.compile("pr[0-9]+");
        // in case you would like to ignore case sensitivity,
        // you could use this statement:
        // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sn);
        String year = null;
        String number = null;
        String novelayear = null;
        String novelanumber = null;
        // check all occurance
        while (matcher.find()) {
            //there is only one match
             String temp = sn.substring(matcher.start() + "pr".length());
        
//            String yearTemp = temp.substring(temp.indexOf("-")+"-".length()); 
//            String year = yearTemp.substring(0, 4);
//            result.put("year", year);

            Pattern p= Pattern.compile("[1-9][0-9]*");
            Matcher m = p.matcher(temp);
            int i = 0;
            while (m.find()) {
                i++;
                String field = temp.substring(m.start(), m.end());
                if (i == 1) number = field;
                else if (i == 2) year = field;
                else if (i == 3) novelanumber = field;
                else if (i == 4) novelayear = field;
                else {
                    //error
                }
                
            }
            break;
                            
        }
        
        //build URI of the form: http://linked.opendata.cz/resource/legislation/cz/act/2002/47-2002/expression/cz/act/2004/690-2004/cs 
        resultingURI = resultingURI + year + "/" + number + "-" + year + "/expression/cz/act/" + novelayear + "/" + novelanumber + "-" + novelayear + "/cs";
        
        return resultingURI;
        
    }
 	
}
