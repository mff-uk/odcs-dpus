package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holds annotations to describe RDF ontology for POJO. It's recommended to store ontology definition in
 * instance of {@link OntologyDefinition} class.
 *
 * @author Škoda Petr
 */
public class EntityDescription {

    private EntityDescription() {

    }

    /**
     * Map RDF object to property.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {

        /**
         *
         * @return String URI of predicate for this property.
         */
        String uri();

    }

    /**
     * Map object to RDF subject of given class. Used only for serialization into RDF.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Entity {

        /**
         *
         * @return Fully denoted entity class type as URI.
         */
        String type();

    }

}
