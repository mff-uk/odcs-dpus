package cz.cuni.mff.xrg.uv.rdf.utils;

import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import eu.unifiedviews.dpu.DPUException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * Helper for simple processing of select query result.
 *
 * Example of processing {@link BindingSet} from SPARQL select query.
 * <pre>
 * {@code
 * SimpleRdfRead rdf;
 String query = "SELECT ?s WHERE {?s ?p ?o}";
 SelectQuery.iterate(rdf, query, new SelectQuery.BindingIterator() {
  @Override
 *  public void processStatement(BindingSet binding) throws DPUException {
 *      // process binding here
 *      String s = binding.getBinding("s").getValue().getStringValue();
 *      // you can throw DPUException to terminate the iteration
 *  }
 * });
 * }
 * </pre>
 * @author Škoda Petr
 */
public class SelectQuery {

    public interface BindingIterator {

        /**
         * Called for every tuple in result.
         * 
         * @param binding
         * @throws eu.unifiedviews.dpu.DPUException
         */
        public void processStatement(BindingSet binding) throws DPUException;
        
    }

    private SelectQuery() {
        
    }

    /**
     * Execute given query and iterate over the results with user given class.
     * 
     * @param rdf
     * @param query
     * @param iterator
     * @throws cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException
     * @throws org.openrdf.query.QueryEvaluationException
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public static void iterate(SimpleRdfRead rdf, String query, BindingIterator iterator)
            throws OperationFailedException, QueryEvaluationException, DPUException {
        try (ConnectionPair<TupleQueryResult> connection =
                rdf.executeSelectQuery(query)) {
            final TupleQueryResult result = connection.getObject();
            while (result.hasNext()) {
                final BindingSet bindingSet = result.next();
                iterator.processStatement(bindingSet);
            }
        }
    }

}
