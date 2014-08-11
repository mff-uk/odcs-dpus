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
public class ConfigManagerTest {

    SerializationXmlGeneral serialization = SerializationXmlFactory
            .serializationXmlGeneral();

    /**
     * Test serialization of class that contains '<' or '>' chars in
     * configuration as strings.
     *
     * @throws SerializationXmlFailure
     * @throws ConfigException
     */
    @Test
    public void innerClassWtihLtGt() throws SerializationXmlFailure, ConfigException {
        
        ConfigManager cm = new ConfigManager(new MasterConfigObject(), serialization);
        
        Config_V3 v3 = new Config_V3();
        v3.setStr2("<a>12</a>");
        cm.set(v3, "V3");
        
        String mcoString = serialization.convert(cm.getMasterConfig());
       
        MasterConfigObject mco = serialization.convert(MasterConfigObject.class,
                mcoString);

        cm = new ConfigManager(mco, serialization);
        Config_V3 newV3 = cm.get("V3", Config_V3.class);

        Assert.assertEquals(v3.getStr2(), newV3.getStr2());        
    }

}
