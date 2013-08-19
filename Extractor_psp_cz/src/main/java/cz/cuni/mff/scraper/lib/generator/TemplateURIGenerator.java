package cz.cuni.mff.scraper.lib.generator;

import java.net.URL;

/**
 *
 * @author Jakub Starka
 */
public abstract class TemplateURIGenerator {
    protected String contractId = null;
    protected String uriString = null;
    protected URL uri = null;
    
    public TemplateURIGenerator() {}
    
    public TemplateURIGenerator(String contractId) {
        this.contractId = contractId;
    }

    public String getQName() {
        return "";
    }
    
    public Boolean isQName() {
        return false;
    }

    @Override
    public String toString() {
        if (uriString == null) {
            uri = getUrl();
            uriString = uri.toString();
        }
        return uriString.toString();
    }
    
    protected abstract URL generateUrl();
    
    public final URL getUrl() {
        if (uri == null) {
            uri = generateUrl();
            uriString = uri.toString();
        }
        return uri;
    }


    public Boolean exists() {
        return false;
    }
}
