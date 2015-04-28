package cz.cuni.mff.xrg.uv.service.external;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.context.Context;
import eu.unifiedviews.helpers.dpu.exec.ExecContext;
import eu.unifiedviews.helpers.dpu.extension.Extension;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;

/**
 * Represents remote RDF repository in form of {@link RDFDataUnit}.
 * 
 * @author Škoda Petr
 */
public class RemoteRdfDataUnit implements RDFDataUnit, AutoCloseable, Extension, Extension.Executable {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteRdfDataUnit.class);

    private final ExecContext<?> execContext;

    /**
     * From: https://groups.google.com/forum/#!topic/sesame-users/5UYsqct3y-w
     *
     * Virtuoso does not implement the full Sesame Server protocol (which is an
     * extension of the SPARQL protocol), so you should not be using HTTPRepository to
     * connect to it. Instead, use SPARQLRepository, which is specifically designed to
     * connect to third-party SPARQL endpoints:
     *
     */
    private final SPARQLRepository repository;
    
    private final List<RDFDataUnit.Entry> entries;

    private boolean closed = false;

    RemoteRdfDataUnit(ExecContext<?> execContext, String endpointUrl, URI ... graphs)
            throws ExternalError {
        this.execContext = execContext;
        this.repository = new SPARQLRepository(endpointUrl);
        try {
            this.repository.initialize();
        } catch (RepositoryException ex) {
            throw new ExternalError("Can't create remote HTTP repository.", ex);
        }
        // Initialize entries.
        entries = new ArrayList<>(graphs.length);
        for (URI uri : graphs) {
            entries.add(new RdfDataUnitUtils.InMemoryEntry(uri, "remote: " + endpointUrl));
        }
    }

    @Override
    public Iteration getIteration() throws DataUnitException {
        return new Iteration() {

            private final ListIterator<RDFDataUnit.Entry> iter = entries.listIterator();

            @Override
            public Entry next() throws DataUnitException {
                return iter.next();
            }

            @Override
            public boolean hasNext() throws DataUnitException {
                return iter.hasNext();
            }

            @Override
            public void close() throws DataUnitException {
                // No operation here.
            }
        };
    }

    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        try {
            return repository.getConnection();
        } catch (RepositoryException ex) {
            throw new DataUnitException("Can't get connection.", ex);
        }
    }

    @Override
    public Set<URI> getMetadataGraphnames() throws DataUnitException {
        return Collections.EMPTY_SET;
    }

    @Override
    public void close() throws Exception {
        if (!closed) {
            repository.shutDown();
            closed = true;
        } else {
            LOG.info("Close called on already closed RemoteRdfDataUnit.");
        }
    }

    @Override
    public void preInit(String param) throws DPUException {
        // This methos is not really called, as RemoteRdfDataUnit is added at runtime.
    }

    @Override
    public void afterInit(Context context) throws DPUException {
        // This methos is not really called, as RemoteRdfDataUnit is added at runtime.
    }

    @Override
    public void execute(ExecutionPoint execPoint) throws ExtensionException {
        if (execPoint == ExecutionPoint.POST_EXECUTE) {
            // If RdfDataUnit is not closed then call close.
            if (!closed) {
                try {
                    close();
                } catch (Exception ex) {
                    throw new ExtensionException("Can't close RemoteRdfDataUnit.");
                }
            }
        }
    }

}
