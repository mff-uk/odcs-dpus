package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import java.util.LinkedList;
import java.util.List;

/**
 * DPU's configuration class.
 */
public class HttpDownloadConfig_V2 {

    private List<DownloadInfo_V1> toDownload = new LinkedList<>();

    public HttpDownloadConfig_V2() {

    }

    public List<DownloadInfo_V1> getToDownload() {
        return toDownload;
    }

    public void setToDownload(List<DownloadInfo_V1> toDownload) {
        this.toDownload = toDownload;
    }
    
}
