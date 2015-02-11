package cz.cuni.mff.xrg.uv.boost.serialization.xml;

/**
 *
 * @author Škoda Petr
 */
public class SerializationXmlFactory {
    
    private SerializationXmlFactory() {
    }

    /**
     * 
     * @return
     */
    public static SerializationXml serializationXml() {
        return new SerializationXmlImpl();
    }

}
