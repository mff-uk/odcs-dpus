package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.IntLibLink;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.module.utils.AddTripleWorkaround;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.options.OptionsAdd;

import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@AsTransformer
public class UriGenerator extends ConfigurableBase<UriGeneratorConfig> implements ConfigDialogProvider<UriGeneratorConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(
            UriGenerator.class);

    public UriGenerator() {
        super(UriGeneratorConfig.class);
    }
	
    @InputDataUnit(name = "input")
    public RDFDataUnit rdfInput;
    
    //should be used when subject URI is needed in further DPUs (such as in XSLT producing result of the transformation in literal)
    @OutputDataUnit(name = "rdfOutput", optional = true)
    public WritableRDFDataUnit rdfOutput;
    
    //used for data when subject URI is further not needed.
    @OutputDataUnit(name = "fileOutput", optional = true)
    public FileDataUnit fileOutput;

    @Override
    public AbstractConfigDialog<UriGeneratorConfig> getConfigurationDialog() {
        return new UriGeneratorDialog();
    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {		
		LOG.info("\n ****************************************************** \n STARTING URI GENERATOR \n *****************************************************");
        //get working dir
        File workingDir = context.getWorkingDir();
        workingDir.mkdirs();

        String pathToWorkingDir = null;
        try {
            pathToWorkingDir = workingDir.getCanonicalPath();
        } catch (IOException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
        
        if (config.getStoredXsltFilePath().isEmpty()) {
                     LOG.error("Configuration file is missing, the processing will NOT continue");
                     context.sendMessage(MessageType.ERROR, "Configuration file is missing, the processing will NOT continue");
        }

        //prepare inputs, call xslt for each input
//        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o} ORDER BY ?s ?o";
        LOG.debug("Query for getting input files: {}", query);
        //get the return values
        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
        //        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
		
		SimpleRdfRead rdfInputWrap = new SimpleRdfRead(rdfInput, context);
		try (ConnectionPair<TupleQueryResult> queryRes = rdfInputWrap.executeSelectQuery(query)) {
			processQueryResult(queryRes.getObject(), context,	pathToWorkingDir);
		} catch (QueryEvaluationException ex) {
			context.sendMessage(MessageType.ERROR, "Problem evaluating the query to obtain files to be processed. Processing ends.", ex.getLocalizedMessage());
			LOG.error("Problem evaluating the query to obtain values of the {} literals. Processing ends.", config.getInputPredicate());
			LOG.debug(ex.getLocalizedMessage());
		}
    }

	private void processQueryResult(
			TupleQueryResult executeSelectQueryAsTuples, DPUContext context,
			String pathToWorkingDir) throws DataUnitException, QueryEvaluationException {
		//log.info(executeSelectQueryAsTuples.asList().)
		int i = 0;

		while (executeSelectQueryAsTuples.hasNext()) {
			if (context.canceled()) {
				LOG.info("DPU cancelled");
				return;
			}
			i++;
			//process the inputs
			BindingSet solution = executeSelectQueryAsTuples.next();
			Binding b = solution.getBinding("o");
			String fileContent = b.getValue().stringValue();
			String subject = solution.getBinding("s").getValue().stringValue();
			LOG.info("Processing new file for subject {}", subject);
			//log.debug("Processing file {}", fileContent);
			String inputFilePath = pathToWorkingDir + File.separator + String.valueOf(i) + ".xml";
			//store the input content to file, inputs are xml files!
			File file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath);
			if (file == null) {
				LOG.warn("Problem processing object for subject {}", subject);
				continue;
			}
			//run URI Generator
			String outputURIGeneratorFilename = pathToWorkingDir + File.separator + "outURIGen" + File.separator + String.valueOf(i) + ".xml";
			DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGen" + File.separator);
			runURIGenerator(inputFilePath, outputURIGeneratorFilename, config.getStoredXsltFilePath(), context) ;
			//check output
			if (!outputGenerated(outputURIGeneratorFilename)) {
				continue;
			}
			LOG.info("URI generator successfully executed, creating output");
			//RDF DataUnit OUTPUT
			if (rdfOutput != null) {
				String outputString = DataUnitUtils.readFile(outputURIGeneratorFilename);

				SimpleRdfWrite rdfOutputWrap = new SimpleRdfWrite(rdfOutput, context);	
				final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();

				Resource subj = valueFactory.createURI(subject);
				URI pred = valueFactory.createURI(config.getOutputPredicate());
				Value obj = valueFactory.createLiteral(outputString);

				String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);

				DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
				String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".ttl";

				DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
				rdfOutputWrap.extract(new File(tempFileLoc), RDFFormat.TURTLE, null);
			}
			//log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
			LOG.info("RF Output successfully created");
			//End of output creation
			//FILE DataUnit OUTPUT
			if (fileOutput != null) {
				DirectoryHandler rootDir = fileOutput.getRootDir();
				FileHandler addedFile = rootDir.addExistingFile(new File(outputURIGeneratorFilename), new OptionsAdd(false));
				//add(new File(outputURIGeneratorFilename), false);

				LOG.info("File Output successfully created");
				//End of output creation
			}

			//Add metadata triples 
//               <http://file/i> <http://linked.opendata.cz/ontology/odcs/dataunit/file/filePath> "/input01.xml" .
//               <http://xxx> <http://linked.opendata.cz/ontology/odcs/dataunit/file/fileURI> <http://linked.opendata.cz/resource/file/legislation/cz/decision/2013/8-Tdo-873-2013> .
			//to put metadata about the processed files (subject URIs) for XSLT -> used by RDFa XSLT


//               Resource  subj = rdfOutput.createURI("http://file/" + i);
//               URI pred = rdfOutput.createURI(OdcsTerms.DATA_UNIT_FILE_PATH_PREDICATE);
//               Value obj = rdfOutput.createLiteral(addedFile.getRootedPath());
//               
//               rdfOutput.addTriple(subj, pred, obj);
//               
//               subj = rdfOutput.createURI("http://file/" + i);
//               pred = rdfOutput.createURI(OdcsTerms.DATA_UNIT_FILE_URI_PREDICATE);
//                String subjectURI = subjectURIPrefix + year + "/" + spZn;
//               obj = rdfOutput.createLiteral(subject);
//               
//                rdfOutput.addTriple(subj, pred, obj);
//               //end of adding metadata triples

//               String subjectURIPrefix = "http://linked.opendata.cz/resource/file/legislation/cz/decision/";
//               String year = null;
//               String spZn = null;
//               
//               try {
//               DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//                DocumentBuilder builder = factory.newDocumentBuilder();
//                Document doc = builder.parse(new File(outputURIGeneratorFilename));
//                XPathFactory xPathfactory = XPathFactory.newInstance();
//                XPath xpath = xPathfactory.newXPath();
//                XPathExpression expr = xpath.compile("normalize-space(substring-after(substring-after(document/metadata/table//td[preceding-sibling::node()[contains(text(),'Datum podání')]]/normalize-space(text()),'.'),'.'))"); 
//                year = (String) expr.evaluate(doc, XPathConstants.STRING);
//
//                if (year.matches("[0-9]{4}")) {  
//                    log.debug("Building subject URI for the file, year is: {}", year);
//                }  
//                else {
//                    log.warn("Problem building subject URI for the given file, year taken from decision's metadata is not valid: {}", year);
//                    log.warn("No metadata is stored for this file");
//                    continue;
//                }
//
//
//
//
//                  XPathExpression expr2 = xpath.compile("replace(replace(replace(replace(document/metadata/table//td[preceding-sibling::node()[contains(text(),'Spisová značka')]]/normalize-space(text()),'\\.','-'),' ','-'),'Ú','U'),'/','-')"); 
//                spZn = (String) expr2.evaluate(doc, XPathConstants.STRING);
//
//                if (spZn.matches("[0-9]{4}")) {  
//                    log.debug("Building subject URI for the file, spZn is: {}", spZn);
//                }  
//                else {
//                    log.warn("Problem building subject URI for the given file, spZn taken from decision's metadata is not valid: {}", spZn);
//                    log.warn("No metadata is stored for this file");
//                    continue;
//                }
//
//                    
//                } catch (XPathExpressionException ex) {
//                    log.error(ex.getLocalizedMessage());
//                } catch (ParserConfigurationException ex) {
//                    log.error(ex.getLocalizedMessage());
//                } catch (SAXException ex) {
//                    log.error(ex.getLocalizedMessage());
//                } catch (IOException ex) {
//                    log.error(ex.getLocalizedMessage());
//                }
//
//
//               
//              
		}
		LOG.info("Processed {} files - values of predicate {}", i, config.getInputPredicate());
	}

	private void runURIGenerator(String file, String output, String configURiGen,
			DPUContext context) {
		//log.info("About to run URI generator for {}", file);
		IntLibLink.processFiles(file, output, configURiGen, context);

	}

    private boolean outputGenerated(String output) {
        File f = new File(output);
        if (!f.exists()) {
            LOG.warn("File {} was not created", output);
            LOG.warn("Skipping rest of the steps for the given file");
            return false;
        } else {
            LOG.info("File {} was generated as result of URI generator",
                    output);
            return true;
        }
    }

    private static void unzip(String source, String destination) throws IOException, ZipException {

        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted()) {
                LOG.error("Zip encrypted");
            }
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            LOG.error("Error {}", e.getLocalizedMessage());
        }
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
