package cz.cuni.mff.xrg.uv.boost.rdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Class designed for easy entity building. Each builder can be used to build just one entity.
 *
 * As entity builder is in-memory entity is should not be used to build bigger objects (100+ statement).
 *
 * @author Škoda Petr
 */
public class EntityBuilder {

    /**
     * Store statement about currently constructed object.
     */
    private final List<Statement> statemetns = new ArrayList<>(20);

    /**
     * Uri of currently constructed entity.
     */
    private final URI entityUri;

    /**
     * Used value factory.
     */
    private final ValueFactory valueFactory;

    /**
     *
     * @param entityUri
     * @param valueFactory
     * @param ontology     Ontology used during creation of this object. Can be null but in such case methods
     *                     which utilize ontology must no be called.
     */
    public EntityBuilder(URI entityUri, ValueFactory valueFactory) {
        this.entityUri = entityUri;
        this.valueFactory = valueFactory;
    }

    /**
     * Add a property to this object as a string literal without language tag.
     *
     * @param property
     * @param value
     * @return
     */
    public EntityBuilder property(URI property, String value) {
        statemetns.add(valueFactory.createStatement(entityUri, property, valueFactory.createLiteral(value)));
        return this;
    }

    /**
     * Add a property to this object.
     *
     * @param property
     * @param value
     * @return
     */
    public EntityBuilder property(URI property, Value value) {
        statemetns.add(valueFactory.createStatement(entityUri, property, value));
        return this;
    }

    /**
     * Add a property to this object. As a value the entity URI of given {@link EntityBuilder} is used.
     *
     * @param property
     * @param entity
     * @return
     */
    public EntityBuilder property(URI property, EntityBuilder entity) {
        statemetns.add(valueFactory.createStatement(entityUri, property, entity.getEntityUri()));
        return this;
    }

    /**
     *
     * @return Representation of current entity as a list of subject. Do not modify the returned list.
     */
    public List<Statement> asStatements() {
        return this.statemetns;
    }

    /**
     *
     * @return URI of entity that is under construction.
     */
    public URI getEntityUri() {
        return entityUri;
    }

}
