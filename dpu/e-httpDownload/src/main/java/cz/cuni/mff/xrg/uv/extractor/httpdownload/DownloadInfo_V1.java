package cz.cuni.mff.xrg.uv.extractor.httpdownload;

/**
 *
 * @author Škoda Petr
 */
public class DownloadInfo_V1 {

    /**
     * Path from which download the file.
     */
    private String uri;

    /**
     * Virtual path.
     */
    private String virtualPath;

    public DownloadInfo_V1() {
    }

    public DownloadInfo_V1(String uri) {
        this.uri = uri;
        this.virtualPath = null;
    }

    public DownloadInfo_V1(String uri, String virtualPath) {
        this.uri = uri;
        this.virtualPath = virtualPath;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVirtualPath() {
        return virtualPath;
    }

    public void setVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
    }

}
