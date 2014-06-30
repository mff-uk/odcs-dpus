package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.*;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

/**
 * Given triples about single s:PostallAdreess subject prepare query for
 * mapping.
 *
 * @author Škoda Petr
 */
public class RequirementsCreator {
        
    private final List<StatementMapper> parsers;

    private final SimpleRdfRead rdfPostalAddress;

    private final ErrorLogger errorLogger;
    
    /**
     *
     * @param rdfPostalAddress
     * @param errorLogger
     * @param knowledgeBase
     */
    public RequirementsCreator(SimpleRdfRead rdfPostalAddress, 
            ErrorLogger errorLogger, KnowledgeBase knowledgeBase) {
        this.rdfPostalAddress = rdfPostalAddress;
        this.errorLogger = errorLogger;
        // add parsers
        this.parsers = new LinkedList<>();
        this.parsers.add(new PostalCodeMapper(errorLogger));
        this.parsers.add(new StreetAddressMapper(errorLogger, knowledgeBase));
        this.parsers.add(new AddressRegionMapper(errorLogger));
    }
    
    public List<Requirement> createRequirements(Value addr) throws QueryEvaluationException,
            QueryException, OperationFailedException {
        final List<Requirement> requirements = new LinkedList<>();
        
        // fill requirements
        errorLogger.start(addr.toString());
        prepareRequirements(addr, requirements);        
        errorLogger.end();
        
        if (requirements.isEmpty()) {
            throw new EmptyQueryException();
        }        
        // build string query
        return requirements;
    }
    
    /**
     * Prepare requirements for mapping for given s:PostalAddress.
     *
     * @param addr
     * @param requirements
     * @throws QueryEvaluationException
     * @throws FailedToCreateMapping
     * @throws OperationFailedException
     */
    private void prepareRequirements(Value addr, List<Requirement> requirements)
            throws QueryEvaluationException, QueryException, OperationFailedException {
        final String query = String.format("select ?p ?o where {<%s> ?p ?o}",
                addr);
        try (ConnectionPair<TupleQueryResult> triples = rdfPostalAddress
                .executeSelectQuery(query)) {
            while (triples.getObject().hasNext()) {
                final BindingSet binding = triples.getObject().next();
                // extract information from triples
                final String predicate = binding.getBinding("p").getValue()
                        .stringValue();
                final String object = binding.getBinding("o").getValue()
                        .stringValue();
                
                boolean hasBeenUsed = false;
                for (StatementMapper parser : parsers) {
                    if (parser.canMap(predicate)) {
                        // add requirements
                        List<Requirement> toAdd = parser.map(predicate, object);
                        errorLogger.mapped(predicate, object, toAdd);
                        requirements.addAll(toAdd);
                        // save something for debug purpose
                        hasBeenUsed = true;
                    }
                }
                if (!hasBeenUsed) {
                    errorLogger.unused(predicate, object);
                }
            }
        }
    }
}
