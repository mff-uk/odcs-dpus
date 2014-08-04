package cz.cuni.mff.xrg.uv.service.serialization.xml;

import com.thoughtworks.xstream.converters.Converter;
import cz.cuni.mff.xrg.uv.service.serialization.xml.subpackage.ConfigObjectCopy;
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

    //@Test
    public void crossObjects() throws SerializationXmlFailure {
        final ConfigObject original = new ConfigObject();
        original.setIntegralValue(10);

        // serialize one object
        final SerializationXml<ConfigObject> service_source = SerializationXmlFactory
                .serializationXml(ConfigObject.class, "cnf");
        final String str = service_source.convert(original);

        // deserialize as other, but use same alias
        final SerializationXml<ConfigObjectCopy> service_target = SerializationXmlFactory
                .serializationXml(ConfigObjectCopy.class, "cnf");
        final ConfigObjectCopy copy = service_target.convert(str);

        Assert.assertNotEquals(original, copy);
        Assert.assertEquals(original.getIntegralValue(), copy
                .getIntegralValue());
        Assert.assertEquals("value", ConfigObject.PUBLIC_FINAL);
    }

    //@Test
    public void versionTest() throws SerializationXmlFailure {
        final ConfigObject original = new ConfigObject();
        original.setIntegralValue(10);

        // serialize one object
        final SerializationXml<ConfigObject> service_source = SerializationXmlFactory
                .serializationXml(ConfigObject.class);

        SerializationXmlImpl<ConfigObject> impl = (SerializationXmlImpl<ConfigObject>)service_source;

        Converter innerConverter =
                impl.xstream.getConverterLookup().lookupConverterForType(ConfigObject.class);

//        impl.xstream.registerConverter(new WithVersionConvertor("1.0.0",
//                innerConverter, ConfigObject.class));

//        impl.xstream.registerConverter(new WithVersionConvertor("2.0.0",
//                innerConverter, ConfigObjectCopy.class));


        final String str = service_source.convert(original);
        System.out.println(str);

    }

}
