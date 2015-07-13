package cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.addressRegion;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * Set:
 *  vusc
 *  okres
 *
 * @author Škoda Petr
 */
public class AddressRegionMapper extends AbstractMapper {

    private static final Logger LOG = LoggerFactory.getLogger(AddressRegionMapper.class);

    private final KnowledgeBase knowledgeBase;

    public AddressRegionMapper(KnowledgeBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }
    
    @Override
    public List<RuianEntity> map(PostalAddress address, RuianEntity entity) throws KnowledgeBaseException {
        final RuianEntity outputEntity = new RuianEntity(entity);

        if (address.getAddressRegion() == null) {
            return Arrays.asList(outputEntity);
        }
        // Try to get VUSC.
        final List<String> vusc = knowledgeBase.getVusc(address.getAddressRegion());
        LOG.info("map vusc.size() = {}", vusc.size());
        if (vusc.isEmpty()) {
            // Do nothing here.
        } else if (vusc.size() == 1) {
            outputEntity.setVusc(vusc.get(0));
        } else {
            outputEntity.setVusc(vusc.get(0));
            final Report report = new Report(AddressMapperOntology.MAPPER_ADDRESS_REGION,
                    String.format("Multiple options for vusc:'%s', only one is used.",
                            address.getAddressRegion()));
            outputEntity.getReports().add(report);            
        }
        // Try to get Okres.
        final List<String> okres = knowledgeBase.getOkres(address.getAddressRegion());
        if (okres.isEmpty()) {
            // Do nothing here.
        } else if (okres.size() == 1) {
            outputEntity.setOkres(okres.get(0));
        } else {
            outputEntity.setOkres(okres.get(0));
            final Report report = new Report(AddressMapperOntology.MAPPER_ADDRESS_REGION,
                    String.format("Multiple options for okres:'%s', only one is used.",
                            address.getAddressRegion()));
            outputEntity.getReports().add(report);
        }
        // Final check, if AddressReagion is not empty and we found no mapping then write exception.
        if (vusc.isEmpty() && okres.isEmpty() && address.getAddressRegion() != null) {
            final Report report = new Report(AddressMapperOntology.MAPPER_ADDRESS_REGION,
                    String.format("Unknown address region: '%s'", address.getAddressRegion()));
            outputEntity.getReports().add(report);
        }
        return Arrays.asList(outputEntity);
    }

}
