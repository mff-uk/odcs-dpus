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
import cz.cuni.xrg.intlib.commons.dpu.annotation.InputDataUnit;
import cz.cuni.xrg.intlib.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.xrg.intlib.commons.message.MessageType;
import cz.cuni.xrg.intlib.commons.module.dpu.ConfigurableBase;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;
import cz.cuni.xrg.intlib.commons.web.ConfigDialogProvider;
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
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@AsExtractor
public class SimpleXSLT extends ConfigurableBase<SimpleXSLTConfig> implements DPU, ConfigDialogProvider<SimpleXSLTConfig> {
    
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(
            SimpleXSLT.class);
    
    public SimpleXSLT(){
            super(SimpleXSLTConfig.class);
        }
    
     @InputDataUnit
	public RDFDataUnit rdfInput;
    
    @OutputDataUnit
	public RDFDataUnit rdfOutput;

    /**
     * DPU's configuration.
     */
//    private SimpleXSLTConfig config;

    @Override
    public AbstractConfigDialog<SimpleXSLTConfig> getConfigurationDialog() {
        return new SimpleXSLTDialog();
    }

//    @Override
//    public void configure(SimpleXSLTConfig c) throws ConfigException {
//        config = c;
//    }
//
//    @Override
//    public SimpleXSLTConfig getConfiguration() {
//        return config;
//    }

    // TODO 2: Provide implementation of unimplemented methods 
    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException  {
      //store xslt to file
      
       
     
        
         //TODO store the config directly to file, global dir/{dpu_instance_id}/template.xslt
        File xslTemplate = storeStringToTempFile(context, config.getXslTemplate(),"template.xslt");
        if (xslTemplate == null) {
            log.error("No xslt file specified");
            context.sendMessage(MessageType.ERROR, "No xslt file specifed ");
            return;
        }
       
        
        
      //prepare inputs, call xslt for each input
      String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
      
      //get the return values
      Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
      for (int i = 0; i < executeSelectQuery.get("o").size(); i++) {
         
          String subject = executeSelectQuery.get("s").get(i);
        String fileContent = executeSelectQuery.get("o").get(i);
            //store the xmll content to file
            File file = storeStringToTempFile(context, fileContent, String.valueOf(i) + ".xml");
            //File outputFile = new File(context.getWorkingDir() + "/out/" + String.valueOf(i) + ".xml");
        
        //call xslt
        String outputString = executeXSLT(xslTemplate,file);
        
        if (outputString.isEmpty()) {
            log.warn("Template applied to input generated empty output. Input was: ", fileContent);
            continue;
        }
        //create output
          switch(config.getOutputType()) {
             case RDFXML:    rdfOutput.addFromRDFXMLFile(file); break;
                 case TTL:  rdfOutput.addFromTurtleFile(file); break; 
                     case Literal: 
                         URI subj = rdfOutput.createURI(subject);
                         URI pred = rdfOutput.createURI(config.getOutputPredicate());
                         Literal obj = rdfOutput.createLiteral(outputString);
                         
                       
                         rdfOutput.addTriple(subj, pred, obj);  break; 
         }
        
        rdfOutput.addFromFile(file);
        
      }
    
           


//        //xslt
//        Processor proc = new Processor(false);
//        XsltCompiler compiler = proc.newXsltCompiler();
//        XsltExecutable exp;
//        try {
//            exp = compiler.compile(new StreamSource(stylesheet));
//
//
//            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(inputFile));
//
//
//
//            Serializer out = new Serializer();
//            out.setOutputProperty(Serializer.Property.METHOD, "text");
//            out.setOutputProperty(Serializer.Property.INDENT, "yes");
//            out.setOutputFile(outputFile);
//
//            XsltTransformer trans = exp.load();
//
//            trans.setInitialContextNode(source);
//            trans.setDestination(out);
//            trans.transform();
//
//        } catch (SaxonApiException ex) {
//            
//        }
//        
              
            ////////////
       
        

    }

    @Override
    public void cleanUp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private File storeStringToTempFile(DPUContext context, String s, String fileName) {

//       if (s == null || s.isEmpty()) {
//            log.error("No file specifed");
//            context.sendMessage(MessageType.ERROR, "No file specifed: ");
//            return;
//            
//        }
        log.info("File content is: {}", s);

        //prepare temp file where the a is stored
        File workingDir = context.getWorkingDir();
        File configFile = null;
        try {
            configFile = new File(workingDir.getCanonicalPath() + "/" + fileName);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SimpleXSLT.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            log.debug("File path {}", configFile.getCanonicalPath());
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
        
        Charset charset = StandardCharsets.UTF_8;

        try (BufferedWriter writer = Files.newBufferedWriter(configFile.toPath(), charset)) {
            writer.write(s, 0, s.length());
        } catch (IOException x) {
            log.error("IOException: %s%n", x);
        }
        
        return configFile;
    
    
    }

    private String executeXSLT(File xslTemplate, File file) {
        
       
         
         //xslt
        Processor proc = new Processor(false);
        XsltCompiler compiler = proc.newXsltCompiler();
        XsltExecutable exp;
        try {
            exp = compiler.compile(new StreamSource(xslTemplate));


            XdmNode source = proc.newDocumentBuilder().build(new StreamSource(file));



            Serializer out = new Serializer();
            out.setOutputProperty(Serializer.Property.METHOD, "text");
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
}

