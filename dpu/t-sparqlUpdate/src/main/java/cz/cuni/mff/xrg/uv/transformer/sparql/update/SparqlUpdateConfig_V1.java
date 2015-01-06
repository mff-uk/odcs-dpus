package cz.cuni.mff.xrg.uv.transformer.sparql.update;

/**
 * DPU's configuration class.
 */
public class SparqlUpdateConfig_V1 {

    /**
     * SPARQL update query.
     */
    private String query = "INSERT {?s ?p ?o} WHERE {?s ?p ?o}";

    private boolean perGraph = true;

    private boolean useDataset = false;

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

    public boolean isUseDataset() {
        return useDataset;
    }

    public void setUseDataset(boolean useDataset) {
        this.useDataset = useDataset;
    }

}
