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
    
    /**
     *
     * @param predicate
     * @return True if statement with given predicate can be mapd by this
         mapr.
     */
    public abstract boolean canMap(String predicate);

    /**
     * Parse information from given statement and create requirements.
     *
     * @param predicate
     * @param object
     * @return Template of triples that will be required by select query.
     * @throws MappingException
     */
    public abstract List<Requirement> map(String predicate, String object)
            throws MappingException;

}
