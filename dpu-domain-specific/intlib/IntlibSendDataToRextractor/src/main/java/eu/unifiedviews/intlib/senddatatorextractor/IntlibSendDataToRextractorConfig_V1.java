package eu.unifiedviews.intlib.senddatatorextractor;

/**
 * DPU's configuration class.
 */
public class IntlibSendDataToRextractorConfig_V1 {

      private String targetRextractorServer = "http://odcs.xrg.cz/prod-rextractor";
    
    public IntlibSendDataToRextractorConfig_V1() {

    }

    public String getTargetRextractorServer() {
        return targetRextractorServer;
    }

    public void setTargetRextractorServer(String targetRextractorServer) {
        this.targetRextractorServer = targetRextractorServer;
    }
    

    
    

}
