package cz.cuni.mff.xrg.odcs.transformer.converter.rdf.csv;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.openrdf.query.*;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.text.csv.SPARQLResultsCSVWriterFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@AsTransformer
public class Main extends ConfigurableBase<Configuration> implements
		ConfigDialogProvider<Configuration> {
	
	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	@InputDataUnit
	public RDFDataUnit input;
	
	@OutputDataUnit
	public FileDataUnit output;	

	public Main() {
		super(Configuration.class);
	}
	
	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		
		final String[] filePath = config.getTargetPath().split("/", -1);
		// add subdirs
		for (int i = 0; i < filePath.length - 1; i++) {
			if (filePath[i].isEmpty()) {
				continue;
			}
			dir = dir.addNewDirectory(filePath[i]);
		}
		final File outFile = dir.addNewFile(filePath[filePath.length - 1]).asFile();
	
				
		RepositoryConnection connection = null;
		try (OutputStream outputStream = new FileOutputStream(outFile)) {
			connection = input.getConnection();			
			
			// prepare resultwriter
			SPARQLResultsCSVWriterFactory writerFactory = new SPARQLResultsCSVWriterFactory();
			TupleQueryResultWriter resultWriter =  writerFactory.getWriter(outputStream);
			// write result
			connection.prepareTupleQuery(QueryLanguage.SPARQL, config.getQuery()).evaluate(resultWriter);
		} catch (IOException ex) {
			LOG.warn("IOException", ex);
			context.sendMessage(MessageType.ERROR, "DPU failed because of IOException.");
		} catch (RepositoryException ex) {
			LOG.warn("RepositoryException", ex);
			context.sendMessage(MessageType.ERROR, "DPU failed because of RepositoryException.");
		} catch (MalformedQueryException ex) {
			LOG.warn("MalformedQueryException", ex);
			context.sendMessage(MessageType.ERROR, "Invalid query.");
		} catch (QueryEvaluationException ex) {
			LOG.warn("QueryEvaluationException", ex);
			context.sendMessage(MessageType.ERROR, "Failed to evaluate query.");
		} catch (TupleQueryResultHandlerException ex) {
			LOG.warn("TupleQueryResultHandlerException", ex);
			context.sendMessage(MessageType.ERROR, "Filed to write results.");
		} finally {
			// in every case close conneciton
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (RepositoryException ex) {
				LOG.warn("Faield to get connection.", ex);
				context.sendMessage(MessageType.WARNING, "Failed to close conneciton.");
			}
		}
	}
	
	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}
	
}
