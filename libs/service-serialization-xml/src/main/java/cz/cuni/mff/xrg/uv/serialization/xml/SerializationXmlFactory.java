package cz.cuni.mff.xrg.uv.serialization.xml;

/**
 *
 * @author Škoda Petr
 */
public class SerializationXmlFactory {
    
    private SerializationXmlFactory() { }
    
    public static <T> SerializationXml<T> serializationXml(Class<T> clazz) {
        return new SerializationXmlImpl(clazz);
    }
    
    /**
     * 
     * @param <T>
     * @param clazz
     * @param rootName Root name to use instead of class name.
     * @return 
     */
    public static <T> SerializationXml<T> serializationXml(
            Class<T> clazz, String rootName) {
        SerializationXmlImpl impl = new SerializationXmlImpl(clazz);
        // add alias for core class
        impl.xstream.alias(rootName, clazz);
        impl.xstreamUTF.alias(rootName, clazz);
        // and return
        return impl;
    }
    
    
}
