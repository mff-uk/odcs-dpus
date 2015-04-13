package cz.cuni.mff.xrg.uv.addressmapper.address.processing;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;

import cz.cuni.mff.xrg.uv.addressmapper.objects.ReportAlternative;
import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

/**
 * Abstract class for alternatives generation.
 * 
 * @author Škoda Petr
 */
public abstract class AbstractAlternative {

    private final URI alternativeUri;

    public AbstractAlternative(URI alternativeUri) {
        this.alternativeUri = alternativeUri;
    }

    /**
     * Generate alternatives.
     * 
     * @param entities
     * @return
     */
    public List<RuianEntity> alternatives(List<RuianEntity> entities) {
        final List<RuianEntity> output = new LinkedList<>();
        for (RuianEntity item : entities) {
            final RuianEntity newEntity = alternative(item);
            if (newEntity != null) {
                // Add message.
                newEntity.getReports().add(
                        new ReportAlternative(alternativeUri, ""));
                // Add to output.
                output.add(newEntity);
            }
        }
        return output;
    }

    /**
     *
     * @param entity
     * @return Null if there is no alternative for this value.
     */
    protected abstract RuianEntity alternative(RuianEntity entity);

}
