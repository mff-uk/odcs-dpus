package cz.cuni.mff.xrg.uv.addressmapper.address.processing;

import java.util.List;

import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 *
 * @author Škoda Petr
 */
public class AlternativesFacade {

    private final LandAndHouseNumber landAndHouseNumber = new LandAndHouseNumber();
            
    private final ObecAndCastObce obecAndCastObce = new ObecAndCastObce();
    
    private final RemovePsc removePsc = new RemovePsc();

    public void addAlternatives(List<RuianEntity> entities) {

        entities.addAll(obecAndCastObce.alternatives(entities));

        entities.addAll(removePsc.alternatives(entities));

        entities.addAll(landAndHouseNumber.alternatives(entities));
        
    }

}
