package cz.cuni.mff.xrg.uv.addressmapper;

public class AddressMapperConfig_V1 {

    private String addressPredicate = "http://localhost/ontology/address";

    private String serviceEndpoint = "http://ruian.linked.opendata.cz:8080/address-mapper/rest/v1/services/map?fullList=true";

    public AddressMapperConfig_V1() {
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getAddressPredicate() {
        return addressPredicate;
    }

    public void setAddressPredicate(String addressPredicate) {
        this.addressPredicate = addressPredicate;
    }

}
