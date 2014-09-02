package cz.cuni.mff.xrg.uv.loader.solr;

/**
 * DPU's configuration class.
 */
public class SolrConfig_V1 {

    private String server = "http://localhost:8983/";

    public SolrConfig_V1() {

    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

}
