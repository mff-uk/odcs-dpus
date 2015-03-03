package cz.opendata.linked.cz.gov.smlouvy;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataVocabulary;

/**
 *
 * @author Škoda Petr
 */
public class ExtractorOntology {

    /**
     * Class form main configuration object.
     */
    public static final URI XSLT_CLASS;

    /**
     * Class to associate certain symbolic name with set of XSLT parameter.
     */
    public static final URI XSLT_FILEINFO_CLASS;

    /**
     * Class contains a single XSLT parameter.
     */
    public static final URI XSLT_PARAM_CLASS;

    /**
     * Predicate to associate certain symbolic name with an XSLT parameter
     */
    public static final URI XSLT_FILEINFO_PREDICATE;

    /**
     * Predicate to an XSLT parameter.
     */
    public static final URI XSLT_FILEINFO_PARAM_PREDICATE;

    /**
     * Predicate to symbolic name.
     */
    public static final URI XSLT_FILEINFO_SYMBOLICNAME_PREDICATE
            = MetadataVocabulary.UV_SYMBOLIC_NAME;

    /**
     * XSLT parameter's name
     */
    public static final URI XSLT_PARAM_NAME_PREDICATE;

    /**
     * XSLT parameter's value
     */
    public static final URI XSLT_PARAM_VALUE_PREDICATE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

        XSLT_CLASS = valueFactory.createURI("http://linked.opendata.cz/ontology/uv/dpu/xslt/Config");

        XSLT_FILEINFO_CLASS = valueFactory
                .createURI("http://linked.opendata.cz/ontology/uv/dpu/xslt/FileInfo");

        XSLT_PARAM_CLASS = valueFactory.createURI("http://linked.opendata.cz/ontology/uv/dpu/xslt/Param");

        XSLT_FILEINFO_PREDICATE = valueFactory.createURI(
                "http://linked.opendata.cz/ontology/uv/dpu/xslt/fileInfo");

        XSLT_FILEINFO_PARAM_PREDICATE = valueFactory.createURI(
                "http://linked.opendata.cz/ontology/uv/dpu/xslt/param");

        XSLT_PARAM_NAME_PREDICATE = valueFactory.createURI(
                "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/name");

        XSLT_PARAM_VALUE_PREDICATE = valueFactory.createURI(
                "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/value");

    }

}
