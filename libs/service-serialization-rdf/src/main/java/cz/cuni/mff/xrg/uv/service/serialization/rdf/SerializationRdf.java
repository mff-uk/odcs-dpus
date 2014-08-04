package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import java.util.List;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Škoda Petr
 * @param <T>
 */
public interface SerializationRdf<T> {

    public void convert(List<Statement> rdf, T object) 
            throws SerializationRdfFailure;
    
    public List<Statement> convert(T object, URI rootUri, 
            ValueFactory valueFactory) throws SerializationRdfFailure;
    
}
