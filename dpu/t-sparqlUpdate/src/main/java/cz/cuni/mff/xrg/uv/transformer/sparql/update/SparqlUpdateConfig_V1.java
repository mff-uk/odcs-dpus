package cz.cuni.mff.xrg.uv.transformer.sparql.update;

/**
 * 
 * @author Škoda Petr
 */
public class SparqlUpdateConfig_V1 {

    /**
     * SPARQL update query.
     */
    private String query = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    private boolean perGraph = true;

    public SparqlUpdateConfig_V1() {

    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isPerGraph() {
        return perGraph;
    }

    public void setPerGraph(boolean perGraph) {
        this.perGraph = perGraph;
    }

}
