package cz.cuni.mff.xrg.uv.addressmapper.facades;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.xrg.uv.addressmapper.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.addressLocality.AddressLocalityMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.addressRegion.AddressRegionMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.postalCode.PostalCodeMapper;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.streetAddress.StreetAddressMapper;
import cz.cuni.mff.xrg.uv.addressmapper.objects.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.objects.RuianEntity;
import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class RuianFacade {

    private final AddressLocalityMapper addressLocalityMapper = new AddressLocalityMapper();
    
    private final AddressRegionMapper addressRegionMapper = new AddressRegionMapper();
    
    private final PostalCodeMapper postalCodeMapper = new PostalCodeMapper();
    
    private final StreetAddressMapper streetAddressMapper = new StreetAddressMapper();
    
    /**
     * Convert {@link PostalAddress} to possible {@link RuianEntity}s.
     *
     * @param postalAddress
     * @return First entity is the one with greatest relevance.
     */
    public List<RuianEntity> toRuainEntities(PostalAddress postalAddress) throws DPUException {
        List<RuianEntity> entities = new LinkedList<>();
        // Add initial entity.
        entities.add(new RuianEntity(postalAddress.getEntity()));
        // Apply transformations.
        entities = map(addressLocalityMapper, entities, postalAddress);
        entities = map(addressRegionMapper, entities, postalAddress);
        entities = map(postalCodeMapper, entities, postalAddress);
        entities = map(streetAddressMapper, entities, postalAddress);
        return entities;
    }

    protected List<RuianEntity> map(AbstractMapper mapper, List<RuianEntity> entities,
            PostalAddress postalAddress) throws DPUException {
        final List<RuianEntity> results = new LinkedList<>();
        // Transform.
        for (RuianEntity entity : entities) {
            results.addAll(mapper.map(postalAddress, entity));
        }
        // Return results.
        return results;
    }

}
