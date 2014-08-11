package cz.cuni.mff.xrg.uv.boost.dpu.config;

import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Škoda Petr
 */
public class VersionedConfigTest {

    SerializationXmlGeneral serialization = SerializationXmlFactory
            .serializationXmlGeneral();

    ConfigHistory<Config_V3> historyHolder = ConfigHistory
            .create(Config_V1.class)
            .add(Config_V2.class)
            .addCurrent(Config_V3.class);

    @Test
    public void fromFirstToThird() throws SerializationXmlFailure, ConfigException {
        Config_V1 v1 = new Config_V1();
        v1.setValue(3);

        final String v1Str = serialization.convert(v1);
        Config_V3 v3 = historyHolder.parse(v1Str, serialization);

        Assert.assertEquals("3", v3.getStr1());
        Assert.assertEquals("<a>3</a>", v3.getStr2());
    }

    @Test
    public void lastToLast() throws SerializationXmlFailure, ConfigException {
        Config_V3 v3 = new Config_V3();
        v3.setStr1("3");
        v3.setStr2("abc");

        final String v3Str = serialization.convert(v3);
        Config_V3 v3New = historyHolder.parse(v3Str, serialization);

        Assert.assertEquals("3", v3New.getStr1());
        Assert.assertEquals("abc", v3New.getStr2());
    }

}
