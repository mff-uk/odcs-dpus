package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping;

import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.Requirement;
import java.util.List;

/**
 * Parse triple with certain predicate and extract information that can be used
 * in mapping.
 *
 * @author Škoda Petr
 */
public abstract class StatementMapper {
    
    protected ErrorLogger errorLogger;

    public StatementMapper(ErrorLogger errorLogger) {
        this.errorLogger = errorLogger;
    }
        
    /**
     *
     * @param predicate
     * @return True if statement with given predicate can be mapped by this
         mapper.
     */
    public abstract boolean canMap(String predicate);

    /**
     * Parse information from given statement and create requirements.
     *
     * @param predicate
     * @param object
     * @return Template of triples that will be required by select query.
     */
    public abstract List<Requirement> map(String predicate, String object);

}
