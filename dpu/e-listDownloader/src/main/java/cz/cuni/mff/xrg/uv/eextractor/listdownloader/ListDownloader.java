package cz.cuni.mff.xrg.uv.eextractor.listdownloader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.files.CachedFileDownloader;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class ListDownloader extends AbstractDpu<ListDownloaderConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(ListDownloader.class);

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit filesOutput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init
    public CachedFileDownloader fileCache;

	public ListDownloader() {
		super(ListDownloaderVaadinDialog.class, ConfigHistory.noHistory(ListDownloaderConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Donwload page -> add to output.
        int downloaded = 0;
        int pageCounter = config.getStartIndex();
        while (!ctx.canceled()) {
            final String pageUrlStr = config.getPagePattern().replace("{}", Integer.toString(pageCounter));
            final String fileNameStr = String.format("Page-%d", pageCounter);
            final URL pageUrl;
            try {
                pageUrl = new URL(pageUrlStr);
            } catch (MalformedURLException ex) {
                throw new DPUException(ex);
            }
            LOG.info("Downloading from '{}' as '{}'", pageUrlStr, fileNameStr);
            // Download file.
            final FilesDataUnit.Entry fileEntry = downloadFile(pageUrl, fileNameStr);
            // Move to next index.
            ++pageCounter;
            ++downloaded;
            // Check for next page.
            if (!downloadNext(fileEntry)) {
                break;
            }
        }
        // Print message.
        ContextUtils.sendShortInfo(ctx, "Downloaded {0} pages.", downloaded);
    }

    /**
     * Download file and store it into output {@link #outFilesFiles}.
     *
     * @param sourceUrl
     * @param fileName
     * @return Entry for downloaded file.
     * @throws DPUException
     */
    private FilesDataUnit.Entry downloadFile(URL sourceUrl, final String fileName) throws DPUException {
        final File file;
        try {
            file = fileCache.get(sourceUrl);
        } catch (ExtensionException | IOException ex) {
            throw new DPUException(ex);
        }
        // Add to output.
        return faultTolerance.execute(new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

            @Override
            public FilesDataUnit.Entry action() throws Exception {
                return FilesDataUnitUtils.addFile(filesOutput, file, fileName);
            }
        });
    }

    /**
     * Check if next page should be downloaded.
     *
     * @param fileEntry
     * @return True if next page in pattern should be downloaded.
     * @throws DPUException
     */
    private boolean downloadNext(FilesDataUnit.Entry fileEntry) throws DPUException {
        final File file = FaultToleranceUtils.asFile(faultTolerance, fileEntry);
        final Document doc;
        try {
            doc = Jsoup.parse(file, null);
        } catch (IOException ex) {
            throw new DPUException(ex);
        }
        // All conditions must hold.
        for (ListDownloaderConfig_V1.NextPageCondition condition : config.getNextPageConditions()) {
            final Elements elemetns = doc.select(condition.getNextButtonSelector());
             if (elemetns.isEmpty()) {
                 LOG.info("Required element is not presented: {}", condition.getNextButtonSelector());
                 return false;
             }
        }
        return true;
    }

}