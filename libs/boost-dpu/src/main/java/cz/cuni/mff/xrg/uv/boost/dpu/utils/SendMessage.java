package cz.cuni.mff.xrg.uv.boost.dpu.utils;

import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPUContext;
import org.openrdf.repository.RepositoryException;

/**
 * Utility for common message sending.
 *
 * @author Škoda Petr
 */
public class SendMessage {

    private SendMessage() {
        
    }

    public static void sendMessage(DPUContext context, OperationFailedException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR,
                "Operation on SimpleRdf failed.", "", ex);
    }

    public static void sendMessage(DPUContext context, DataUnitException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR,
                "Problem with data unit.", "", ex);
    }

    public static void sendMessage(DPUContext context, RepositoryException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR, 
                "Problem with repository.", "", ex);
    }

}
