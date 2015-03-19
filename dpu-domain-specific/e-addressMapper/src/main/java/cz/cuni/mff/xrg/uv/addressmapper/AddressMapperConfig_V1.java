package cz.cuni.mff.xrg.uv.addressmapper;

public class AddressMapperConfig_V1 {

    private String ruainEndpoint = "http://ruian.linked.opendata.cz/sparql";

    public AddressMapperConfig_V1() {
    }

    public String getRuainEndpoint() {
        return ruainEndpoint;
    }

    public void setRuainEndpoint(String ruainEndpoint) {
        this.ruainEndpoint = ruainEndpoint;
    }

}
