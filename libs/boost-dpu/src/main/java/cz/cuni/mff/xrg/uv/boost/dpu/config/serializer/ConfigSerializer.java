package cz.cuni.mff.xrg.uv.boost.dpu.config.serializer;

/**
 *
 * @author Škoda Petr
 */
public interface ConfigSerializer {

    /**
     * 
     * @param <TYPE>
     * @param configAsString
     * @param clazz
     * @return True if given object may be deserialise by this serializer.
     */
    public <TYPE> boolean canDeserialize(String configAsString, Class<TYPE> clazz);

    /**
     * 
     * @param <TYPE>
     * @param configAsString
     * @param className
     * @return True if given object may be deserialise by this serializer.
     */
    public <TYPE> boolean canDeserialize(String configAsString, String className);

    /**
     * Convert string form of representation info a configuration object.
     *
     * @param <TYPE>
     * @param configAsString
     * @param clazz
     * @return Null if object can't be deserialise.
     */
    public <TYPE> TYPE deserialize(String configAsString, Class<TYPE> clazz);

    /**
     * Convert object into a string form.
     *
     * @param <TYPE>
     * @param configObject
     * @return Null if object can't be serialised.
     */
    public <TYPE> String serialize(TYPE configObject);

}
