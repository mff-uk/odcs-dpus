package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import cz.cuni.mff.xrg.uv.boost.dpu.config.VersionedConfig;
import java.net.URL;

/**
 *
 * @author Škoda Petr
 */
public class HttpDownloadConfig_V1 implements VersionedConfig<HttpDownloadConfig_V2> {

    private URL URL = null;

    private String target = "/file";

    /**
     * Number of attempts to try before failure, -1 for infinite.
     */
    private int retryCount = -1;

    private int retryDelay = 1000;

    public URL getURL() {
        return URL;
    }

    public void setURL(URL URL) {
        this.URL = URL;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public void setRetryDelay(int retryDelay) {
        this.retryDelay = retryDelay;
    }

    @Override
    public HttpDownloadConfig_V2 toNextVersion() {
        final HttpDownloadConfig_V2 result = new HttpDownloadConfig_V2();

        result.getToDownload().add(new DownloadInfo_V1(URL.toString(), target));

        return result;
    }
}
