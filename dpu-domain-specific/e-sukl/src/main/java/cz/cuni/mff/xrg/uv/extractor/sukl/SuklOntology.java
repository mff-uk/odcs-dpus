package cz.cuni.mff.xrg.uv.extractor.sukl;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.SKOS;

/**
 *
 * @author Škoda Petr
 */
public class SuklOntology {

    public static final String RESOURCE_URI = "http://linked.opendata.cz/resource/sukl/";

    public static final String ONTOLOGY_URI = "http://linked.opendata.cz/ontology/sukl/";

    public static final String SUBJECT_PREFIX = RESOURCE_URI + "medicinal-product-packaging/";

    public static URI HAS_INGREDIEND;

    public static final String INGREDIEND_PREFIX = RESOURCE_URI + "active-ingredient/";

    public static URI INGREDIEND_CLASS;

    public static URI INGREDIEND_NAME_SKOS;

    public static URI INGREDIEND_NAME_DCTERMS;

    public static URI SPC;

    public static URI SPC_FILE;

    public static URI PIL;

    public static URI PIL_FILE;

    public static URI TEXT_ON_THE_WRAP;

    public static URI TEXT_ON_THE_WRAP_FILE;

    static {
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        
        HAS_INGREDIEND = valueFactory.createURI(ONTOLOGY_URI + "hasActiveIngredient");
        INGREDIEND_CLASS = valueFactory.createURI(ONTOLOGY_URI + "ActiveIngredient");
        INGREDIEND_NAME_SKOS = SKOS.PREF_LABEL;
        INGREDIEND_NAME_DCTERMS = DCTERMS.TITLE;
        SPC = valueFactory.createURI(ONTOLOGY_URI + "spcUri");
        SPC_FILE = valueFactory.createURI(ONTOLOGY_URI + "spcFile");
        PIL = valueFactory.createURI(ONTOLOGY_URI + "pilUri");
        PIL_FILE = valueFactory.createURI(ONTOLOGY_URI + "pilFile");
        TEXT_ON_THE_WRAP = valueFactory.createURI(ONTOLOGY_URI + "textOnTheWrapUri");
        TEXT_ON_THE_WRAP_FILE = valueFactory.createURI(ONTOLOGY_URI + "textOnTheWrapFile");
    }

}
