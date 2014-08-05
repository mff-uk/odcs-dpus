package cz.cuni.mff.xrg.uv.service.serialization.xml;

/**
 *
 * @author Škoda Petr
 */
class SerializationXmlImpl<T> implements SerializationXml<T> {
 
    protected final Class<T> clazz;

    protected SerializationXmlGeneral serialization;

    SerializationXmlImpl(Class<T> clazz) {
        this.clazz = clazz;
        this.serialization = new SerializationXmlGeneralImpl();
    }

    SerializationXmlImpl(Class<T> clazz, String alias) {
        this.clazz = clazz;
        this.serialization = new SerializationXmlGeneralImpl();
        this.serialization.addAlias(clazz, alias);
    }

    @Override
    public T createInstance() throws SerializationXmlFailure {
        return serialization.createInstance(clazz);
    }

    @Override
    public T convert(String string) throws SerializationXmlFailure {
        return serialization.convert(clazz, string);
    }

    @Override
    public String convert(T object) throws SerializationXmlFailure {
       return serialization.convert(object);
    }

}
