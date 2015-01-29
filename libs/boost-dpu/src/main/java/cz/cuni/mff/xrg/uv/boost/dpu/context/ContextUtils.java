package cz.cuni.mff.xrg.uv.boost.dpu.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUContext;

/**
 * Utilities for context.
 *
 * @author Škoda Petr
 */
public class ContextUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ContextUtils.class);

    private ContextUtils() {

    }

    /**
     * Send given formated message. Does not support message with exception.
     *
     * @param context    If null only log about message is stored.
     * @param type
     * @param caption
     * @param bodyFormat
     * @param params
     */
    public static void sendMessage(DPUContext context, DPUContext.MessageType type, String caption,
            String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        if (context == null) {
            LOG.info("Message ignored:\ntype:{}\ncaption:{}\ntext:{}\n", type, caption, body);
        } else {
            context.sendMessage(type, caption, body);
        }
    }

    /**
     * Send given formated message. Does not support message with exception.
     *
     * @param context    If null only log about message is stored.
     * @param type
     * @param caption
     * @param exception
     * @param bodyFormat
     * @param params
     */
    public static void sendMessage(DPUContext context, DPUContext.MessageType type, String caption,
            Exception exception, String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        if (context == null) {
            LOG.info("Message ignored:\ntype:{}\ncaption:{}\ntext:{}\n", type, caption, body);
        } else {
            context.sendMessage(type, caption, body, exception);
        }
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

    public static void sendWarn(DPUContext context, String caption, Exception exception,
            String bodyFormat, Object... params) {
        sendMessage(context, DPUContext.MessageType.WARNING, caption, exception, bodyFormat, params);
    }

    public static void sendError(DPUContext context, String caption, Exception exception,
            String bodyFormat, Object... params) {
        sendMessage(context, DPUContext.MessageType.ERROR, caption, exception, bodyFormat, params);
    }

    public static void sendShortInfo(DPUContext context, String captionFormat, Object... params) {
        final String caption = String.format(captionFormat, params);
        sendMessage(context, DPUContext.MessageType.INFO, caption, "");
    }

    public static void sendShortWarn(DPUContext context, String captionFormat, Object... params) {
        final String caption = String.format(captionFormat, params);
        sendMessage(context, DPUContext.MessageType.WARNING, caption, "");
    }


}
