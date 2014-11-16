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

    public static String RDF_ROW_LABEL = 
            "http://www.w3.org/2000/01/rdf-schema#label";

    public static String TABLE_HAS_ROW =
            "http://linked.opendata.cz/ontology/odcs/tabular/hasRow";

    public static String TABLE_SYMBOLIC_NAME =
            "http://linked.opendata.cz/ontology/odcs/tabular/symbolicName";

    public static String RDF_A_PREDICATE =
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

    public static String TABLE_CLASS = 
            "http://unifiedviews.eu/ontology/t-tabular/Table";

    public static String ROW_CLASS =
            "http://unifiedviews.eu/ontology/t-tabular/Row";

    public static URI URI_BLANK_CELL;

    public static URI URI_ROW_NUMBER;

    public static URI URI_RDF_ROW_LABEL;

    public static URI URI_TABLE_HAS_ROW;

    public static URI URI_TABLE_SYMBOLIC_NAME;

    public static URI URI_RDF_A_PREDICATE;

    public static URI URI_TABLE_CLASS;
    
    public static void init(ValueFactory valueFactory) {
        URI_BLANK_CELL = valueFactory.createURI(BLANK_CElL);
        URI_ROW_NUMBER = valueFactory.createURI(ROW_NUMBER);
        URI_RDF_ROW_LABEL = valueFactory.createURI(RDF_ROW_LABEL);
        URI_TABLE_HAS_ROW = valueFactory.createURI(TABLE_HAS_ROW);
        URI_TABLE_SYMBOLIC_NAME = valueFactory.createURI(TABLE_SYMBOLIC_NAME);
        URI_RDF_A_PREDICATE = valueFactory.createURI(RDF_A_PREDICATE);
        URI_TABLE_CLASS = valueFactory.createURI(TABLE_CLASS);
    }

}
