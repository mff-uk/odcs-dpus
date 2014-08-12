package cz.cuni.mff.xrg.uv.extractor.sukl;

import org.openrdf.model.URI;

/**
 *
 * @author Škoda Petr
 */
public class SuklOntology {

    public static final String BASE_URI = "http://linked.opendata.cz/resource/domain/sukl/";

    public static final String P_EFFECTIVE_SUBSTANCE
            = BASE_URI + "effectiveSubstance";

    public static URI P_EFFECTIVE_SUBSTANCE_URI;

    public static final String P_SPC
            = BASE_URI + "spcUri";

    public static URI P_SPC_URI;

    public static final String P_PIL
            = BASE_URI + "pilUri";

    public static URI P_PIL_URI;

    public static final String P_TEXT_ON_THE_WRAP
            = BASE_URI + "textOnTheWrapUri";

    public static URI P_TEXT_ON_THE_WRAP_URI;

    public static final String O_NOT_SET 
            = BASE_URI + "Unknown";
    
    public static URI O_NOT_SET_URI;

}
