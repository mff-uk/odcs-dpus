package cz.opendata.linked.geocoder;

import java.net.URI;
import java.net.URISyntaxException;

import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;

/**
 *
 * Put your DPU's configuration here.
 *
 */
public class ExtractorConfig implements DPUConfigObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8719241993054209502L;

    public boolean rewriteCache = true;
    
    public String geocoderURI = "http://xrg15.projekty.ms.mff.cuni.cz:5555";
	
	@Override
    public boolean isValid() {
		try {
			new URI(geocoderURI);
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
    }

}
