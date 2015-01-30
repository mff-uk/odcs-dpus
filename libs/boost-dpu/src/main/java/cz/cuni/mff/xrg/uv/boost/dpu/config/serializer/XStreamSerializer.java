package cz.cuni.mff.xrg.uv.boost.dpu.config.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFactory;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlGeneral;

/**
 * Configuration deserializer based on SerializationXmlGeneral - xStream.
 * 
 * @author Škoda Petr
 */
public class XStreamSerializer implements ConfigSerializer {

    private static final Logger LOG = LoggerFactory.getLogger(XStreamSerializer.class);

    private final SerializationXmlGeneral serializationXml;

    public XStreamSerializer() {
        serializationXml = SerializationXmlFactory.serializationXmlGeneral();
    }

    public XStreamSerializer(SerializationXmlGeneral serializationXml) {
        this.serializationXml = serializationXml;
    }

    @Override
    public <TYPE> boolean canDeserialize(String configAsString, Class<TYPE> clazz) {
        // Test if string contains configuration class name.
        return canDeserialize(configAsString, getClassName(clazz));
    }

    @Override
    public <TYPE> boolean canDeserialize(String configAsString, String className) {
        return configAsString.contains(className);        
    }

    @Override
    public <TYPE> TYPE deserialize(String configAsString, Class<TYPE> clazz) {
        try {
            return serializationXml.convert(clazz, configAsString);
        } catch (SerializationXmlFailure ex) {
            LOG.info("Can't deserialized configuration.", ex);
            return null;
        }
    }

    @Override
    public <TYPE> String serialize(TYPE configObject) {
        try {
            return serializationXml.convert(configObject);
        } catch (SerializationXmlFailure ex) {
            LOG.info("Can't deserialized configuration.", ex);
            return null;
        }
    }

    /**
     *
     * @param clazz
     * @return Class name in format of xStream.
     */
    private String getClassName(Class<?> clazz) {
        // Get class name and update it into way in which xStream use it
        // so we can search in string directly.
        String className = clazz.getCanonicalName().replace("_", "__");
        if (clazz.getEnclosingClass() != null) {
            // Change last "." into "_-" used for sub classes - addon configurations.
            int lastDot = className.lastIndexOf(".");
            className = className.substring(0, lastDot) + "_-" + className.substring(lastDot + 1);
        }
        return className;
    }

}
