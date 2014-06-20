package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query;

import cz.cuni.mff.xrg.odcs.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.AddressRegionMapper;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.MappingException;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.PostalCodeMapper;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.StatementMapper;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.StreetAddressMapper;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given triples about single s:PostallAdreess subject prepare query for
 * mapping.
 *
 * @author Škoda Petr
 */
public class QueryCreator {
        
    private final List<StatementMapper> parsers;

    private final boolean isDebugging;

    private final SimpleRdfRead rdfPostalAddress;

    private final List<Requirement> requirements;

    private final RequirementsToQuery reqConvertor;

    private final List<String> notUsedStatements;

    private final List<String> statementsWithoutMapping;

    /**
     *
     * @param isDebugging      If true then information about not used
     *                         statements is stored for dump purpose.
     * @param rdfPostalAddress
     */
    public QueryCreator(boolean isDebugging, SimpleRdfRead rdfPostalAddress) {
        this.isDebugging = isDebugging;
        this.rdfPostalAddress = rdfPostalAddress;
        this.requirements = new LinkedList<>();
        this.reqConvertor = new RequirementsToQuery();
        this.notUsedStatements = new LinkedList<>();
        this.statementsWithoutMapping = new LinkedList<>();
        // add parsers
        this.parsers = new LinkedList<>();
        this.parsers.add(new PostalCodeMapper());
        this.parsers.add(new StreetAddressMapper());
        this.parsers.add(new AddressRegionMapper());
    }

    public String createQuery(Value addr) throws QueryEvaluationException,
            QueryException, OperationFailedException {
        requirements.clear();
        notUsedStatements.clear();
        statementsWithoutMapping.clear();
        // fill requirements
        prepareRequirements(addr);
        
        if (requirements.isEmpty()) {
            throw new EmptyQueryException();
        }

        // build string query
        return reqConvertor.convert(requirements);
    }

    /**
     *
     * @return Dumped information from QueryCreator for debug purpose.
     */
    public String getDump() {
        StringBuilder dump = new StringBuilder();
        dump.append("Not used:\n");
        for (String item : notUsedStatements) {
            dump.append("\t");
            dump.append(item);
            dump.append("\n");
        }
        dump.append("Used, but without output:\n");
        for (String item : statementsWithoutMapping) {
            dump.append("\t");
            dump.append(item);
            dump.append("\n");
        }
        return dump.toString();
    }

    /**
     * Prepare requirements for mapping for given s:PostalAddress.
     *
     * @param addr
     * @throws QueryEvaluationException
     * @throws FailedToCreateMapping
     * @throws OperationFailedException
     */
    private void prepareRequirements(Value addr)
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
                
                boolean triplesGenerated = false;
                boolean hasBeenUsed = false;
                for (StatementMapper parser : parsers) {
                    if (parser.canMap(predicate)) {
                        // add requirements
                        List<Requirement> toAdd = null;
                        try {
                            toAdd = parser.map(predicate, object);
                        } catch (MappingException ex) {
                            // TODO Add some policy here
                            final String msg = "Failed to map <" + predicate + 
                                    "> " + object + "";
                            throw new QueryException(msg, ex);
                        }
                        requirements.addAll(toAdd);
                        // save something for debug purpose
                        hasBeenUsed = true;
                        triplesGenerated |= !toAdd.isEmpty();
                    }
                }
                if (isDebugging) {
                    // store for dump purpose
                    if (!hasBeenUsed) {
                        notUsedStatements.add(predicate + " " + object);
                    } else if (!triplesGenerated) {
                        // is used, but gives no output
                        statementsWithoutMapping.add(predicate + " " + object);
                    }
                }
            }
        }
    }
}
