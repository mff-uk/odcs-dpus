package eu.unifiedviews.intlib.prepareXsltParamsRdfRepresentationOfLaws;

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
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelper;
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelpers;
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
public class IntlibPrepareXSLTParamsRDFRepresentationOfLaws extends DpuAdvancedBase<IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1> {

	 @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;
    @DataUnit.AsOutput(name = "fileOutput")
    public WritableFilesDataUnit filesOutput;
    private static final Logger LOG = LoggerFactory.getLogger(IntlibPrepareXSLTParamsRDFRepresentationOfLaws.class);

	public IntlibPrepareXSLTParamsRDFRepresentationOfLaws() {
		super(IntlibPrepareXSLTParamsRDFRepresentationOfLawsConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        
        LOG.info("DPU is running ...");
        
        //from symbolic name, extract necessary XSLT params
        
         // RepositoryConnection connection = null;
        try {
            
            //FIRST ITERATION to process xml files and put them on the output
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }
            
            //helper for adding map to the output (map holding XSLT params)
            MapHelper mHelper = MapHelpers.create(filesOutput);

            //iterate over files 
            while (filesIteration.hasNext()) {

                FilesDataUnit.Entry entry = filesIteration.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());


                String sn = entry.getSymbolicName();
                
                                
                //get map 
                  Map<String, String> xsltParamsMap = prepareMapOfXSLTParams(sn);

                  
                  LOG.debug("Size of map: {} for symbolic name {}", xsltParamsMap.size(), sn);
                  for (String key : xsltParamsMap.keySet()) {
                      String value = xsltParamsMap.get(key);
                      LOG.debug("key {}, value {}", key, value);
                  }

                
               //OUTPUT
                filesOutput.addExistingFile(sn, entry.getFileURIString());
                
                 //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
               String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
               if (outputVirtualPath != null) {
                VirtualPathHelpers.setVirtualPath(filesOutput, entry.getSymbolicName(), outputVirtualPath);
               }
                  
                //add XSLT map
                mHelper.putMap(sn, "xsltParameters", xsltParamsMap);
                

            }
            
            
          

//                      
//            //VERIFICATION: 
//            //add metadataMaps for all files
//            // iterate over metadata map of the output symbolic names
//             FilesDataUnit.Iteration filesIterationOut = filesOutput.getIteration();
//              while (filesIterationOut.hasNext()) {
//
//                FilesDataUnit.Entry entry = filesIterationOut.next();
//                entry.getSymbolicName();
//                LOG.debug("File URI string: {} for symbolic name {}", entry.getFileURIString(), entry.getSymbolicName());
//                
//                 Map<String, String> xsltParamsMap = mHelper.getMap(entry.getSymbolicName(), "xlstParameters");
//                 LOG.debug("Size of map (OUTPUT) is {} for symbolic name {}", xsltParamsMap.size(), entry.getSymbolicName());
//                    for (String key : xsltParamsMap.keySet()) {
//                        String value = xsltParamsMap.get(key);
//                        LOG.debug("key {}, value {}", key, value);
//                    }
//                 
//                                
//              }

                      
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }
        
        
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibPrepareXSLTParamsRDFRepresentationOfLawsVaadinDialog();
    }
    
    
    
      
     private static Map<String, String> prepareMapOfXSLTParams(String sn) {

    //Export_HTML\predpisy\1992\0357\pr0357-1992_0420-2003.xml
    
     
        Map<String, String> result = new HashMap<>();
        
        result.put("type", "Act");
        
        
        
         Pattern pattern = Pattern.compile("pr[0-9]+");
        // in case you would like to ignore case sensitivity,
        // you could use this statement:
        // Pattern pattern = Pattern.compile("\\s+", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sn);
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
                if (i == 1) result.put("number", field);
                else if (i == 2) result.put("year", field);
                else if (i == 3) result.put("novelanumber", field);
                else if (i == 4) result.put("novelayear", field);
                else {
                    //error
                }
                
            }
            break;
                            
        }
        return result;
    }
	
}
