package cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology;

/**
 *
 * @author Škoda Petr
 */
public class Ruian {

    private static final String BASE_URI_RUIAN = "http://ruian.linked.opendata.cz/ontology/";

    private static final String BASE_URI_W3 = "http://www.w3.org/1999/02/";

    private static final String BASE_URI_SCHEMA = "http://schema.org/";
    
    public static final String P_TYPE = BASE_URI_W3 + "22-rdf-syntax-ns#type";

    public static final String P_NAME = BASE_URI_SCHEMA + "name";
    
    public static final String P_PSC = BASE_URI_RUIAN + "psc";
    
    public static final String P_CISLO_DOMOVNI = BASE_URI_RUIAN + "cisloDomovni";
    
    public static final String P_CISLO_ORIENTACNI = BASE_URI_RUIAN + "cisloOrientacni";
    
    public static final String P_CISLO_ORIENTACNI_PISMENO = BASE_URI_RUIAN + "cisloOrientacniPismeno";

    public static final String O_STAVEBNI_OBJEKT = BASE_URI_RUIAN + "StavebniObjekt";
    
    public static final String P_STAVEBNI_OBJEKT = BASE_URI_RUIAN + "stavebniObjekt";
    
    public static final String O_ADRESNI_MISTO = BASE_URI_RUIAN + "AdresniMisto";
    
    public static final String P_ADRESNI_MISTO = BASE_URI_RUIAN + "adresniMisto";
    
    public static final String P_LINK_ADRESNI_MISTO = BASE_URI_RUIAN + "links/adresni-misto";
    
    public static final String O_ULICE = BASE_URI_RUIAN + "Ulice";
    
    public static final String P_ULICE = BASE_URI_RUIAN + "ulice";
    
    public static final String P_LINK_ULICE = BASE_URI_RUIAN + "links/ulice";
    
    public static final String O_OBEC = BASE_URI_RUIAN + "Obec";
    
    public static final String P_OBEC = BASE_URI_RUIAN + "obec";
    
    public static final String P_LINK_OBEC = BASE_URI_RUIAN + "links/obec";
    
    public static final String O_CAST_OBCE = BASE_URI_RUIAN + "CastObce";
    
    public static final String P_CAST_OBCE = BASE_URI_RUIAN + "castObce";
    
    public static final String P_LINK_CAST_OBCE = BASE_URI_RUIAN + "links/cast-obce";    
    
    public static final String O_POU = BASE_URI_RUIAN + "Pou";
    
    public static final String P_POU = BASE_URI_RUIAN + "pou";
    
    public static final String P_LINK_POU = BASE_URI_RUIAN + "links/pou";
    
    public static final String O_ORP = BASE_URI_RUIAN + "Orp";
    
    public static final String P_ORP = BASE_URI_RUIAN + "orp";
    
    public static final String P_LINK_ORP = BASE_URI_RUIAN + "links/orp";
    
    public static final String O_VUSC = BASE_URI_RUIAN + "Vusc";
    
    public static final String P_VUSC = BASE_URI_RUIAN + "vusc";
    
    public static final String P_LINK_VUSC = BASE_URI_RUIAN + "links/vusc";
    
    private Ruian() { }

}
