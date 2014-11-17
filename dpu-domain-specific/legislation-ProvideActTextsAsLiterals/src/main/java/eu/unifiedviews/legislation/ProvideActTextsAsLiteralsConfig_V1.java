package eu.unifiedviews.legislation;

/**
 * DPU's configuration class.
 */
public class ProvideActTextsAsLiteralsConfig_V1 {

    private String predicateURL = "http://linked.opendata.cz/ontology/odcs/xmlValue";
    
    public ProvideActTextsAsLiteralsConfig_V1() {

    }

    public String getPredicateURL() {
        return predicateURL;
    }

    public void setPredicateURL(String predicateURL) {
        this.predicateURL = predicateURL;
    }

}
