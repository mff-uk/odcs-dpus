package eu.unifiedviews.plugins.extractor.rdffromsparql;

/**
 * Specify one of possible type of HTTP request used for data extraction from
 * SPARQL endpoint.
 *
 * @author Jiri Tomes
 */
public enum ExtractorRequestType {

    /**
     * Use GET method and URL encoder.
     */
    GET_URL_ENCODER,
    /**
     * Use POST method and URL encoder.
     */
    POST_URL_ENCODER,
    /**
     * Use POST method and unencoded query.
     */
    POST_UNENCODED_QUERY;
}