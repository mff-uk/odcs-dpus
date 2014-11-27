package cz.cuni.mff.xrg.odcs.loader.ftp;

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
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@AsLoader
public class Main extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	@InputDataUnit(name = "input")
	public FileDataUnit inputDataUnit;

	public Main() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context) throws DPUException, DataUnitException, InterruptedException {
		FTPClient client;
		if (config.isSFTP()) {
			LOG.debug("SFTP is used, protocol: {} is implicit: {}", config
					.getProtocol(),	config.isImplicit());
			client = new FTPSClient(config.getProtocol(), config.isImplicit());
		} else {
			client = new FTPClient();
			LOG.debug("FTP used");
		}

		if (context.isDebugging()) {
			client.addProtocolCommandListener(new ProtocolCommandListener() {

				@Override
				public void protocolCommandSent(ProtocolCommandEvent event) {
					LOG.trace("ftp > {}", event.getMessage());
				}

				@Override
				public void protocolReplyReceived(ProtocolCommandEvent event) {
					LOG.trace("ftp < {}", event.getMessage());
				}
			});
		}
		
		try {
			LOG.debug("Connecting to {} port {}", config.getHost(),
					config.getPort());
			client.connect(config.getHost(), config.getPort());
			
			if (config.isSFTP()) {
				// Set protection buffer size
				((FTPSClient)client).execPBSZ(0);
				// Set data channel protection to private
				((FTPSClient)client).execPROT("P");
			}
			
			if (config.getUser() != null) {
				LOG.debug("User: {} password: {}", config.getUser(),
						config.getPassword());
				client.login(config.getUser(), config.getPassword());
			} else {
				LOG.debug("No authorization used.");
			}

			int reply = client.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				// failed to login, connect
				client.disconnect();
				context.sendMessage(MessageType.ERROR,
						"Server reply: " + client.getReplyString());
			}

			// the target path must end with separator
			String targetPath = config.getTargetPath();
			if (!targetPath.endsWith("/")) {
				targetPath = targetPath + "/";
			}

			// transfer
			transfer(client, inputDataUnit.getRootDir(), targetPath);
		} catch (IOException | DPUException ex) {
			LOG.error("Data transfer failed", ex);
			context.sendMessage(MessageType.ERROR, "Failed to upload data.");
		} finally {
			try {
				client.logout();
				client.disconnect();
			} catch (IOException e) {
				LOG.error("Failed to disconnect in catch block.", e);
			}
		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	/**
	 * Transfer given represented directory and it's content using given FTP
	 * client to target destination.
	 *
	 * @param client
	 * @param dirHandler
	 * @param target
	 * @throws IOException
	 */
	private void transfer(FTPClient client, DirectoryHandler dirHandler,
			String target) throws IOException, DPUException {
		// makeDirectory
		for (Handler handler : dirHandler) {
			if (handler instanceof FileHandler) {
				transfer(client, (FileHandler) handler, target);
			} else if (handler instanceof DirectoryHandler) {
				final String newTarget = target + handler.getName() + "/";
				LOG.debug("Creating directory {}", newTarget);
				client.makeDirectory(newTarget);

				if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
					// failed to transfer
					LOG.warn("Error response for dir. creation: {}", 
							client.getReplyString());
				} else {
					LOG.debug("Response: {}", client.getReplyString());
				}

				transfer(client, (DirectoryHandler) handler, newTarget);
			}
		}
	}

	/**
	 * Transfer given file using given FPT client. The file is stored in given
	 * location.
	 *
	 * @param client
	 * @param fileHandler
	 * @param target
	 * @throws IOException
	 */
	private void transfer(FTPClient client, FileHandler fileHandler,
			String target) throws IOException, DPUException {
		
		client.changeWorkingDirectory(target);
		client.setFileType(FTP.BINARY_FILE_TYPE);
	
		client.enterLocalPassiveMode();
		
		LOG.debug("Transfer {} to {}", fileHandler.getRootedPath(),
				target + fileHandler.getName());
		try (InputStream input = new FileInputStream(fileHandler.asFile())) {
			if (!client.storeFile(fileHandler.getName(), input)) {
				LOG.error("storeFile return false");
				throw new DPUException("Failed to store file.");
			}
		}
			
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			// failed to transfer
			LOG.warn("Error response for file store: {}", 
					client.getReplyString());
		} else {
			LOG.debug("Response: {}", client.getReplyString());
		}
	}

}
