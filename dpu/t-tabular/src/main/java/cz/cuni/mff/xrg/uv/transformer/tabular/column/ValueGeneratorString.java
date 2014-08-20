package cz.cuni.mff.xrg.uv.transformer.tabular.column;

import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Generate values as string with language tag or without it if not specified.
 *
 * @author Škoda Petr
 */
public class ValueGeneratorString extends ValueGeneratorReplace {

    private final String language;

    public ValueGeneratorString(URI uri, String template, String language) {
        super(uri, template);
        this.language = language;
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = process(row);
        if (rawResult == null) {
            return null;
        }

        if (language != null) {
            return valueFactory.createLiteral(rawResult, language);
        } else {
            return valueFactory.createLiteral(rawResult);
        }
    }

}
