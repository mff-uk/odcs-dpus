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
import cz.cuni.mff.xrg.odcs.rdf.enums.RDFFormatType;
import cz.cuni.mff.xrg.odcs.rdf.exceptions.RDFException;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;
import java.io.File;
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

	@InputDataUnit
	public RDFDataUnit input;

	@OutputDataUnit
	public FileDataUnit output;

	public DPU() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		final RDFFormatType formatType = config.getRDFFileFormat();

		// get file extension
		final String ext;
		switch (formatType) {
			case N3:
				ext = ".n3";
				break;
			case NT:
				ext = ".nt";
				break;
			case RDFXML:
				ext = ".rdf";
				break;
			case TRIG:
				ext = ".trig";
				break;
			case TRIX:
				ext = ".trix";
				break;
			case TTL:
				ext = ".ttl";
				break;
			default:
				throw new DPUException("Unwnown format: " + formatType.toString());
		}
		
		// create output file
		// prepare output file in denoted directories
		DirectoryHandler dir = output.getRootDir();
		final String [] filePath = config.getFileName().split("/");
		for (int i = 0; i < filePath.length - 1; i++) {
			dir = dir.addNewDirectory(filePath[i]);
		} 		
		File dumpFile = dir.addNewFile(filePath[filePath.length - 1] + ext).asFile();

		final long triplesCount = input.getTripleCount();
		LOG.info("Loading {} triples", triplesCount);

		try {
			input.loadToFile(dumpFile, formatType);
		} catch (RDFException ex) {
			context.sendMessage(MessageType.ERROR, ex.getMessage(), ex
					.fillInStackTrace().toString());
		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

}
