package cz.cuni.mff.xrg.uv.test.boost.rdf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Close after use.
 * <pre>
 * {@Code
 * try (TripleInserter ins = TripleInserter.create(dataUnit)) {
 *  ins.subject("http://resource/root")
 *      .add("http://ontology/value", 12)
 *      .add("http://ontology/collection", 12)
 *      .addUri("http://ontology/collection", "http://resource/object");
 *  ins.subject("http://resource/object").add("http://ontology/text", "hi");
 * }
 * }
 * </pre>
 * @author Škoda Petr
 */
public class TripleInserter implements AutoCloseable {

    /**
     * Connection used to insert data.
     */
    private final RepositoryConnection connection;

    private final ValueFactory valueFactory;

    private String subject;

    /**
     * URI of target graph.
     */
    private final URI writegraph;

    public TripleInserter(RepositoryConnection connection, URI writegraph) {
        this.connection = connection;
        this.valueFactory = connection.getValueFactory();
        this.writegraph = writegraph;
    }

    public static TripleInserter create(WritableRDFDataUnit dataUnit) throws DataUnitException {
        return new TripleInserter(dataUnit.getConnection(), dataUnit.addNewDataGraph("output"));
    }

    /**
     * Add data under given graph. Also create the graph in given {@link WritableRDFDataUnit}.
     *
     * @param dataUnit
     * @param graphUri
     * @return
     * @throws DataUnitException
     */
    public static TripleInserter create(WritableRDFDataUnit dataUnit, URI graphUri) throws DataUnitException {
        dataUnit.addExistingDataGraph(graphUri.stringValue(), graphUri);
        return new TripleInserter(dataUnit.getConnection(), graphUri);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public TripleInserter add(String s, String p, String o) throws RepositoryException {
        connection.add(this.valueFactory.createURI(s), this.valueFactory.createURI(p),
                this.valueFactory.createLiteral(o), this.writegraph);
        return this;
    }

    public TripleInserter addUri(String s, String p, String o) throws RepositoryException {
        connection.add(this.valueFactory.createURI(s), this.valueFactory.createURI(p),
                this.valueFactory.createURI(o), this.writegraph);
        return this;
    }

    public TripleInserter add(String s, String p, int o) throws RepositoryException {
        connection.add(this.valueFactory.createURI(s), this.valueFactory.createURI(p),
                this.valueFactory.createLiteral(o), this.writegraph);
        return this;
    }

    public TripleInserter subject(String newSubject) {
        this.subject = newSubject;
        return this;
    }

    public TripleInserter  add(String p, String o) throws RepositoryException {
        return add(subject, p, o);
    }

    public TripleInserter  addUri(String p, String o) throws RepositoryException {
        return addUri(subject, p, o);
    }

    public TripleInserter  add(String p, int o) throws RepositoryException {
        return add(subject, p, o);
    }

}
