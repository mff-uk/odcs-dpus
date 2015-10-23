package cz.cuni.mff.xrg.uv.extractor.sukl;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import eu.unifiedviews.dpu.DPUContext;

import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import eu.unifiedviews.helpers.dpu.config.ConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractExtensionDialog;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.Configurable;
import eu.unifiedviews.dpu.config.DPUConfigException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.Context;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.exec.ExecContext;
import eu.unifiedviews.helpers.dpu.extension.Extension;

/**
 * Main functionality:
 * <ul>
 * <li>Download files from given URL</li>
 * <li>Contains optional simple file cache</li>
 * <li>User can specify the pause between downloads</li>
 * </ul>
 *
 * @see cz.cuni.mff.xrg.uv.boost.dpu.addonAddon
 * @author Škoda Petr
 */
public class CachedFileDownloader implements Extension, Extension.Executable,
        Configurable<CachedFileDownloader.Configuration_V1> {

    public static final String CACHE_FILE = "cacheContent.xml";

    public static final String USED_CONFIG_NAME = "addon/cachedFileDownloader";

    public static final String ADDON_NAME = "Downloader";

    private static final Logger LOG = LoggerFactory.getLogger(CachedFileDownloader.class);

    public static enum ResultType {

        /**
         * File downloaded.
         */
        DOWNLOADED,
        /**
         * We tried to download file but we failed.
         */
        ERROR,
        /**
         * File is presented in cache.
         */
        CACHED,
        /**
         * File is missing and we have no more download free slots.
         */
        MISSING
    }

    public static class DownloadResult {

        private final File file;

        private final ResultType type;

        public DownloadResult(File file, ResultType type) {
            this.file = file;
            this.type = type;
        }

        public File getFile() {
            return file;
        }

        public ResultType getType() {
            return type;
        }

    }

    /**
     * Configuration class.
     */
    public static class Configuration_V1 {

        /**
         * Max number of attempts to download single file.
         */
        private Integer maxAttemps = 2;

        /**
         * Min pause between download of two files.
         */
        private Integer minPause = 37000;

        /**
         * Max pause between download of two files.
         */
        private Integer maxPause = 50000;

        /**
         * If true then data are always downloaded and existing data in caches are rewritten.
         */
        private boolean rewriteCache = false;

        private Integer maxDownloads = 100;

        /**
         * Path to cache directory.
         */
        private String cacheDirectory = "";

        public Configuration_V1() {
        }

        public Integer getMaxAttemps() {
            return maxAttemps;
        }

        public void setMaxAttemps(Integer maxAttemps) {
            this.maxAttemps = maxAttemps;
        }

        public Integer getMinPause() {
            return minPause;
        }

        public void setMinPause(Integer minPause) {
            this.minPause = minPause;
        }

        public Integer getMaxPause() {
            return maxPause;
        }

        public void setMaxPause(Integer maxPause) {
            this.maxPause = maxPause;
        }

        public Boolean isRewriteCache() {
            return rewriteCache;
        }

        public void setRewriteCache(Boolean rewriteCache) {
            this.rewriteCache = rewriteCache;
        }

        public Integer getMaxDownloads() {
            return maxDownloads;
        }

        public void setMaxDownloads(Integer maxDownloads) {
            this.maxDownloads = maxDownloads;
        }

        public String getCacheDirectory() {
            return cacheDirectory;
        }

        public void setCacheDirectory(String cacheDirectory) {
            this.cacheDirectory = cacheDirectory;
        }

    }

    /**
     * Vaadin configuration dialog.
     */
    public class VaadinDialog extends AbstractExtensionDialog<Configuration_V1> {

        private TextField txtMaxAttemps;

        private TextField txtMaxPause;

        private TextField txtMinPause;

        private CheckBox checkRewriteCache;

        private TextField txtMaxDownloads;

        private TextField txtPathToCache;

        public VaadinDialog() {
            super(configHistory);
        }

        @Override
        protected String getConfigClassName() {
            return USED_CONFIG_NAME;
        }

        @Override
        public void buildLayout() {
            final VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);

            txtMaxAttemps = new TextField(
                    "Max number of attemps to download a single file, use -1 for infinity");
            txtMaxAttemps.setDescription("Set to 0 to use only files from cache");
            txtMaxAttemps.setWidth("5em");
            txtMaxAttemps.setRequired(true);
            mainLayout.addComponent(txtMaxAttemps);

            txtMaxPause = new TextField("Max pause in ms between downloads");
            txtMaxPause.setWidth("10em");
            txtMaxPause.setRequired(true);
            mainLayout.addComponent(txtMaxPause);

            txtMinPause = new TextField("Min pause in ms between downloads");
            txtMinPause.setWidth("10em");
            txtMinPause.setRequired(true);
            mainLayout.addComponent(txtMinPause);

            checkRewriteCache = new CheckBox("Rewrite cache");
            checkRewriteCache.setDescription(
                    "If checked then files are always downloaded and existing files in caches are rewritten.");
            mainLayout.addComponent(checkRewriteCache);

            txtMaxDownloads = new TextField("Max number of files to download.");
            txtMaxDownloads.setWidth("5em");
            txtMaxDownloads.setRequired(true);
            mainLayout.addComponent(txtMaxDownloads);

            txtPathToCache = new TextField("Path to cache.");
            txtPathToCache.setWidth("30em");
            txtPathToCache.setRequired(true);
            mainLayout.addComponent(txtPathToCache);

            setCompositionRoot(mainLayout);
        }

        @Override
        protected void setConfiguration(Configuration_V1 c) throws DPUConfigException {
            txtMaxAttemps.setValue(c.getMaxAttemps().toString());
            txtMaxPause.setValue(c.getMaxPause().toString());
            txtMinPause.setValue(c.getMinPause().toString());
            checkRewriteCache.setValue(c.isRewriteCache());
            txtMaxDownloads.setValue(c.getMaxDownloads().toString());
            txtPathToCache.setValue(c.getCacheDirectory());
        }

        @Override
        protected Configuration_V1 getConfiguration() throws DPUConfigException {
            if (!txtMaxAttemps.isValid() || !txtMaxPause.isValid() || !txtMinPause.isValid()) {
                throw new DPUConfigException("All values for " + ADDON_NAME + " must be provided.");
            }

            final Configuration_V1 c = new Configuration_V1();

            try {
                c.setMaxAttemps(Integer.parseInt(txtMaxAttemps.getValue()));
                c.setMaxPause(Integer.parseInt(txtMaxPause.getValue()));
                c.setMinPause(Integer.parseInt(txtMinPause.getValue()));
                c.setMaxDownloads(Integer.parseInt(txtMaxDownloads.getValue()));
            } catch (NumberFormatException ex) {
                throw new ConfigException("Provided valuas must be numbers.", ex);
            }

            if (c.getMaxPause() < c.getMinPause()) {
                throw new ConfigException("Max pause must be greater then min pause.");
            }

            c.setRewriteCache(checkRewriteCache.getValue());
            c.setCacheDirectory(txtPathToCache.getValue());
            return c;
        }

    }

    /**
     * Used configuration.
     */
    private Configuration_V1 config = new Configuration_V1();

    private final ConfigHistory<Configuration_V1> configHistory = ConfigHistory.noHistory(Configuration_V1.class);

    /**
     * Time of next download. Used to create randomly distributes pauses between downloads. Should be ignored if cache
     * is used.
     */
    private long nextDownload = new Date().getTime();

    /**
     * Base directory where store files.
     */
    private File baseDirectory = null;

    private Context context;

    private DPUContext contextDpu;

    private int numberOfDownloads = 0;

    public CachedFileDownloader() {
        LOG.info("CustomCachedFileDownloader");
    }

    @Override
    public void preInit(String param) throws DPUException {
        // No-op.
    }

    @Override
    public void afterInit(Context context) {
        this.context = context;
        if (context instanceof ExecContext) {
            this.contextDpu = ((ExecContext) context).getDpuContext();
        }
    }

    @Override
    public void execute(ExecutionPoint execPoint) throws ExtensionException {
        if (execPoint != ExecutionPoint.PRE_EXECUTE) {
            return;
        }

        // Prepare cache directory.
        try {
            // Load configuration.
            this.config = context.getConfigManager().get(USED_CONFIG_NAME, configHistory);
        } catch (ConfigException ex) {
            ContextUtils.sendWarn(context.asUserContext(), "Addon failed to load configuration", ex,
                    "Failed to load configuration for: {0} default configuration is used.", ADDON_NAME);
            this.config = new Configuration_V1();
        }

        if (this.config == null) {
            ContextUtils.sendWarn(context.asUserContext(), "Addon failed to load configuration",
                    "Failed to load configuration for: {0} default configuration is used.", ADDON_NAME);
            this.config = new Configuration_V1();
        }

        this.baseDirectory = new File(config.cacheDirectory);
        this.baseDirectory.mkdirs();
        LOG.info("Cache directory: {}", baseDirectory);
    }

    @Override
    public Class<CachedFileDownloader.Configuration_V1> getConfigClass() {
        return CachedFileDownloader.Configuration_V1.class;
    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AbstractExtensionDialog<Configuration_V1> getDialog() {
        return new VaadinDialog();
    }

    /**
     * Downloaded given file and store it into a cache. If file is presented in cache the is returned.
     *
     * @param fileUrl
     * @return
     * @throws ExtensionException Is thrown in case of wrong URL format.
     * @throws IOException
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public DownloadResult get(String fileUrl) throws ExtensionException, IOException, DPUException {
        try {
            return get(new URL(fileUrl));
        } catch (MalformedURLException e) {
            throw new ExtensionException("Wrong URL.", e);
        }
    }

    /**
     * Downloaded given file and store it into a cache. If file is presented in cache the is returned.
     *
     * @param fileUrl
     * @return
     * @throws ExtensionException Is thrown in case of wrong URL format.
     * @throws IOException
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public DownloadResult get(URL fileUrl) throws ExtensionException, IOException, DPUException {
        return get(fileUrl.toString(), fileUrl);
    }

    /**
     * If file of given name exists, then it's returned. If not then is downloaded from given URL, saved under given
     * name and then returned.
     *
     * @param fileName Must not be null. Unique identification for the given file.
     * @param fileUrl
     * @return Null if max number to download exceed given limit.
     * @throws ExtensionException
     * @throws IOException
     * @throws eu.unifiedviews.dpu.DPUException
     */
    public DownloadResult get(String fileName, URL fileUrl) throws ExtensionException, IOException, DPUException {
        if (baseDirectory == null) {
            throw new ExtensionException("Not initialized!");
        }
        // Made name secure, so we can use it as a file name.
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Hard coded encoding is not supported!!", ex);
        }
        // Get file name.
        final File file;
        file = getFileNameFromSimpleCache(fileName, fileUrl);

        // Check cache.
        if (file.exists() && !config.rewriteCache) {
            return new DownloadResult(file, ResultType.CACHED);
        }

        // Check if we should download file.
        if (config.maxAttemps == 0 || numberOfDownloads >= config.maxDownloads) {
            return new DownloadResult(file, ResultType.MISSING);
        }

        // Download file with some level of fault tolerance.
        int attempCounter = config.maxAttemps;
        while (attempCounter != 0) {
            // Wait before download.
            waitForNextDownload();
            // Try to download file.
            try {
                FileUtils.copyURLToFile(fileUrl, file);
                ++numberOfDownloads;
                return new DownloadResult(file, ResultType.DOWNLOADED);
            } catch (IOException ex) {
                LOG.warn("Failed to download file from {} attemp {}/{}", fileUrl.toString(), attempCounter,
                        config.maxAttemps, ex);
            }
            // Decrease attemp counted if not set to infinity = -1.
            if (attempCounter > 0) {
                --attempCounter;
            }
            if (contextDpu.canceled()) {
                // Execution has been canceled.
                throw ContextUtils.dpuExceptionCancelled(context.asUserContext());
            }
        }
        return new DownloadResult(null, ResultType.ERROR);
    }

    /**
     * Wait before next download. Before leaving set time for next download.
     */
    private void waitForNextDownload() {
        while ((new Date()).getTime() < nextDownload && !contextDpu.canceled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {

            }
        }
        // Determine min time for next download, ie. time when next file can be downloaded.
        nextDownload = new Date().getTime()
                + (long) (Math.random() * (config.maxPause - config.minPause) + config.minPause);
    }

    private File getFileNameFromSimpleCache(String fileName, URL fileUrl) {
        return new File(baseDirectory, fileName);
    }

}
