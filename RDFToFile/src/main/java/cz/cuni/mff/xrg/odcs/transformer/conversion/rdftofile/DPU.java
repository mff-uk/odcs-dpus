package cz.cuni.mff.xrg.odcs.transformer.conversion.rdftofile;

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
import java.io.*;
import java.nio.charset.Charset;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dump data from RDF data unit into a single file in output file data unit.
 *
 * @author Škoda Petr
 */
@AsTransformer
public class DPU extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(DPU.class);

	private static final String FILE_ENCODE = "UTF-8";
	
	@InputDataUnit(name = "input")
	public RDFDataUnit input;

	@OutputDataUnit(name = "output")
	public FileDataUnit output;

	public DPU() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		final RDFFormat format = config.getRDFFileFormat();

		// create output file
		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		final String[] filePath = config.getFileName().split("/");
		for (int i = 0; i < filePath.length - 1; i++) {
			dir = dir.addNewDirectory(filePath[i]);
		}
		final String outFileName = filePath[filePath.length - 1];
		final File dumpFile = dir.addNewFile(outFileName).asFile();

		load(dumpFile, format, context);
	}

	/**
	 *
	 * @return Array of input contexts.
	 */
	private URI[] getInputContexts() {
		return input.getContexts().toArray(new URI[0]);
	}

	/**
	 * Load data from {@link #input} data unit into given file.
	 *
	 * @param targetFile
	 * @param format
	 * @param context
	 */
	private void load(File targetFile, RDFFormat format, DPUContext context) {
		RepositoryConnection connection = null;
		try (FileOutputStream out = new FileOutputStream(targetFile);
				OutputStreamWriter os = new OutputStreamWriter(out, Charset
						.forName(FILE_ENCODE));) {
			connection = input.getConnection();

			long triplesCount = connection.size(getInputContexts());
			LOG.info("Loading {} triples into file {}", triplesCount, targetFile
					.toString());

			RDFWriter rdfWriter = Rio.createWriter(format, os);
			connection.export(rdfWriter, getInputContexts());
		} catch (RepositoryException | IOException | RDFHandlerException e) {
			LOG.error("Failed to write content of .graph file.", e);
			context.sendMessage(MessageType.ERROR,
					"Failed to write .graph file.");
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (RepositoryException e) {
					LOG.warn("Failed to close repository", e);
				}
			}
		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

}
