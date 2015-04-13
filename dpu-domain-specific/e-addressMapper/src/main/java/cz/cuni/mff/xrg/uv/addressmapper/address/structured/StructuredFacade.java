package cz.cuni.mff.xrg.uv.addressmapper.address.structured;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.objects.ReportAlternative;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.addressLocality.AddressLocalityMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.addressRegion.AddressRegionMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.postalCode.PostalCodeMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.streetAddress.StreetAddressMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.unstructured.UnstructuredFacade;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.address.DuplicityFilter;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdf;
import eu.unifiedviews.helpers.dpu.serialization.rdf.SerializationRdfFactory;

import static cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology.ALT_REMOVE_PSC;

/**
 *
 * @author Škoda Petr
 */
public class StructuredFacade {

    private static final Logger LOG = LoggerFactory.getLogger(StructuredFacade.class);

    private final AddressLocalityMapper addressLocalityMapper;

    private final AddressRegionMapper addressRegionMapper;

    private final PostalCodeMapper postalCodeMapper = new PostalCodeMapper();

    private final StreetAddressMapper streetAddressMapper;

    private final SerializationRdf serialization = SerializationRdfFactory.rdfSimple();

    private final FaultTolerance faultTolerace;

    private final RDFDataUnit input;

    private final List<RDFDataUnit.Entry> entries;

    public StructuredFacade(FaultTolerance faultTolerace, RDFDataUnit input, KnowledgeBase knowledgeBase,
            UnstructuredFacade unstructuredFacade) throws DPUException {
        addressLocalityMapper = new AddressLocalityMapper(knowledgeBase, unstructuredFacade);
        addressRegionMapper = new AddressRegionMapper(knowledgeBase);
        streetAddressMapper = new StreetAddressMapper(knowledgeBase);
        this.faultTolerace = faultTolerace;
        this.input = input;
        // Prepare context.
        entries = FaultToleranceUtils.getEntries(faultTolerace, input, RDFDataUnit.Entry.class);
    }

    /**
     * Load a {@link PostalAddress} entity for given subject.
     *
     * @param entityUri
     * @return
     * @throws DPUException
     */
    public PostalAddress load(final URI entityUri) throws DPUException {
        final PostalAddress address = new PostalAddress(entityUri);
        faultTolerace.execute(input, new FaultTolerance.ConnectionAction() {
            
            @Override
            public void action(RepositoryConnection connection) throws Exception {
                serialization.convert(connection, entityUri, entries, address, null);
            }
        });
        return address;
    }

    /**
     * Convert {@link PostalAddress} to possible {@link RuianEntity}s.
     *
     * @param postalAddress
     * @return First entity is the one with greatest relevance.
     */
    public List<RuianEntity> toRuainEntities(PostalAddress postalAddress) throws DPUException, KnowledgeBaseException {
        List<RuianEntity> entities = new LinkedList<>();
        // Add initial entity.
        entities.add(new RuianEntity(postalAddress.getEntity()));
        // Apply transformations.
        LOG.info("Size.start: {}", entities.size());
        entities = map(addressLocalityMapper, entities, postalAddress);
        LOG.info("Size.addressLocalityMapper: {}", entities.size());
        entities = map(addressRegionMapper, entities, postalAddress);
        LOG.info("Size.addressRegionMapper: {}", entities.size());
        entities = map(postalCodeMapper, entities, postalAddress);
        LOG.info("Size.postalCodeMapper: {}", entities.size());
        entities = map(streetAddressMapper, entities, postalAddress);
        LOG.info("Size.streetAddressMapper: {}", entities.size());

        return entities;
    }

    protected List<RuianEntity> map(AbstractMapper mapper, List<RuianEntity> entities,
            PostalAddress postalAddress) throws DPUException, KnowledgeBaseException {
        final List<RuianEntity> results = new LinkedList<>();
        // Transform.
        for (RuianEntity entity : entities) {
            results.addAll(mapper.map(postalAddress, entity));
        }
        // Return results.
        return results;
    }

    /**
     * If 'obec' is same as 'castObce' then remove the value.
     * 
     * @param input
     * @return 
     */
    protected List<RuianEntity> alternativeObecSameAsCastObce(List<RuianEntity> input) {
        final List<RuianEntity> output = new LinkedList<>();

        for (RuianEntity item : input) {
            if (item.getObec() != null && item.getCastObce() != null &&
                    item.getObec().compareTo(item.getCastObce()) == 0) {
                final RuianEntity newEntity = new RuianEntity(item);
                newEntity.setCastObce(null);
                // Add report.
                newEntity.getReports().add(
                        new ReportAlternative(AddressMapperOntology.ALT_REMOVE_CASTOBCE, ""));
                // Add to output.
                output.add(newEntity);
            }
        }
        return output;
    }

    /**
     * Does not enforce absence of 'pismeno' for 'cisloOrientancni'. Possible duplicity will be filtered later.
     *
     * @param input
     * @return
     */
    protected List<RuianEntity> forceMissingPismeno(List<RuianEntity> input) {
        final List<RuianEntity> output = new LinkedList<>();

        for (RuianEntity item : input) {
            if (item.getObec() != null && item.getCastObce() != null &&
                    item.getObec().compareTo(item.getCastObce()) == 0) {
                final RuianEntity newEntity = new RuianEntity(item);
                newEntity.setForceMissingPismeno(false);
                // Add to output.
                output.add(newEntity);
            }
        }
        return output;
    }

}
