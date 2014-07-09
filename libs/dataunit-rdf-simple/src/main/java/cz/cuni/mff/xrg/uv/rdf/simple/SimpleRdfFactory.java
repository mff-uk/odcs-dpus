package cz.cuni.mff.xrg.uv.rdf.simple;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;

/**
 *
 * @author Škoda Petr
 */
public class SimpleRdfFactory {
    
    private SimpleRdfFactory() {
        
    }
    
    public static SimpleRdfRead create(RDFDataUnit dataUnit, DPUContext context) {
        return new SimpleRdfRead(dataUnit, context);
    }

    public static SimpleRdfWrite create(WritableRDFDataUnit dataUnit, DPUContext context) {
        return new SimpleRdfWrite(dataUnit, context);
    }
    
}
