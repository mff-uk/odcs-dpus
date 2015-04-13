package cz.cuni.mff.xrg.uv.addressmapper.address.structured.mapping;

import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;
import cz.cuni.mff.xrg.uv.addressmapper.address.structured.PostalAddress;
import cz.cuni.mff.xrg.uv.addressmapper.knowledgebase.KnowledgeBaseException;

/**
 *
 * @author Škoda Petr
 */
public abstract class AbstractMapper {

    protected interface Transformer<E> {

        /**
         * In this function given value should be set to given entity.
         *
         * @param value
         * @param entity
         */
        public void transform(E value, RuianEntity entity);
        
    };

    /**
     * Map given {@link PostalAddress} to {@link RuianEntity}.
     *
     * @param address
     * @param entity
     * @return All possible mappings, do not add given entity to the result - create a copy.
     */
    public abstract List<RuianEntity> map(final PostalAddress address, final RuianEntity entity)
            throws KnowledgeBaseException;

    /**
     * Can be used to generate alternatives for given list of values.
     *
     * @param <E>
     * @param values
     * @param entities
     * @param transformer
     * @return
     */
    protected <E> List<RuianEntity> generateAlternatives(List<E> values, List<RuianEntity> entities,
            Transformer<E> transformer) {
        final List<RuianEntity> newEntities = new LinkedList<>();
        for (RuianEntity entity : entities) {
            for (E value : values) {
                final RuianEntity newEntity = new RuianEntity(entity);
                transformer.transform(value, newEntity);
                newEntities.add(entity);
            }
        }
        return newEntities;
    }

}
