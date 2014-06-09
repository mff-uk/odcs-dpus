package cz.cuni.mff.xrg.odcs.loader.scp;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsLoader;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.marcoratto.scp.SCP;
import uk.co.marcoratto.scp.listeners.SCPListenerPrintStream;

/**
 * Upload data to server by SCP.
 *
 * @author Škoda Petr
 */
@AsLoader
public class DPU extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(DPU.class);

	@InputDataUnit(name = "input")
	public FileDataUnit input;

	public DPU() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) 
			throws DPUException, DataUnitException, InterruptedException {
		SCP scp = new SCP(new SCPListenerPrintStream());
		scp.setPort(config.getPort());
		scp.setPassword(config.getPassword());
		scp.setTrust(true);
		// non recursion we copy ourselfs
		scp.setRecursive(false);
		
		// prepare destination
		final String destination = config.getUsername() + '@'
				+ config.getHostname() + ':' + config.getDestination();

		LOG.debug("Global destination: {}", destination);
		
		SCPFacade scpFacade = new SCPFacade();
		try {
			scpFacade.upload(scp, input, destination);
		} catch (SCPFacadeException ex) {
			LOG.error("Failed to upload file/directory.", ex);
			final String msg = ex.getMessage();
			
			if (config.isSoftFail()) {
				context.sendMessage(MessageType.WARNING, msg, "See logs for more details.", ex);
			} else {
				context.sendMessage(MessageType.ERROR, msg, "See logs for more details.", ex);
			}
		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

}
