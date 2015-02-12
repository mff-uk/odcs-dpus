package cz.cuni.mff.xrg.uv.utils.dataunit.files;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;

/**
 * Vocabulary definition for files.
 *
 * @author Škoda Petr
 */
public class FilesVocabulary {

    public static final URI UV_VIRTUAL_PATH;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        UV_VIRTUAL_PATH = valueFactory.createURI(VirtualPathHelper.PREDICATE_VIRTUAL_PATH);
    }

}
