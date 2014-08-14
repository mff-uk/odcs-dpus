package cz.cuni.mff.xrg.uv.rdf.simple;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPUContext;

/**
 * Create {@link SimpleRdfRead}/{@link SimpleRdfWrite} wrap for given
 * {@link RDFDataUnit}/{@link WritableRDFDataUnit}.
 *
 * @author Škoda Petr
 */
public class SimpleRdfFactory {
    
    private SimpleRdfFactory() {
        
    }
    
    public static SimpleRdfRead create(RDFDataUnit dataUnit, DPUContext context) 
            throws OperationFailedException {
        return new SimpleRdfReadImpl(dataUnit, context);
    }

    public static SimpleRdfWrite create(WritableRDFDataUnit dataUnit,
            DPUContext context) throws OperationFailedException {
        return new SimpleRdfWriteImpl(dataUnit, context);
    }
    
}
