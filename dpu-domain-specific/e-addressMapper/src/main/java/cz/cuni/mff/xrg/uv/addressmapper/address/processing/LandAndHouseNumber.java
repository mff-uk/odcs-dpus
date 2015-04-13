package cz.cuni.mff.xrg.uv.addressmapper.address.processing;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * If presented swap house and land number.
 *
 * @author Škoda Petr
 */
public class LandAndHouseNumber extends AbstractAlternative {

    public LandAndHouseNumber() {
        super(AddressMapperOntology.ALT_SWAP_HOUSE_AND_LAND_NUMBER);
    }

    @Override
    protected RuianEntity alternative(RuianEntity entity) {
        if (entity.getCisloDomovni() != null && entity.getCisloOrientancni() != null) {
            final RuianEntity newEntity = new RuianEntity(entity);
            // Swap values.
            Integer tempSwap = newEntity.getCisloDomovni();
            newEntity.setCisloDomovni(newEntity.getCisloOrientancni());
            newEntity.setCisloOrientancni(tempSwap);
            return newEntity;
        }
        return null;
    }

}
