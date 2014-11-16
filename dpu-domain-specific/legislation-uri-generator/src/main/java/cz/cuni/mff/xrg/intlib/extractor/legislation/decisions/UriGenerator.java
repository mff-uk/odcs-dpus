package cz.cuni.mff.xrg.intlib.extractor.legislation.decisions;

import cz.cuni.mff.xrg.intlib.extractor.legislation.decisions.uriGenerator.IntLibLink;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;


import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelpers;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple XSLT Extractor
 *
 * @author tomasknap
 */
@DPU.AsTransformer
public class UriGenerator extends DpuAdvancedBase<UriGeneratorConfig> {

    private static final Logger LOG = LoggerFactory.getLogger(
            UriGenerator.class);

    public UriGenerator() {
        super(UriGeneratorConfig.class, AddonInitializer.noAddons());
    }
	
     @DataUnit.AsInput(name = "input")
    public FilesDataUnit filesInput;
    
//    //should be used when subject URI is needed in further DPUs (such as in XSLT producing result of the transformation in literal)
//    @OutputDataUnit(name = "rdfOutput", optional = true)
//    public WritableRDFDataUnit rdfOutput;
//    
    //used for data when subject URI is further not needed.
    @DataUnit.AsOutput (name = "output")
    public WritableFilesDataUnit filesOutput;

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new UriGeneratorDialog();
    }

    
     @Override
    protected void innerExecute() throws DPUException, DataUnitException {
        
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
        try {
            FilesDataUnit.Iteration filesIteration = filesInput.getIteration();

            if (!filesIteration.hasNext()) {
                return;
            }

            //iterate over files 
            int i = 0;
            while (filesIteration.hasNext()) {

                i++;
                FilesDataUnit.Entry entry = filesIteration.next();
                
                 //Extracting file entry, symbolic name 2003/0062/pr0062-2003_original.xml path URI file:/home/tkn/data/UZ_HTML/predpisy/2003/0062/pr0062-2003_original.xml
                LOG.debug("Working with file entry, symbolic name " + entry.getSymbolicName() + " path URI " + entry.getFileURIString());
              
                //remove "file:" from file path
                String entryFilePath = entry.getFileURIString().substring("file:".length());
           
                //run URI Generator
                String outputURIGeneratorFilename = pathToWorkingDir + File.separator + "outURIGen" + File.separator + String.valueOf(i) + ".xml";
                Utils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGen" + File.separator);
                runURIGenerator(entryFilePath, outputURIGeneratorFilename, config.getStoredXsltFilePath(), context) ;
                //check output
                if (!outputGenerated(outputURIGeneratorFilename)) {
                        continue;
                }
                LOG.info("URI generator successfully executed, creating output");
                
                
                
               //OUTPUT
               File newFileToBeAdded = new File(outputURIGeneratorFilename);
               filesOutput.addExistingFile(entry.getSymbolicName(), newFileToBeAdded.toURI().toASCIIString());
                
                //set up virtual path of the output, so that the loader to file at the end knows under which name the output should be stored. 
               String outputVirtualPath = VirtualPathHelpers.getVirtualPath(filesInput, entry.getSymbolicName());
               if (outputVirtualPath != null) {
                VirtualPathHelpers.setVirtualPath(filesOutput, entry.getSymbolicName(), outputVirtualPath);
               }
               
                
                LOG.info("Output created successfully, sn: {}, file: {}", entry.getSymbolicName(), newFileToBeAdded.toURI().toASCIIString());
                
                
                if (context.canceled()) {
                        LOG.info("DPU cancelled");
                        return;
                }
                
                

                                
            }
        } catch (DataUnitException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Error when extracting.", "", ex);
        }
        
        
        
        
////        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
//        String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o} ORDER BY ?s ?o";
//        LOG.debug("Query for getting input files: {}", query);
//        //get the return values
//        //Map<String, List<String>> executeSelectQuery = rdfInput.executeSelectQuery(query);
//        //        TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
//		
//		SimpleRdfRead rdfInputWrap = SimpleRdfFactory.create(rdfInput, context);
//		try (ConnectionPair<TupleQueryResult> queryRes = rdfInputWrap.executeSelectQuery(query)) {
//			processQueryResult(queryRes.getObject(), context,	pathToWorkingDir);
//		} catch (QueryEvaluationException ex) {
//			context.sendMessage(MessageType.ERROR, "Problem evaluating the query to obtain files to be processed. Processing ends.", ex.getLocalizedMessage());
//			LOG.error("Problem evaluating the query to obtain values of the {} literals. Processing ends.", config.getInputPredicate());
//			LOG.debug(ex.getLocalizedMessage());
//		}
    }

//	private void processQueryResult(
//			TupleQueryResult executeSelectQueryAsTuples, DPUContext context,
//			String pathToWorkingDir) throws DataUnitException, QueryEvaluationException {
//		//log.info(executeSelectQueryAsTuples.asList().)
//		int i = 0;
//
//		while (executeSelectQueryAsTuples.hasNext()) {
//			if (context.canceled()) {
//				LOG.info("DPU cancelled");
//				return;
//			}
//			i++;
//			//process the inputs
//			BindingSet solution = executeSelectQueryAsTuples.next();
//			Binding b = solution.getBinding("o");
//			String fileContent = b.getValue().stringValue();
//			String subject = solution.getBinding("s").getValue().stringValue();
//			LOG.info("Processing new file for subject {}", subject);
//			//log.debug("Processing file {}", fileContent);
//			String inputFilePath = pathToWorkingDir + File.separator + String.valueOf(i) + ".xml";
//			//store the input content to file, inputs are xml files!
//			File file = DataUnitUtils.storeStringToTempFile(removeTrailingQuotes(fileContent), inputFilePath);
//			if (file == null) {
//				LOG.warn("Problem processing object for subject {}", subject);
//				continue;
//			}
//			//run URI Generator
//			String outputURIGeneratorFilename = pathToWorkingDir + File.separator + "outURIGen" + File.separator + String.valueOf(i) + ".xml";
//			DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outURIGen" + File.separator);
//			runURIGenerator(inputFilePath, outputURIGeneratorFilename, config.getStoredXsltFilePath(), context) ;
//			//check output
//			if (!outputGenerated(outputURIGeneratorFilename)) {
//				continue;
//			}
//			LOG.info("URI generator successfully executed, creating output");
//			//RDF DataUnit OUTPUT
//			if (rdfOutput != null) {
//				String outputString = DataUnitUtils.readFile(outputURIGeneratorFilename);
//
//				SimpleRdfWrite rdfOutputWrap = SimpleRdfFactory.create(rdfOutput, context);	
//				final ValueFactory valueFactory = rdfOutputWrap.getValueFactory();
//
//				Resource subj = valueFactory.createURI(subject);
//				URI pred = valueFactory.createURI(config.getOutputPredicate());
//				Value obj = valueFactory.createLiteral(outputString);
//
//				String preparedTriple = AddTripleWorkaround.prepareTriple(subj, pred, obj);
//
//				DataUnitUtils.checkExistanceOfDir(pathToWorkingDir + File.separator + "out");
//				String tempFileLoc = pathToWorkingDir + File.separator + "out" + File.separator + String.valueOf(i) + ".ttl";
//
//				DataUnitUtils.storeStringToTempFile(preparedTriple, tempFileLoc);
//				rdfOutputWrap.extract(new File(tempFileLoc), RDFFormat.TURTLE, null);
//			}
//			//log.debug("Result was added to output data unit as turtle data containing one triple {}", preparedTriple);
//			LOG.info("RF Output successfully created");
//			//End of output creation
//			//FILE DataUnit OUTPUT
//			if (fileOutput != null) {
//				DirectoryHandler rootDir = fileOutput.getRootDir();
//				FileHandler addedFile = rootDir.addExistingFile(new File(outputURIGeneratorFilename), new OptionsAdd(false));
//				//add(new File(outputURIGeneratorFilename), false);
//
//				LOG.info("File Output successfully created");
//				//End of output creation
//			}
//
//      
//		}
//		LOG.info("Processed {} files - values of predicate {}", i, config.getInputPredicate());
//	}

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
