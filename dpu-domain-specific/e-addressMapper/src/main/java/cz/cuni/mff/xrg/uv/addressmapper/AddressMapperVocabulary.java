package cz.cuni.mff.xrg.uv.addressmapper;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public final class AddressMapperVocabulary {

    private static final String ONTOLOGY = "http://linked.opendata.cz/ontology/domain/address-linker/";

    public static final URI HAS_MAPPING;
    
    public static final URI MAPPING;

    public static final URI MAPPING_EMPTY;

    public static final URI HAS_RUIAN;

    public static final URI HAS_CONFIDENCE;

    public static final URI HAS_COMPLETENESS;

    public static final URI HAS_NUMBER_OF_MAPPING;

    static {
       final ValueFactory valueFactory = ValueFactoryImpl.getInstance();

       HAS_MAPPING = valueFactory.createURI(ONTOLOGY + "mapping");
       MAPPING = valueFactory.createURI(ONTOLOGY + "Mapping");
       MAPPING_EMPTY = valueFactory.createURI(ONTOLOGY + "emptyMapping");
       HAS_RUIAN = valueFactory.createURI(ONTOLOGY + "ruainMapping");
       HAS_CONFIDENCE = valueFactory.createURI(ONTOLOGY + "confidence");
       HAS_COMPLETENESS = valueFactory.createURI(ONTOLOGY + "completeness");
       HAS_NUMBER_OF_MAPPING = valueFactory.createURI(ONTOLOGY + "numberOfMapping");;
    }

}
