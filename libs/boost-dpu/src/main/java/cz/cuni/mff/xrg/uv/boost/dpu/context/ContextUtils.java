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

    /**
     * Send formated {@link DPUContext.MessageType#INFO} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param params
     */
    public static void sendInfo(DPUContext context, String caption, String bodyFormat, Object... params) {
        sendMessage(context, DPUContext.MessageType.INFO, caption, bodyFormat, params);
    }

    /**
     * Send formated {@link DPUContext.MessageType#WARNING} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param params
     */
    public static void sendWarn(DPUContext context, String caption, String bodyFormat, Object... params) {
        sendMessage(context, DPUContext.MessageType.WARNING, caption, bodyFormat, params);
    }

    /**
     * Send formated {@link DPUContext.MessageType#WARNING} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param exception
     * @param bodyFormat
     * @param params
     */
    public static void sendWarn(DPUContext context, String caption, Exception exception,
            String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        context.sendMessage(DPUContext.MessageType.WARNING, caption, body, exception);
    }

}
