package cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.postalCode;

import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.xrg.uv.addressmapper.AddressMapperOntology;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping.AbstractMapper;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;
import cz.cuni.mff.xrg.uv.addressmapper.objects.Report;

/**
 * Set:
 *    pcs
 *
 * @author Škoda Petr
 */
public class PostalCodeMapper extends AbstractMapper {

    @Override
    public List<RuianEntity> map(PostalAddress address, RuianEntity entity) throws KnowledgeBaseException {
        final RuianEntity outputEntity = new RuianEntity(entity);

        if (address.getPostalCode() == null) {
            return Arrays.asList(outputEntity);
        }

        try {
            final Integer value = Integer.parseInt(address.getPostalCode().replaceAll("\\s", ""));
            // Set to the given entity.
            outputEntity.setPsc(value);
        } catch (NumberFormatException ex) {
            // Add report about our failure.
            Report report = new Report(
                    AddressMapperOntology.MAPPER_POSTAL_CODE,
                    "PSČ není platné.");
            outputEntity.getReports().add(report);
        }        
        return Arrays.asList(outputEntity);
    }

}
