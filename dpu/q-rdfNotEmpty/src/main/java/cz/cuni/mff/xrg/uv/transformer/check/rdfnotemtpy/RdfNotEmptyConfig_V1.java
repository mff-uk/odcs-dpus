package cz.cuni.mff.xrg.uv.transformer.check.rdfnotemtpy;

import eu.unifiedviews.dpu.DPUContext;

/**
 * DPU's configuration class.
 */
public class RdfNotEmptyConfig_V1 {

    public static final String AUTO_MESSAGE = "Check failed: Input dataUnit is empty!";

    private DPUContext.MessageType messageType = DPUContext.MessageType.ERROR;
    
    private String message = null;

    public RdfNotEmptyConfig_V1() {

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
