package cz.cuni.mff.xrg.uv.transformer.tabular.column;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 *
 * @author Škoda Petr
 */
public class ValueGeneratorTest {

    @Test
    public void stringWithLanguage() {
        ValueGenerator gen = new ValueGenerator(null, "\"{name}\"@en");

        Map<String, Integer> nameToIndex = new HashMap<>();
        nameToIndex.put("{name}", 1);

        List<Object> row = (List)Arrays.asList("first", "second");

        Literal value = (Literal)gen.generateValue(row, nameToIndex,
                new ValueFactoryImpl());

        Assert.assertEquals("second", value.stringValue());
        Assert.assertEquals("en", value.getLanguage());
    }

    @Test
    public void integerWithClass() {
        ValueGenerator gen = new ValueGenerator(null, "\"{name}\"^^<http://www.w3.org/2001/XMLSchema#int>");

        Map<String, Integer> nameToIndex = new HashMap<>();
        nameToIndex.put("{name}", 1);

        List<Object> row = (List)Arrays.asList("text", "5");

        Literal value = (Literal)gen.generateValue(row, nameToIndex,
                new ValueFactoryImpl());

        Assert.assertEquals("5", value.stringValue());
        Assert.assertEquals("<http://www.w3.org/2001/XMLSchema#int>",
                value.getDatatype().stringValue());
    }

}
