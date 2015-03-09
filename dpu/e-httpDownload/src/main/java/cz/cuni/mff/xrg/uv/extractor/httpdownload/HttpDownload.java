package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.CachedFileDownloader;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class HttpDownload extends AbstractDpu<HttpDownloadConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpDownload.class);

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", optional = true, description = "DPU's configuration.")
    public RDFDataUnit inRdfToDownload;

    @DataUnit.AsOutput(name = "files", description = "Downloaded files.")
    public WritableFilesDataUnit outFilesFiles;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;
    
    @ExtensionInitializer.Init(param = "outFilesFiles")
    public WritableSimpleFiles outFiles;

    @ExtensionInitializer.Init
    public CachedFileDownloader fileDownloader;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

    public HttpDownload() {
        super(HttpDownloadVaadinDialog.class, ConfigHistory.noHistory(HttpDownloadConfig_V2.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "{0} file to download", config.getToDownload().size());

        int index = 0;
        for (DownloadInfo_V1 info : config.getToDownload()) {
            LOG.info("Downloading ({}/{}) : '{}' ", index++, config.getToDownload().size(), info.getUri());
            URL url;
            try {
                url = new URL(info.getUri());
            } catch (MalformedURLException ex) {
                LOG.error("Wrong URI format: {}", info.getUri(), ex);
                continue;
            }
            // Prepare virtual path.
            String virtualPath = info.getVirtualPath();
            if (virtualPath == null || virtualPath.isEmpty()) {
                // Just use something as virtual path.
                final String uriTail = info.getUri().substring(info.getUri().lastIndexOf("/") + 1);
                try {
                    virtualPath = URLEncoder.encode(uriTail, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    throw ContextUtils.dpuException(ctx, ex, "UTF-8 is not supported!");
                }
            }
            // Download file.
            try {
                downloadFile(url, virtualPath);
            } catch (ExtensionException | IOException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Can''t download: {0}", info.getUri());
            }
        }

    }

    /**
     * Download file and store it into output {@link #outFilesFiles}.
     *
     * @param sourceUri
     * @param fileName
     * @throws ExtensionException
     * @throws DPUException
     * @throws IOException
     */
    private void downloadFile(URL sourceUri, String fileName) throws ExtensionException, DPUException, IOException {
        final File file = fileDownloader.get(sourceUri);
        outFiles.add(file, fileName);
    }

}
