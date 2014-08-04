package cz.cuni.mff.xrg.uv.service.external;

import cz.cuni.mff.xrg.uv.service.external.rdf.RemoteRepository;
import eu.unifiedviews.dpu.DPUContext;

/**
 * General purpose factory. Use this class to create classes from this module.
 * 
 * @author Škoda Petr
 */
public class ExternalServicesFactory {
    
    private ExternalServicesFactory() { }
        
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
