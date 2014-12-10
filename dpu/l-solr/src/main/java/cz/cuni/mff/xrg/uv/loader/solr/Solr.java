package cz.cuni.mff.xrg.uv.loader.solr;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CloseCloseable;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsLoader
public class Solr extends DpuAdvancedBase<SolrConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Solr.class);

    @DataUnit.AsInput(name = "files")
    public FilesDataUnit inFilesToLoad;

    public Solr() {
        super(SolrConfig_V1.class, AddonInitializer.create(new CloseCloseable()));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Prepare target URL.
        final URL url;
        try {
            url = new URL(config.getServer());
        } catch (MalformedURLException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Invalid solr URL", "", ex);
            return;
        }
        // Execute.
        final FilesDataUnit.Iteration iter;
        try {
            iter = inFilesToLoad.getIteration();
            getAddon(CloseCloseable.class).add(iter);
            while (iter.hasNext()) {
                final FilesDataUnit.Entry entry = iter.next();
                if (!uploadFile(url, entry)) {
                    // Upload failed.
                    return;
                }
            }
            commit();
        } catch (DataUnitException | IOException ex) {
            throw new DPUException(ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SolrVaadinDialog();
    }

    /**
     *
     * @param url
     * @param entry
     * @return False if next file should not be uploaded.
     * @throws DataUnitException
     * @throws IOException
     */
    private boolean uploadFile(URL url, FilesDataUnit.Entry entry) throws DataUnitException, IOException {
        LOG.debug("uploading file: {}", entry);
        final File file = new File(java.net.URI.create(entry.getFileURIString()));
        final String type = "text/csv";
        try (InputStream is = new FileInputStream(file)) {
            final HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Content-type", type);
            // Use chunk mode with auto chunk size. This is necesery for large data, otherwise
            // HttpURLConnection tries to store all the data to calculate length (for header).
            conn.setChunkedStreamingMode(0);
            conn.connect();
            // Copy data.
            try (final OutputStream out = conn.getOutputStream()) {
                IOUtils.copy(is, out);
            }
            // Check response.
            if (!checkResponse(conn)) {
                context.sendMessage(DPUContext.MessageType.ERROR,
                        "Upload failed.", "Failed to upload file: " + entry.getSymbolicName());
                return false;
            }            
        }
        return true;
    }

    /**
     * Check HRML response on given connection.
     *
     * @param conn
     * @return
     * @throws IOException
     */
    private boolean checkResponse(HttpURLConnection conn) throws IOException {
        final int responseCode = conn.getResponseCode();
        LOG.info("Response code is {}", responseCode);
        // Print response every time for debuggin purpose.

        try {
            final StringBuilder logString = new StringBuilder();
            final List<String> response = IOUtils.readLines(conn.getInputStream());
            for (String line : response) {
                logString.append(line);
                logString.append("\n");
            }
            LOG.debug("Response: {}", logString);
        } catch (IOException ex) {
            LOG.error("Can't read response.", ex);
        }

        if (responseCode >= 400) {
            // Print error response.
            final StringBuilder logString = new StringBuilder();
            final List<String> response = IOUtils.readLines(conn.getErrorStream());
            for (String line : response) {
                logString.append(line);
                logString.append("\n");
            }
            LOG.error("Response (error): {}", logString);
            return false;
        }
        return true;
    }

    /**
     * Commit data into Solr.
     */
    private void commit() {
        URL urlCommit;
        try {
            urlCommit = new URL(config.getServer() + "?commit=true");
        } catch (MalformedURLException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Commit failed",
                    "Invalid URL: " + config.getServer() + "?commit=true", ex);
            return;
        }
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) urlCommit.openConnection();
            if (urlCommit.getUserInfo() != null) {
                // Set user info here.
            }
            connection.connect();
            if (!checkResponse(connection)) {
                context.sendMessage(DPUContext.MessageType.ERROR, "Commit faield",
                        "checkResponse return false check logs for more info");
            }
            connection.disconnect();
        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Commit failed", "", ex);
        }
    }

}
