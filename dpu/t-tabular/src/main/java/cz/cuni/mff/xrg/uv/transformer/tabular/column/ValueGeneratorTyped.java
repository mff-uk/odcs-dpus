package cz.cuni.mff.xrg.uv.transformer.tabular.column;

import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import java.util.List;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

/**
 * Create value with given type.
 *
 * @author Škoda Petr
 */
public class ValueGeneratorTyped extends ValueGeneratorReplace {

    private final String typeStr;

    private URI typeUri;

    public ValueGeneratorTyped(URI uri, String template, String typeStr) {
        super(uri, template);
        this.typeStr = typeStr;
    }

    @Override
    public Value generateValue(List<Object> row, ValueFactory valueFactory) {
        final String rawResult = process(row);
        if (rawResult == null) {
            return null;
        }

        return valueFactory.createLiteral(rawResult,typeUri);
    }

    @Override
    public void compile(Map<String, Integer> nameToIndex,
            ValueFactory valueFactory) throws ParseFailed {
        super.compile(nameToIndex, valueFactory);
        typeUri = valueFactory.createURI(typeStr);
    }

}
