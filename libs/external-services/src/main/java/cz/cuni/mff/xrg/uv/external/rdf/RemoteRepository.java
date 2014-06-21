package cz.cuni.mff.xrg.uv.external.rdf;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.external.FaultTolerantPolicy;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents remote RDF repository.
 * Sample usage:
 * <pre>{@code
 * List<BindingSet> data;
 * // create remote repository, in case of failure we try again up to 10 
 * // attempts, delay between attempts is 5 seconds
 * try (RemoteRepository remote = Factory.remoteRepository(
 *          uri, dpuContext, 5000, 10)) {
 *      // query for data
 *      data = remote.remote.select("SELECT * WHERE {?s ?p ?o} LIMIT 10");
 * } catch (ExternalFailure ex) {
 *      // operation failed
 * }
 * }</pre>
 * 
 * 
 * @author Škoda Petr
 */
public class RemoteRepository implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(
            RemoteRepository.class);

    private final HTTPRepository repository;

    private final FaultTolerantPolicy faultPolicy;
    
    private final DPUContext context;
    
    public RemoteRepository(String endpointUrl, FaultTolerantPolicy faultPolicy, 
            DPUContext context) throws ExternalFailure {
        this.repository = new HTTPRepository(endpointUrl);
        this.faultPolicy = faultPolicy;
        this.context = context;
        try {
            this.repository.initialize();
        } catch (RepositoryException ex) {
            throw new ExternalFailure("Failed to initialize HTTPRepository", ex);
        }        
    }

    /**
     * Execute given query and return results. This method supports DPU 
     * cancellation on user request.
     *
     * @param strQuery
     * @return
     * @throws cz.cuni.mff.xrg.uv.external.ExternalFailure
     */
    public List<BindingSet> select(String strQuery) throws ExternalFailure{
        // prepare counters
        int retryCounter = faultPolicy.getNumberOfRetries();
        int retryMod = retryCounter > 0 ? 1 : 0;
        do {
            retryCounter -= retryMod;
            try {
                return innerSelect(strQuery);
            } catch (MalformedQueryException ex) {
                throw new ExternalFailure("Malformed query", ex);
            } catch (RepositoryException | QueryEvaluationException ex) {
                // ok, give it next try
                LOG.error("External service failed {}/{}", 
                        retryCounter, faultPolicy.getNumberOfRetries(), ex);
            }
            // wait before another try
            try {
                Thread.sleep(faultPolicy.getTimeDelay());
            } catch (InterruptedException ex) {
                // just, continue ..
            }
        } while (retryCounter != 0 && !context.canceled());
        // external service failed
        throw new ExternalFailure("External service failed.");
    }

    private List<BindingSet> innerSelect(String strQuery) 
            throws RepositoryException, QueryEvaluationException, MalformedQueryException {
        RepositoryConnection connection = null;
        try {
            connection = repository.getConnection();
            // prepare query
            final TupleQuery query = connection.prepareTupleQuery(
                    QueryLanguage.SPARQL, strQuery);
            final TupleQueryResult result = query.evaluate();
            // get data
            final List<BindingSet> results = new LinkedList<>();
            while (result.hasNext()) {
                results.add(result.next());
            }
            closeConnection(connection);
            return results;
        } catch (RepositoryException | QueryEvaluationException | MalformedQueryException ex) {
            closeConnection(connection);
            throw ex;
        }
    }

    /**
     * Close given connection.
     * 
     * @param connection 
     */
    private void closeConnection(RepositoryConnection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (RepositoryException e) {
                // we ignore the exception
            }
        }
    }

    @Override
    public void close() throws Exception {
        repository.shutDown();
    }

}
