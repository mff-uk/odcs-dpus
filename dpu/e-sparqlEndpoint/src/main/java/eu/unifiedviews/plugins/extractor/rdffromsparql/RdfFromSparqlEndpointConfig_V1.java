package eu.unifiedviews.plugins.extractor.rdffromsparql;

import java.util.List;

import cz.cuni.mff.xrg.uv.extractor.sparqlendpoint.SparqlEndpointConfig_V1;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.VersionedConfig;

/**
 * SPARQL extractor configuration.
 *
 * @author Petr Škoda
 * @author Jiri Tomes
 */
public class RdfFromSparqlEndpointConfig_V1 implements VersionedConfig<SparqlEndpointConfig_V1> {

    private String SPARQL_endpoint;

    private String Host_name;

    private String Password;

    private String SPARQL_query;

    private boolean ExtractFail;

    private boolean UseStatisticalHandler;

    private boolean failWhenErrors;

    private Long retryTime;

    private Integer retrySize;

    private ExtractorEndpointParams endpointParams;

    private List<String> GraphsUri;

    private boolean useSplitConstruct;

    private Integer splitConstructSize;

    private String outputGraphSymbolicName = "E-RDFFromSPARQL/output" + String.valueOf(new java.util.Random().nextInt(100));

    public RdfFromSparqlEndpointConfig_V1() {
        this.SPARQL_endpoint = "";
        this.Host_name = "";
        this.Password = "";
        this.SPARQL_query = "";
        this.ExtractFail = true;
        this.UseStatisticalHandler = true;
        this.failWhenErrors = false;
        this.retrySize = -1;
        this.retryTime = 1000L;
        this.endpointParams = new ExtractorEndpointParams();
        this.useSplitConstruct = false;
        this.splitConstructSize = 50000;
    }

    public RdfFromSparqlEndpointConfig_V1(String SPARQL_endpoint, String Host_name,
            String Password, String SPARQL_query, boolean ExtractFail,
            boolean UseStatisticalHandler, boolean failWhenErrors, int retrySize,
            long retryTime, ExtractorEndpointParams endpointParams,
            boolean useSplitConstruct, int splitConstructSize) {

        this.SPARQL_endpoint = SPARQL_endpoint;
        this.Host_name = Host_name;
        this.Password = Password;
        this.SPARQL_query = SPARQL_query;
        this.ExtractFail = ExtractFail;
        this.UseStatisticalHandler = UseStatisticalHandler;
        this.failWhenErrors = failWhenErrors;
        this.retrySize = retrySize;
        this.retryTime = retryTime;
        this.endpointParams = endpointParams;
        this.useSplitConstruct = useSplitConstruct;
        this.splitConstructSize = splitConstructSize;
    }

    /**
     * Returns parameters for target SPARQL endpoint as {@link ExtractorEndpointParams} instance.
     *
     * @return parameters for target SPARQL endpoint as {@link ExtractorEndpointParams} instance.
     */
    public ExtractorEndpointParams getEndpointParams() {
        return endpointParams;
    }

    /**
     * Returns URL address of SPARQL endpoint as string.
     *
     * @return URL address of SPARQL endpoint as string.
     */
    public String getSPARQLEndpoint() {
        return SPARQL_endpoint;
    }

    /**
     * Returns host name for target SPARQL endpoint.
     *
     * @return host name for target SPARQL endpoint.
     */
    public String getHostName() {
        return Host_name;
    }

    /**
     * Returns password for access to the target SPARQL endpoint.
     *
     * @return password for access to the target SPARQL endpoint.
     */
    public String getPassword() {
        return Password;
    }

    /**
     * Returns string value of SPARQL query.
     *
     * @return SPARQL query.
     */
    public String getSPARQLQuery() {
        return SPARQL_query;
    }

    /**
     * Returns true, if extraction wil be stopped when errors, false otherwise.
     *
     * @return true, if extraction wil be stopped when errors, false otherwise.
     */
    public boolean isExtractFail() {
        return ExtractFail;
    }

    /**
     * Returns true, if is used statistical handler for data extraction, false
     * otherwise.
     *
     * @return true, if is used statistical handler for data extraction, false
     *         otherwise.
     */
    public boolean isUsedStatisticalHandler() {
        return UseStatisticalHandler;
    }

    /**
     * Returns true, if execution should fail when some errors are detected,
     * false otherwise.
     *
     * @return true, if execution should fail when some errors are detected,
     *         false otherwise.
     */
    public boolean isFailWhenErrors() {
        return failWhenErrors;
    }

    /**
     * Returns time in ms how long wait before re-connection attempt.
     *
     * @return Time in ms how long wait before re-connection attempt.
     */
    public Long getRetryTime() {
        return retryTime;
    }

    /**
     * Returns count of re-connection if connection failed. For infinite loop
     * use zero or negative integer.
     *
     * @return Count of re-connection if connection failed. For infinite loop
     *         use zero or negative integer.
     */
    public Integer getRetrySize() {
        return retrySize;
    }

    /**
     * Returns true, if construct query should be split in more SPARQL queries,
     * false otherwise.
     *
     * @return true, if construct query should be split in more SPARQL queries,
     *         false otherwise.
     */
    public boolean isUsedSplitConstruct() {
        return useSplitConstruct;
    }

    /**
     * Returns maximum size of one data part for contruct query when is used
     * split.
     *
     * @return maximum size of one data part for contruct query when is used
     *         split.
     */
    public Integer getSplitConstructSize() {
        return splitConstructSize;
    }

    public String getSPARQL_endpoint() {
        return SPARQL_endpoint;
    }

    public String getHost_name() {
        return Host_name;
    }

    public String getSPARQL_query() {
        return SPARQL_query;
    }

    public boolean isUseStatisticalHandler() {
        return UseStatisticalHandler;
    }

    public List<String> getGraphsUri() {
        return GraphsUri;
    }

    public boolean isUseSplitConstruct() {
        return useSplitConstruct;
    }

    public void setSPARQL_endpoint(String SPARQL_endpoint) {
        this.SPARQL_endpoint = SPARQL_endpoint;
    }

    public void setHost_name(String Host_name) {
        this.Host_name = Host_name;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }

    public void setSPARQL_query(String SPARQL_query) {
        this.SPARQL_query = SPARQL_query;
    }

    public void setExtractFail(boolean ExtractFail) {
        this.ExtractFail = ExtractFail;
    }

    public void setUseStatisticalHandler(boolean UseStatisticalHandler) {
        this.UseStatisticalHandler = UseStatisticalHandler;
    }

    public void setFailWhenErrors(boolean failWhenErrors) {
        this.failWhenErrors = failWhenErrors;
    }

    public void setRetryTime(Long retryTime) {
        this.retryTime = retryTime;
    }

    public void setRetrySize(Integer retrySize) {
        this.retrySize = retrySize;
    }

    public void setEndpointParams(ExtractorEndpointParams endpointParams) {
        this.endpointParams = endpointParams;
    }

    public void setGraphsUri(List<String> GraphsUri) {
        this.GraphsUri = GraphsUri;
    }

    public void setUseSplitConstruct(boolean useSplitConstruct) {
        this.useSplitConstruct = useSplitConstruct;
    }

    public void setSplitConstructSize(Integer splitConstructSize) {
        this.splitConstructSize = splitConstructSize;
    }

    public String getOutputGraphSymbolicName() {
        return outputGraphSymbolicName;
    }

    public void setOutputGraphSymbolicName(String outputGraphSymbolicName) {
        this.outputGraphSymbolicName = outputGraphSymbolicName;
    }

    @Override
    public SparqlEndpointConfig_V1 toNextVersion() throws DPUConfigException {
        SparqlEndpointConfig_V1 c = new SparqlEndpointConfig_V1();
        c.setEndpoint(this.SPARQL_endpoint);
        
        final StringBuilder builder = new StringBuilder(this.SPARQL_query);
        final StringBuilder fromClause = new StringBuilder();
        if (this.GraphsUri != null && !this.GraphsUri.isEmpty()) {
            builder.append("\n\n-> Can't convert: GraphsUri\n");
            for (String uri : this.GraphsUri) {
                builder.append(uri);
                builder.append("\n");
            }            
        }
        if (this.endpointParams != null) {
            if (this.endpointParams.getDefaultGraphURI() != null && !this.endpointParams.getDefaultGraphURI().isEmpty()) {
                for (String uri : this.endpointParams.getDefaultGraphURI()) {
                    fromClause.append("FROM <");
                    fromClause.append(uri);
                    fromClause.append(">\n");
                }
            }            
            if (this.endpointParams.getNamedGraphURI() != null && !this.endpointParams.getNamedGraphURI().isEmpty()) {
                for (String uri : this.endpointParams.getNamedGraphURI()) {
                    fromClause.append("FROM NAMED <");
                    fromClause.append(uri);
                    fromClause.append(">\n");
                }
            }
        }
        String query = builder.toString();
        if (fromClause.length() > 0) {
            query = query.replaceFirst("(?i)WHERE", fromClause + "WHERE");
        }
        c.setQuery(query);

        return c;
    }

    

}