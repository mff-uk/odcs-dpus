package cz.cuni.mff.xrg.uv.service.external;

import eu.unifiedviews.dpu.DPUContext;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.exec.ExecContext;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 * Test suite for {@link RemoteRepository}.
 *
 * @author Škoda Petr
 */
public class RemoteRepositoryTest {

    @Test(timeout = 10000)
    public void passRuain() throws Exception {
        final String uri = "http://dbpedia.org/sparql";
        final String select = "SELECT distinct ?s WHERE {?s ?p ?o} LIMIT 10";

        ExecContext<?> context = Mockito.mock(ExecContext.class);
        UserExecContext usercontext = Mockito.mock(UserExecContext.class);

        // Get remote repository - in a DPU use ExternalServiceFactory instead.
        RemoteRdfDataUnit remote = new RemoteRdfDataUnit(context, uri, 
                ValueFactoryImpl.getInstance().createURI("http://dbpedia.org"));

        // Prepare query.
        SparqlUtils.SparqlSelectObject query = SparqlUtils.createSelect(select, DataUnitUtils.getEntries(remote, RDFDataUnit.Entry.class));
        SparqlUtils.QueryResultCollector collector = new SparqlUtils.QueryResultCollector();

        // Execute query.
        RepositoryConnection connection = remote.getConnection();
        SparqlUtils.execute(connection, usercontext, query, collector);
        connection.close();

        // And close in DPU this is done automatically.
        remote.close();

        // execute and check result size
        Assert.assertTrue(collector.getResults().size() == 10);
    }

}
