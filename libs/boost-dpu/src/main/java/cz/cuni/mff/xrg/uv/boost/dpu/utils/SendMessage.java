package cz.cuni.mff.xrg.uv.boost.dpu.utils;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dpu.DPUContext;
import org.openrdf.repository.RepositoryException;

/**
 * Utility for easy message sending.
 *
 * @author Škoda Petr
 */
public class SendMessage {

    private SendMessage() {

    }

    public static void sendMessage(DPUContext context, OperationFailedException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR, "Operation on SimpleRdf failed.", "", ex);
    }

    public static void sendMessage(DPUContext context, DataUnitException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR, "Problem with data unit.", "", ex);
    }

    public static void sendMessage(DPUContext context, RepositoryException ex) {
        context.sendMessage(DPUContext.MessageType.ERROR, "Problem with repository.", "", ex);
    }

    public static void sendMessage(DPUContext context, AddonException ex, String addonName) {
        context.sendMessage(DPUContext.MessageType.ERROR, "Problem with add-on: " + addonName, "", ex);
    }

    /**
     * Send formated message.
     *
     * @param context
     * @param type       Type of message.
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param params
     */
    public static void sendMessage(DPUContext context, DPUContext.MessageType type, String caption,
            String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        context.sendMessage(type, caption, body);
    }

    /**
     * Send formated message.
     *
     * @param context
     * @param type       Type of message.
     * @param caption    Caption ie. short message.
     * @param exception  Exception.
     * @param bodyFormat
     * @param params
     *
     */
    public static void sendMessage(DPUContext context, DPUContext.MessageType type, String caption,
            Exception exception, String bodyFormat, Object... params) {
        final String body = String.format(bodyFormat, params);
        context.sendMessage(type, caption, body, exception);
    }

    /**
     * Send formated {@link DPUContext.MessageType#ERROR} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param bodyFormat
     * @param params
     */
    public static void sendError(DPUContext context, String caption, String bodyFormat, Object... params) {
        sendMessage(context, DPUContext.MessageType.ERROR, caption, bodyFormat, params);
    }

    /**
     * Send formated {@link DPUContext.MessageType#ERROR} message.
     *
     * @param context
     * @param caption    Caption ie. short message.
     * @param exception
     * @param bodyFormat
     * @param params
     */
    public static void sendError(DPUContext context, String caption, Exception exception, String bodyFormat,
            Object... params) {
        sendMessage(context, DPUContext.MessageType.ERROR, caption, exception, bodyFormat, params);
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
        sendMessage(context, DPUContext.MessageType.WARNING, caption, exception, bodyFormat, params);
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

}
