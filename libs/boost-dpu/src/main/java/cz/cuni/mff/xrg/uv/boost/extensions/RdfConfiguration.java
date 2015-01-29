package cz.cuni.mff.xrg.uv.boost.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.Addon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.ontology.Ontology;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationFailure;
import cz.cuni.mff.xrg.uv.boost.serialization.SerializationUtils;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdf;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdfFactory;
import cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdfFailure;
import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;
import info.aduna.iteration.Iterations;

/**
 * Load configuration from RDFDataUnit into configuration classes.
 *
 * Has same limitations as {@link cz.cuni.mff.xrg.uv.boost.serialization.rdf.SerializationRdfSimple}.
 *
 * @author Škoda Petr
 */
public class RdfConfiguration implements ConfigTransformer, Addon {

    /**
     * Use to mark RDFDataUnit that contains rdf configuration data.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ContainsConfiguration {
    
    }

    private static final Logger LOG = LoggerFactory.getLogger(RdfConfiguration.class);

    private final SerializationRdf serialization = SerializationRdfFactory.rdfSimple();

    private RDFDataUnit sourceDataUnit;
    
    private Context context;

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {

    }

    @Override
    public String transformString(String configName, String config) throws ConfigException {
        return config;
    }

    @Override
    public <TYPE> void transformObject(String configName, final TYPE config) throws ConfigException {
        if (sourceDataUnit == null) {
            return;
        }
        final FaultTolerance wrap = (FaultTolerance)context.getInstance(FaultTolerance.class);
        // Check if object has annotations.
        final Ontology.Entity annotation = config.getClass().getAnnotation(Ontology.Entity.class);
        if (annotation == null) {
            return;
        }
        // Get class resource type.
        final URI resourceClass = ValueFactoryImpl.getInstance().createURI(annotation.type());
        if (wrap == null) {
            RepositoryConnection connection = null;
            try {
                connection = sourceDataUnit.getConnection();
                loadRdfIntoObject(connection, resourceClass, config);
            }  catch (DataUnitException | RepositoryException | SerializationFailure |
                    SerializationRdfFailure ex) {
                throw new ConfigException("Can't load configuration from RDF.", ex);
            }
            finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection!");
                }
            }
        } else {
            // Use fault tolerant helper.
            try {
                wrap.execute(sourceDataUnit, new FaultTolerance.ConnectionAction() {

                    @Override
                    public void action(RepositoryConnection connection) throws Exception {
                        // Create an instance.
                        TYPE newInstance;
                        try {
                            newInstance = (TYPE)config.getClass().newInstance();
                        } catch (IllegalAccessException | InstantiationException ex) {
                            throw new Exception(ex);
                        }
                        // Load into created instance.
                        loadRdfIntoObject(connection, resourceClass, newInstance);
                        // Merge into given isntance.
                        try {
                            SerializationUtils.merge(newInstance, config);
                        } catch (RuntimeException ex) {
                            throw new Exception("Can't metge data.", ex);
                        }
                        // We need to do this in this order so in case of multiple execution we
                        // got the same results as in case of single execution.
                    }
                });
            } catch (DPUException ex) {
                throw new ConfigException("Can't load configuration from RDF.", ex);
            }
        }
    }

    private <TYPE> void loadRdfIntoObject(RepositoryConnection connection, URI resourceClass, TYPE object)
            throws DataUnitException, RepositoryException, SerializationFailure, SerializationRdfFailure {
        final List<RDFDataUnit.Entry> etries = DataUnitUtils.getEntries(sourceDataUnit,
                RDFDataUnit.Entry.class);
        final URI [] graphs = RdfDataUnitUtils.asGraphs(etries);
        // Load subjects.
        final List<Resource> resources = getConfigurationSubject(resourceClass, graphs);
        if (resources.isEmpty()) {
            // No configuration resource.
            LOG.debug("No configuration object found.");
            return;
        }
        if (resources.size() > 1) {
            LOG.info("Loading from multiple instances.");
        }
        // For each subject load data.
        for (Resource resource : resources) {
            serialization.convert(connection, resource, etries, object, null);
        }
    }

    /**
     * 
     * @param clazz URI of configuration subject.
     * @param graphs
     * @return Resources of given class.
     */
    private List<Resource> getConfigurationSubject(URI clazz, URI [] graphs) throws DataUnitException, RepositoryException {
        final List<Resource> subjects = new LinkedList<>();
        RepositoryConnection conn = null;
        try {
            conn = sourceDataUnit.getConnection();
            final URI type = conn.getValueFactory().createURI("http://www.w3.org/2000/01/rdf-schema#a");
            // Load statemetns into memory.
            List<Statement> statements;
            RepositoryResult<Statement> result = conn.getStatements(null, type, clazz, true, graphs);
            try {
                statements = Iterations.asList(result);
            } finally {
                result.close();
            }
            // Get subjects.
            for (Statement statement : statements) {
                subjects.add(statement.getSubject());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (RepositoryException ex) {
                LOG.info("Can't close exception.", ex);
            }
        }
        return subjects;
    };

    @Override
    public void preInit(String param) throws DPUException {
        // No-op.
    }

    @Override
    public void afterInit(Context context) throws DPUException {
        this.context = context;
        if (context instanceof AbstractDpu.ExecutionContext) {

        } else {
            return;
        }
        final AbstractDpu.ExecutionContext execContext = (AbstractDpu.ExecutionContext)context;
        final AbstractDpu dpu = execContext.getDpu();
        // Search for source RDFDataUnit.
        for (Field field : dpu.getClass().getFields()) {
            if (field.getAnnotation(ContainsConfiguration.class) != null) {
                // We got annotated field. Get it's value.
                try {
                    final Object value = field.get(dpu);
                    if (value == null) {
                        // We are in scope of dialog, or input is not set.
                        return;
                    }
                    if (value instanceof RDFDataUnit) {
                        sourceDataUnit = (RDFDataUnit)value;
                        return;
                    } else {
                        throw new DPUException("Field " + field.getName() + " does not have requered type "
                                + "RDFDataUnit");
                    }
                } catch (IllegalAccessException | IllegalArgumentException ex) {
                    throw new DPUException("Can't get value for annotated field: " + field.getName(), ex);
                }
            }
        }
        throw new DPUException("Missing configuration RDFDataUnit! "
                + "Use RdfConfiguration.ContainsConfiguration annotation to mark it.");
    }

}
