package cz.cuni.mff.xrg.odcs.rdf.simple;

import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Auto closeable wrap for {@link RepositoryConnection}. Enable usage of 
 * {@link RepositoryConnection} as a try-catch resource.
 *
 * @author Škoda Petr
 */
class ClosableConnection implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(
			ClosableConnection.class);

	private RepositoryConnection connection = null;

	public ClosableConnection(RDFDataUnit rdf) throws OperationFailedException {
		try {
			connection = rdf.getConnection();
		} catch (RepositoryException e) {
			throw new OperationFailedException("Failed to get exception.", e);
		}
	}

	@Override
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (RepositoryException ex) {
				LOG.warn("Failed to close repository connection.", ex);
			}
		}
	}

	/**
	 * 
	 * @return Wrapped connection, not null.
	 */
	public RepositoryConnection c() {
		return connection;
	}

}
