package cz.cuni.mff.xrg.uv.transformer.tabular.column;

import cz.cuni.mff.xrg.uv.transformer.tabular.parser.ParseFailed;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class ValueGeneratorTest {

    @Test
    public void stringWithLanguage() throws ParseFailed {
        ValueGenerator gen = ValueGeneratorReplace.create(null,
                "\"{name}\"@en");

        Map<String, Integer> nameToIndex = new HashMap<>();
        nameToIndex.put("name", 1);

        List<Object> row = (List)Arrays.asList("first", "second");

        gen.compile(nameToIndex, new ValueFactoryImpl());
        Literal value = (Literal)gen.generateValue(row, new ValueFactoryImpl());

        Assert.assertEquals("second", value.stringValue());
        Assert.assertEquals("en", value.getLanguage());
    }

    @Test
    public void integerWithClass() throws ParseFailed {
        ValueGenerator gen = ValueGeneratorReplace.create(null,
                "\"{name}\"^^<http://www.w3.org/2001/XMLSchema#int>");

        Map<String, Integer> nameToIndex = new HashMap<>();
        nameToIndex.put("name", 1);

        List<Object> row = (List)Arrays.asList("text", "5");

        gen.compile(nameToIndex, new ValueFactoryImpl());
        Literal value = (Literal)gen.generateValue(row, new ValueFactoryImpl());

        Assert.assertEquals("5", value.stringValue());
        Assert.assertEquals("<http://www.w3.org/2001/XMLSchema#int>",
                value.getDatatype().stringValue());
    }

    @Test
    public void constructUri() throws ParseFailed {
        ValueGenerator gen = ValueGeneratorReplace.create(null,
                "<{base}{+suff}>");

        Map<String, Integer> nameToIndex = new HashMap<>();
        nameToIndex.put("base", 0);
        nameToIndex.put("suff", 1);

        List<Object> row = (List)Arrays.asList("http://", "1");

        gen.compile(nameToIndex, new ValueFactoryImpl());
        URI value = (URI)gen.generateValue(row, new ValueFactoryImpl());

        Assert.assertEquals("http://1", value.stringValue());
    }

}
