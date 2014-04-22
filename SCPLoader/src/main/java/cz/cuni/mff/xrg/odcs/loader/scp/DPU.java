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
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.marcoratto.scp.SCP;
import uk.co.marcoratto.scp.SCPPException;
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

	@InputDataUnit
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
			context.sendMessage(MessageType.ERROR, "Upload failed.", ex.getMessage());
		}

//		Iterator<Handler> iter = input.getRootDir().getFlatIterator();
//		while (iter.hasNext()) {
//			final Handler handler = iter.next();
//			if (handler instanceof FileHandler) {
//				LOG.debug("Uploading file: {}", handler.getRootedPath());
//				try {
//					// set source
//					scp.setFromUri(handler.asFile().toString());
//					// set destiantion
//					scp.setToUri(destination + handler.getRootedPath());
//					// transfer
//					scp.execute();
//				} catch (SCPPException ex) {
//					LOG.error("Failed to upload file.", ex);
//					context.sendMessage(MessageType.ERROR,
//							"Failed to upload file.", ex.getMessage());
//				}
//			} else if (handler instanceof DirectoryHandler) {
//				DirectoryHandler dir = (DirectoryHandler) handler;
//				LOG.debug("Uploading directory: {}", handler.getRootedPath());
//				try {
//					// set source
//					scp.setFromUri(handler.asFile().toString());
//					// set destiantion - here is the difference
//					scp.setToUri(destination);
//					// transfer
//					scp.execute();
//				} catch (SCPPException ex) {
//					LOG.error("Failed to upload directory.", ex);
//					context.sendMessage(MessageType.ERROR,
//							"Failed to upload directory.", ex.getMessage());
//				}
//				// this will also upload all non-link data in directory
//			}
//		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	/**
	 * Upload given file to given destination using given SCP connection.
	 *
	 * @param scp
	 * @param handler     File to upload.
	 * @param destination Server location to upload in, must start with '/'.
	 * @throws SCPPException
	 */
	private void upload(SCP scp, FileHandler handler, String destination) throws SCPPException {
		LOG.debug("Uploading file: {}", handler.getRootedPath());

		// set source
		scp.setFromUri(handler.asFile().toString());
		// set destiantion
		scp.setToUri(destination + destination);
		// transfer
		scp.execute();
	}

	/**
	 * Upload content of given directory to the given destination.
	 *
	 * @param scp
	 * @param handler     Directory to upload.
	 * @param destination Server location to upload in, must start with '/'.
	 * @throws SCPPException
	 */
	private void upload(SCP scp, DirectoryHandler handler, String destination)
			throws SCPPException {

	}

}
