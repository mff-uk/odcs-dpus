package cz.cuni.mff.xrg.uv.extractor.sparqlendpoint.query;

/**
 * Configuration class for SparqlConstructRemote.
 *
 * @author Petr Škoda
 */
public class SparqlEndpointQueryConfig_V1 {

    private String selectQuery = "SELECT ?type WHERE { [] a ?type. } LIMIT 1";

    private String endpoint = "http://internal.opendata.cz:8890/sparql";

    private String queryTemplate = "CONSTRUCT {?s ?p ?o} WHERE {\n ?s a <${type}> ;\n ?p ?o.\n }";

    public SparqlEndpointQueryConfig_V1() {

    }

    public String getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getQueryTemplate() {
        return queryTemplate;
    }

    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }

}
