package cz.cuni.mff.xrg.uv.boost.serialization;

import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;

/**
 * Interface for XML serialisation interface.
 * Notes:
 * <ul>
 * <li>Support substitution of new items only to first level.</li>
 * </ul>
 * 
 * @author Škoda Petr
 * @deprecated Use {@link SerializationXmlGeneral} instead.
 */
public interface SerializationXml {

    /**
     * Add alias to class.
     *
     * @param clazz
     * @param alias
     */
    public void addAlias(Class<?> clazz, String alias);

    public <T> T convert(Class<T> clazz, String string) throws SerializationFailure, SerializationXmlFailure;

    public <T> String convert(T object) throws SerializationFailure, SerializationXmlFailure;
    
}
