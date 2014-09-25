package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.CancelledException;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonVaadinDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonWithVaadinDialog;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide cache for files.
 *
 * @author Škoda Petr
 */
public class CachedFileDownloader
        implements AddonWithVaadinDialog<CachedFileDownloader.Configuration> {

    public static final String USED_USER_DIRECTORY
            = "addon/cachedFileDownloader";

    public static final String USED_CONFIG_NAME
            = "addon/cachedFileDownloader";

    public static final String ADDON_NAME
            = "Cached file downloader";

    private static final Logger LOG = LoggerFactory.getLogger(
            CachedFileDownloader.class);

    /**
     * Addons' configuration.
     */
    public static class Configuration {

        /**
         * Max number of attempts to download singe file.
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
     * Addon's configuration dialog.
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
            if (!txtMaxAttemps.isValid() || !txtMaxPause.isValid()
                    || !txtMinPause.isValid()) {
                throw new DPUConfigException("All values for " + ADDON_NAME
                        + " must be provided.");
            }

            final Configuration c = new Configuration();

            try {
                c.setMaxAttemps(Integer.parseInt(txtMaxAttemps.getValue()));
                c.setMaxPause(Integer.parseInt(txtMaxPause.getValue()));
                c.setMinPause(Integer.parseInt(txtMinPause.getValue()));
            } catch (NumberFormatException ex) {
                throw new ConfigException("Provided valuas must be numbers",
                        ex);
            }

            if (c.getMaxPause() < c.getMinPause()) {
                throw new ConfigException(
                        "max pause must be greater then min pause");
            }

            c.setRewriteCache(checkRewriteCache.getValue());

            return c;
        }

    }

    /**
     * Addon's configuration.
     */
    private Configuration config = new Configuration();

    /**
     * Context of our master DPU.
     */
    private DPUContext dpuContext = null;

    /**
     * Time of next download.
     */
    private long nextDownload = (new Date()).getTime();

    /**
     * Base directory where store files.
     */
    private File baseDirectory = null;

    public CachedFileDownloader() {
    }

    @Override
    public boolean preAction(DpuAdvancedBase.Context context) {
        this.dpuContext = context.getDpuContext();
        this.baseDirectory = new File(this.dpuContext.getUserDirectory(),
                USED_USER_DIRECTORY);
        try {
            // load configuration
            this.config = context.getConfigManager().get(USED_CONFIG_NAME,
                    Configuration.class);
        } catch (ConfigException ex) {
            this.dpuContext.sendMessage(DPUContext.MessageType.WARNING,
                    "Addon failed to load configuration",
                    "Failed to load configuration for: " + ADDON_NAME
                    + " default configuration is used.", ex);

            this.config  = new Configuration();
        }

        if (this.config == null) {
            this.dpuContext.sendMessage(DPUContext.MessageType.WARNING,
                    "Addon configuration is null.",
                    "Failed to load configuration for: " + ADDON_NAME
                    + " default configuration is used.");

            this.config  = new Configuration();
        }

        return true;
    }

    @Override
    public void postAction(DpuAdvancedBase.Context context) {
        // do nothing
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
     * Downloaded given file and store it into a cache. If file is presented in
     * cache the is returned. If URI is in bad format then throw
     * {@link AddonException}.
     *
     * @param fileUrl
     * @return
     * @throws AddonException
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
     * Downloaded given file and store it into a cache. If file is presented in
     * cache the is returned.
     *
     * @param fileURI
     * @return
     */
    public File get(URL fileUrl) throws AddonException, IOException {
        if (baseDirectory == null) {
            throw new AddonException("Not initialized!");
        }
        //
        // prepare file name
        //
        final String fileName;
        try {
            fileName = URLEncoder.encode(fileUrl.toString(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Hard coded encoding is not supported!!",
                    ex);
        }
        //
        // check for file existance
        //
        final File file = new File(baseDirectory, fileName);
        if (file.exists() && !config.rewriteCache) {
            LOG.info("get({}) - file from cache ", fileUrl.toString());
            return file;
        }
        //
        // download
        //
        int attempCounter = config.maxAttemps;
        while (attempCounter != 0 && !dpuContext.canceled()) {
            // wait before download
            waitForNextDownload();
            // try to download
            try {
                FileUtils.copyURLToFile(fileUrl, file);
                LOG.info("get({}) - file downloaded ", fileUrl.toString());
                return file;
            } catch (IOException ex) {
                LOG.warn("Failed to download file from {} attemp {}/{}",
                        fileUrl.toString(), attempCounter, config.maxAttemps,
                        ex);
            }
            // decrease attemp counted if not set to infinity = -1
            if (attempCounter > 0) {
                --attempCounter;
            }
        }
        //
        // download failed
        //
        if (dpuContext.canceled()) {
            throw new CancelledException();
        } else {
            throw new IOException("Can't obtain file.");
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
     * Wait before next download. BVefore leaving set time for next download.
     */
    private void waitForNextDownload() {
        while ((new Date()).getTime() < nextDownload && !dpuContext.canceled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {

            }
        }
        // determine min time for next download
        nextDownload = (new Date()).getTime()
                + (long) ((Math.random() * (config.maxPause - config.minPause))
                + config.minPause);
    }

}
