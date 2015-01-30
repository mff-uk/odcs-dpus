package cz.cuni.mff.xrg.uv.extractor.virtuosoextractor;

/**
 * DPU's configuration class.
 */
public class VirtuosoExtractorConfig_V1 {

    private String serverUrl = "jdbc:virtuoso://127.0.0.1:1111/charset=UTF-8";

    private String username = "dba";
    
    private String password = "dba";

    /**
     * Name of graph to extract.
     */
    private String graphUri = "";

    /**
     * Path to output.
     */
    private String outputPath = "";

    public VirtuosoExtractorConfig_V1() {

    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGraphUri() {
        return graphUri;
    }

    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

}
