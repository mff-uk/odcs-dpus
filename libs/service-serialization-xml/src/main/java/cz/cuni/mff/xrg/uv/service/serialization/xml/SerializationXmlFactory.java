package cz.cuni.mff.xrg.uv.service.serialization.xml;

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
        return new SerializationXmlImpl(clazz, rootName);
    }

    /**
     * 
     * @return
     */
    public static SerializationXmlGeneral serializationXmlGeneral() {
        return new SerializationXmlGeneralImpl();
    }

}
