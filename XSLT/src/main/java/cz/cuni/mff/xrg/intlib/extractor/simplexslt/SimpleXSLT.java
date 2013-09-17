package cz.cuni.mff.xrg.intlib.extractor.simplexslt;

import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.Literal;
import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.RDFXML;
import static cz.cuni.mff.xrg.intlib.extractor.simplexslt.SimpleXSLTConfig.OutputType.TTL;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;

import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.data.DataUnitException;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;
import cz.cuni.xrg.intlib.commons.dpu.DPU;
import cz.cuni.xrg.intlib.commons.dpu.DPUContext;
import cz.cuni.xrg.intlib.commons.dpu.DPUException;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsExtractor;
import cz.cuni.xrg.intlib.commons.dpu.annotation.AsTransformer;
import cz.cuni.xrg.intlib.commons.dpu.annotation.InputDataUnit;
import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.module.utils.AddTripleWorkaround;
import cz.cuni.xrg.intlib.commons.module.utils.DataUnitUtils;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
import cz.cuni.xrg.intlib.rdf.impl.MyTupleQueryResult;
import cz.cuni.xrg.intlib.rdf.interfaces.RDFDataUnit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 * 
 *
 * @author tomasknap
 */
@AsTransformer
public class SimpleXSLT extends ConfigurableBase<SimpleXSLTConfig> implements ConfigDialogProvider<SimpleXSLTConfig> {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            SimpleXSLT.class);

    public SimpleXSLT() {
        super(SimpleXSLTConfig.class);
    }
    @InputDataUnit
    public RDFDataUnit rdfInput;
    @OutputDataUnit
    public RDFDataUnit rdfOutput;

    @Override
    public AbstractConfigDialog<SimpleXSLTConfig> getConfigurationDialog() {
        return new SimpleXSLTDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {


        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();

                    
        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            log.error("Cannot get path to working dir");
            log.debug(ex.getLocalizedMessage());
            //TODO adjust to send descr + detail
            context.sendMessage(MessageType.ERROR, "Cannot get path to working dir "
					+ ex.getLocalizedMessage());
        }
        
        //TODO store the config directly to file, global dir/{dpu_instance_id}/template.xslt
        //store xslt
        File xslTemplate = DataUnitUtils.storeStringToTempFile(config.getXslTemplate(), pathToWorkingDir + File.separator + "template.xslt");
        if (xslTemplate == null) {
            log.error("No xslt file specified");
            context.sendMessage(MessageType.ERROR, "No xslt file specifed ");
            return;
        }

        //prepare inputs, call xslt for each input
        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
        log.debug("Query for getting input files: {}", query);
        //get the return values
        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
        MyTupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
        
//        for (int i = 0; i < executeSelectQuery.get("o").size(); i++) {
//
//
//
//            String subject = executeSelectQuery.get("s").get(i);
//            log.info("About to execute xslt for subject: {}", subject);
//
//            String fileContent = executeSelectQuery.get("o").get(i);
//            log.debug("The file being processed: {}", fileContent);
//            
        
        
        int i = 0;
        try {
            while (executeSelectQueryAsTuples.hasNext()) {
       
            i++;
        
        //for (int i = 0; i < executeSelectQuery.get("o").size(); i++) {

            //process the inputs
                BindingSet solution = executeSelectQueryAsTuples.next();
                Binding b = solution.getBinding("o");
                String fileContent = b.getValue().toString();
                String subject = solution.getBinding("s").getValue().toString();
                log.info("Processing new file for subject {}", subject);
                log.debug("Processing file {}", fileContent);


//            String subject = executeSelectQuery.get("s").get(i);
//            log.info("About to execute xslt for subject: {}", subject);

//            String fileContent = executeSelectQuery.get("o").get(i);
//            log.debug("The file being processed: {}", fileContent);
//            
            //store the input content to file, inputs are xml files!
            String inputFilePath = pathToWorkingDir + File.separator + String.valueOf(i) + ".xml";
            File file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath);
            if (file == null) {
                log.warn("Problem processing object for subject {}", subject);
                continue;
            }
            
            //call xslt, obtain result in a string
            String outputString = executeXSLT(xslTemplate, file);
            if (outputString == null) {
                log.warn("Problem generating output of xslt transformation for subject {}", subject);
                continue;
            }

            if (outputString.isEmpty()) {
                log.warn("Template applied to the subject {} generated empty output. Input was: ", subject, fileContent);
                continue;
            }
            log.info("XSLT executed successfully, about to create output");
            log.debug("Output of the transformation: {}", outputString);
            
                      
            
            //create output (based on the settings)
            String workingDirPath = null;
            try {
                workingDirPath = context.getWorkingDir().getCanonicalPath();
            } catch (IOException ex) {
                log.error(ex.getLocalizedMessage());
                log.warn("Record skipped");
                continue;
            }
            
            switch (config.getOutputType()) {
                case RDFXML:
                    String outputPath = pathToWorkingDir + File.separator + String.valueOf(i) + "out.xml";
                    DataUnitUtils.storeStringToTempFile(outputString, outputPath);
                    rdfOutput.addFromRDFXMLFile(new File(outputPath));
                    log.debug("Result was added to output data unit as RDF/XML data");
                    break;
                case TTL:
                    outputPath = pathToWorkingDir + File.separator + String.valueOf(i) + "out.ttl";
                    DataUnitUtils.storeStringToTempFile(outputString, outputPath);
                    rdfOutput.addFromTurtleFile(new File(outputPath));
                    log.debug("Result was added to output data unit as turtle data");

                    break;
                case Literal:
                   
                    
                    
                           //OUTPUT
             
               Resource subj = rdfOutput.createURI(subject);
               URI pred = rdfOutput.createURI(config.getOutputPredicate());
               //encode object as needed
               Value obj = rdfOutput.createLiteral(encode(outputString,config.getEscapedString())); 
            
               
               String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
               String tempFileLoc = pathToWorkingDir + File.separator + String.valueOf(i) + "out.ttl";
         
                
               DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
               rdfOutput.addFromTurtleFile(new File(tempFileLoc));
                    
//                    String prepareTripleString = prepareTriple(subject,config.getOutputPredicate(),outputString);
//                    outputPath = pathToWorkingDir + File.separator + String.valueOf(i) + "out.ttl";
                    
//                    DataUnitUtils.storeStringToTempFile(prepareTripleString, outputPath);
//                    rdfOutput.addFromTurtleFile(new File(outputPath));
                    
                    log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
                     
                    break;
            }

            log.info("Output created successfully");

        } 
        } catch (QueryEvaluationException ex) {
            Logger.getLogger(SimpleXSLT.class.getName()).log(Level.SEVERE, null, ex);
        }
      
    

    }
    
     public static String encode(String literalValue, String escapedMappings) {
        
        String val = literalValue;
         String[] split = escapedMappings.split("\\s+");
        for (String s : split) {
            String[] keyAndVal = s.split(":");
            if (keyAndVal.length == 2) {
                val = val.replaceAll(keyAndVal[0],keyAndVal[1]);
                log.debug("Encoding mapping {} to {} was applied.", keyAndVal[0], keyAndVal[1] );
          
            } else {
                log.warn("Wrong format of escaped character mappings, skipping the mapping");
                
            }
        }
        return val;
        
    }
     
     


    private String executeXSLT(File xslTemplate, File file) {

        if (xslTemplate == null || file == null) {
            log.error("Invalid inputs to executeXSLT method");
            return null;
        }
        
        //xslt
        Processor proc = new Processor(false);
        XsltCompiler compiler = proc.newXsltCompiler();
        XsltExecutable exp;
        try {
            exp = compiler.compile(new StreamSource(xslTemplate));

            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(file));

            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, config.getOutputXSLTMethod());
            out.setOutputProperty(Serializer.Property.INDENT, "yes");
            StringWriter sw = new StringWriter();
            out.setOutputWriter(sw);
            //out.setOutputFile(outputFile);

            XsltTransformer trans = exp.load();

            trans.setInitialContextNode(source);
            trans.setDestination(out);
            trans.transform();
            return sw.toString();

        } catch (SaxonApiException ex) {
            log.error(ex.getLocalizedMessage());
        }

        return null;

    }

    private String removeTrailingQuotes(String fileContent) {
        
        if (fileContent.startsWith("\"")) {
            fileContent = fileContent.substring(1);
        }
        if (fileContent.endsWith("\"")) {
            fileContent = fileContent.substring(0, fileContent.length()-1);
        }
        return fileContent;
    }

   }
