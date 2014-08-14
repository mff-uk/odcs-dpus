package cz.cuni.mff.xrg.uv.rdf.simple;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to hold result of a query and associated connection. The query require
 * opened connection during the result retrieval. This class create a pair of
 * query result and used connection. Also this class can be (and should be) used
 * as try-catch resource.
 *
 * The usage may differ based on used query method.
 *
 * <pre>
 * String query = "SELECT ?s ?p ?o WHERE {?s ?p ?o}";
 * try (ConnectionPair<TupleQueryResult> resultWrap = simpleReadRdf.executeSelectQuery(query)) {
 *	final TupleQueryResult result = queryWrap.getObject();
 *	while (result.hasNext()) {
 *		final BindingSet statement = result.next();
 *		String objectValue = solution.getBinding("o").getValue().stringValue();
 *		// ..
 *  }
 * } catch (OperationFailedException ex) {
 *	LOG.error("SimpleReadRdf operation failed.", ex)
 * } catch (QueryEvaluationException ex) {
 *	LOG.error("Problem evaluating the query: {}", query, ex);
 * }
 * // in every case the associated connection is automatically close
 * </pre>
 *
 * @author Škoda Petr
 * @param <T>
 */
public class ConnectionPair<T> implements AutoCloseable {

	private static final Logger LOG = LoggerFactory.getLogger(
			ConnectionPair.class);

	/**
	 * Query associated with object.
	 */
	private final RepositoryConnection connection;

	/**
	 * associated object - like query result.
	 */
	private final T object;

	ConnectionPair(RepositoryConnection connection, T object) {
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
