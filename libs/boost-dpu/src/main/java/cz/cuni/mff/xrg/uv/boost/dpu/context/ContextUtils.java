package cz.cuni.mff.xrg.uv.boost.dpu.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.ExecContext;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.UserExecContext;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;

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
     * @param args
     */
    public static void sendMessage(UserContext context, DPUContext.MessageType type, String caption,
            String bodyFormat, Object... args) {
        final String body = String.format(bodyFormat, args);
        if (context.getMasterContext() instanceof ExecContext) {
            final DPUContext dpuContext = ((ExecContext)context.getMasterContext()).getDpuContext();
            if (dpuContext != null) {
                dpuContext.sendMessage(type, caption, body);
                return;
            }
        }
        // Else context has not yet been initialized.
        LOG.info("Message ignored:\ntype:{}\ncaption:{}\ntext:{}\n", type, caption, body);
    }

    /**
     * Send given formated message. Does not support message with exception.
     *
     * @param context    If null only log about message is stored.
     * @param type
     * @param caption
     * @param exception
     * @param bodyFormat
     * @param args
     */
    public static void sendMessage(UserContext context, DPUContext.MessageType type, String caption,
            Exception exception, String bodyFormat, Object... args) {
        final String body = String.format(bodyFormat, args);
        if (context.getMasterContext() instanceof ExecContext) {
            final DPUContext dpuContext = ((ExecContext)context.getMasterContext()).getDpuContext();
            if (dpuContext != null) {
                dpuContext.sendMessage(type, caption, body, exception);
                return;
            }
        }
        // Else context has not yet been initialized.
        LOG.info("Message ignored:\ntype:{}\ncaption:{}\ntext:{}\n", type, caption, body);
    }

    /**
     * Send formated {@link DPUContext.MessageType#INFO} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param args
     */
    public static void sendInfo(UserContext context, String caption, String bodyFormat, Object... args) {
        sendMessage(context, DPUContext.MessageType.INFO, caption, bodyFormat, args);
    }

    /**
     * Send formated {@link DPUContext.MessageType#WARNING} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param args
     */
    public static void sendWarn(UserContext context, String caption, String bodyFormat, Object... args) {
        sendMessage(context, DPUContext.MessageType.WARNING, caption, bodyFormat, args);
    }

    /**
     * Send formated {@link DPUContext.MessageType#WARNING} message.
     *
     * @param context
     * @param caption
     * @param exception
     * @param bodyFormat
     * @param args
     */
    public static void sendWarn(UserContext context, String caption, Exception exception,
            String bodyFormat, Object... args) {
        sendMessage(context, DPUContext.MessageType.WARNING, caption, exception, bodyFormat, args);
    }

    /**
     * Send formated {@link DPUContext.MessageType#ERROR} message.
     *
     * @param context
     * @param caption
     * @param exception
     * @param bodyFormat
     * @param args
     */
    public static void sendError(UserContext context, String caption, Exception exception,
            String bodyFormat, Object... args) {
        sendMessage(context, DPUContext.MessageType.ERROR, caption, exception, bodyFormat, args);
    }

    /**
     * Send formated {@link DPUContext.MessageType#ERROR} message.
     *
     * @param context
     * @param caption
     * @param exception
     * @param bodyFormat
     * @param args
     */
    public static void sendError(UserContext context, String caption, String bodyFormat,
            Object... args) {
        sendMessage(context, DPUContext.MessageType.ERROR, caption, bodyFormat, args);
    }

    /**
     * Send short {@link DPUContext.MessageType#INFO message (caption only). The caption is formated.
     *
     * @param context
     * @param captionFormat
     * @param args
     */
    public static void sendShortInfo(UserContext context, String captionFormat, Object... args) {
        final String caption = String.format(captionFormat, args);
        sendMessage(context, DPUContext.MessageType.INFO, caption, "");
    }

    /**
     * Send short {@link DPUContext.MessageType#WARNING} message (caption only). The caption is formated.
     *
     * @param context
     * @param captionFormat
     * @param args
     */
    public static void sendShortWarn(UserContext context, String captionFormat, Object... args) {
        final String caption = String.format(captionFormat, args);
        sendMessage(context, DPUContext.MessageType.WARNING, caption, "");
    }

    /**
     * Throw DPU exception of given text. Before throw given text is localized based on current locale
     * setting.
     *
     * @param message Exception message.
     * @throws DPUException
     */
    public void throwDpuException(String message) throws DPUException {

    }

    /**
     * Throw DPU exception of given text. Before throw given text is localized based on current locale
     * setting.
     * 
     * @param message
     * @param args
     * @throws DPUException
     */
    public void throwDpuException(String message, Object... args) throws DPUException {

    }

}
