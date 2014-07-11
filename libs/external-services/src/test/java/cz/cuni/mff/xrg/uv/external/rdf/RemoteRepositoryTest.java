package cz.cuni.mff.xrg.uv.external.rdf;

import cz.cuni.mff.xrg.odcs.dpu.test.TestEnvironment;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.external.ExternalServicesFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test suite for {@link RemoteRepository}.
 *
 * @author Škoda Petr
 */
public class RemoteRepositoryTest {

    @Test(timeout = 30000)
    public void passRuain() throws ExternalFailure {
        final String uri = "http://dbpedia.org/sparql";
        final String select = "SELECT * WHERE {?s ?p ?o} LIMIT 10";
        
        TestEnvironment env = new TestEnvironment();
        
        // prepare repository
        RemoteRepository remote = ExternalServicesFactory.remoteRepository(uri, 
                env.getContext(), 5000);
        // execute and check result size
        Assert.assertTrue(remote.select(select).size() == 10);
    }

    @Test(expected = ExternalFailure.class)
    public void nonExistingRemote() throws ExternalFailure {
        final String uri = "http://not.exist.cz/sparql";
        final String select = "SELECT * WHERE {?s ?p ?o} LIMIT 10";
        
        TestEnvironment env = new TestEnvironment();
        
        // prepare repository
        RemoteRepository remote = ExternalServicesFactory.remoteRepository(uri, 
                env.getContext(), 1000, 2);
        // execute and check result size
        Assert.assertTrue(remote.select(select).size() == 10);
    }    
    
}
