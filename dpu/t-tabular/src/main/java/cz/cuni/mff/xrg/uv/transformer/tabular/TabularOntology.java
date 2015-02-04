package cz.cuni.mff.xrg.uv.transformer.tabular;

import cz.cuni.mff.xrg.uv.boost.ontology.OntologyDefinition;

/**
 *
 * @author Škoda Petr
 */
public class TabularOntology extends OntologyDefinition {

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
    
}
