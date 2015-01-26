package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holds annotations to describe RDF ontology for POJO.
 *
 * @author Škoda Petr
 */
public class Ontology {

    private Ontology() {

    }

    /**
     * Map RDF object to property.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Property {

        /**
         *
         * @return URI of predicate for this property.
         */
        String uri();

        /**
         *
         * @return Property description in RDF, used only during serialization into RDF.
         */
        String description() default "";

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

        /**
         *
         * @return If this object is used in other object that is being serialized then resource URI for this
         *         object must be created. The URI of parent is taken appended by number and then by this
         *         value.
         */
        String resourceSuffix() default "";

    }
}
