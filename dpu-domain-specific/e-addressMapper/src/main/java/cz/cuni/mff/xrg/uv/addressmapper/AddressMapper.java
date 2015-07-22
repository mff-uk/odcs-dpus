package cz.cuni.mff.xrg.uv.addressmapper;

import java.util.Arrays;
import java.util.Collections;
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

import cz.cuni.mff.xrg.uv.addressmapper.service.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.service.Response;
import cz.cuni.mff.xrg.uv.addressmapper.service.ServiceFacade;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdf;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdfFactory;

import static cz.cuni.mff.xrg.uv.addressmapper.AddressMapperVocabulary.HAS_NUMBER_OF_MAPPING;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public final class AddressMapper extends AbstractDpu<AddressMapperConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(AddressMapper.class);

    /**
     * %s - Predicate URI.
     */
    private static final String QUERY = "SELECT ?s ?o WHERE { ?s <%s> ?o. }";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit inRdfPostalAddress;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit outRdfOutput;

    @ExtensionInitializer.Init(param = "outRdfOutput")
    public WritableSimpleRdf rdfOutput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    private final SerializationRdf serialization = SerializationRdfFactory.rdfSimple();

    public AddressMapper() throws DPUException {
        super(AddressMapperVaadinDialog.class, ConfigHistory.noHistory(AddressMapperConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final ServiceFacade serviceFacade = new ServiceFacade(ctx);
        // Get input entries.
        final List<RDFDataUnit.Entry> inputs =
                FaultToleranceUtils.getEntries(faultTolerance, inRdfPostalAddress, RDFDataUnit.Entry.class);
        final ValueFactory valueFactory = rdfOutput.getValueFactory();
        final URI integerType = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#integer");
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
                    return RdfDataUnitUtils.addGraph(outRdfOutput, graphName);
                }
            });
            // Set output.
            rdfOutput.setOutput(output);
            int counterMapped = 0;
            int progressCounter = 0;
            for (Map<String, Value> item : adresses.getResults()) {
                LOG.info("Mapping {}/{}", progressCounter++, adresses.getResults().size());
                final URI entityUri = (URI)item.get("s");
                final List<Response> entities;
                if (item.get("o") instanceof URI) {
                    // Entity - structured.
                    final PostalAddress postalAddress = readAddress((URI)item.get("o"), inputs);
                    entities = serviceFacade.resolve(postalAddress, config.getServiceEndpoint());
                } else {
                    // It's literal - unstructured.
                    final String value = item.get("o").stringValue();
                    entities = Collections.EMPTY_LIST;
                }

                final List<Statement> statements = new LinkedList<>();
                if (entities == null) {
                    // Service not available.
                    throw new DPUException("Null returned!");
                } else if (entities.isEmpty()) {
                    // No result.
                    statements.add(valueFactory.createStatement(entityUri,
                            AddressMapperVocabulary.HAS_MAPPING,
                            AddressMapperVocabulary.MAPPING_EMPTY));
                    statements.add(valueFactory.createStatement(entityUri,
                            AddressMapperVocabulary.HAS_NUMBER_OF_MAPPING,
                            valueFactory.createLiteral("0", integerType)));

                } else {
                    // Some results.
                    statements.add(valueFactory.createStatement(entityUri,
                            AddressMapperVocabulary.HAS_NUMBER_OF_MAPPING,
                            valueFactory.createLiteral(Integer.toString(entities.size()),
                                    integerType)));

                    Integer counter = 0;
                    for (Response response : entities) {
                        final URI mappingSubject = valueFactory.createURI(
                                entityUri.stringValue() + "/mapping-" + (counter++));
                        statements.add(valueFactory.createStatement(entityUri,
                                AddressMapperVocabulary.HAS_MAPPING,
                                mappingSubject));
                        // Entity.
                        statements.add(valueFactory.createStatement(mappingSubject,
                                AddressMapperVocabulary.HAS_RUIAN,
                                valueFactory.createURI(response.getUri())));
                        statements.add(valueFactory.createStatement(mappingSubject,
                                AddressMapperVocabulary.HAS_COMPLETENESS,
                                valueFactory.createLiteral(response.getCompleteness())));
                        statements.add(valueFactory.createStatement(mappingSubject,
                                AddressMapperVocabulary.HAS_CONFIDENCE,
                                valueFactory.createLiteral(response.getConfidence())));
                    }
                }
                rdfOutput.add(statements);
            }
            // Print results.
            LOG.info("Mapped '{}' from '{}' in {}", counterMapped, adresses.getResults().size(), graph);
            ContextUtils.sendShortInfo(ctx, "Ok/Failed to parse {0}/{1}",
                    counterMapped, adresses.getResults().size() - counterMapped);
        }
    }

    /**
     * Load a {@link PostalAddress} entity for given subject.
     *
     * @param entityUri
     * @param entries
     * @return Loaded entity of given URI.
     * @throws DPUException
     */
    public PostalAddress readAddress(final URI entityUri, final List<RDFDataUnit.Entry> entries) throws DPUException {
        final PostalAddress address = new PostalAddress(entityUri);
        faultTolerance.execute(inRdfPostalAddress, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                serialization.convert(connection, entityUri, entries, address, null);
            }
        });
        return address;
    }

}
