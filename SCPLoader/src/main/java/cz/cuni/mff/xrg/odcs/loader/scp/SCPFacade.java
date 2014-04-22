package cz.cuni.mff.xrg.odcs.loader.scp;

import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.FileHandler;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.Handler;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.marcoratto.scp.SCP;
import uk.co.marcoratto.scp.SCPPException;

/**
 *
 * @author Škoda Petr
 */
class SCPFacade {

	private static final Logger LOG = LoggerFactory.getLogger(SCPFacade.class);

	/**
	 * Used SCP connection.
	 */
	private SCP scp;

	/**
	 * List of paths to deeply loaded directories. As SCP load directories
	 * recursively, we need this list to prevent duplicity of items loading.
	 *
	 * Also recursively loaded directory can contains as-link files, so we can
	 * skip the whole directory.
	 */
	private final LinkedList<String> uploadedDir = new LinkedList<>();

	/**
	 * Upload file/directory represented by given handler to given destination.
	 *
	 * @param handler     Handler to upload.
	 * @param destination Server location to upload in, must start with '/'.
	 * @throws SCPPException
	 */
	private void upload(Handler handler, String destination) throws SCPPException {
		scp.setFromUri(handler.asFile().toString());
		scp.setToUri(destination);
		scp.execute();
	}

	/**
	 * Upload content of given file data unit into given location using given
	 * SCP.
	 *
	 * @param scp         Configured SCP connection.
	 * @param dataUnit
	 * @param destination
	 * @throws SCPPException
	 */
	public void upload(SCP scp, FileDataUnit dataUnit, String destination)
			throws SCPFacadeException {
		// initial setup
		this.scp = scp;
		this.uploadedDir.clear();
		// copy
		Iterator<Handler> iter = dataUnit.getRootDir().getFlatIterator();
		while (iter.hasNext()) {
			final Handler handler = iter.next();

			// check if it's not in already loaded directory
			boolean ignore = false;

			final String handlerPath;
			try {
				handlerPath = handler.asFile().getCanonicalPath();
			} catch (IOException ex) {
				final String msg = "Failed to get canonical path for " + handler
						.getRootedPath();
				throw new SCPFacadeException(msg, ex);
			}

			for (String dirPath : uploadedDir) {
				if (handlerPath.startsWith(dirPath)) {
					// already loaded
					ignore = true;
					break;
				}
			}
			if (ignore) {
				// skip, as already loded
				continue;
			}

			if (handler instanceof FileHandler) {
				// ok, just load
			} else if (handler instanceof DirectoryHandler) {
				// add file path to the loaded directories
				this.uploadedDir.add(handlerPath);
			} else {
				LOG.warn("Unknown entity: {} skipped.", handler.getRootedPath());
			}
			// upload
			try {
				// where to load the directory on server path
				// relative to the destination
				final String serverPath = handler.getRootedPath().substring(0,
						handler.getRootedPath().lastIndexOf('/'));
				
				LOG.debug("Uploading '{}' into '{}'", handler.getRootedPath(),
						destination + serverPath);
				
				upload(handler, destination + serverPath);
			} catch (SCPPException ex) {
				final String msg = "Failed to upload: " + handler
						.getRootedPath();
				throw new SCPFacadeException(msg, ex);
			}
		}
	}

}
