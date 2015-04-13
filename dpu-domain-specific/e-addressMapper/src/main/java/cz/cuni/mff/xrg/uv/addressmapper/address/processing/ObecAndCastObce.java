package cz.cuni.mff.xrg.uv.addressmapper.address.processing;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * In some cases 'cast obce' can be misleadingly dedicated from 'obec'. As an alternatives we try to 
 * remove 'cast obce' if it's same as 'obec' or if it's 
 * 
 *
 *
 * @author Škoda Petr
 */
public class ObecAndCastObce extends AbstractAlternative {

    public ObecAndCastObce() {
        super(AddressMapperOntology.ALT_REMOVE_CASTOBCE);
    }

    @Override
    protected RuianEntity alternative(RuianEntity entity) {
        if (entity.getCastObce() == null) {
            return null;
        }
        if (entity.getUlice() != null) {
            // 'ulice' is presented, create alternative.
            final RuianEntity newEntity = new RuianEntity(entity);
            newEntity.setCastObce(null);
            return newEntity;
        }
        if (entity.getObec() != null && entity.getObec().contains(entity.getObec())) {
            // 'cast obce' is the same or contains part of 'obec'.

            // TODO We can use meaning mapping to detect collitions instead.
            final RuianEntity newEntity = new RuianEntity(entity);
            newEntity.setCastObce(null);
            return newEntity;
        }
        return null;
    }

}
