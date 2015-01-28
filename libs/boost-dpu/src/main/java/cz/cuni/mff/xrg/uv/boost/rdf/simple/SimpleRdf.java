package cz.cuni.mff.xrg.uv.boost.rdf.simple;

import java.lang.reflect.Field;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import cz.cuni.mff.xrg.uv.boost.dpu.context.Context;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to handler RDF functionality and
 * also reduce code duplicity.
 *
 *
 * @author Škoda Petr
 */
public class SimpleRdf implements AutoInitializer.Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleRdf.class);

    private RDFDataUnit readDataUnit = null;

    /**
     * Name of filed that should be bound as a data unit.
     */
    protected String dataUnitName;

    private ValueFactory valueFactory = null;

    public RDFDataUnit getReadDataUnit() {
        return readDataUnit;
    }

    /**
     *
     * @return True if this object is active, initialized and can be used.
     */
    public boolean isActive() {
        return readDataUnit != null;
    }

    @Override
    public void preInit(String param) throws DPUException {
        dataUnitName = param;
    }

    @Override
    public void afterInit(Context context) throws DPUException {
        if (context instanceof AbstractDpu.ExecutionContext) {
            // Ok we can process.
        } else {
            // Nothin for the dialog.
            return;
        }
        // Get underliyng RDFDataUnit.
        final AbstractDpu.ExecutionContext execContext = (AbstractDpu.ExecutionContext)context;
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
            if (RDFDataUnit.class.isAssignableFrom(value.getClass())) {
                readDataUnit = (RDFDataUnit)value;
            } else {
                throw new DPUException("Class" + value.getClass().getCanonicalName()
                        + " can't be assigned to RDFDataUnit.");
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new DPUException("Can't get value for: " + dataUnitName, ex);
        }
    }

    /**
     * Cache result. After first successful call does not fail.
     *
     * @return
     * @throws DataUnitException
     */
    public ValueFactory getValueFactory() throws DataUnitException {
        if (valueFactory == null) {
            RepositoryConnection connection = null;
            try {
                connection = readDataUnit.getConnection();
                valueFactory = connection.getValueFactory();
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
        return valueFactory;
    }

}
