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
import eu.unifiedviews.helpers.cuni.dpu.addon.AddonException;
import eu.unifiedviews.helpers.cuni.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.cuni.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.cuni.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.cuni.dpu.exec.AutoInitializer;
import eu.unifiedviews.helpers.cuni.extensions.CachedFileDownloader;
import eu.unifiedviews.helpers.cuni.extensions.FaultTolerance;
import eu.unifiedviews.helpers.cuni.extensions.FaultToleranceUtils;
import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class ListDownloader extends AbstractDpu<ListDownloaderConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(ListDownloader.class);

    @DataUnit.AsInput(name = "output")
    public WritableFilesDataUnit filesOutput;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    @AutoInitializer.Init
    public CachedFileDownloader fileCache;

	public ListDownloader() {
		super(ListDownloaderVaadinDialog.class, ConfigHistory.noHistory(ListDownloaderConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Donwload page -> add to output.
        int pageCounter = config.getStartIndex();
        while (!ctx.canceled()) {
            final String pageUrlStr = String.format(config.getPagePattern(), pageCounter);
            final String fileNameStr = String.format("Vysledky-RIV-%d", pageCounter);
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
            // Check for next page.
            if (!downloadNext(fileEntry)) {
                break;
            }

            // TODO Removed, for debug purpose only
            if (pageCounter > 3) {
                break;
            }

        }
        // Print message.
        ContextUtils.sendShortInfo(ctx, "Downloaded %d pages.", pageCounter);
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
        } catch (AddonException | IOException ex) {
            throw new DPUException(ex);
        }
        // Add to output.
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                FilesDataUnitUtils.addFile(filesOutput, file, fileName);
            }
        });
        return null;
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
        // All conitions must hold.
        for (ListDownloaderConfig_V1.NextPageCondition condition : config.getNextPageConditions()) {
            final Elements elemetns = doc.select(condition.getNextButtonSelector());
             if (elemetns.isEmpty()) {
                 LOG.info("Condition does not hold: {}", condition.getNextButtonSelector());
                 return false;
             }
        }
        return true;
    }

}