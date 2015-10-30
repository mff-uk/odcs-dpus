package cz.cuni.mff.xrg.uv.extractor.ftp;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Report progress after 1MB.
 * @author Petr Škoda
 */
public class ProgressPrinter implements CopyStreamListener {

    private static final Logger LOG = LoggerFactory.getLogger(ProgressPrinter.class);

    int lastDownloaded = 0;

    @Override
    public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
        if (totalBytesTransferred > lastDownloaded || totalBytesTransferred > 158291900l ) {
            lastDownloaded += (1024 * 1024);
            LOG.info("Transfered: {} MB, {} B",
                    totalBytesTransferred / (1024 * 1024), totalBytesTransferred);
        }
    }

}
