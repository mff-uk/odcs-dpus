package cz.opendata.linked.geocoder;

import java.net.URI;
import java.net.URISyntaxException;

import cz.cuni.mff.xrg.odcs.commons.module.config.DPUConfigObjectBase;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig extends DPUConfigObjectBase {
	
	private static final long serialVersionUID = 8719241993054209502L;

    private boolean rewriteCache = true;
    
    private String geocoderURI = "http://xrg15.projekty.ms.mff.cuni.cz:5555";
		
	@Override
    public boolean isValid() {
		try {
			new URI(geocoderURI);
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
    }

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
