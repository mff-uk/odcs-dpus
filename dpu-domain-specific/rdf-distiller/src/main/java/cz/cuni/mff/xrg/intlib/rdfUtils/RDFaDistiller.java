package cz.cuni.mff.xrg.intlib.rdfUtils;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Simple XSLT Extractor
 *
 *
 * @author tomasknap
 */
@DPU.AsTransformer
public class RDFaDistiller extends DpuAdvancedBase<RDFaDistillerConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(RDFaDistiller.class);

	public RDFaDistiller() {
		super(RDFaDistillerConfig.class, AddonInitializer.noAddons());
	}

	 @DataUnit.AsInput(name = "input")
	public FilesDataUnit filesInput;

	 @DataUnit.AsOutput (name = "output")
	public WritableFilesDataUnit filesOutput;

	@Override
	 public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
		return new RDFDistillerDialog();
	}

        @Override
        protected void innerExecute() throws DPUException, DataUnitException {
            
		LOG.info(
				"\n ****************************************************** \n RDFa Distiller \n *****************************************************");

		//get working dir
		File workingDir = context.getWorkingDir();
		workingDir.mkdirs();

		String pathToWorkingDir = null;
		try {
			pathToWorkingDir = workingDir.getCanonicalPath();
		} catch (IOException ex) {
			LOG.error("Problem of getting path to the working dir");
			LOG.debug(ex.getLocalizedMessage());
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
                
               LOG.info("Distiller is about to be executed");
               String inputFilePath = entryFilePath;
               String outputFilePath = pathToWorkingDir + File.separator + "outDistiller" + File.separator + String
						.valueOf(i) + ".ttl";
				Utils.checkExistanceOfDir(pathToWorkingDir + File.separator + "outDistiller" + File.separator);
               
               	try {
                    Process p = Runtime.getRuntime().exec("java -jar /data/uv/libs/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" + outputFilePath);

                    //java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
					printProcessOutput(p);
				} catch (IOException ex) {
					LOG.error(ex.getLocalizedMessage());
					context.sendMessage(MessageType.ERROR, "Problem executing: "
							+ ex.getMessage());
				}
               
                LOG.info("Distiller was executed, output is being prepared");
                
               //OUTPUT
               File newFileToBeAdded = new File(outputFilePath);
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
                
    }    
                
                
                
                
//        //prepare inputs, call xslt for each input
//		//String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
//		String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o} ORDER BY ?s ?o";
//		LOG.debug("Query for getting input files: {}", query);
//        //get the return values
//		//TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
//		OrderTupleQueryResult executeSelectQueryAsTuples = rdfInput
//				.executeOrderSelectQueryAsTuples(query);
//
//		int i = 0;
//		try {
//
//			while (executeSelectQueryAsTuples.hasNext()) {
//
//				i++;
//				//process the inputs
//				BindingSet solution = executeSelectQueryAsTuples.next();
//				Binding b = solution.getBinding("o");
//				String fileContent = b.getValue().toString();
//				String subject = solution.getBinding("s").getValue().toString();
//				LOG.info("Processing new file for subject {}", subject);
//                //log.debug("Processing file {}", fileContent);
//
//				String inputFilePath = pathToWorkingDir + File.separator + String
//						.valueOf(i) + ".xml";
//
//				String outputFilePath = pathToWorkingDir + File.separator + "outDistiller" + File.separator + String
//						.valueOf(i) + ".ttl";
//				DataUnitUtils.checkExistanceOfDir(
//						pathToWorkingDir + File.separator + "outDistiller" + File.separator);
//
//				//store the input content to file, inputs are xml files!
//				File file = DataUnitUtils.storeStringToTempFile(decode(
//						removeTrailingQuotes(fileContent)), inputFilePath);
//				if (file == null) {
//					LOG
//							.warn("Problem processing object for subject {}",
//									subject);
//					continue;
//				}
//
//				LOG.info("Distiller is about to be executed");
//
////                try {
////                    CharOutputSink outputSink = new CharOutputSink();
////                    StreamProcessor sp = new StreamProcessor(RdfaParser.connect(TurtleSerializer.connect(outputSink)));
////
////                    outputSink.connect(new File(outputFilePath));
////                    // properties can be changed both for single pipe and its children (if they exist)
////                    //outputSink.setProperty(CharOutputSink.CHARSET_PROPERTY, "UTF-8");
////                    sp.process(file);
////                } catch (ParseException ex) {
////                    log.error("RDFa extraction failed");
////                    log.debug(ex.getLocalizedMessage());
////                }
//                //Rio.parse(inputStream, "", RDFFormat.RDFA);
//				try {
//            //logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
////            Process p = Runtime.getRuntime().exec("java -DconfigFile=/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/be-sameAs.xml -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");
//					//log.debug("About to execute: java -jar /Users/tomasknap/NetBeansProjects/RDFaDistiller/target/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" +outputFilePath);
//
//					//Process p = Runtime.getRuntime().exec("java -jar /Users/tomasknap/NetBeansProjects/RDFaDistiller/target/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" +outputFilePath);
//					Process p = Runtime.getRuntime().exec(
//							"java -jar /data/odcs/libs/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" + outputFilePath);
//
//            //java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
//					printProcessOutput(p);
//				} catch (IOException ex) {
//					LOG.error(ex.getLocalizedMessage());
//					context.sendMessage(MessageType.ERROR, "Problem executing: "
//							+ ex.getMessage());
//				}
//
////                     try {
////            //logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
//////            Process p = Runtime.getRuntime().exec("java -DconfigFile=/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/be-sameAs.xml -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");
////            Process p = Runtime.getRuntime().exec("java -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/java-rdfa-0.4.3-SNAPSHOT.jar file:///" + inputFilePath + " > " + outputFilePath);
////
////            //java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
////            
////            printProcessOutput(p);
////        } catch (IOException ex) {
////            log.error(ex.getLocalizedMessage());
////            context.sendMessage(MessageType.ERROR, "Problem executing: "
////                    + ex.getMessage());
////        }
//				LOG.info("Distiller was executed, output is being prepared");
//
//				try {
//					LOG.info("Output file name is: {}", outputFilePath);
//					
//					final SimpleRdfWrite rdfOutputWrap = SimpleRdfFactory.create(rdfOutput, context);	
//					rdfOutputWrap.extract(new File(outputFilePath), RDFFormat.TURTLE, null);
//				} catch (OperationFailedException ex) {
//					LOG.error("OperationFailed", ex);
//					context.sendMessage(MessageType.ERROR, "OperationFailed: "
//							+ ex.getMessage());
//				}
//
//				if (context.canceled()) {
//					LOG.info("DPU cancelled");
//					return;
//				}
//
//			}
//		} catch (QueryEvaluationException ex) {
//			context.sendMessage(MessageType.ERROR,
//					"Problem evaluating the query to obtain files to be processed. Processing ends.",
//					ex.getLocalizedMessage());
//			LOG.error(
//					"Problem evaluating the query to obtain values of the {} literals. Processing ends.",
//					config.getInputPredicate());
//			LOG.debug(ex.getLocalizedMessage());
//		}
//
//		LOG.info("Processed {} files - values of predicate {}", i, config
//				.getInputPredicate());
//
//	}

//	public static String encode(String literalValue, String escapedMappings) {
//
//		String val = literalValue;
//		String[] split = escapedMappings.split("\\s+");
//		for (String s : split) {
//			String[] keyAndVal = s.split(":");
//			if (keyAndVal.length == 2) {
//				val = val.replaceAll(keyAndVal[0], keyAndVal[1]);
//				LOG
//						.debug("Encoding mapping {} to {} was applied.",
//								keyAndVal[0], keyAndVal[1]);
//
//			} else {
//				LOG.warn(
//						"Wrong format of escaped character mappings, skipping the mapping");
//
//			}
//		}
//		return val;
//
//	}

	private static void printProcessOutput(Process process) {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(process
					.getErrorStream()));
			String line = "";
			while ((line = in.readLine()) != null) {
				LOG.warn(line);
			}
			in.close();

			in = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			line = "";
			while ((line = in.readLine()) != null) {
				//log.debug(line);
			}
			in.close();
		} catch (Exception e) {
			LOG.warn("Vyjimka... " + e);
		}
	}

//	private String removeTrailingQuotes(String fileContent) {
//
//		if (fileContent.startsWith("\"")) {
//			fileContent = fileContent.substring(1);
//		}
//		if (fileContent.endsWith("\"")) {
//			fileContent = fileContent.substring(0, fileContent.length() - 1);
//		}
//		return fileContent;
//	}
//
//	//TODO hardcoded!
//	private String decode(String input) {
//		return input;
//		//return input.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quote;", "\"");
//	}

 	
}
