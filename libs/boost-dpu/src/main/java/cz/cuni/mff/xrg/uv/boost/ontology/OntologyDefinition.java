package cz.cuni.mff.xrg.uv.boost.ontology;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author Škoda Petr
 */
public final class OntologyDefinition {

//    /**
//     * Can be used only on public static non-final fields.
//     */
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.FIELD)
//    public @interface External {
//
//        /**
//         *
//         * @return Full path to field. The value of given field may be loaded into this variable.
//         */
//        String path();
//
//    }

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
