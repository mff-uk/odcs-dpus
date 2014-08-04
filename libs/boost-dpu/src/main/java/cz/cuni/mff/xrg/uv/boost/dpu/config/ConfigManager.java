package cz.cuni.mff.xrg.uv.boost.dpu.config;

import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXml;
import cz.cuni.mff.xrg.uv.service.serialization.xml.SerializationXmlFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to multiple configurations (strings) under name (string).
 * 
 * @author Škoda Petr
 */
public class ConfigManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(
            ConfigManager.class);
    
    private final MasterConfigObject masterConfig;

    private final SerializationXml<Object> serializer;
    
    public ConfigManager() {
        this.masterConfig = null;
        this.serializer = null;
    }
    
    /**
     * 
     * @param masterConfig Can be null.
     * @param serializer
     */
    public ConfigManager(MasterConfigObject masterConfig, SerializationXml serializer) {
        this.masterConfig = masterConfig;
        this.serializer = serializer;
    }
    
    public <TYPE> TYPE get(String name, Class<TYPE> clazz) {
        if (masterConfig == null) {
            return null;
        }
        final String strValue = masterConfig.getConfigurations().get(name);
        if (strValue == null) {
            return null;
        }
        // set class loader
        serializer.setClassLoader(clazz.getClassLoader());
        
        try {
            final Object obj = serializer.convert(strValue);
            return (TYPE)obj;
        } catch (SerializationXmlFailure ex) {
            LOG.error("Failed to convert configuration class.", ex);
        }
        return null;
        //return masterConfig.getConfigurations().get(name);
    }

    public <TYPE> void set(TYPE object, String name) {
        if (masterConfig == null) {
            return;
        }
        
        serializer.setClassLoader(object.getClass().getClassLoader());
        
        try {
            final String objectAsStr = serializer.convert(object);
            // put into configuration
            masterConfig.getConfigurations().put(name, objectAsStr);            
        } catch (SerializationXmlFailure ex) {
            throw new RuntimeException("Serialization failed.", ex);
        }
    }
    
}
