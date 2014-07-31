package cz.cuni.mff.xrg.uv.serialization.xml;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class SerializationXmlTest {

    @Test
    public void serializeAndDeserialize() throws SerializationXmlFailure {
        final ConfigObject original = new ConfigObject();
        original.setIntegralValue(10);
        
        final SerializationXml<ConfigObject> service = SerializationXmlFactory
                .serializationXml(ConfigObject.class, "cnf");

        final String str = service.convert(original);
        final ConfigObject copy = service.convert(str);
        
        Assert.assertNotEquals(original, copy);
        Assert.assertEquals(original.getIntegralValue(), copy.getIntegralValue());
        Assert.assertEquals("value", ConfigObject.PUBLIC_FINAL);
    }

}
