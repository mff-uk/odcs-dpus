package cz.cuni.mff.xrg.uv.service.serialization.xml;

/**
 *
 * @author Škoda Petr
 */
public interface SerializationXmlGeneral {

    public <T> T createInstance(Class<T> clazz) throws SerializationXmlFailure;

    public <T> T convert(Class<T> clazz, String string) throws SerializationXmlFailure;

    public Object convert(ClassLoader classLoader, String string) throws SerializationXmlFailure;
    
    public <T> String convert(T object) throws SerializationXmlFailure;

    /**
     * Add alias to class.
     *
     * @param clazz
     * @param alias
     */
    public void addAlias(Class<?> clazz, String alias);

}
