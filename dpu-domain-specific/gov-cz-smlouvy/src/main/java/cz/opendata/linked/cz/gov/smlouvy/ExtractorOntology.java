package cz.opendata.linked.cz.gov.smlouvy;

import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;
import eu.unifiedviews.dataunit.MetadataDataUnit;

/**
 *
 * @author Škoda Petr
 */
public interface ExtractorOntology {

    /**
     * Class form main configuration object.
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_CLASS")
    public static final String XSLT_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/Config";

    /**
     * Class to associate certain symbolic name with set of XSLT parameter.
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_FILEINFO_CLASS")
    public static final String XSLT_FILEINFO_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/FileInfo";

    /**
     * Class contains a single XSLT parameter.
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_PARAM_CLASS")
    public static final String XSLT_PARAM_CLASS
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/Param";

    /**
     * Predicate to associate certain symbolic name with an XSLT parameter
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_FILEINFO_PREDICATE")
    public static final String XSLT_FILEINFO_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/fileInfo";

    /**
     * Predicate to an XSLT parameter.
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_FILEINFO_PARAM_PREDICATE")
    public static final String XSLT_FILEINFO_PARAM_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param";

    /**
     * Predicate to symbolic name.
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_FILEINFO_SYMBOLICNAME_PREDICATE")
    public static final String XSLT_FILEINFO_SYMBOLICNAME_PREDICATE
            = MetadataDataUnit.PREDICATE_SYMBOLIC_NAME;

    /**
     * XSLT parameter's name
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_PARAM_NAME_PREDICATE")
    public static final String XSLT_PARAM_NAME_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/name";

    /**
     * XSLT parameter's value
     */
    @OntologyDefinition.UpdateFrom(path
            = "cz.cuni.mff.xrg.uv.transformer.xslt.XsltTOntology.XSLT_PARAM_VALUE_PREDICATE")
    public static final String XSLT_PARAM_VALUE_PREDICATE
            = "http://linked.opendata.cz/ontology/uv/dpu/xslt/param/value";

}
