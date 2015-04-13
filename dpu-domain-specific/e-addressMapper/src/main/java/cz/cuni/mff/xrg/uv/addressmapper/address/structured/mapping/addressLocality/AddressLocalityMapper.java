package cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.addressLocality;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.addressmapper.address.StringAddress;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.address.unstructured.UnstructuredFacade;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * Set:
 *  obec
 *  castObce
 *
 * TODO: USe knowledge base!!!
 *
 * @author Škoda Petr
 */
public class AddressLocalityMapper extends AbstractMapper {

    private final UnstructuredFacade unstructuredFacade;

    private static final Logger LOG = LoggerFactory.getLogger(AddressLocalityMapper.class);

    public AddressLocalityMapper(KnowledgeBase knowledgeBase, UnstructuredFacade unstructuredFacade) {
        this.unstructuredFacade = unstructuredFacade;
    }

    @Override
    public List<RuianEntity> map(PostalAddress address, RuianEntity entity) throws KnowledgeBaseException {
        final RuianEntity outputEntity = new RuianEntity(entity);
        if (address.getAddressLocality() == null || address.getAddressLocality().isEmpty()) {
            return Arrays.asList(outputEntity);
        }
        LOG.info("AddressLocalityMapper.map: {}", address.getAddressLocality());
        final StringAddress stringAddress = new StringAddress(address.getAddressLocality());

        // Try to fill obec and then cast obce.
        final Set<RuianEntity> obec = new HashSet<>();
        obec.addAll(unstructuredFacade.fillObec(outputEntity, stringAddress));

        LOG.info("Size.obec: {}", obec.size());

        final Set<RuianEntity> castObce = new HashSet<>();
        for (RuianEntity item : obec) {
            castObce.addAll(unstructuredFacade.fillCastObce(item, stringAddress));
        }

        LOG.info("Size.castObce: {}", castObce.size());
        
        return Arrays.asList(castObce.toArray(new RuianEntity[0]));
    }

}
