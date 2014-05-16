package cz.cuni.mff.xrg.odcs.rdf.simple;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to hold result of query and associated connection.
 * 
 * @author Škoda Petr
 * @param <T>
 */
public class ConnectionPair <T> implements AutoCloseable {
	
	private static final Logger LOG = LoggerFactory.getLogger(
			ConnectionPair.class);
	
	/**
	 * Query associated with object.
	 */
	private final RepositoryConnection connection;
	
	/**
	 * Object like query result.
	 */
	private final T object;
	
	public ConnectionPair(RepositoryConnection connection, T object) {
		this.connection = connection;
		this.object = object;
	}

	@Override
	public void close() throws OperationFailedException {
		if (connection != null) {
			try {
				if (connection.isOpen()) {
					connection.close();
				}
			} catch (RepositoryException e) {
				LOG.warn("Failed to close connection.", e);
			}
		}
	}

	public T getObject() {
		return object;
	}
	
}
