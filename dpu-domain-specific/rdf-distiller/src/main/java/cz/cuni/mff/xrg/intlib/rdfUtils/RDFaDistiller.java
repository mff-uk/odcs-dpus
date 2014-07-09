package cz.cuni.mff.xrg.intlib.rdfUtils;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.module.utils.DataUnitUtils;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.odcs.rdf.help.OrderTupleQueryResult;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;

/**
 * Simple XSLT Extractor
 *
 *
 * @author tomasknap
 */
@AsTransformer
public class RDFaDistiller extends ConfigurableBase<RDFaDistillerConfig>
		implements ConfigDialogProvider<RDFaDistillerConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(RDFaDistiller.class);

	public RDFaDistiller() {
		super(RDFaDistillerConfig.class);
	}

	@InputDataUnit(name = "input")
	public RDFDataUnit rdfInput;

	@OutputDataUnit(name = "output")
	public WritableRDFDataUnit rdfOutput;

	@Override
	public AbstractConfigDialog<RDFaDistillerConfig> getConfigurationDialog() {
		return new RDFDistillerDialog();
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException {

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
		//String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o}";
		String query = "SELECT ?s ?o where {?s <" + config.getInputPredicate() + "> ?o} ORDER BY ?s ?o";
		LOG.debug("Query for getting input files: {}", query);
        //get the return values
		//TupleQueryResult executeSelectQueryAsTuples = rdfInput.executeSelectQueryAsTuples(query);
		OrderTupleQueryResult executeSelectQueryAsTuples = rdfInput
				.executeOrderSelectQueryAsTuples(query);

		int i = 0;
		try {

			while (executeSelectQueryAsTuples.hasNext()) {

				i++;
				//process the inputs
				BindingSet solution = executeSelectQueryAsTuples.next();
				Binding b = solution.getBinding("o");
				String fileContent = b.getValue().toString();
				String subject = solution.getBinding("s").getValue().toString();
				LOG.info("Processing new file for subject {}", subject);
                //log.debug("Processing file {}", fileContent);

				String inputFilePath = pathToWorkingDir + File.separator + String
						.valueOf(i) + ".xml";

				String outputFilePath = pathToWorkingDir + File.separator + "outDistiller" + File.separator + String
						.valueOf(i) + ".ttl";
				DataUnitUtils.checkExistanceOfDir(
						pathToWorkingDir + File.separator + "outDistiller" + File.separator);

				//store the input content to file, inputs are xml files!
				File file = DataUnitUtils.storeStringToTempFile(decode(
						removeTrailingQuotes(fileContent)), inputFilePath);
				if (file == null) {
					LOG
							.warn("Problem processing object for subject {}",
									subject);
					continue;
				}

				LOG.info("Distiller is about to be executed");

//                try {
//                    CharOutputSink outputSink = new CharOutputSink();
//                    StreamProcessor sp = new StreamProcessor(RdfaParser.connect(TurtleSerializer.connect(outputSink)));
//
//                    outputSink.connect(new File(outputFilePath));
//                    // properties can be changed both for single pipe and its children (if they exist)
//                    //outputSink.setProperty(CharOutputSink.CHARSET_PROPERTY, "UTF-8");
//                    sp.process(file);
//                } catch (ParseException ex) {
//                    log.error("RDFa extraction failed");
//                    log.debug(ex.getLocalizedMessage());
//                }
                //Rio.parse(inputStream, "", RDFFormat.RDFA);
				try {
            //logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
//            Process p = Runtime.getRuntime().exec("java -DconfigFile=/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/be-sameAs.xml -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");
					//log.debug("About to execute: java -jar /Users/tomasknap/NetBeansProjects/RDFaDistiller/target/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" +outputFilePath);

					//Process p = Runtime.getRuntime().exec("java -jar /Users/tomasknap/NetBeansProjects/RDFaDistiller/target/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" +outputFilePath);
					Process p = Runtime.getRuntime().exec(
							"java -jar /data/odcs/libs/RDFaDistiller-1.0-SNAPSHOT-jar-with-dependencies.jar -inputFile=file:///" + inputFilePath + " -outputFile=" + outputFilePath);

            //java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
					printProcessOutput(p);
				} catch (IOException ex) {
					LOG.error(ex.getLocalizedMessage());
					context.sendMessage(MessageType.ERROR, "Problem executing: "
							+ ex.getMessage());
				}

//                     try {
//            //logger.debug(perlIntro + "jtagger/txt2vxml.pl /tmp/jtagger/txt /tmp/jtagger/txt_source/judikatura.zakon.txt");
////            Process p = Runtime.getRuntime().exec("java -DconfigFile=/Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/be-sameAs.xml -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/silk_2.5.2/silk.jar");
//            Process p = Runtime.getRuntime().exec("java -jar /Users/tomasknap/Documents/PROJECTS/ETL-SWProj/intlib/tmp/java-rdfa-0.4.3-SNAPSHOT.jar file:///" + inputFilePath + " > " + outputFilePath);
//
//            //java -jar java-rdfa-0.4.jar http://examples.tobyinkster.co.uk/hcard
//            
//            printProcessOutput(p);
//        } catch (IOException ex) {
//            log.error(ex.getLocalizedMessage());
//            context.sendMessage(MessageType.ERROR, "Problem executing: "
//                    + ex.getMessage());
//        }
				LOG.info("Distiller was executed, output is being prepared");

				try {
					LOG.info("Output file name is: {}", outputFilePath);
					
					final SimpleRdfWrite rdfOutputWrap = SimpleRdfFactory.create(rdfOutput, context);	
					rdfOutputWrap.extract(new File(outputFilePath), RDFFormat.TURTLE, null);
				} catch (OperationFailedException ex) {
					LOG.error("OperationFailed", ex);
					context.sendMessage(MessageType.ERROR, "OperationFailed: "
							+ ex.getMessage());
				}

				if (context.canceled()) {
					LOG.info("DPU cancelled");
					return;
				}

			}
		} catch (QueryEvaluationException ex) {
			context.sendMessage(MessageType.ERROR,
					"Problem evaluating the query to obtain files to be processed. Processing ends.",
					ex.getLocalizedMessage());
			LOG.error(
					"Problem evaluating the query to obtain values of the {} literals. Processing ends.",
					config.getInputPredicate());
			LOG.debug(ex.getLocalizedMessage());
		}

		LOG.info("Processed {} files - values of predicate {}", i, config
				.getInputPredicate());

	}

	public static String encode(String literalValue, String escapedMappings) {

		String val = literalValue;
		String[] split = escapedMappings.split("\\s+");
		for (String s : split) {
			String[] keyAndVal = s.split(":");
			if (keyAndVal.length == 2) {
				val = val.replaceAll(keyAndVal[0], keyAndVal[1]);
				LOG
						.debug("Encoding mapping {} to {} was applied.",
								keyAndVal[0], keyAndVal[1]);

			} else {
				LOG.warn(
						"Wrong format of escaped character mappings, skipping the mapping");

			}
		}
		return val;

	}

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

	private String removeTrailingQuotes(String fileContent) {

		if (fileContent.startsWith("\"")) {
			fileContent = fileContent.substring(1);
		}
		if (fileContent.endsWith("\"")) {
			fileContent = fileContent.substring(0, fileContent.length() - 1);
		}
		return fileContent;
	}

	//TODO hardcoded!
	private String decode(String input) {
		return input;
		//return input.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quote;", "\"");
	}
	
}
