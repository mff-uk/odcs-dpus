package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.CancelledException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.ExecutableAddon;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.ConfigurableAddon;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import javax.net.ssl.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;

/**
 * Main functionality:
 * <ul>
 *  <li>Download files from given URL</li>
 *  <li>Contains optional simple file cache</li>
 *  <li>User can specify the pause between downloads</li>
 * </ul>
 *
 * TODO: Should use versioned configuration. The configuration class should be renamed to "Configuration_V1".
 *
 * @see cz.cuni.mff.xrg.uv.boost.dpu.addonAddon
 * @author Škoda Petr
 */
public class CachedFileDownloader
    implements ExecutableAddon, ConfigurableAddon<CachedFileDownloader.Configuration> {

    public static final String USED_USER_DIRECTORY = "addon/cachedFileDownloader";

    public static final String USED_CONFIG_NAME = "addon/cachedFileDownloader";

    public static final String ADDON_NAME = "Cached file downloader";

    private static final Logger LOG = LoggerFactory.getLogger(CachedFileDownloader.class);

    /**
     * Configuration class.
     */
    public static class Configuration {

        /**
         * Max number of attempts to download single file.
         */
        private Integer maxAttemps = 10;

        /**
         * Min pause between download of two files.
         */
        private Integer minPause = 1000;

        /**
         * Max pause between download of two files.
         */
        private Integer maxPause = 2000;

        /**
         * If true then data are always downloaded and existing data in caches
         * are rewritten.
         */
        private Boolean rewriteCache = false;

        public Configuration() {
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

    }

    /**
     * Vaadin configuration dialog.
     */
    public class VaadinDialog extends AddonVaadinDialogBase<Configuration> {

        private TextField txtMaxAttemps;

        private TextField txtMaxPause;

        private TextField txtMinPause;

        private CheckBox checkRewriteCache;

        public VaadinDialog() {
            super(Configuration.class);
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

            txtMaxAttemps = new TextField("Max number of attemps to download a single file, use -1 for infinity");
            txtMaxAttemps.setDescription("Set to 0 to use only files from cache.");
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
            checkRewriteCache.setDescription("If checked then files are always downloaded and existing files in caches are rewritten.");
            mainLayout.addComponent(checkRewriteCache);

            setCompositionRoot(mainLayout);
        }

        @Override
        protected void setConfiguration(Configuration c) throws DPUConfigException {
            txtMaxAttemps.setValue(c.getMaxAttemps().toString());
            txtMaxPause.setValue(c.getMaxPause().toString());
            txtMinPause.setValue(c.getMinPause().toString());
            checkRewriteCache.setValue(c.isRewriteCache());
        }

        @Override
        protected Configuration getConfiguration() throws DPUConfigException {
            if (!txtMaxAttemps.isValid() || !txtMaxPause.isValid() || !txtMinPause.isValid()) {
                throw new DPUConfigException("All values for " + ADDON_NAME + " must be provided.");
            }

            final Configuration c = new Configuration();

            try {
                c.setMaxAttemps(Integer.parseInt(txtMaxAttemps.getValue()));
                c.setMaxPause(Integer.parseInt(txtMaxPause.getValue()));
                c.setMinPause(Integer.parseInt(txtMinPause.getValue()));
            } catch (NumberFormatException ex) {
                throw new ConfigException("Provided valuas must be numbers.", ex);
            }

            if (c.getMaxPause() < c.getMinPause()) {
                throw new ConfigException("Max pause must be greater then min pause.");
            }

            c.setRewriteCache(checkRewriteCache.getValue());
            return c;
        }

    }

    /**
     * Used configuration.
     */
    private Configuration config = new Configuration();

    /**
     * Time of next download. Used to create randomly distributes pauses between downloads.
     * Should be ignored if cache is used.
     */
    private long nextDownload = new Date().getTime();

    /**
     * Base directory where store files.
     */
    private File baseDirectory = null;

    /**
     * DPU's master context.
     */
    private DpuAdvancedBase.Context context;

    public CachedFileDownloader() {
    }

    @Override
    public void init(DpuAdvancedBase.Context context) {
        this.context = context;
    }

    @Override
    public void init(AdvancedVaadinDialogBase.Context context) {
        // Do nothing here.
    }

    @Override
    public void execute(ExecutionPoint execPoint) throws AddonException {

        if (execPoint != ExecutionPoint.PRE_EXECUTE) {
            return;
        }
        // Prepare cache directory.
        final DPUContext dpuContext = context.getDpuContext();
        this.baseDirectory = new File(dpuContext.getUserDirectory(), USED_USER_DIRECTORY);
        try {
            // Load configuration.
            this.config = context.getConfigManager().get(USED_CONFIG_NAME, Configuration.class);
        } catch (ConfigException ex) {
            dpuContext.sendMessage(DPUContext.MessageType.WARNING, "Addon failed to load configuration",
                    "Failed to load configuration for: " + ADDON_NAME + " default configuration is used.", ex);
            this.config  = new Configuration();
        }

        if (this.config == null) {
            dpuContext.sendMessage(DPUContext.MessageType.WARNING, "Addon configuration is null.",
                    "Failed to load configuration for: " + ADDON_NAME + " default configuration is used.");
            this.config  = new Configuration();
        }

        LOG.info("BaseDirectory: {}", baseDirectory);

        // Ignore all certificates, added because of MICR_3 pipeline.
        // TODO: should be more focused on current job, not generaly remove the ssh check!
        try {
            setTrustAllCerts();
        } catch (Exception ex) {
            throw new AddonException("setTrustAllCerts throws", ex);
        }
    }

    @Override
    public Class<CachedFileDownloader.Configuration> getConfigClass() {
        return CachedFileDownloader.Configuration.class;
    }

    @Override
    public String getDialogCaption() {
        return ADDON_NAME;
    }

    @Override
    public AddonVaadinDialogBase<Configuration> getDialog() {
        return new VaadinDialog();
    }

    /**
     * Downloaded given file and store it into a cache. If file is presented in cache the is returned.
     *
     * @param fileUrl
     * @return
     * @throws AddonException Is thrown in case of wrong URL format.
     * @throws IOException
     */
    public File get(String fileUrl) throws AddonException, IOException {
        try {
            return get(new URL(fileUrl));
        } catch (MalformedURLException e) {
            throw new AddonException("Wrong URL.", e);
        }
    }

    /**
     * Downloaded given file and store it into a cache. If file is presented in cache the is returned.
     *
     * @param fileUrl
     * @return
     * @throws AddonException Is thrown in case of wrong URL format.
     * @throws IOException
     */
    public File get(URL fileUrl) throws AddonException, IOException {
        return get(fileUrl.toString(), fileUrl);
    }

    /**
     * If file of given name exists, then it's returned. If not then is downloaded from given URL,
     * saved under given name and then returned.
     *
     * @param fileName
     * @param fileUrl
     * @return Can return null!
     * @throws AddonException
     * @throws IOException
     */
    public File get(String fileName, URL fileUrl) throws AddonException, IOException {
        if (baseDirectory == null) {
            throw new AddonException("Not initialized!");
        }
        // Made name secure, so we can use it as a file name.
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Hard coded encoding is not supported!!", ex);
        }
        // Check for file existance, ie. can we use cached file?
        final File file = new File(baseDirectory, fileName);
        LOG.debug("file: {} rewrite: {}", file.toString(), config.rewriteCache);
        if (file.exists() && !config.rewriteCache) {
            LOG.info("get({}, {}) - file from cache ", fileName, fileUrl.toString());
            return file;
        }
        // Check if we should download file.
        if (config.maxAttemps == 0) {
            LOG.info("No file found for: {}, {}", fileName, fileUrl);
            return null;
        }
        // Download file with some level of fault tolerance.
        int attempCounter = config.maxAttemps;
        while (attempCounter != 0 && !context.getDpuContext().canceled()) {
            // Wait before download.
            waitForNextDownload();
            // Try to download file.
            try {
                FileUtils.copyURLToFile(fileUrl, file);
                LOG.info("get({}, {}) - file downloaded ", fileName, fileUrl.toString());
                return file;
            } catch (IOException ex) {
                LOG.warn("Failed to download file from {} attemp {}/{}", fileUrl.toString(), attempCounter,
                        config.maxAttemps, ex);
            }
            // Decrease attemp counted if not set to infinity = -1.
            if (attempCounter > 0) {
                --attempCounter;
            }
        }
        // If we are here we does not manage to download file. So check for the reason.
        if (context.getDpuContext().canceled()) {
            // Execution has been canceled.
            throw new CancelledException();
        } else {
            // We were unable to download file in given number of attemps, we have faild.
            throw new IOException("Can't obtain file: '" + fileUrl.toString() + "' named: '" + fileName + "'");
        }
    }

    /**
     * Get all given files and store them in a cache. The files are downloaded
     * in given order.
     *
     * @param uris
     */
    public void get(List<URL> urls) throws AddonException, IOException {
        for (URL url : urls) {
            get(url);
        }
    }

    /**
     * Wait before next download. Before leaving set time for next download.
     */
    private void waitForNextDownload() {
        while ((new Date()).getTime() < nextDownload && !context.getDpuContext().canceled()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {

            }
        }
        // Determine min time for next download, ie. time when next file can be downloaded.
        nextDownload = new Date().getTime() +
                (long) (Math.random() * (config.maxPause - config.minPause) + config.minPause);
    }

    /**
     * We will trust all certificates!
     *
     * Code source is MICR_3 DPU.
     * TODO: Do not trust all certificates globally.
     * 
     * @throws Exception
     */
    public static void setTrustAllCerts() throws Exception
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                @Override
                public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType ) {    }
                @Override
                public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType ) {    }
            }
        };

        // Install the all-trusting trust manager.
        try {
            SSLContext sc = SSLContext.getInstance( "SSL" );
            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(
                new HostnameVerifier() {
                    @Override
                    public boolean verify(String urlHostName, SSLSession session) {
                        return true;
                    }
                });
        }
        catch (KeyManagementException | NoSuchAlgorithmException ex) {
            LOG.error("Can't install all-trusting trus manager", ex);
        }
    }

}
