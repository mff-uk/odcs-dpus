package cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import java.util.*;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add write functionality to {@link SimpleRdfRead} by wrapping
 * {@link WritableRDFDataUnit}.
 *
 * @author Škoda Petr
 */
class SimpleRdfWriteImpl extends SimpleRdfReadImpl implements SimpleRdfWrite {

	private static final Logger LOG = LoggerFactory.getLogger(
			SimpleRdfWrite.class);

    private static final String DEFAULT_GRAPH_NAME = "default-output";

	/**
	 * Add policy.
	 */
	protected AddPolicy addPolicy = AddPolicy.IMMEDIATE;

	/**
	 * Max size of {@link #toAddBuffer}. If the buffer is larger and
	 * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * is called then the buffer is flushed by {@link #flushBuffer()}.
	 */
	protected int toAddBufferFlushSize = 100000;

	/**
	 * Buffer for triples that should be added into wrapped {@link #dataUnit}.
	 */
	protected final ArrayList<Statement> toAddBuffer = new ArrayList<>();

	/**
	 * Wrapped {@link WritableRDFDataUnit}.
	 */
	protected final WritableRDFDataUnit writableDataUnit;

    /**
     * Holds info about all added graphs.
     */
    protected Map<String, URI> writeSetAll;

    /**
     * Current write set.
     */
    protected Map<String, URI> writeSetCurrent;

	/**
	 *
	 * @param dataUnit
	 * @param context
     * @throws cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException
	 */
	SimpleRdfWriteImpl(WritableRDFDataUnit dataUnit, DPUContext context)
            throws OperationFailedException {
		super(dataUnit, context);
		this.writableDataUnit = dataUnit;
        this.writeSetAll = new HashMap<>();
        this.writeSetCurrent = writeSetAll;
	}

	/**
	 * Add triple into repository. Based on current {@link AddPolicy} can add
	 * triple in immediate or lazy way.
	 *
	 * In the second case the {@link #flushBuffer()} method must be called in
	 * order to add triples into used repository, until that the triples are
	 * stored in inner buffer - the triples are not visible in any read function.
	 *
	 * @param s
	 * @param p
	 * @param o
	 * @throws OperationFailedException
	 */
    @Override
	public void add(Resource s, URI p, Value o) throws OperationFailedException {
		final Statement statement = getValueFactory().createStatement(s, p, o);
		// add to bufer
		toAddBuffer.add(statement);
		// based on policy
		switch (addPolicy) {
			case BUFFERED:
				// flush only if we have enough data
				if (toAddBuffer.size() > toAddBufferFlushSize) {
					LOG.trace("Flush on full buffer, size {}.",
							toAddBuffer.size());
					flushBuffer();
				}
				break;
			case IMMEDIATE:
				// flush in evry case
				flushBuffer();
				break;
		}
	}

	/**
	 * Set policy that determines how the
	 * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * behaves.
	 *
	 * @param policy Add policy.
	 */
    @Override
	public void setPolicy(AddPolicy policy) {
		this.addPolicy = policy;
	}

	/**
	 * Immediately store buffered triples into repository. The inner buffer is
	 * cleared only if all the triples are added successfully. If throws
	 * exception then the state of repository is undefined.
	 *
	 * If
	 * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)}
	 * is called with {@link AddPolicy#BUFFERED} this method must be called in
	 * order to save added statements into repository.
	 *
	 * @throws OperationFailedException
	 */
    @Override
	public void flushBuffer() throws OperationFailedException {
		if (toAddBuffer.isEmpty()) {
			// nothing to add
			return;
        }
		try (ClosableConnection conn = new ClosableConnection(dataUnit)) {
			conn.c().begin();
			// add to repository
			conn.c().add(toAddBuffer, getCurrentWriteContexts());
			conn.c().commit();
			// clear buffer
			toAddBuffer.clear();
		} catch (RepositoryException ex) {
			throw new OperationFailedException(
					"Failed to add triples into repository.", ex);
		}
	}

    @Override
    public void setOutputGraph(String symbolicName) throws OperationFailedException {
        writeSetCurrent.clear();
        writeSetCurrent.put(symbolicName, createNewGraph(symbolicName));
    }

    /**
     * If no graph is in {@link #writeSetCurrent} then new one is added.
     *
     * @return array of current write {@likn URI}s
     */
    private URI[] getCurrentWriteContexts() throws OperationFailedException {
        // test for graph
        if (writeSetCurrent.isEmpty()) {
            // no write set .. add new graph
            writeSetCurrent.put(DEFAULT_GRAPH_NAME,
                    createNewGraph(DEFAULT_GRAPH_NAME));
            LOG.info("Default graph used: " + DEFAULT_GRAPH_NAME);
        }

        return writeSetCurrent.values().toArray(new URI[0]);
    }

    /**
     * Set {@link #writeSetCurrent} to all graphs in {@link #writableDataUnit}.
     */
    private void setCurrentWriteSetToAll() {
        writeSetCurrent = writeSetAll;
    }


    /**
     * Use {@link WritableRDFDataUnit#getBaseDataGraphURI()} and given name
     * to generate new graph {@link URI} and create a graph with it.
     *
     * @param symbolicName
     * @return
     */
    private URI createNewGraph(String symbolicName)
            throws OperationFailedException {

        if (writeSetAll.containsKey(symbolicName)) {
            LOG.warn("DPU ask me to create graph that already exists: {}", symbolicName);
            return writeSetAll.get(symbolicName);
        }

        final URI newUri;
        try {
            newUri = writableDataUnit.addNewDataGraph(symbolicName);
        } catch (DataUnitException ex) {
            throw new OperationFailedException("Failed to add new graph.", ex);
        }

        // add to all Uri repository
        writeSetAll.put(symbolicName, newUri);

        return newUri;
    }

}
