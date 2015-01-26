package cz.cuni.mff.xrg.uv.boost.rdf.simple;

import java.lang.reflect.Field;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPUException;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to handler RDF functionality and
 * also reduce code duplicity.
 *
 * @author Škoda Petr
 */
public class SimpleRdf implements AutoInitializer.Initializable {

    private RDFDataUnit readDataUnit = null;

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
    public void init(DpuAdvancedBase.Context context, String param) throws DPUException {
        final Object dpu = context.getDpu();
        final Field field;
        try {
            field = dpu.getClass().getField(param);
        } catch (NoSuchFieldException | SecurityException ex) {
            throw new DPUException("Wrong initial parameters for SimpleRdf: " + param
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
            throw new DPUException("Can't get value for: " + param, ex);
        }
    }

}
