package cz.cuni.mff.xrg.uv.addressmapper.mapping;

import cz.cuni.mff.xrg.uv.addressmapper.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.query.Requirement;
import java.util.List;

/**
 * Parse triple with certain predicate and extract information that can be used
 * in mapping.
 *
 * @author Škoda Petr
 */
public abstract class StatementMapper {
    
    protected ErrorLogger errorLogger;

    /**
     * Uri that should mapper try to parse.
     */
    protected List<String> mapUri;
    
    protected KnowledgeBase knowledgeBase;
    
    public void bind (ErrorLogger errorLogger, List<String> uri, 
            KnowledgeBase knowledgeBase) {
        this.errorLogger = errorLogger;
        this.mapUri = uri;
        this.knowledgeBase = knowledgeBase;
    }

    public abstract String getName();

    /**
     *
     * @param predicate
     * @return True if statement with given predicate can be mapped by this
         mapper.
     */
    public boolean canMap(String predicate) {
        return mapUri.contains(predicate);
    }

    /**
     * Parse information from given statement and create requirements.
     *
     * @param predicate
     * @param object
     * @return Template of triples that will be required by select query.
     */
    public abstract List<Requirement> map(String predicate, String object);

}
