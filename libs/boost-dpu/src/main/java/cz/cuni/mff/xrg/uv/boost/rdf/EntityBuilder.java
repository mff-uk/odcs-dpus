package cz.cuni.mff.xrg.uv.boost.rdf;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Class designed for easy entity building.
 *
 * @author Škoda Petr
 */
public class EntityBuilder {

    /**
     * Used to count objects.
     */
    private int objectCounter = 0;

    /**
     * Template for resource URI, must contains exactly one '%d' that is replaced by numbers as objects are
     * created.
     */
    private final String entityUriTemplate;

    /**
     * Store statement about currently constructed object.
     */
    private final List<Statement> statemetns = new ArrayList<>(20);

    /**
     * Uri of currently constructed entity.
     */
    private URI entityUri;

    /**
     * Used value factory.
     */
    private final ValueFactory valueFactory;

    /**
     *
     * @param entityUriTemplate Template for resource URI. Must contains just one %d that will be replaced by
     *                          index.
     * @param valueFactory
     */
    public EntityBuilder(String entityUriTemplate, ValueFactory valueFactory) {
        this.entityUriTemplate = entityUriTemplate;
        this.valueFactory = valueFactory;
    }

    /**
     * Create a new object. All information about previous object are lost during this call.
     *
     * @return
     */
    public EntityBuilder createNewObject() {
        statemetns.clear();
        entityUri = valueFactory.createURI(String.format(entityUriTemplate, ++objectCounter));
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
