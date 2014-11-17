package eu.unifiedviews.legislation;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsTransformer
public class ProvideActTextsAsLiterals extends DpuAdvancedBase<ProvideActTextsAsLiteralsConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(ProvideActTextsAsLiterals.class);
    private SimpleRdfWrite rdfWrap;
    
    @DataUnit.AsInput(name = "filesInput")
    public FilesDataUnit filesInput;

    @DataUnit.AsOutput(name = "rdfOutput")
    public WritableRDFDataUnit rdfOutput;
        
    
    public ProvideActTextsAsLiterals() {
            super(ProvideActTextsAsLiteralsConfig_V1.class, AddonInitializer.noAddons());
    }
		
    @Override
    protected void innerExecute() throws DPUException {
        
        LOG.info("DPU is running ...");
        
        try {
            rdfWrap = SimpleRdfFactory.create(rdfOutput, context);
            rdfWrap.setOutputGraph("http://linked.opendata.cz/legislation/nsoud");
            //rdfWrap.setPolicy(AddPolicy.BUFFERED);
        } catch (OperationFailedException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, ex.getLocalizedMessage()); 
        }
        
        RepositoryConnection connection = null;
        try {
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }

            //iterate over files!!
            while (filesIteration.hasNext()) {
                
               FilesDataUnit.Entry entry = filesIteration.next();
               
               //Extracting file entry, symbolic name predpisy/2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
               LOG.debug("Extracting file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());
               
                //remove "file:" from file path
                String entryFilePath = entry.getFileURIString().substring("file:".length());
               
               //get subject, subject = symbolic name
               String subject = entry.getSymbolicName();
               
               //get all info needed to build URI for subject (needed when creating subject URI from fileName - output of Justinian)
               //URI sample http://linked.opendata.cz/resource/legislation/cz/act/2003/62-2003/expression/cz/act/2012/222-2012/cs
//               String subject = buildSubjectURI(entry.getSymbolicName());
               
               LOG.debug("Subject: {}", subject);    
               
               //predicate
               String predicate = config.getPredicateURL();
               
               //object - content of the file
               String object = null;
               try {
                   Path p = Paths.get(entryFilePath);
                   LOG.debug("Path to file: {}", p.toString());
                   object = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
//                   String filename = entry.getFileURIString().substring(entry.getFileURIString().indexOf("file:/") + 6);
//                   LOG.debug("Filename: {}", object.substring(0,500));
//                   object = readFile("file:///" + filename);
//                    LOG.debug("Object: {}", object.substring(0,500));
               } catch (IOException exc) {
                   LOG.error("Problem loading file: {}", exc.getLocalizedMessage());
                   LOG.info("This entry is skipped");
                   continue;
                }
              
               if (object == null) {
                   LOG.error("Object is null, this entry is skipped");
                   continue;
               }
                
//                try {
                    
                    //get connection to output rdf data unit
                    //connection = rdfOutput.getConnection();
                
                    //add one triple to the connection
                    ValueFactory factory = ValueFactoryImpl.getInstance();
                    rdfWrap.add(factory.createURI(subject), factory.createURI(predicate), factory.createLiteral(object));
                    
                    //                    rdfWrap.setOutputGraph("");
                    
//                     //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
//               String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
//               if (outputVirtualPath != null) {
//                VirtualPathHelpers.setVirtualPath(filesOutput, entry.getSymbolicName(), outputVirtualPath);
//               } 
                  
             
//               } finally {
//                    if (connection != null) {
//                        try {
//                            connection.close();
//                        } catch (RepositoryException ex) {
//                            context.sendMessage(DPUContext.MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
//                        }
//                    }
//                }
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    context.sendMessage(DPUContext.MessageType.WARNING, ex.getMessage(), ex.fillInStackTrace().toString());
                }
            }
        }
        
        try {
            rdfWrap.flushBuffer();
        } catch (OperationFailedException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, ex.getLocalizedMessage()); 
        }
        
        
        
        
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new ProvideActTextsAsLiteralsVaadinDialog();
    }

    //builds URI for subject, 
    //sample http://linked.opendata.cz/resource/legislation/cz/act/2003/62-2003/expression/cz/act/2012/222-2012/cs
    //symbolic name sample: symbolic name predpisy/2003/0062/pr0062-2003_original.xml
    //                      redpisy/2003/0062/pr0062-2003_0222-2012.xml
    private String buildSubjectURI(String sn) {

        StringBuilder result = new StringBuilder("http://linked.opendata.cz/resource/legislation/cz/act/");

        String year = sn.substring(sn.indexOf("predpisy/") + "predpisy/".length(), sn.indexOf("predpisy/") + "predpisy/".length() + 4);
        result.append(year);
        result.append("/");
        
        //remove trailing zeros:
        String temp = sn.substring(sn.indexOf("predpisy/") + "predpisy/".length());
        temp = temp.substring(temp.indexOf("/") + 1);
        
        String znacka = temp.substring(0, 4).replaceAll("^0+", "");
        result.append(znacka);
        result.append("-");
        result.append(year);
        result.append("/expression/cz/act/");
        
        //if original, then repeat
        if (sn.contains("original")) {
            result.append(year);
             result.append("/");
              result.append(znacka);
            result.append("-");
            result.append(year);
             
        } else {
            
            //if novelized, then use the number from the file name
            //try to parse the novel id
            temp = temp.substring(temp.indexOf("_") + 1);
            String novelID = temp.substring(0, 4).replaceAll("^0+", "");
            temp = temp.substring(temp.indexOf("-") + 1);
            String novelYear = temp.substring(0, 4);
            
             result.append(novelYear);
             result.append("/");
              result.append(novelID);
            result.append("-");
            result.append(novelYear);
            
            
        }
        
        
        
         result.append("/");
              result.append("cs");
        
        return result.toString();
    
    }
	
    
    private String readFile( String file ) throws IOException {
    BufferedReader reader = new BufferedReader( new FileReader (file));
    String         line = null;
    StringBuilder  stringBuilder = new StringBuilder();
    String         ls = System.getProperty("line.separator");

    while( ( line = reader.readLine() ) != null ) {
        stringBuilder.append( line );
        stringBuilder.append( ls );
    }

    return stringBuilder.toString();
}
    
}
