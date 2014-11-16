package cz.cuni.mff.xrg.uv.test.boost.rdf;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import java.util.*;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple implementation of InMemoryRdfData unit that can be used in tests.
 *
 * Known limitations:
 * <ul>
 * <li>Does not support metadata.</li>
 * </ul>
 *
 * Don't forget to call {@link #shutDown()} at the end to release resources.
 *
 * @author Škoda Petr
 */
public class InMemoryRDFDataUnit implements WritableRDFDataUnit {

    private static final Logger LOG = LoggerFactory.getLogger(InMemoryRDFDataUnit.class);

    private final Repository repository;

    /**
     * URI of write graph.
     */
    private final URI writeDataGraph;

    /**
     * Used instead of metadata, store list of data graphs.
     * (symbolic name;graph URI)
     */
    private final Map<String, URI> dataGraphs = new HashMap<>();

    /**
     * Used to generate unique data graph names.
     */
    private int dataGraphCounter = 0;

    public InMemoryRDFDataUnit() throws RepositoryException {
        this.repository = new SailRepository(new MemoryStore());
        this.repository.initialize();
        this.writeDataGraph = this.repository.getValueFactory().createURI("http://localhost/test/write");
    }

    @Override
    public URI getBaseDataGraphURI() throws DataUnitException {
        return writeDataGraph;
    }

    @Override
    public void addExistingDataGraph(String symbolicName, URI existingDataGraphURI) throws DataUnitException {
        dataGraphs.put(symbolicName, existingDataGraphURI);
    }

    @Override
    public URI addNewDataGraph(String symbolicName) throws DataUnitException {
        final String newUriStr = "http://localhost/test/" + Integer.toString(dataGraphCounter++);
        final URI newUri = ValueFactoryImpl.getInstance().createURI(newUriStr);

        dataGraphs.put(symbolicName, newUri);

        return newUri;
    }

    @Override
    public RDFDataUnit.Iteration getIteration() throws DataUnitException {
        return new RDFDataUnit.Iteration() {

            private final Iterator<String> iterator = dataGraphs.keySet().iterator();

            @Override
            public RDFDataUnit.Entry next() throws DataUnitException {
                final String key = iterator.next();
                return new RDFDataUnit.Entry() {

                    @Override
                    public URI getDataGraphURI() throws DataUnitException {
                        return dataGraphs.get(key);
                    }

                    @Override
                    public String getSymbolicName() throws DataUnitException {
                        return key;
                    }

                };
            }

            @Override
            public boolean hasNext() throws DataUnitException {
                return iterator.hasNext();
            }

            @Override
            public void close() throws DataUnitException {
                // Do nothing here.
            }

        };
    }

    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        try {
            return repository.getConnection();
        } catch (RepositoryException ex) {
            throw new DataUnitException(ex);
        }
    }

    @Override
    public Set<URI> getMetadataGraphnames() throws DataUnitException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addEntry(String symbolicName) throws DataUnitException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public URI getMetadataWriteGraphname() throws DataUnitException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void shutDown() {
        try {
            repository.shutDown();
        } catch (RepositoryException ex) {
            LOG.warn("Problem with close.", ex);
        }
    }

}
