package cz.cuni.mff.xrg.uv.transformer.tabular;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Škoda Petr
 */
public class TabularOntology {

    private TabularOntology() {

    }

    public static String BLANK_CElL =
            "http://linked.opendata.cz/ontology/odcs/tabular/blank-cell";

    public static String ROW_NUMBER = 
            "http://linked.opendata.cz/ontology/odcs/tabular/row";

    public static URI URI_BLANK_CELL;

    public static URI URI_ROW_NUMBER;
    
    public static void init(ValueFactory valueFactory) {
        URI_BLANK_CELL = valueFactory.createURI(BLANK_CElL);
        URI_ROW_NUMBER = valueFactory.createURI(ROW_NUMBER);
    }

}
