package cz.cuni.mff.xrg.uv.transformer.check.rdfemtpy;

import eu.unifiedviews.dpu.DPUContext;

/**
 * DPU's configuration class.
 */
public class RdfEmptyConfig_V1 {

    public static final String AUTO_MESSAGE = "Check failed: Input dataUnit is not empty!";

    private DPUContext.MessageType messageType = DPUContext.MessageType.ERROR;
    
    private String message = null;

    public RdfEmptyConfig_V1() {

    }

    public DPUContext.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(DPUContext.MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
