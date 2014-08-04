package cz.cuni.mff.xrg.uv.service.serialization.rdf;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryException;

/**
 *
 * @author Škoda Petr
 */
public class SerializationRdfImplTest {

    @Test
    public void loadAndStore() throws RepositoryException, SerializationRdfFailure {
        TestObject source = new TestObject();
        source.setText_one("myValue");
        source.setText_two(null);
        source.setValue(42);
        
        TestObject target = new TestObject();
        target.setText_two("text");
        
        ValueFactory valueFactory = new ValueFactoryImpl();        
        SerializationRdfSimple<TestObject> serializer = 
                new SerializationRdfSimple<>(TestObject.class);
        // in and out
        List<Statement> statements = serializer.convert(source, 
                valueFactory.createURI("http://localhost/config"), valueFactory);        
        serializer.convert(statements, target);
        // test
        Assert.assertEquals(source.getText_one(), target.getText_one());
        Assert.assertEquals("text", target.getText_two());
        Assert.assertEquals(source.getValue(), target.getValue());        
    }
    
}
