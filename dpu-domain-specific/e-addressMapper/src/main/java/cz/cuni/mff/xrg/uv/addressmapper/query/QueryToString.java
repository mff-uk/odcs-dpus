package cz.cuni.mff.xrg.uv.addressmapper.query;

import cz.cuni.mff.xrg.uv.addressmapper.ontology.Ruian;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Subject;

/**
 * Convert {@link Query} to {@link String}.
 *
 * @author Škoda Petr
 */
public class QueryToString {

    private final static String SELECT_PREAMBLE = "SELECT DISTINCT ";

    private final static String SELECT_WHERE = " WHERE {\n";

    private final static String SELECT_END = "} LIMIT ";

    private QueryToString() {
    }

    public static String convert(Query query, int resultLimit) {
        if (query.getContent().isEmpty()) {
            return null;
        }
        
        StringBuilder result = new StringBuilder(SELECT_PREAMBLE);
        StringBuilder notExistFilter = null;
        // put name of the result in the query
        result.append(query.getMainSubject().getValueName());
        result.append(SELECT_WHERE);

        for (Subject key : query.getContent().keySet()) {
            result.append(key.getValueName());
            // add type information
            result.append(" ");
            result.append("<" + Ruian.P_TYPE + ">");
            result.append(" <");
            result.append(key.geClassName());
            result.append(">");

            for (PredicatObject predObj : query.getContent().get(key)) {
                if (predObj.object == null) {
                    // NOT EXISTS filter
                    if (notExistFilter == null) {
                        notExistFilter = new StringBuilder();
                    }
                    notExistFilter.append(" ");
                    notExistFilter.append(key.getValueName());
                    notExistFilter.append(" ");
                    notExistFilter.append(predObj.predicate);
                    notExistFilter.append(" [].");
                } else {
                    result.append(";\n");
                    result.append("    ");
                    result.append(predObj.predicate);
                    result.append(" ");
                    result.append(predObj.object);
                }
            }
            result.append(".\n");
        }
        // add filters if included 
        if (notExistFilter != null) {
            result.append("FILTER NOT EXISTS {");
            result.append(notExistFilter);
            result.append(" }\n");
        }        
        // return string
        result.append(SELECT_END);
        result.append(resultLimit);
        return result.toString();
    }

}
