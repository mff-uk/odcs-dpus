/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.cuni.mff.xrg.uv.external.solr;

import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SolrHttpTest {
 
    @Test
    public void simpleQuery() throws ExternalFailure {
        SolrHttp solr = new SolrHttp("http://xrg13.projekty.ms.mff.cuni.cz:8080/solr/ruian");        
        
        //solr.query("amnumber","4192575");
        
    }
    
}
