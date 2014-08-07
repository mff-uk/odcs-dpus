package cz.cuni.mff.xrg.uv.boost.dpu.addon.impl;

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.CancelledException;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonDialogBase;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AddonWithVaadinDialog;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.config.DPUConfigException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

    public static final String USED_USER_DIRECTORY =
            "addon/cachedFileDownloader";

    public static final String USED_CONFIG_NAME = 
            "addon/cachedFileDownloader";

    public static final String ADDON_NAME = 
            "Ceched file downloader";

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

    }

    /**
     * Addon's configuration dialog.
     */
    public class VaadinDialog extends AddonDialogBase<Configuration> {

        private TextField txtMaxAttemps;

        private TextField txtMaxPause;

        private TextField txtMinPause;

        public VaadinDialog() {
            super(Configuration.class);
        }

        @Override
        protected String getConfigClassName() {
            return ADDON_NAME;
        }

        @Override
        public void buildLayout() {
            final VerticalLayout mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();
            mainLayout.setSpacing(true);
            mainLayout.setMargin(true);

            mainLayout.addComponent( new Label(
                    "Max number of attemps to download a single file, use -1 for infinity"));
            txtMaxAttemps = new TextField();
            txtMaxAttemps.setWidth("100%");
            txtMaxAttemps.setRequired(true);
            mainLayout.addComponent(txtMaxAttemps);

            mainLayout.addComponent( new Label(
                    "Max pause in ms between downloads"));
            txtMaxPause = new TextField();
            txtMaxPause.setWidth("100%");
            txtMaxPause.setRequired(true);
            mainLayout.addComponent(txtMaxPause);

            mainLayout.addComponent( new Label(
                    "Min pause in ms between downloads"));
            txtMinPause = new TextField();
            txtMinPause.setWidth("100%");
            txtMinPause.setRequired(true);
            mainLayout.addComponent(txtMinPause);

            setCompositionRoot(mainLayout);
        }

        @Override
        protected void setConfiguration(Configuration conf) throws DPUConfigException {
            txtMaxAttemps.setValue(conf.getMaxAttemps().toString());
            txtMaxPause.setValue(conf.getMaxPause().toString());
            txtMinPause.setValue(conf.getMinPause().toString());
        }

        @Override
        protected Configuration getConfiguration() throws DPUConfigException {
            if (!txtMaxAttemps.isValid() || !txtMaxPause.isValid() ||
                    !txtMinPause.isValid()) {
                throw new DPUConfigException("All values for " + ADDON_NAME +
                        " must be provided.");
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
    private long nextDownload = Long.MAX_VALUE;

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
                    "Failed to load configuration for: " + ADDON_NAME +
                            " default configuration is used.", ex);
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
    public AddonDialogBase<Configuration> getDialog() {
        return new VaadinDialog();
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
        if (file.exists()) {
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
                + (long) ((Math.random() * config.maxPause) - config.minPause);
    }

}
