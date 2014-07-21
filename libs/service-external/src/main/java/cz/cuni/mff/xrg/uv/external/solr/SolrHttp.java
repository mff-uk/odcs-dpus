package cz.cuni.mff.xrg.uv.external.solr;

import cz.cuni.mff.xrg.uv.external.ExternalFailure;
//import org.apache.solr.client.solrj.SolrQuery;
//import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.HttpSolrServer;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.common.SolrDocumentList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
public class SolrHttp {
    
    private static final Logger LOG = LoggerFactory.getLogger(SolrHttp.class);
    
//    private final HttpSolrServer solr;
    
    SolrHttp(String solrUri) {
//        solr = new HttpSolrServer(solrUri);
    }
    
    public void query(String column, String value) throws ExternalFailure {
        
//        SolrQuery query = new SolrQuery();
//        query.setQuery(column + ":" + value);
//        
//        final QueryResponse response;
//        try {
//            response = solr.query(query);
//        } catch (SolrServerException ex) {
//            throw new ExternalFailure("Failed to execute solr query.", ex);
//        }
//        
//        SolrDocumentList results = response.getResults();
        
    }
    
}
