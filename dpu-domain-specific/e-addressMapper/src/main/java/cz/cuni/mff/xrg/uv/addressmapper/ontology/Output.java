package cz.cuni.mff.xrg.uv.addressmapper.ontology;

/**
 *
 * @author Škoda Petr
 */
public class Output {
    
    private static final String BASE_URI = "http://linked.opendata.cz/resource/domain/address-linker/";
    
    private static final String BASE_URI_W3 = "http://www.w3.org/1999/02/";
    
    public static final String P_TYPE = BASE_URI_W3 + "22-rdf-syntax-ns#type";
    
    public static final String O_CLASS = BASE_URI + "Link";
    
    public static final String P_SOURCE = BASE_URI + "source";
    
    public static final String P_TARGET = BASE_URI + "target";
    
    public static final String P_PROPERTY = BASE_URI + "property";
    
    public static final String O_ALTERNATIVE = BASE_URI + "Alternative";
    
    public static final String O_REDUCTION = BASE_URI + "Reduction";
    
    public static final String P_MAPPING_TYPE = BASE_URI + "type";
    
    private Output() { }
    
}
