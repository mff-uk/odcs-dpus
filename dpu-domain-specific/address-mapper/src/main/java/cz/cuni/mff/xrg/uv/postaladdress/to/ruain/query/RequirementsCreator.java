package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        
    private final List<StatementMapper> mapper;

    private final SimpleRdfRead rdfPostalAddress;

    private final ErrorLogger errorLogger;
    
    /**
     *
     * @param rdfPostalAddress
     * @param errorLogger
     * @param knowledgeBase
     * @param mapperConfig
     * @throws cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException
     */
    public RequirementsCreator(SimpleRdfRead rdfPostalAddress, 
            ErrorLogger errorLogger, KnowledgeBase knowledgeBase,
            Map<String, List<String>> mapperConfig) throws ConfigException {
        this.rdfPostalAddress = rdfPostalAddress;
        this.errorLogger = errorLogger;
        // add parsers
        this.mapper = MapperFactory.construct(errorLogger, knowledgeBase, mapperConfig);
    }
    
    /**
     * 
     * @param addr
     * @return Can return empty collection.
     * @throws QueryEvaluationException
     * @throws QueryException
     * @throws OperationFailedException 
     */
    public List<Requirement> createRequirements(Value addr) throws QueryEvaluationException,
            QueryException, OperationFailedException {
        final List<Requirement> requirements = new LinkedList<>();
        
        // fill requirements
        errorLogger.start(addr.toString());
        prepareRequirements(addr, requirements);        
        errorLogger.end();
              
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
        final String query = String.format("SELECT ?p ?o WHERE {<%s> ?p ?o}",
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
                for (StatementMapper parser : mapper) {
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
