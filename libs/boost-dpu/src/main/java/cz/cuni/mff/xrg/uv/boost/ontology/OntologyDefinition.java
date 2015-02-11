
package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contains definitions of annotations that can be used to represent RDF vocabulary or ontology.
 *
 * @author Škoda Petr
 */
public class OntologyDefinition {

    /**
     * Can be used only on public static non-final fields. If presented the URI value for the field
     * is determined by the value of class and attribute refereed in 'path' - annotation property.
     * Does not work in transitive way, if. target field has also {@link UpdateFrom} annotation it's
     * not followed.
     *
     * Can be use to secure synchronization of ontologies on "soft" level. Use with caution as it change
     * ontology on runtime based on presented libraries and DPUs.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface UpdateFrom {

        /**
         *
         * @return Full path to field. The value of given field may be loaded into this variable.
         */
        String path();

    }

    /**
     * Filed annotated with this value is considered to not be a URI definition.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotUri {
        
    }

    private OntologyDefinition() {
        
    }

}
