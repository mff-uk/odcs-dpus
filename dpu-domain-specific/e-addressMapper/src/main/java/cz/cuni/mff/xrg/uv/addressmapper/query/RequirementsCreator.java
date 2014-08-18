package cz.cuni.mff.xrg.uv.addressmapper.query;

import cz.cuni.mff.xrg.uv.addressmapper.mapping.StatementMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.ErrorLogger;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.MapperFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.addressmapper.knowledge.KnowledgeBase;
import eu.unifiedviews.dpu.config.DPUConfigException;
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
     * @throws eu.unifiedviews.dpu.config.DPUConfigException
     */
    public RequirementsCreator(SimpleRdfRead rdfPostalAddress, 
            ErrorLogger errorLogger, KnowledgeBase knowledgeBase,
            Map<String, List<String>> mapperConfig) throws DPUConfigException {
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
