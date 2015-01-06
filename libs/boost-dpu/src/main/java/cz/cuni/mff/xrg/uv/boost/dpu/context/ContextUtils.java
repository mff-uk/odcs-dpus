package cz.cuni.mff.xrg.uv.boost.dpu.context;

import eu.unifiedviews.dpu.DPUContext;

/**
 * Utilities for context.
 *
 * @author Škoda Petr
 */
public class ContextUtils {

    private ContextUtils() {
        
    }

    /**
     * Send given formated message. Does not support message with exception.
     * 
     * @param context
     * @param type
     * @param caption
     * @param bodyFormat
     * @param params 
     */
    public static void sendMessage(DPUContext context, DPUContext.MessageType type, String caption,
            String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        context.sendMessage(type, caption, body);
    }

}
