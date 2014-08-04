package cz.cuni.mff.xrg.uv.service.external.solr;

import cz.cuni.mff.xrg.uv.service.external.ExternalFailure;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SolrHttpTest {

    @Test
    public void simpleQuery() throws ExternalFailure {
        SolrHttp solr = new SolrHttp("");

        //solr.query("amnumber","4192575");
    }

}
