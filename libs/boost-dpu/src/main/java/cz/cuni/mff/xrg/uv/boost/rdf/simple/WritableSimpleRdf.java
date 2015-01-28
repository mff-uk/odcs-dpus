package cz.cuni.mff.xrg.uv.boost.rdf.simple;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.boost.ontology.Ontology;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;

/**
 * Add write functionality to {@link SimpleRdfRead} by wrapping {@link WritableRDFDataUnit}.
 *
 * <pre>
 * {@code
 * SimpleRdfWrite rdf = SimpleRdfFactory(rdfDataUnit, dpuContext);
 * rdf.setPolicy(AddPolicy.BUFFERED);
 * rdf.setOutputGraph("myOutputGraph");
 * // Add triple and flush buffer called as a chain.
 * rdf.add(subject, predicate, object).flushBuffer();
 * }
 * </pre>
 *
 * Features:
 * <ul>
 * <li>This class is fault tolerant if {@link FaultTolerance} is presented.</li>
 * <li>Buffer is automatically flushed at the end of the execution.</li>s
 * </ul>
 *
 *
 * @author Škoda Petr
 */
public class WritableSimpleRdf extends SimpleRdf implements Addon.Executable {

    public static final String CONFIGURATION_CLASS_URI
            = "http://uv.xrg.mff.cuni.cz/ontology/dpu/boost/rdf/simple/Configuration";

    @Ontology.Entity(type = CONFIGURATION_CLASS_URI)
    public static class Configuration {

        public enum AddPolicy {

            /**
             * Triples are added into repository immediately. For each addition new connection is created.
             * This approach provide immediate reaction on possible problem (repository is offline) but in
             * case of saving greater number of triples can be computationally demanding.
             */
            IMMEDIATE,
            /**
             * Triples are stored in in memory buffer and added into repository once upon a time. This option
             * should be used if larger number of statements should be added into repository. There is a
             * disadvantage in current implementation where a single failure will cause failure there is no
             * guaranteed state of used repository. The buffer is cleared only if all the triples are
             * successfully added into repository.
             */
            BUFFERED,
            /**
             * Data are stored only on user request (call of {@link #flushBuffer()}. In this case
             * {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)} does
             * not throw.
             */
            ON_DEMAND
        }

        public AddPolicy addPolicy = AddPolicy.BUFFERED;

        /**
         * Commit size in case the {@link AddPolicy#BUFFERED} is used.
         */
        public Integer commitSize = 100000;

    }

    private static final Logger LOG = LoggerFactory.getLogger(WritableSimpleRdf.class);

    private static final String DEFAULT_SYMBOLIC_NAME = "default-output";

    private WritableRDFDataUnit writableDataUnit = null;

    /**
     * Buffer for triples that should be added into wrapped {@link #dataUnit}.
     */
    protected final List<Statement> writeBuffer = new ArrayList<>(100000);

    /**
     * Current write set.
     */
    protected List<URI> writeContext = new ArrayList<>();

    protected Configuration configuration = new Configuration();

    private FaultTolerance faultTolerance = null;

    /**
     * Add triple into repository. Based on current {@link AddPolicy} can add triple in immediate or lazy way.
     *
     * In the second case the {@link #flushBuffer()} method must be called in order to add triples into used
     * repository, until that the triples are stored in inner buffer - the triples are not visible in any read
     * function.
     *
     * @param s
     * @param p
     * @param o
     * @return
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException
     */
    public WritableSimpleRdf add(Resource s, URI p, Value o) throws SimpleRdfException {
        // Add to buffer.
        writeBuffer.add(new StatementImpl(s, p, o));
        applyFlushBufferPolicy();
        return this;
    }

    public WritableSimpleRdf add(List<Statement> statements) throws SimpleRdfException {
        writeBuffer.addAll(statements);
        applyFlushBufferPolicy();
        return this;
    }

    /**
     * Immediately store buffered triples into repository. The inner buffer is cleared only if all the triples
     * are added successfully. If throws exception then the state of repository is undefined.
     *
     * If {@link #add(org.openrdf.model.Resource, org.openrdf.model.URI, org.openrdf.model.Value)} is called
     * with {@link AddPolicy#BUFFERED} this method must be called in order to save added statements into
     * repository.
     *
     * If throws, then the buffer remain unchanged so the function can be called again until it does not pass
     * or caller give up.
     *
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException
     */
    public void flushBuffer() throws SimpleRdfException {
        if (faultTolerance == null) {
            flushBufferInner();
        } else {
            try {
                faultTolerance.execute(new FaultTolerance.Action() {

                    @Override
                    public void action() throws Exception {
                        flushBuffer();
                    }
                });
            } catch (DPUException ex) {
                throw new SimpleRdfException("Can't flush rdf data.", ex);
            }
        }
    }

    /**
     * Same as {@link #flushBuffer()}. Reason for this class is easier usage with fault tolerant wrap.
     *
     * @throws SimpleRdfException
     */
    private void flushBufferInner() throws SimpleRdfException {
        if (writeBuffer.isEmpty()) {
            // Nothing to save into repository.
            return;
        }
        if (writeContext.isEmpty()) {
            createDefaultWriteGraph();
        }
        // Prepare write contexts.
        final URI[] contexts = writeContext.toArray(new URI[0]);
        // Get connection and add data.
        RepositoryConnection connection = null;
        try {
            connection = writableDataUnit.getConnection();
            connection.begin();
            for (Statement statemnt : writeBuffer) {
                connection.add(statemnt, contexts);
            }
            connection.commit();
        } catch (DataUnitException ex) {
            throw new SimpleRdfException("Problem with DataUnit.", ex);
        } catch (RepositoryException ex) {
            throw new SimpleRdfException("Problem with Repository.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
        // Data sucesfully stored, clear the buffer.
        writeBuffer.clear();
    }

    /**
     * Set given graph as current output. If existing graph is set then the existing graph is used.
     *
     * @param entry
     * @return
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException
     */
    public WritableSimpleRdf setOutput(RDFDataUnit.Entry entry) throws SimpleRdfException {
        return setOutput(Arrays.asList(entry));
    }

    /**
     * Set given graph as current output. If existing graph is set then the existing graph is used.
     *
     * @param entries
     * @return
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException
     */
    public WritableSimpleRdf setOutput(List<RDFDataUnit.Entry> entries) throws SimpleRdfException {
        // Flush buffer first.
        flushBuffer();
        // Change target graph.
        final List<URI> newWriteContext = new ArrayList<>(entries.size());
        for (RDFDataUnit.Entry entry : entries) {
            try {
                newWriteContext.add(entry.getDataGraphURI());
            } catch (DataUnitException ex) {
                throw new SimpleRdfException("Can't read graph name.", ex);
            }
        }
        this.writeContext = newWriteContext;
        return this;
    }

    public List<URI> getWriteContext() {
        return writeContext;
    }

    public void setWriteContext(List<URI> writeContext) {
        this.writeContext = writeContext;
    }

    /**
     *
     * @return If modify the configuration must be set back.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * If {@link AddPolicy} change from {@link AddPolicy#BUFFERED} to {@link AddPolicy#IMMEDIATE} then
     * {@link #flushBuffer()} is called.
     *
     * @param configuration
     * @throws cz.cuni.mff.xrg.uv.boost.serialization.rdf.SimpleRdfException
     */
    public void setConfiguration(Configuration configuration) throws SimpleRdfException {
        this.configuration = configuration;
        // Check if we should not flush buffer.
        if (this.configuration.addPolicy == Configuration.AddPolicy.IMMEDIATE) {
            this.flushBuffer();
        }
    }

    /**
     * Create and set new default output graph. Fixed symbolic name is used for output graph.
     *
     * @throws SimpleRdfException
     */
    private void createDefaultWriteGraph() throws SimpleRdfException {
        LOG.warn("Default output graph used.");
        final URI writeGraphUri;
        try {
            writeGraphUri = writableDataUnit.addNewDataGraph(DEFAULT_SYMBOLIC_NAME);
        } catch (DataUnitException ex) {
            throw new SimpleRdfException("Failed to add new graph.", ex);
        }
        writeContext.add(writeGraphUri);
    }

    /**
     * Based on policy call {@link #flushBuffer()} if needed.
     *
     * @throws SimpleRdfException
     */
    private void applyFlushBufferPolicy() throws SimpleRdfException {
        switch (configuration.addPolicy) {
            case BUFFERED:
                if (writeBuffer.size() > configuration.commitSize) {
                    flushBuffer();
                }
                break;
            case IMMEDIATE:
                flushBuffer();
                break;
            case ON_DEMAND:
                // No operation here.
                break;
            default:
                throw new RuntimeException("Unknown AddPolicy type: " + configuration.addPolicy.toString());
        }
    }

    @Override
    public void preInit(String param) throws DPUException {
        super.preInit(param);
    }

    @Override
    public void afterInit(Context context) throws DPUException {
        super.afterInit(context);
        //
        if (context instanceof AbstractDpu.ExecutionContext) {
            // Ok we can process.
        } else {
            // Nothin for the dialog.
            return;
        }
        // Get underliyng RDFDataUnit and other objects.
        final AbstractDpu.ExecutionContext execContext = (AbstractDpu.ExecutionContext) context;
        //
        final Object dpu = execContext.getDpu();
        final Field field;
        try {
            field = dpu.getClass().getField(dataUnitName);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DPUException("Wrong initial parameters for SimpleRdf: " + dataUnitName
                    + ". Can't access such field.", ex);
        }
        try {
            final Object value = field.get(dpu);
            if (value == null) {
                return;
            }
            if (WritableRDFDataUnit.class.isAssignableFrom(value.getClass())) {
                writableDataUnit = (WritableRDFDataUnit) value;
            } else {
                throw new DPUException("Class" + value.getClass().getCanonicalName()
                        + " can't be assigned to WritableRDFDataUnit.");
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new DPUException("Can't get value for: " + dataUnitName, ex);
        }
        // Check for fault tolerance addon.
        faultTolerance = (FaultTolerance) context.getInstance(FaultTolerance.class);
    }

    @Override
    public void execute(Addon.ExecutionPoint execPoint) throws AddonException {
        if (execPoint == Addon.ExecutionPoint.POST_EXECUTE) {
            // Made sure that all data are saved.
            try {
                flushBuffer();
            } catch (SimpleRdfException ex) {
                throw new AddonException("Can't flush data at the end of execution.", ex);
            }
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive() && writableDataUnit != null;
    }

}
