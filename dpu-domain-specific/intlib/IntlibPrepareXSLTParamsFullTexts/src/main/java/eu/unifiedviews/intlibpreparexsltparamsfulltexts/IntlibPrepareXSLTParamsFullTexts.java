package eu.unifiedviews.intlibpreparexsltparamsfulltexts;

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
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelper;
import eu.unifiedviews.helpers.dataunit.copyhelper.CopyHelpers;
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelper;
import eu.unifiedviews.helpers.dataunit.maphelper.MapHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Specify relation ID, which is then used to parametrize XSLT for relations. If empty, entities are transformed


@DPU.AsTransformer
public class IntlibPrepareXSLTParamsFullTexts extends DpuAdvancedBase<IntlibPrepareXSLTParamsFullTextsConfig_V1> {

    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;
    @DataUnit.AsOutput(name = "fileOutput")
    public WritableFilesDataUnit filesOutput;
    private static final Logger LOG = LoggerFactory.getLogger(IntlibPrepareXSLTParamsFullTexts.class);

    public IntlibPrepareXSLTParamsFullTexts() {
        super(IntlibPrepareXSLTParamsFullTextsConfig_V1.class, AddonInitializer.noAddons());
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
            while (filesIteration.hasNext()) {

                FilesDataUnit.Entry entry = filesIteration.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());


                if (entry.getSymbolicName().contains(".xml")) {
                    //if it is an XML resource, put it on the output.
                    if (config.getRelationID().length() > 0) {
                        //it is a relation, adjust symbolic name tp put relation number to symbolic name
                         final String xmlSymbolicName = entry.getSymbolicName().replace(".xml", "_"+config.getRelationID()+".xml");
                         filesOutput.addExistingFile(xmlSymbolicName, entry.getFileURIString());
                    }
                    else {
                        //no relations, transform entities
                        filesOutput.addExistingFile(entry.getSymbolicName(), entry.getFileURIString());
                    }
                } else if (entry.getSymbolicName().contains(".html")) {
                    //skip in the first iteration
                } else {
                    LOG.warn("Strange file {} processed", entry.getSymbolicName());
                }


            }
            
            
            //SECOND ITERATION to process html files and put them on the output
            FilesDataUnit.Iteration filesIteration2 = filesInput.getIteration();

            if (!filesIteration2.hasNext()) {
                return;
            }

            MapHelper mHelper = MapHelpers.create(filesOutput);

            //iterate over files!!
            while (filesIteration2.hasNext()) {

                FilesDataUnit.Entry entry = filesIteration2.next();

                //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());


                if (entry.getSymbolicName().contains(".xml")) {
                    //was processed in the first iteration
                } else if (entry.getSymbolicName().contains(".html")) {
                    //if it is an HTML resource, 
                    //create XSLT params, attach it to symbolic name with .xml (so that it matches the symbolic name of XML files!)
                    
                    String xmlSymbolicName;
                    if (config.getRelationID().length() > 0) {
                        //it is a relation, add also relation number to symbolic name
                         xmlSymbolicName = entry.getSymbolicName().replace(".html", "_"+config.getRelationID()+".xml");
                    } else {
                        //it is transform entities, only replace html with xml
                        xmlSymbolicName = entry.getSymbolicName().replace(".html", ".xml");
                    }
                    
                    LOG.debug("XML symbolic name is: {}", xmlSymbolicName);
                    
                    
                    //get URI from resource
                    String fileURIString = entry.getFileURIString();
                    LOG.debug("File URI is: {}", fileURIString);
                    
                    String filePath = fileURIString.substring("file:".length());
                    
                    String expressionURI;
                    try { 
                        expressionURI = getResourceURIFromFile(filePath);
                      } catch(IOException exc) {
                        LOG.error("Error processing file: {}", exc.getLocalizedMessage());
                        LOG.info("Processing of the file skippede");
                        continue;
                    }
                    
                    
                    if (expressionURI == null) {
                        LOG.error("Expression URI not parsed successfully, skipped");
                    }
                    LOG.debug("Expression URI is {}", expressionURI);
                    

                    //get map 
                    Map<String, String> xsltParamsMap = prepareMapOfXSLTParams(expressionURI);

                    
                     if (config.getRelationID().length() > 0) {
                         //add one extra XSLT param for relations: 
                         xsltParamsMap.put("queryProcessed", config.getRelationID());
                         
                         
                     }
                     
                    LOG.debug("Size of map: {} for symbolic name {}", xsltParamsMap.size(), xmlSymbolicName);
                    for (String key : xsltParamsMap.keySet()) {
                        String value = xsltParamsMap.get(key);
                        LOG.debug("key {}, value {}", key, value);
                    }

                    //copy metadata to the output
                    //cHelper.copyMetadata(entry.getSymbolicName());

                    //put map to the output data unit (attach it to XML symbolic name)
                    //filesOutput.addExistingFile(entry.getSymbolicName(), entry.getFileURIString());
                    //mHelper.putMap(entry.getSymbolicName(), "xlstParameters", xsltParamsMap);
                    
                    //TODO problem if HTML is processed before XML - in this case,map is not added. 
                    //filesOutput.addEntry(xmlSymbolicName);
                    mHelper.putMap(xmlSymbolicName, "xlstParameters", xsltParamsMap);

                } else {
                    LOG.warn("Strange file {} processed", entry.getSymbolicName());
                }


            }
            
            
            
            
            
            //VERIFICATION: 
            //add metadataMaps for all files
            // iterate over metadata map of the output symbolic names
             FilesDataUnit.Iteration filesIterationOut = filesOutput.getIteration();
              while (filesIterationOut.hasNext()) {

                FilesDataUnit.Entry entry = filesIterationOut.next();
                entry.getSymbolicName();
                LOG.debug("File URI string: {} for symbolic name {}", entry.getFileURIString(), entry.getSymbolicName());
                
                 Map<String, String> xsltParamsMap = mHelper.getMap(entry.getSymbolicName(), "xlstParameters");
                 LOG.debug("Size of map (OUTPUT) is {} for symbolic name {}", xsltParamsMap.size(), entry.getSymbolicName());
                    for (String key : xsltParamsMap.keySet()) {
                        String value = xsltParamsMap.get(key);
                        LOG.debug("key {}, value {}", key, value);
                    }
                 
                                
              }

                      
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }
//        } finally {
//            if (connection != null) {
//                try {
//                    connection.close();
//                } catch (RepositoryException ex) {
//                    context.sendMessage(DPUContext.MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
//                }
//            }
//        }



    }
    
    private String getResourceURIFromFile( String file ) throws IOException {
    BufferedReader reader = new BufferedReader( new FileReader (file));
    String         line = null;

    while( ( line = reader.readLine() ) != null ) {
        if (line.contains("article resource=")) {
            LOG.debug("matched resource URl");
            //to get start of the resource
            String temp = line.substring(line.indexOf("article resource=")+"article resource=".length()+1);
            
            //to get proper end
            return temp.substring(0, temp.indexOf("\""));
            
            
        }
        
      
    }

    return null;
}

    private Map<String, String> prepareMapOfXSLTParams(String sn) {

        //input is: http://linked.opendata.cz/resource/legislation/cz/act/1993/182-1993/expression/cz/act/2012/404-2012/cs
        
       
        Map<String, String> result = new HashMap<>();
        
        result.put("actType", "act");
        
        String temp = sn.substring(sn.indexOf("http://linked.opendata.cz/resource/legislation/cz/act/") + "http://linked.opendata.cz/resource/legislation/cz/act/".length());
        String year = temp.substring(0, 4);
        result.put("actYear", year);

        temp = temp.substring(5); //year + /
        String znacka = temp.substring(0, temp.indexOf("/"));
        result.put("actNumber", znacka);
        
        temp = temp.substring(temp.indexOf("/expression/cz/act/") + "/expression/cz/act/".length());
        year = temp.substring(0, 4);
        result.put("novelizingActYear", year);
        
        
        temp = temp.substring(5);
        znacka = temp.substring(0, temp.indexOf("/"));
        result.put("novelizingActNumber", znacka);
        
        
              
        


        return result;
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IntlibPrepareXSLTParamsFullTextsVaadinDialog();
    }
}
