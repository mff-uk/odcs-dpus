package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents content as {@link Map} of {@link Requirement}s.
 * 
 * The NOT EXIST requirements are represented in a same way as in 
 * {@link Requirement} class. 
 *
 * @author Škoda Petr
 */
public class Query {

    private final Map<Subject, List<PredicatObject>> content;
    
    private final boolean reduction;
    
    private final boolean alternative;
    
    Query() {
        content = new HashMap<>();
        reduction = false;
        alternative = false;
    }

    /**
     * Copy constructor.
     *
     * @param q
     */
    Query(Query q, boolean reduction, boolean alternative) {
        content = new HashMap<>();
        for (Subject s : q.content.keySet()) {
            List<PredicatObject> poList = new LinkedList<>();
            poList.addAll(q.content.get(s));
            content.put(s, poList);
        }
        this.reduction = reduction || q.reduction;
        this.alternative = alternative || q.alternative;
    }

    Map<Subject, List<PredicatObject>> getContent() {
        return content;
    }

    public Subject getMainSubject() {
        if (content.isEmpty()) {
            return null;
        }
        // find minimal subject
        Subject minSubject = content.keySet().iterator().next();
        
        
        
        for (Subject subject : content.keySet()) {
            if (minSubject.getLevel() == null || (subject.getLevel() != null &&
                    subject.getLevel() < minSubject.getLevel())) {
                minSubject = subject;
            }
        }
        return minSubject;
    }

    public boolean isReduction() {
        return reduction;
    }

    public boolean isAlternative() {
        return alternative;
    }    
    
}
