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

import cz.cuni.mff.xrg.uv.addressmapper.facades.RuianFacade;
import cz.cuni.mff.xrg.uv.addressmapper.facades.SchemaFacade;
import cz.cuni.mff.xrg.uv.addressmapper.objects.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.objects.RuianEntity;
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

    private static final String QUERY = "SELECT ?s WHERE { ?s a <http://schema.org/PostalAddress> . }";

    @DataUnit.AsInput(name = "input", description = "Trojice s s:PostalAddress a související jenž se mají namapovat na ruian.")
    public RDFDataUnit inRdfPostalAddress;

    @DataUnit.AsOutput(name = "output", description = "Mapovani z postalAddress na ruain pomoci http://ruian.linked.opendata.cz/ontology/links/.")
    public WritableRDFDataUnit outRdfMapping;

    @ExtensionInitializer.Init(param = "outRdfMapping")
    public WritableSimpleRdf rdfMapping;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public AddressMapper() {
        super(AddressMapperVaadinDialog.class, ConfigHistory.noHistory(AddressMapperConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
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
        final RuianFacade ruianFacade = new RuianFacade();
        final SchemaFacade schemaFacade = new SchemaFacade(faultTolerance, inRdfPostalAddress);
        // For each graph.
        for (final RDFDataUnit.Entry graph : inputs) {
            final SparqlUtils.QueryResultCollector adresses = new SparqlUtils.QueryResultCollector();
            faultTolerance.execute(inRdfPostalAddress, new FaultTolerance.ConnectionAction() {

                @Override
                public void action(RepositoryConnection connection) throws Exception {
                    final SparqlUtils.SparqlSelectObject select =
                            SparqlUtils.createSelect(QUERY, Arrays.asList(graph));
                    SparqlUtils.execute(connection, ctx, select, adresses);
                }
            });
            // Create output for input.
            RDFDataUnit.Entry output = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

                @Override
                public RDFDataUnit.Entry action() throws Exception {
                    final String graphName = graph.getSymbolicName();
                    LOG.debug("Ouput symbolic name: %s", graphName);
                    return RdfDataUnitUtils.addGraph(outRdfMapping, graphName);
                }
            });
            // Set output.
            rdfMapping.setOutput(output);
            for (Map<String, Value> item : adresses.getResults()) {
                final URI postalAddressUri = (URI)item.get("s");
                // Load entity.
                final PostalAddress postalAddress = schemaFacade.load(postalAddressUri);
                // Convert into entity(ies).
                final List<RuianEntity> entities = ruianFacade.toRuainEntities(postalAddress);
                // Ask for queries ..
                for (final RuianEntity entity : entities) {              
                    final SparqlUtils.QueryResultCollector ruianResponse = new SparqlUtils.QueryResultCollector();
                    // Execute query.
                    faultTolerance.execute(ruian, new FaultTolerance.ConnectionAction() {

                        @Override
                        public void action(RepositoryConnection connection) throws Exception {
                            final SparqlUtils.SparqlSelectObject select = SparqlUtils.createSelect(
                                    entity.asRuianQuery(),
                                    DataUnitUtils.getEntries(ruian, RDFDataUnit.Entry.class));
                            SparqlUtils.execute(connection, ctx, select, ruianResponse);
                        }
                    });
                    // Prepare storage for results.
                    final List<Statement> statemetns = new LinkedList<>();

                    // Extract ruianResponse, format is defined by RuianEntity.
                    final URI entityUri = valueFactory.createURI(postalAddressUri.stringValue() + "/mapping");
                    for (Map<String, Value> mapping : ruianResponse.getResults()) {
                        final URI mappingUri = (URI)mapping.get(RuianEntity.ENTITY_BINDING);

                        statemetns.add(valueFactory.createStatement(entityUri,
                                AddressMapperOntology.MAPPING,
                                mappingUri));

                        // TODO We could add type of mapping (ulice, obec, .. ) here as well.
                    }
                    // Based on number of results determine outpu.
                    switch (ruianResponse.getResults().size()) {
                        case 0: // No results.
                            statemetns.add(valueFactory.createStatement(entityUri,
                                    AddressMapperOntology.HAS_RESULT,
                                    AddressMapperOntology.RESULT_NO_MAPPING));
                            break;
                        case 1: // This is what we want.
                            statemetns.add(valueFactory.createStatement(entityUri,
                                    AddressMapperOntology.HAS_RESULT,
                                    AddressMapperOntology.RESULT_SINGLE_MAPPING));
                            break;
                        default: // > 1
                            statemetns.add(valueFactory.createStatement(entityUri,
                                    AddressMapperOntology.HAS_RESULT,
                                    AddressMapperOntology.RESULT_MULTIPLE_MAPPINGS));
                            break;
                    }
                    // Add information from query.
                    statemetns.addAll(entity.asStatements(entityUri));
                    // Add to the output.
                    rdfMapping.add(statemetns);

                    // Check for end.
                    if (ruianResponse.getResults().size() == 1) {
                        // We have found mapping, continue with another entity.
                        break;
                    }
                }
            }
        }
    }
     
}
