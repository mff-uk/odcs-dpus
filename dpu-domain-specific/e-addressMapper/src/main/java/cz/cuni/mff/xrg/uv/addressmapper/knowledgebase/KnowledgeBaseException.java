package cz.cuni.mff.xrg.uv.addressmapper.knowledgebase;

/**
 * Used to report failure in {@link KnowledgeBase}.
 * 
 * @author Škoda Petr
 */
public class KnowledgeBaseException extends Exception {

    public KnowledgeBaseException(String message) {
        super(message);
    }

    public KnowledgeBaseException(String message, Throwable cause) {
        super(message, cause);
    }

}
