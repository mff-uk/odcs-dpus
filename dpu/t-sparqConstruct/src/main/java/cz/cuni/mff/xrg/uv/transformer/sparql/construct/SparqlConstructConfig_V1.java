package cz.cuni.mff.xrg.uv.transformer.sparql.construct;

/**
 * DPU's configuration class.
 */
public class SparqlConstructConfig_V1 {

    /**
     * SPARQL construct query.
     */
    private String query = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";

    public SparqlConstructConfig_V1() {

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
