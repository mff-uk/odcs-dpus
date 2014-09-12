package cz.opendata.linked.geocoder;

import java.net.URI;
import java.net.URISyntaxException;


/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig  {
    
    private static final long serialVersionUID = 8719241993054209502L;

    private boolean rewriteCache = true;
    
    private String geocoderURI = "http://xrg15.projekty.ms.mff.cuni.cz:5555";
        
    public boolean isRewriteCache() {
        return rewriteCache;
    }

    public void setRewriteCache(boolean rewriteCache) {
        this.rewriteCache = rewriteCache;
    }

    public String getGeocoderURI() {
        return geocoderURI;
    }

    public void setGeocoderURI(String geocoderURI) {
        this.geocoderURI = geocoderURI;
    }

}
