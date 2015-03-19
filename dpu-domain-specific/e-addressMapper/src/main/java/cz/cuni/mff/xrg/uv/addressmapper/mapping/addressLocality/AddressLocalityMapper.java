package cz.cuni.mff.xrg.uv.addressmapper.mapping.addressLocality;

import java.util.Arrays;
import java.util.List;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.objects.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.objects.RuianEntity;
import eu.unifiedviews.dpu.DPUException;

/**
 * Set:
 *    obec
 *    castObce
 *
 * @author Škoda Petr
 */
public class AddressLocalityMapper extends AbstractMapper {

    @Override
    public List<RuianEntity> map(PostalAddress address, RuianEntity entity) throws DPUException {
        final RuianEntity outputEntity = new RuianEntity(entity);

        if (address.getAddressLocality() == null) {
            return Arrays.asList(outputEntity);
        }

        final String[] objectSplit = address.getAddressLocality().split(",", 2);
        // TODO Check values?        
        outputEntity.setObec(objectSplit[0]);
        if (objectSplit.length > 1) {
            outputEntity.setCastObce(objectSplit[1].trim());
        }
        return Arrays.asList(outputEntity);
    }

}
