package cz.cuni.mff.xrg.uv.rdf.simple;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.rdf.CleverDataset;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import java.util.ArrayList;
import java.util.List;
import org.openrdf.model.*;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to
 * handler RDF functionality and also reduce code duplicity.
 *
 * @author Škoda Petr
 */
public class SimpleRdfRead {

	/**
	 * Wrapped data unit.
	 */
	protected RDFDataUnit dataUnit;

	/**
	 * Execution context.
	 */
	protected DPUContext context;

	/**
	 * Value factory.
	 */
	protected ValueFactory valueFactory = null;

	/**
	 * 
	 * @param dataUnit
	 * @param context 
	 */
	public SimpleRdfRead(RDFDataUnit dataUnit, DPUContext context) {
		this.dataUnit = dataUnit;
		this.context = context;
	}

	/**
	 * In case of multiple calls the dame {@link ValueFactory} will be returned.
	 *
	 * @return Value factory for wrapped {@link RDFDataUnit}
	 * @throws OperationFailedException
	 */
	public ValueFactory getValueFactory() throws OperationFailedException {
		if (valueFactory == null) {
			try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
				valueFactory = conn.c().getValueFactory();
			}
		}
		return valueFactory;
	}

	/**
	 * Eagerly load all triples and store them into list.
	 *
	 * @return List of all triples in the repository.
	 * @throws OperationFailedException
	 */
	public List<Statement> getStatements() throws OperationFailedException {
		List<Statement> statemens = new ArrayList<>();
		try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
			RepositoryResult<Statement> repoResult
					= conn.c().getStatements(null, null, null, true,
							getContextsAsArray());
			// add all data into list
			while (repoResult.hasNext()) {
				Statement next = repoResult.next();
				statemens.add(next);
			}
			return statemens;
		} catch (RepositoryException ex) {
			throw new OperationFailedException(
					"Failed to get statements from repository.", ex);
		}
	}

	/**
	 * Execute given select query and return result. See {@link ConnectionPair}
	 * for more information about usage.
	 *
	 * @param query SPARQL select query
	 * @return
	 * @throws OperationFailedException
	 */
	public ConnectionPair<TupleQueryResult> executeSelectQuery(String query)
			throws OperationFailedException {
		// the connction needs to stay open during the whole query		
		ClosableConnection conn = new ClosableConnection(dataUnit);
		try {
			// prepare query
			TupleQuery tupleQuery = conn.c().prepareTupleQuery(
					QueryLanguage.SPARQL, query);
			// prepare dataset
			CleverDataset dataset = new CleverDataset();
			dataset.addDefaultGraphs(dataUnit.getContexts());
			tupleQuery.setDataset(dataset);
			// wrap result and return
			return new ConnectionPair<>(conn.c(), tupleQuery.evaluate());
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			conn.close();
			throw new OperationFailedException("Failed to execute select query.",
					e);
		}
	}

	/**
	 * Execute given construct query and return result. See
	 * {@link ConnectionPair} for more information about usage.
	 *
	 * @param query SPARQL construct query
	 * @return
	 * @throws OperationFailedException
	 */
	public ConnectionPair<Graph> executeConstructQuery(String query) throws OperationFailedException {
		// the connction needs to stay open during the whole query
		ClosableConnection conn = new ClosableConnection(dataUnit);
		try {
			// prepare query
			GraphQuery graphQuery = conn.c().prepareGraphQuery(
					QueryLanguage.SPARQL,
					query);
			CleverDataset dataset = new CleverDataset();
			dataset.addDefaultGraphs(dataUnit.getContexts());
			graphQuery.setDataset(dataset);
			// evaluate
			GraphQueryResult result = graphQuery.evaluate();
			// convert into graph
			Model resultGraph = QueryResults.asModel(result);
			// wrap result and return
			return new ConnectionPair<>(conn.c(), (Graph) resultGraph);
		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
			conn.close();
			throw new OperationFailedException(
					"Failed to execute construct query.", e);
		}
	}

	/**
	 *
	 * @return read contexts for wrapped {@link RDFDataUnit} in form of array
	 */
	protected URI[] getContextsAsArray() {
		return dataUnit.getContexts().toArray(new URI[0]);
	}

}
