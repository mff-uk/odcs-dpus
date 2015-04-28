package eu.unifiedviews.plugins.extractor.rdffromsparql;

import java.util.LinkedList;
import java.util.List;

/**
 * Specify parameters should be used for target SPARQL endpoint.
 * 
 * @author Jiri Tomes
 */
public final class ExtractorEndpointParams {

    /**
     * Default string value of query request parameter for SPARQL extractor
     */
    public static String DEFAULT_QUERY_PARAM = "query";

    /**
     * Default string value of default graph request parameter for SPARQL
     * extractor
     */
    public static String DEFAULT_GRAPH_PARAM = "default-graph-uri";

    /**
     * Default string value of named graph request parameter for SPARQL
     * extractor
     */
    public static String DEFAULT_NAMED_GRAPH_PARAM = "named-graph-uri";

    private String queryParam;

    private String defaultGraphParam;

    private String namedGraphParam;

    private List<String> defaultGraphURI;

    private List<String> namedGraphURI;

    private ExtractorRequestType requestType;

    /**
     * Create SPARQL extractor default setting for VIRTUOSO endpoint.
     */
    public ExtractorEndpointParams() {
        this.queryParam = DEFAULT_QUERY_PARAM;
        this.defaultGraphParam = DEFAULT_GRAPH_PARAM;
        this.namedGraphParam = DEFAULT_NAMED_GRAPH_PARAM;
        this.defaultGraphURI = new LinkedList<>();
        this.namedGraphURI = new LinkedList<>();
        this.requestType = ExtractorRequestType.POST_URL_ENCODER;
    }

    /**
     * Create SPARQL extractor setting for ENDPOINT depends on given parameters.
     *
     * @param queryParam
     *            String value of query parameter.
     * @param defaultGraphParam
     *            String value of default graph parameter.
     * @param namedGraphParam
     *            String value of named graph parameter.
     * @param requestType
     *            HTTP request type for SPARQL extractor.
     */
    public ExtractorEndpointParams(String queryParam, String defaultGraphParam,
            String namedGraphParam, ExtractorRequestType requestType) {
        this.queryParam = queryParam;
        this.defaultGraphParam = defaultGraphParam;
        this.namedGraphParam = namedGraphParam;
        this.defaultGraphURI = new LinkedList<>();
        this.namedGraphURI = new LinkedList<>();
        this.requestType = requestType;
    }

    /**
     * Create SPARQL extractor with setting for ENDPOINT depends on given
     * parameters based on collection of default/named graphs.
     *
     * @param queryParam
     *            String value of query parameter.
     * @param defaultGraphParam
     *            String value of default graph parameter.
     * @param namedGraphParam
     *            String value of named graph parameter.
     * @param defaultGraphURI
     *            Collection of default graphs used for
     *            extraction.
     * @param namedGraphURI
     *            Collection of named graphs used for extraction.
     * @param requestType
     *            HTTP request type for SPARQL extractor.
     */
    public ExtractorEndpointParams(String queryParam,
            String defaultGraphParam,
            String namedGraphParam, List<String> defaultGraphURI,
            List<String> namedGraphURI, ExtractorRequestType requestType) {
        this.queryParam = queryParam;
        this.defaultGraphParam = defaultGraphParam;
        this.namedGraphParam = namedGraphParam;
        this.defaultGraphURI = defaultGraphURI;
        this.namedGraphURI = namedGraphURI;
        this.requestType = requestType;
    }

    /**
     * Add next default graph to default graphs collection.
     *
     * @param graphURI
     *            String value of default graph URI.
     */
    public void addDefaultGraph(String graphURI) {
        if (graphURI != null && !graphURI.isEmpty()) {
            defaultGraphURI.add(graphURI);
        }

    }

    /**
     * Add next named graph to named graphs collection.
     *
     * @param graphURI
     *            String value of named graph URI.
     */
    public void addNamedGraph(String graphURI) {
        if (graphURI != null && !graphURI.isEmpty()) {
            namedGraphURI.add(graphURI);
        }
    }

    /**
     * Returns HTTP request type for SPARQL extractor.
     *
     * @return HTTP request type for SPARQL extractor.
     */
    public ExtractorRequestType getRequestType() {
        return requestType;
    }

    /**
     * Returns string value of query parameter.
     *
     * @return String value of query parameter.
     */
    public String getQueryParam() {
        return queryParam;
    }

    /**
     * Returns string value of default graph parameter.
     *
     * @return String value of default graph parameter.
     */
    public String getDefaultGraphParam() {
        return defaultGraphParam;
    }

    /**
     * Returns string value of named graph parameter.
     *
     * @return String value of named graph parameter.
     */
    public String getNamedGraphParam() {
        return namedGraphParam;
    }

    /**
     * Returns collection of default graphs (URI type) used for SPARQL
     * extractor.
     *
     * @return collection of default graphs (URI type) used for SPARQL
     *         extractor.
     */
    public List<String> getDefaultGraphURI() {
        return defaultGraphURI;
    }

    /**
     * Returns collection of named graphs (URI type) used for SPARQL extractor.
     *
     * @return collection of named graphs (URI type) used for SPARQL extractor.
     */
    public List<String> getNamedGraphURI() {
        return namedGraphURI;
    }
}