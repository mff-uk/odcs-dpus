package cz.mff.cuni.scraper.lib.generator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jakub Starka
 */
public class NoticeGenerator extends TemplateURIGenerator {

    String noticeId;
    String type;
   
    public NoticeGenerator(String type, String documentId) {
        super(null);
        this.type = type;
        this.noticeId = documentId;
    }
    
    public boolean exists = false;
    
    @Override
    protected URL generateUrl() {
        if (uri == null) {
            try {
                uri = URIGenerator.getContractNoticeUri(noticeId, type);
            } catch (MalformedURLException ex) {
                Logger.getLogger(NoticeGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return uri;
    }   
}
