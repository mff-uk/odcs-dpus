package cz.cuni.mff.xrg.uv.boost.serialization;

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
