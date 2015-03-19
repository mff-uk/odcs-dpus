package cz.cuni.mff.xrg.uv.service.external;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;

/**
 * General purpose factory. Use this class to create classes from this module.
 * 
 * @author Škoda Petr
 */
public class ExternalServicesFactory {
    
    private ExternalServicesFactory() { }

    /**
     * Create and return instance of {@link RDFDataUnit} that use remote repository.
     *
     * @param ctx
     * @param endpointUrl
     * @param graphs
     * @return
     * @throws ExternalError
     */
    public static RDFDataUnit remoteRdf(UserExecContext ctx, String endpointUrl, URI ... graphs) throws ExternalError {
        RemoteRdfDataUnit remote = new RemoteRdfDataUnit(ctx.getExecMasterContext(), endpointUrl, graphs);
        // Also add remove to list of extensions. In this way the create RemoteRdfDataUnit
        // can close itself after innerExecute.
        ctx.getExecMasterContext().getExtensions().add(remote);
        return remote;
    }

    /**
     * Convert graphs to URIs and call
     * {@link #remoteRdf(eu.unifiedviews.helpers.dpu.exec.UserExecContext, java.lang.String, org.openrdf.model.URI...)}.
     *
     * @param ctx
     * @param endpointUrl
     * @param graphs
     * @return
     * @throws ExternalError
     */
    public static RDFDataUnit remoteRdf(UserExecContext ctx, String endpointUrl, String ... graphs) throws ExternalError {
        final URI[] graphsUri = new URI[graphs.length];
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        for (int i = 0; i < graphs.length; ++i) {
            graphsUri[i] = valueFactory.createURI(graphs[i]);
        }
        return remoteRdf(ctx, endpointUrl, graphsUri);
    }


}
