package cz.cuni.mff.xrg.uv.addressmapper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.address.processing.AlternativesFacade;
import cz.cuni.mff.xrg.uv.addressmapper.address.DuplicityFilter;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.StructuredFacade;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.address.unstructured.UnstructuredFacade;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;
import cz.cuni.mff.xrg.uv.service.external.ExternalError;
import cz.cuni.mff.xrg.uv.service.external.ExternalServicesFactory;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class AddressMapper extends AbstractDpu<AddressMapperConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(AddressMapper.class);

    /**
     * %s - Predicate URI.
     */
    private static final String QUERY = "SELECT ?s ?o WHERE { ?s <%s> ?o. }";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inRdfPostalAddress;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit outRdfMapping;

    @ExtensionInitializer.Init(param = "outRdfMapping")
    public WritableSimpleRdf rdfMapping;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;


    public AddressMapper() throws DPUException {
        super(AddressMapperVaadinDialog.class, ConfigHistory.noHistory(AddressMapperConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Get input entries.
        final List<RDFDataUnit.Entry> inputs =
                FaultToleranceUtils.getEntries(faultTolerance, inRdfPostalAddress, RDFDataUnit.Entry.class);
        final ValueFactory valueFactory = rdfMapping.getValueFactory();
        // Create remote repository.
        final RDFDataUnit ruian;
        try {
            ruian = ExternalServicesFactory.remoteRdf(ctx, config.getRuainEndpoint(), new URI[0]);
        } catch (ExternalError ex) {
            throw ContextUtils.dpuException(ctx, ex, "Can't connect to remote SPARQL endpoint.");
        }
        // Prepare knowledge base.
        KnowledgeBase knowledgeBase = new KnowledgeBase(config.getSolrEndpoint());
        final UnstructuredFacade unstructuredFacade = new UnstructuredFacade(knowledgeBase);
        final StructuredFacade structuredFacade = new StructuredFacade(faultTolerance, inRdfPostalAddress,
                knowledgeBase, unstructuredFacade);
        final AlternativesFacade alternativesFacade = new AlternativesFacade();
        // For each graph.
        final String inputEntitiesQuery = String.format(QUERY, config.getAddressPredicate());
        for (final RDFDataUnit.Entry graph : inputs) {
            final SparqlUtils.QueryResultCollector adresses = new SparqlUtils.QueryResultCollector();
            faultTolerance.execute(inRdfPostalAddress, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    final SparqlUtils.SparqlSelectObject select =
                            SparqlUtils.createSelect(inputEntitiesQuery, Arrays.asList(graph));
                    SparqlUtils.execute(connection, ctx, select, adresses);
                }
            });
            // Create output for input.
            RDFDataUnit.Entry output = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                @Override
                public RDFDataUnit.Entry action() throws Exception {
                    final String graphName = graph.getSymbolicName() + "/mapping";
                    LOG.debug("Ouput symbolic name: {}", graphName);
                    return RdfDataUnitUtils.addGraph(outRdfMapping, graphName);
                }
            });
            // Set output.
            rdfMapping.setOutput(output);
            int counterMapped = 0;
            int progressCounter = 0;
            for (Map<String, Value> item : adresses.getResults()) {
                LOG.info("Mapping {}/{}", progressCounter++, adresses.getResults().size());

                final URI entityUri = (URI)item.get("s");
                List<RuianEntity> entities;

                if (item.get("o") instanceof URI) {
                    // Entity - structured.
                    final PostalAddress postalAddress = structuredFacade.load((URI)item.get("o"));
                    try {
                        entities = structuredFacade.toRuainEntities(postalAddress);
                    } catch (KnowledgeBaseException ex) {
                        // Can't parse enetity.
                        ContextUtils.sendWarn(ctx, "Can't paser entity for exception.", ex,
                                "Entity: {0}", entityUri);
                        continue;
                    }
                } else {
                    // It's literal - unstructured.
                    final String value = item.get("o").stringValue();
                    RuianEntity initialEntity = new RuianEntity(value);
                    try {
                        entities = unstructuredFacade.map(initialEntity, value);
                    } catch (KnowledgeBaseException ex) {
                        // Can't parse enetity.
                        ContextUtils.sendWarn(ctx, "Can't paser entity for exception.", ex,
                                "Entity: {0} Value: ''{1}''", entityUri, value);
                        continue;
                    }
                }
                // Generate alternatives and filter the results.
                entities = DuplicityFilter.filter(entities);
                alternativesFacade.addAlternatives(entities);
                entities = DuplicityFilter.filter(entities);
                // ...
                Integer counter = 0;
                // Ask for queries ..
                List<Statement> allStatements = new LinkedList<>();

                for (final RuianEntity entity : entities) {
                    final String entityQuery = entity.asRuianQuery();
                    if (entityQuery.isEmpty()) {
                        // Skip empty.
                        continue;
                    }
                    final SparqlUtils.QueryResultCollector ruianResponse = new SparqlUtils.QueryResultCollector();
                    // Execute query.
                    faultTolerance.execute(ruian, new FaultTolerance.ConnectionAction() {

                        @Override
                        public void action(RepositoryConnection connection) throws Exception {
                            final SparqlUtils.SparqlSelectObject select = SparqlUtils.createSelect(
                                    entityQuery,
                                    DataUnitUtils.getEntries(ruian, RDFDataUnit.Entry.class));
                            SparqlUtils.execute(connection, ctx, select, ruianResponse);
                        }
                    });
                    // Prepare storage for results.
                    final List<Statement> statemetns = new LinkedList<>();

                    // Extract ruianResponse, format is defined by RuianEntity.
                    final URI mappingUri = valueFactory.createURI(
                            entityUri.stringValue() + "/mapping-" + (counter++));
                    for (Map<String, Value> mapping : ruianResponse.getResults()) {
                        final URI targetInRuain = (URI)mapping.get(RuianEntity.ENTITY_BINDING);

                        statemetns.add(valueFactory.createStatement(entityUri,
                                AddressMapperOntology.HAS_ENTITY_RUIAN,
                                mappingUri));

                        statemetns.add(valueFactory.createStatement(mappingUri,
                                AddressMapperOntology.HAS_MAPPING,
                                targetInRuain));

                        // TODO We could add type of mapping (ulice, obec, .. ) here as well.
                    }
                    LOG.info("Results size: {}", ruianResponse.getResults().size());


                    // Based on number of results determine outpu.
                    switch (ruianResponse.getResults().size()) {
                        case 0: // No results.
                            statemetns.add(valueFactory.createStatement(mappingUri,
                                    AddressMapperOntology.HAS_RESULT,
                                    AddressMapperOntology.RESULT_NO_MAPPING));
                            entity.getReports().add(new Report(AddressMapperOntology.HAS_QUERY,
                                    entity.asRuianQuery()));

                            break;
                        case 1: // This is what we want.
                            statemetns.add(valueFactory.createStatement(mappingUri,
                                    AddressMapperOntology.HAS_RESULT,
                                   AddressMapperOntology.RESULT_SINGLE_MAPPING));
                            ++counterMapped;
                            break;
                        default: // > 1
                            statemetns.add(valueFactory.createStatement(mappingUri,
                                    AddressMapperOntology.HAS_RESULT,
                                    AddressMapperOntology.RESULT_MULTIPLE_MAPPINGS));
                            entity.getReports().add(new Report(AddressMapperOntology.HAS_QUERY,
                                    entity.asRuianQuery()));
                            break;
                    }
                    // Add information from query.
                    statemetns.addAll(entity.asStatements(mappingUri));

                    // Check for end.
                    if (ruianResponse.getResults().size() == 1) {
                        // We have found mapping, add only sucess mapping and continu ewith another object.
                        allStatements = statemetns;
                        break;
                    } else {
                        // Add information about failure to output.
                        allStatements.addAll(statemetns);
                    }
                }
                // Add all - if mapping has been found we add information only about used mapping entity
                //           in other case we all information about all the attemps.
                rdfMapping.add(allStatements);
            }
            // Print results.
            LOG.info("Mapped '{}' from '{}' in {}", counterMapped, adresses.getResults().size(), graph);
            ContextUtils.sendShortInfo(ctx, "Ok/Failed to parse {0}/{1}",
                    counterMapped, adresses.getResults().size() - counterMapped);
        }
    }

}
