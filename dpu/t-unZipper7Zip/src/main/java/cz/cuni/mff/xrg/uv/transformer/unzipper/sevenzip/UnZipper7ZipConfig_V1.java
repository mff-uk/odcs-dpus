package cz.cuni.mff.xrg.uv.transformer.unzipper.sevenzip;

/**
 * DPU's configuration class.
 */
public class UnZipper7ZipConfig_V1 {

    /**
     * If true then symbolic name of output files
     * is not prefixed with symbolic name of input file.
     */
    public boolean notPrefixed = false;

    public UnZipper7ZipConfig_V1() {
    }

    public boolean isNotPrefixed() {
        return notPrefixed;
    }

    public void setNotPrefixed(boolean notPrefixed) {
        this.notPrefixed = notPrefixed;
    }
}
