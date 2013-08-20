package cz.mff.cuni.scraper.lib.generator;

import cz.mff.cuni.scraper.lib.triple.StandaloneTriple;
import cz.mff.cuni.scraper.lib.triple.Triple;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class BusinessEntityGenerator extends TemplateURIGenerator {

    String name;
    String postal;
    String ic;
    String documentId;
    URL url = null;
    
    public BusinessEntityGenerator(String contractingName, String contractingPostal, String contractingIC, String documentId) {
        super(null);
        this.name = contractingName;
        this.postal = contractingPostal;
        this.ic = contractingIC;        
        this.documentId = documentId;
        // exists = BEDirectory.HasIdentifier(name, postal, ic);
    }
    
    public boolean exists = false;
    
    @Override
    protected URL generateUrl() {
        if (url == null) {
            try {
                url = new URL(BEDirectory.GetIdentifier(ic, documentId));
            } catch (MalformedURLException ex) {
                Logger.getLogger(BusinessEntityGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return url;
    }
    
    public Triple getIdentifier() {
        if (ic != null && !ic.isEmpty()) {
            try {
                return new StandaloneTriple(
                            getUrl() + "/identifier/1",
                            "adms:identifier", 
                            new Triple(
                                new Triple("a", "adms:Identifier", true),
                                new Triple("skos:notation", ic, null, "xsd:string", false),
                                new Triple("dcterms:creator", new URL("http://ld.opendata.cz/resource/business-entity/CZ66002222")),
                                new Triple("adms:schemeAgency", "Ministerstvo pro místní rozvoj")
                            )
                        );
            } catch (MalformedURLException ex) {
                Logger.getLogger(BusinessEntityGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Triple();
    }
    
}
