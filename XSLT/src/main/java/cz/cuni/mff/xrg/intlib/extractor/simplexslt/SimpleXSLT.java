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


        //TODO store the config directly to file, global dir/{dpu_instance_id}/template.xslt
        //store xslt
        File xslTemplate = storeStringToTempFile(context, config.getXslTemplate(), "template.xslt");
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
        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
        
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
        
        
        while(executeSelectQueryAsTuples.hasNext()) 
        
        for (int i = 0; i < executeSelectQuery.get("o").size(); i++) {



            String subject = executeSelectQuery.get("s").get(i);
            log.info("About to execute xslt for subject: {}", subject);

            String fileContent = executeSelectQuery.get("o").get(i);
            log.debug("The file being processed: {}", fileContent);
            
            //store the input content to file, inputs are xml files!
            File file = storeStringToTempFile(context, fileContent, String.valueOf(i) + ".xml");
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
                    storeStringToTempFile(context, outputString, "out.xml");
                    rdfOutput.addFromRDFXMLFile(new File(workingDirPath + "/out.xml"));
                    log.debug("Result was added to output data unit as RDF/XML data");
                    break;
                case TTL:
                    storeStringToTempFile(context, outputString, "out.ttl");
                    rdfOutput.addFromTurtleFile(new File(workingDirPath + "/out.ttl"));
                    log.debug("Result was added to output data unit as turtle data");

                    break;
                case Literal:
                    String prepareTripleString = prepareTriple(subject,config.getOutputPredicate(),outputString);
                     storeStringToTempFile(context, prepareTripleString, "out.ttl");
                    rdfOutput.addFromTurtleFile(new File(workingDirPath + "/out.ttl"));
                    
                    log.debug("Result was added to output data unit as turtle data containing one triple {}", prepareTripleString);
                     
//                    URI subj = rdfOutput.createURI(subject);
//                    URI pred = rdfOutput.createURI(config.getOutputPredicate());
//                    Literal obj = rdfOutput.createQuotedLiteral(outputString);                
//                                       
//                    rdfOutput.addTriple(subj, pred, obj);
//                    
//                    log.debug("Created output triple: {}", subj.toString() + ", " + pred.toString() + ", "+ obj.toString());
                    break;
            }

            log.info("Output created successfully");

        }


    }
    
     private String prepareTriple(String subject, String outputPredicate, String outputString) {
        
        
        Resource subj = rdfOutput.createURI(subject);
        URI pred = rdfOutput.createURI(outputPredicate);
        Value obj = rdfOutput.createLiteral(outputString); 
         
 
       
        String triple= getSubjectInsertText(subj) + " "
                        + getPredicateInsertText(pred) + " "
                        + getObjectInsertText(obj) + " .";

//        String escapedrdfa = rdfa.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quote;").replaceAll("\\*", "&#42;").replaceAll("\\\\", "&#92;");
//	writer.println("<" + decisionURI + "> sdo:hasHTMLContent \"\"\"" + escapedrdfa + "\"\"\"@cs .");
                                
                                return triple;
    }

     
     private String getSubjectInsertText(Resource subject) throws IllegalArgumentException {

		if (subject instanceof URI) {
			return prepareURIresource((URI) subject);
		}

		if (subject instanceof BNode) {
			return prepareBlankNodeResource((BNode) subject);
		}
		throw new IllegalArgumentException("Subject must be URI or blank node");
	}

	private String getPredicateInsertText(URI predicate) {
		if (predicate instanceof URI) {
			return prepareURIresource((URI) predicate);
		}
		throw new IllegalArgumentException("Predicatemust be URI");

	}

	private String getObjectInsertText(Value object) throws IllegalArgumentException {

		if (object instanceof URI) {
			return prepareURIresource((URI) object);
		}

		if (object instanceof BNode) {
			return prepareBlankNodeResource((BNode) object);
		}

		if (object instanceof Literal) {
			return prepareLiteral((Literal) object);
		}

		throw new IllegalArgumentException(
				"Object must be URI, blank node or literal");
	}

	private String prepareURIresource(URI uri) {
		return "<" + uri.stringValue() + ">";
	}

	private String prepareBlankNodeResource(BNode bnode) {
		return "_:" + bnode.getID();
	}

	private String prepareLiteral(Literal literal) {
		String label = "\"\"\"" + literal.getLabel() + "\"\"\"";
		if (literal.getLanguage() != null) {
			//there is language tag
			return label + "@" + literal.getLanguage();
		} else if (literal.getDatatype() != null) {
			return label + "^^" + prepareURIresource(literal.getDatatype());
		}
		//plain literal (return in """)
		return label;

	}
     

    private File storeStringToTempFile(DPUContext context, String s, String fileName) {

        if (s == null || s.isEmpty()) {
            log.warn("Nothing to be stored to a file");
            return null;
        }
        
        if (fileName == null || fileName.isEmpty()) {
             log.error("File name is missing");
             return null;
        }
        
        //log.debug("File content is: {}", s);

        //prepare temp file where the a is stored
        File workingDir = context.getWorkingDir();
        File configFile = null;
        try {
            configFile = new File(workingDir.getCanonicalPath() + "/" + fileName);
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }

        if (configFile == null) {
            log.error("Created file is null or empty, although the original string was non-empty .");
            return null;
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

   }
