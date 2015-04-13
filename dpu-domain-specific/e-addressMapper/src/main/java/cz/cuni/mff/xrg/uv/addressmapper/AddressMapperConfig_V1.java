package cz.cuni.mff.xrg.uv.addressmapper;

public class AddressMapperConfig_V1 {

    private String ruainEndpoint = "http://ruian.linked.opendata.cz/sparql";

    private String solrEndpoint = "http://ruian.linked.opendata.cz/solr/ruian/query";

    private String addressPredicate = "http://localhost/ontology/address";

    public AddressMapperConfig_V1() {
    }

    public String getRuainEndpoint() {
        return ruainEndpoint;
    }

    public void setRuainEndpoint(String ruainEndpoint) {
        this.ruainEndpoint = ruainEndpoint;
    }

    public String getSolrEndpoint() {
        return solrEndpoint;
    }

    public void setSolrEndpoint(String solrEndpoint) {
        this.solrEndpoint = solrEndpoint;
    }

    public String getAddressPredicate() {
        return addressPredicate;
    }

    public void setAddressPredicate(String addressPredicate) {
        this.addressPredicate = addressPredicate;
    }

}
