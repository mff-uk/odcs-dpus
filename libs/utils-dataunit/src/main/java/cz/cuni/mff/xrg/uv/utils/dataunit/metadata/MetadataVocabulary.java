package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import eu.unifiedviews.dataunit.MetadataDataUnit;

/**
 * Common vocabulary definition.
 *
 * @author Škoda Petr
 */
public class MetadataVocabulary {

    public static final URI UV_SYMBOLIC_NAME;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        UV_SYMBOLIC_NAME = valueFactory.createURI(MetadataDataUnit.PREDICATE_SYMBOLIC_NAME);
    }

}
