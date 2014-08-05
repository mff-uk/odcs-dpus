package cz.cuni.mff.xrg.uv.transformer.tabular.column;

import cz.cuni.mff.xrg.uv.transformer.tabular.TabularOntology;
import java.util.List;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Informations about a single column. Used during execution.
 *
 * TODO: Improve this
 *
 * @author Škoda Petr
 */
public class ValueGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(
            ValueGenerator.class);

    /**
     * Used URI for column.
     */
    private final URI Uri;

    /**
     * Filed reference according to
     * http://w3c.github.io/csvw/csv2rdf/#dfn-field-reference.
     */
    private final String template;

    public ValueGenerator(URI Uri, String template) {
        this.Uri = Uri;
        this.template = template;
    }

    public URI getUri() {
        return Uri;
    }

    public String getTemplate() {
        return template;
    }

    /**
     * Generate value based on stored information.
     *
     * @param row
     * @param nameToIndex
     * @param valueFactory
     * @return
     */
    public Value generateValue(List<Object> row,
            Map<String, Integer> nameToIndex, ValueFactory valueFactory) {
        //
        // we support only a simple replace
        //
        String name = template.substring(template.indexOf("{"),
                template.indexOf("}") + 1);

        // TODO add support for URI and other stuff

        final Integer column = nameToIndex.get(name);

        //
        // get object
        //
        final Object value = row.get(column);
        if (value == null) {
            return null;
        }
        //
        // prepare triple
        //
        if (template.contains("\"@")) {
            return valueFactory.createLiteral(value.toString(),
                    template.substring(template.lastIndexOf("\"@") + 2));

        } else if (template.contains("\"^^")) {

            return valueFactory.createLiteral(value.toString(),
                    valueFactory.createURI(
                            template.substring(template.lastIndexOf("\"^^") + 3)));

        } else {
            return valueFactory.createLiteral(value.toString());
        }
    }

}
