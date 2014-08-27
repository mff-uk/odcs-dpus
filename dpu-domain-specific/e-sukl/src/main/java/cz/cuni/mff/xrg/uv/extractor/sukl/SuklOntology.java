package cz.cuni.mff.xrg.uv.extractor.sukl;

import org.openrdf.model.URI;

/**
 *
 * @author Škoda Petr
 */
public class SuklOntology {

    public static final String RESOURCE_URI = "http://linked.opendata.cz/resource/sukl/";

    public static final String ONTOLOGY_URI = "http://linked.opendata.cz/ontology/sukl/";

    public static final String SUBJECT_PREFIX =
            RESOURCE_URI + "medicinal-product-packaging/";

    public static final String P_HAS_INGREDIEND =
            ONTOLOGY_URI + "hasActiveIngredient";

    public static URI P_HAS_INGREDIEND_URI;

    public static final String INGREDIEND_PREFIX =
            RESOURCE_URI + "active-ingredient/";

    public static final String O_INGREDIEND_CLASS =
            ONTOLOGY_URI + "ActiveIngredient";

    public static URI O_INGREDIEND_CLASS_URI;

    public static final String P_INGREDIEND_NAME_SKOS =
            "http://www.w3.org/2004/02/skos/core#prefLabel";

    public static URI P_INGREDIEND_NAME_SKOS_URI;

    public static final String P_INGREDIEND_NAME_DCTERMS =
            "http://purl.org/dc/terms/title";

    public static URI P_INGREDIEND_NAME_DCTERMS_URI;

    public static final String P_SPC
            = ONTOLOGY_URI + "spcUri";

    public static URI P_SPC_URI;

    public static final String P_PIL
            = ONTOLOGY_URI + "pilUri";

    public static URI P_PIL_URI;

    public static final String P_TEXT_ON_THE_WRAP
            = ONTOLOGY_URI + "textOnTheWrapUri";

    public static URI P_TEXT_ON_THE_WRAP_URI;

    public static final String O_INGREDIEND_NOT_SET
            = RESOURCE_URI + "Unknown";
    
    public static URI O_INGREDIEND_NOT_SET_URI;

}
