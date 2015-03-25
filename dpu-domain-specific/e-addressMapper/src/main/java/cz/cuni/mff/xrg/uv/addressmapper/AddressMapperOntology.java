package cz.cuni.mff.xrg.uv.addressmapper;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class AddressMapperOntology {

    private static final String BASE_URI = "http://linked.opendata.cz/resource/domain/address-linker/";

    // Reprezentuje mapper
    public static final URI MAPPER_POSTAL_CODE;

    // Reprezentuje mapper
    public static final URI MAPPER_ADDRESS_REGION;

    // Reprezentuje mapper
    public static final URI MAPPER_STREET_ADDRESS;

    // Reprezentuje metodu generujici alternativni podoby entit
    public static final URI ALT_SWAP_HOUSE_AND_LAND_NUMBER;

    // Do tohoto objektu se parsuji informace ye strukturovane adresy
    public static final URI ENTITY_RUIAN;

    // ENTITY_RUIAN na puvodni adresu, ze ktere byl vytvoren
    public static final URI HAS_POSTAL_ADDRESS;

    // ENTITY_RUIAN ma ..
    public static final URI CISLO_DOMOVNI;

    // ENTITY_RUIAN ma ..
    public static final URI CISLO_ORIENTACNI;

    // ENTITY_RUIAN ma ..
    public static final URI CISLO_ORIENTACNI_PISMENO;

    // ENTITY_RUIAN ma ..
    public static final URI PSC;

    // ENTITY_RUIAN ma ..
    public static final URI ULICE_NAME;

    // ENTITY_RUIAN ma ..
    public static final URI CAST_OBECT_NAME;

    // ENTITY_RUIAN ma ..
    public static final URI OBEC_NAME;

    // ENTITY_RUIAN ma ..
    public static final URI OKRES_NAME;

    // ENTITY_RUIAN ma ..
    public static final URI VUSC_NAME;

    // REPORT je hlaseni o nejake chybe, nebo zmene v entite
    public static final URI REPORT;

    // ENTITY_RUIAN ma ..
    public static final URI HAS_REPORT;

    // HAS_REPORT ma ..
    public static final URI MESSAGE;

    // HAS_REPORT ma .. SOURCE (to jsou hlavne mappery a tvurci alternative)
    public static final URI SOURCE;

    // potomek od REPORT
    public static final URI REPORT_SUBSTITUTE;

    // potomek od REPORT
    public static final URI REPORT_ALTERNATIVE;

    // REPORT_SUBSTITUTE ... jaka byla puvodne menena hodnota
    public static final URI FROM;

    // REPORT_SUBSTITUTE ... naco se hodnota zmenila
    public static final URI TO;

    // ENTITY_RUIAN ma mapovani na SCHEMA_POSTAL_ADDRESS
    public static final URI MAPPING;

    // ENTITY_RUIAN ma vysledek mapovani
    public static final URI HAS_RESULT;

    // Vysledek kdy bylo nalezeno jedno mapovani
    public static final URI RESULT_SINGLE_MAPPING;

    // Vysledek kdy nebylo nalezeno mapovani
    public static final URI RESULT_NO_MAPPING;

    // Vysledek kdy bylo nalezeno vice mapovani
    public static final URI RESULT_MULTIPLE_MAPPINGS;

    private static final String BASE_SCHEMA = "http://schema.org/";

    public static final URI SCHEMA_NAME;

    public static final URI SCHEMA_POSTAL_ADDRESS;

    private static final String BASE_RUAIN = "http://ruian.linked.opendata.cz/ontology/";

    public static final URI RUAIN_ADRESNI_MISTO;

    public static final URI RUAIN_CISLO_DOMOVNI;

    public static final URI RUAIN_CISLO_ORIENTACNI;

    public static final URI RUAIN_CISLO_ORIENTACNI_PISMENO;

    public static final URI RUAIN_PSC;

    public static final URI RUAIN_ULICE;

    public static final URI RUAIN_HAS_ULICE;

    public static final URI RUIAN_STAVEBNI_OBJEKT;

    public static final URI RUIAN_HAS_STAVEBNI_OBJEKT;

    public static final URI RUIAN_CAST_OBCE;

    public static final URI RUIAN_HAS_CAST_OBCE;

    public static final URI RUIAN_OBEC;

    public static final URI RUIAN_HAS_OBEC;

    public static final URI RUIAN_POU;
    
    public static final URI RUIAN_HAS_POU;
    
    public static final URI RUIAN_ORP;
    
    public static final URI RUIAN_HAS_ORP;
    
    public static final URI RUIAN_VUSC;
    
    public static final URI RUIAN_HAS_VUSC;

    public static final URI RUIAN_OKRES;

    public static final URI RUIAN_HAS_OKRES;

    static {
       final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
       
       MAPPER_POSTAL_CODE = valueFactory.createURI(BASE_URI + "mapper/PostalCodeMapper");
       MAPPER_ADDRESS_REGION = valueFactory.createURI(BASE_URI + "mapper/AddressRegionMapper");
       MAPPER_STREET_ADDRESS = valueFactory.createURI(BASE_URI + "mapper/StreetAddressMapper");

       ALT_SWAP_HOUSE_AND_LAND_NUMBER = valueFactory.createURI(BASE_URI + "alternative/swapHouseAndLandNumber");

       ENTITY_RUIAN = valueFactory.createURI(BASE_URI + "RuianEntity");
       HAS_POSTAL_ADDRESS = valueFactory.createURI(BASE_URI + "postalAddress");

       CISLO_DOMOVNI = valueFactory.createURI(BASE_URI + "cisloDomovni");
       CISLO_ORIENTACNI = valueFactory.createURI(BASE_URI + "cisloOrientaceni");
       CISLO_ORIENTACNI_PISMENO = valueFactory.createURI(BASE_URI + "cisloOrientaceniPismeno");
       PSC = valueFactory.createURI(BASE_URI + "psc");
       ULICE_NAME = valueFactory.createURI(BASE_URI + "ulice");
       CAST_OBECT_NAME = valueFactory.createURI(BASE_URI + "castObce");
       OBEC_NAME = valueFactory.createURI(BASE_URI + "obec");
       OKRES_NAME = valueFactory.createURI(BASE_URI + "okres");
       VUSC_NAME = valueFactory.createURI(BASE_URI + "vusc");

       REPORT = valueFactory.createURI(BASE_URI + "Report");
       HAS_REPORT = valueFactory.createURI(BASE_URI + "report");

       MESSAGE = valueFactory.createURI(BASE_URI + "report/message");
       SOURCE = valueFactory.createURI(BASE_URI + "report/source");

       REPORT_SUBSTITUTE = valueFactory.createURI(BASE_URI + "Substitution");
       REPORT_ALTERNATIVE = valueFactory.createURI(BASE_URI + "Alternative");

       FROM = valueFactory.createURI(BASE_URI + "substitution/from");
       TO = valueFactory.createURI(BASE_URI + "substitution/to");

       MAPPING = valueFactory.createURI(BASE_URI + "mapping");
       HAS_RESULT = valueFactory.createURI(BASE_URI + "resultType");

       RESULT_SINGLE_MAPPING = valueFactory.createURI(BASE_URI + "singleMapping");
       RESULT_NO_MAPPING = valueFactory.createURI(BASE_URI + "noMapping");
       RESULT_MULTIPLE_MAPPINGS = valueFactory.createURI(BASE_URI + "multipleMappings");

       // - - -

       SCHEMA_NAME = valueFactory.createURI(BASE_SCHEMA + "name");
       SCHEMA_POSTAL_ADDRESS = valueFactory.createURI(BASE_SCHEMA + "PostalAddress");

       // - - -

       RUAIN_ADRESNI_MISTO = valueFactory.createURI(BASE_RUAIN + "AdresniMisto");
       RUAIN_CISLO_DOMOVNI = valueFactory.createURI(BASE_RUAIN + "cisloDomovni");
       RUAIN_CISLO_ORIENTACNI = valueFactory.createURI(BASE_RUAIN + "cisloOrientacni");
       RUAIN_CISLO_ORIENTACNI_PISMENO = valueFactory.createURI(BASE_RUAIN + "cisloOrientacniPismeno");
       RUAIN_PSC = valueFactory.createURI(BASE_RUAIN + "psc");

       RUAIN_ULICE = valueFactory.createURI(BASE_RUAIN + "Ulice");
       RUAIN_HAS_ULICE = valueFactory.createURI(BASE_RUAIN + "ulice");

       RUIAN_STAVEBNI_OBJEKT = valueFactory.createURI(BASE_RUAIN + "StavebniObjekt");
       RUIAN_HAS_STAVEBNI_OBJEKT = valueFactory.createURI(BASE_RUAIN + "stavebniObjekt");

       RUIAN_CAST_OBCE = valueFactory.createURI(BASE_RUAIN + "CastObce");
       RUIAN_HAS_CAST_OBCE = valueFactory.createURI(BASE_RUAIN + "castObce");

       RUIAN_OBEC = valueFactory.createURI(BASE_RUAIN + "Obec");
       RUIAN_HAS_OBEC = valueFactory.createURI(BASE_RUAIN + "obec");

       RUIAN_POU = valueFactory.createURI(BASE_RUAIN + "Pou");
       RUIAN_HAS_POU = valueFactory.createURI(BASE_RUAIN + "pou");

       RUIAN_ORP = valueFactory.createURI(BASE_RUAIN + "Orp");
       RUIAN_HAS_ORP = valueFactory.createURI(BASE_RUAIN + "orp");

       RUIAN_VUSC = valueFactory.createURI(BASE_RUAIN + "Vusc");
       RUIAN_HAS_VUSC = valueFactory.createURI(BASE_RUAIN + "vusc");

       RUIAN_OKRES = valueFactory.createURI(BASE_RUAIN + "Okres");
       RUIAN_HAS_OKRES = valueFactory.createURI(BASE_RUAIN + "okres");
    }

}
