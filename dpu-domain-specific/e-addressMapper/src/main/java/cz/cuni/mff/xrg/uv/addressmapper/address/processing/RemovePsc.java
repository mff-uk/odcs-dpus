package cz.cuni.mff.xrg.uv.addressmapper.address.processing;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * Remove 'psc' if presented.
 *
 * @author Škoda Petr
 */
public class RemovePsc extends AbstractAlternative {

    public RemovePsc() {
        super(AddressMapperOntology.ALT_REMOVE_PSC);
    }

    @Override
    protected RuianEntity alternative(RuianEntity entity) {
        if (entity.getPsc()!= null) {
            final RuianEntity newEntity = new RuianEntity(entity);
            newEntity.setPsc(null);
            return newEntity;
        }
        return null;
    }

}
