package cz.cuni.mff.xrg.uv.extractor.sukl;

/**
 * DPU's configuration class.
 */
public class SuklConfig_V1 {

    private boolean countNumberOfMissing = false;

    private boolean failOnDownloadError = true;

    public SuklConfig_V1() {

    }

    public boolean isCountNumberOfMissing() {
        return countNumberOfMissing;
    }

    public void setCountNumberOfMissing(boolean countNumberOfMissing) {
        this.countNumberOfMissing = countNumberOfMissing;
    }

    public boolean isFailOnDownloadError() {
        return failOnDownloadError;
    }

    public void setFailOnDownloadError(boolean failOnDownloadError) {
        this.failOnDownloadError = failOnDownloadError;
    }

}
