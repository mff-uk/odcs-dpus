package cz.cuni.mff.xrg.uv.extractor.ftp;

import java.util.LinkedList;
import java.util.List;

import eu.unifiedviews.helpers.dpu.ontology.EntityDescription;

/**
 * DPU's configuration class.
 */
@EntityDescription.Entity(type = FtpVocabulary.STR_CONFIG_CLASS)
public class FtpConfig_V1 {

    @EntityDescription.Property(uri = FtpVocabulary.STR_CONFIG_HAS_FILE)
    private List<DownloadInfo_V1> toDownload = new LinkedList<>();

    private boolean usePassiveMode = true;

    private boolean useBinaryMode = false;

    private int keepAliveControl = 0;

    public FtpConfig_V1() {

    }

    public List<DownloadInfo_V1> getToDownload() {
        return toDownload;
    }

    public void setToDownload(List<DownloadInfo_V1> toDownload) {
        this.toDownload = toDownload;
    }

    public boolean isUsePassiveMode() {
        return usePassiveMode;
    }

    public void setUsePassiveMode(boolean usePassiveMode) {
        this.usePassiveMode = usePassiveMode;
    }

    public boolean isUseBinaryMode() {
        return useBinaryMode;
    }

    public void setUseBinaryMode(boolean useBinaryMode) {
        this.useBinaryMode = useBinaryMode;
    }

    public int getKeepAliveControl() {
        return keepAliveControl;
    }

    public void setKeepAliveControl(int keepAliveControl) {
        this.keepAliveControl = keepAliveControl;
    }

}
