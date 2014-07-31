package cz.cuni.mff.xrg.uv.rdf.simple.test;

import cz.cuni.mff.xrg.uv.rdf.simple.AddPolicy;
import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import java.util.Collections;
import java.util.List;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQueryResult;

/**
 * Do nothing class for simple usage in tests as base class for lambda classes,
 * where user can redefine desired actions.
 *
 * <pre>
 * {@code
 * // print all added triples into standart output
 * SimpleRdfWrite outRdf = new SimpleRdfDummyBase() {
 *  @Override
 *  public void add(Resource s, URI p, Value o) throws OperationFailedException {
 *      System.out.println("> " +  s.stringValue() + " " +
 *          p.stringValue() + " " + o.stringValue());
 *  }
 * };
 * }
 * </pre>
 * @author Škoda Petr
 */
public class SimpleRdfDummyBase implements SimpleRdfWrite {

    @Override
    public void add(Resource s, URI p, Value o) throws OperationFailedException {
        
    }

    @Override
    public void setPolicy(AddPolicy policy) {
        
    }

    @Override
    public void flushBuffer() throws OperationFailedException {
        
    }

    @Override
    public ValueFactory getValueFactory() throws OperationFailedException {
        return new ValueFactoryImpl();
    }

    @Override
    public List<Statement> getStatements() throws OperationFailedException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public ConnectionPair<TupleQueryResult> executeSelectQuery(String query)
            throws OperationFailedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ConnectionPair<Graph> executeConstructQuery(String query) throws OperationFailedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOutputGraph(String graphUriAsString) throws OperationFailedException {
        
    }

}
