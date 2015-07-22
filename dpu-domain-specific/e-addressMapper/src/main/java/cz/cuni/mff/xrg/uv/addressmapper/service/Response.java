package cz.cuni.mff.xrg.uv.addressmapper.service;

/**
 * Represents a entity returned by Address service.
 * 
 * @author Škoda Petr
 */
public final class Response {

    private final String uri;
    
    private final double confidence;
    
    private final double completeness;

    public Response(String uri, double confidence, double completeness) {
        this.uri = uri;
        this.confidence = confidence;
        this.completeness = completeness;
    }

    public String getUri() {
        return uri;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getCompleteness() {
        return completeness;
    }

}
