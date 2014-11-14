package cz.cuni.mff.xrg.uv.extractor.httpdownload;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.ConfigurationFromRdf;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.Manipulator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class HttpDownload extends DpuAdvancedBase<HttpDownloadConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpDownload.class);

    @DataUnit.AsInput(name = "config", optional = true, description = "DPU's configuration.")
    public RDFDataUnit inRdfToDownload;

    @DataUnit.AsOutput(name = "files", description = "Downloaded files.")
    public WritableFilesDataUnit outFilesFiles;

    public HttpDownload() {
        super(ConfigHistory.create(HttpDownloadConfig_V1.class,
                "eu.unifiedviews.plugins.extractor.httpdownload.HttpDownloadConfig_V1")
                .addCurrent(HttpDownloadConfig_V2.class),
                AddonInitializer.create(new CachedFileDownloader(),
                        new ConfigurationFromRdf("inRdfToDownload")));
    }

    @Override
    protected void innerExecute() throws DPUException {
        context.sendMessage(DPUContext.MessageType.INFO,
                String.format("%d file(s) to download", config.getToDownload().size()));

        int index = 0;
        int downloaded = 0;
        for (DownloadInfo_V1 info : config.getToDownload()) {
            LOG.info("Downloading file number: {} from: {}", index, info.getUri());
            URL url;
            try {
                url = new URL(info.getUri());
            } catch (MalformedURLException ex) {
                SendMessage.sendError(context, "Download failed", ex, "Wrong URI format: %s", info.getUri());
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
                    context.sendMessage(DPUContext.MessageType.ERROR, "UTF-8 is not supported!", "", ex);
                    return;
                }
            }
            // Download file.
            try {
                downloadFile(url, virtualPath, virtualPath);
                ++downloaded;
            } catch (DataUnitException ex) {
                SendMessage.sendMessage(context, ex);
                return;
            } catch (AddonException | IOException ex) {
                SendMessage.sendError(context, "Download failed", ex, "File: %s", info.getUri());
                return;
            }
        }
        SendMessage.sendInfo(context, String.format("%d file(s) have been downloaded", downloaded), "");
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new HttpDownloadVaadinDialog();
    }

    /**
     * Download file and store it into output {@link #outFilesFiles}.
     *
     * @param sourceUri
     * @param symbolicName
     * @param virtualPath
     * @throws AddonException
     * @throws IOException
     * @throws DataUnitException
     */
    private void downloadFile(URL sourceUri, String symbolicName, String virtualPath)
            throws AddonException, IOException, DataUnitException {
        final File file = getAddon(CachedFileDownloader.class).get(sourceUri);
        outFilesFiles.addExistingFile(symbolicName, file.toURI().toString());

        // TODO We can add more metadata here ...
        Manipulator.set(outFilesFiles, symbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH, virtualPath);
    }

}
