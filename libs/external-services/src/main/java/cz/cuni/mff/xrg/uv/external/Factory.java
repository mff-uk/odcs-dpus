package cz.cuni.mff.xrg.uv.external;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.uv.external.rdf.RemoteRepository;

/**
 * General purpose factory. Use this class to create classes from this module.
 * 
 * @author Škoda Petr
 */
public class Factory {
    
    private Factory() { }
        
    public static RemoteRepository remoteRepository(String uri, 
            DPUContext context, int delay) throws ExternalFailure {
        return new RemoteRepository(uri,
                new FaultTolerantPolicy(delay), context);
    }
    
    public static RemoteRepository remoteRepository(String uri, 
            DPUContext context, int delay, int numberOfRetries) 
            throws ExternalFailure {
        return new RemoteRepository(uri, 
                new FaultTolerantPolicy(delay, numberOfRetries), context);
    }
    
}
