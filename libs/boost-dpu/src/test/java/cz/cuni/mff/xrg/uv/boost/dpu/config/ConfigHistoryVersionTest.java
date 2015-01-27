package cz.cuni.mff.xrg.uv.boost.dpu.config;

import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

import cz.cuni.mff.xrg.uv.boost.dpu.config.serializer.ConfigSerializer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.serializer.XStreamSerializer;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXml;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationXmlFailure;

/**
 * 
 * @author Škoda Petr
 */
public class ConfigHistoryVersionTest {

    SerializationXml serialization = SerializationXmlFactory.serializationXml();

    ConfigHistory<Config_V3> historyHolder = ConfigHistory
            .history(Config_V1.class)
            .add(Config_V2.class)
            .addCurrent(Config_V3.class);

    @Test
    public void fromFirstToThird() throws SerializationXmlFailure, ConfigException, SerializationFailure {
        Config_V1 v1 = new Config_V1();
        v1.setValue(3);

        final String v1Str = serialization.convert(v1);
        final List<ConfigSerializer> serializers = new LinkedList<>();
        serializers.add(new XStreamSerializer(serialization));
        Config_V3 v3 = historyHolder.parse(v1Str, serializers);

        Assert.assertEquals("3", v3.getStr1());
        Assert.assertEquals("<a>3</a>", v3.getStr2());
    }

    @Test
    public void lastToLast() throws SerializationXmlFailure, ConfigException, SerializationFailure {
        Config_V3 v3 = new Config_V3();
        v3.setStr1("3");
        v3.setStr2("abc");

        final String v3Str = serialization.convert(v3);

        final List<ConfigSerializer> serializers = new LinkedList<>();
        serializers.add(new XStreamSerializer(serialization));
        Config_V3 v3New = historyHolder.parse(v3Str, serializers);

        Assert.assertEquals("3", v3New.getStr1());
        Assert.assertEquals("abc", v3New.getStr2());
    }

}
