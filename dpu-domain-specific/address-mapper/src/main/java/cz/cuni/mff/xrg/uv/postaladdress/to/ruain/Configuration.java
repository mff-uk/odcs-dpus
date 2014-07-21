package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.AddressLocality;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.AddressRegionMapper;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.PostalCodeMapper;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.StreetAddressMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration extends DPUConfigObjectBase {

    /**
     * Query used to get addresses to parse.
     */
    private String addressQuery = "SELECT ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/PostalAddress>}";

    private String ruainEndpoint = "http://ruian.linked.opendata.cz/sparql";
    
    /**
     * -1 for infinity.
     */
    private Integer ruianFailRetry = -1;
    
    private Integer ruianFailDelay = 10000;

    private Map<String, List<String>> mapperConfig = null;

    private String baseUri = "http://ruian.linked.opendata.cz/ontology/links/";
    
    @Override
    public void onDeserialize() {
        if (mapperConfig == null) {
            // use some basic configuration
            mapperConfig = new HashMap<>();
            mapperConfig.put(AddressRegionMapper.NAME, 
                    Arrays.asList("http://schema.org/addressRegion"));
            mapperConfig.put(PostalCodeMapper.NAME, 
                    Arrays.asList("http://schema.org/postalCode"));
            mapperConfig.put(StreetAddressMapper.NAME, 
                    Arrays.asList("http://schema.org/streetAddress"));
            mapperConfig.put(AddressLocality.NAME, 
                    Arrays.asList("http://schema.org/addressLocality"));
        }
    }
    
    public String getAddressQuery() {
        return addressQuery;
    }

    public void setAddressQuery(String addressQuery) {
        this.addressQuery = addressQuery;
    }

    public String getRuainEndpoint() {
        return ruainEndpoint;
    }

    public void setRuainEndpoint(String ruainEndpoint) {
        this.ruainEndpoint = ruainEndpoint;
    }

    public Integer getRuianFailRetry() {
        return ruianFailRetry;
    }

    public void setRuianFailRetry(Integer ruianFailRetry) {
        this.ruianFailRetry = ruianFailRetry;
    }

    public Integer getRuianFailDelay() {
        return ruianFailDelay;
    }

    public void setRuianFailDelay(Integer ruianFailDelay) {
        this.ruianFailDelay = ruianFailDelay;
    }

    public Map<String, List<String>> getMapperConfig() {
        return mapperConfig;
    }

    public void setMapperConfig(Map<String, List<String>> mapperConfig) {
        this.mapperConfig = mapperConfig;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
    
}
