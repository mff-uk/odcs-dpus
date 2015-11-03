package cz.cuni.mff.xrg.uv.extractor.ftp;

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
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;
import java.io.FileOutputStream;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class Ftp extends AbstractDpu<FtpConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(Ftp.class);

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
    public RdfConfiguration _rdfConfiguration;

    public Ftp() {
        super(FtpVaadinDialog.class, ConfigHistory.noHistory(FtpConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "{0} file to download", config.getToDownload().size());

        int index = 0;
        for (DownloadInfo_V1 info : config.getToDownload()) {
            ContextUtils.sendShortInfo(ctx, "Downloading: ", info.getUri());
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
        // Split uri name.
        final String host = sourceUri.getHost();
        final String filePath = sourceUri.getPath();
        LOG.debug("Host: {} Path: {} -> {}", host, filePath, fileName);

        final File file = outFiles.create(fileName);

        // Connect to remote host.
        final FTPClient client = new FTPClient();
        client.connect(host);

        // Can be used to track progress.
        client.setCopyStreamListener(new ProgressPrinter());

        client.addProtocolCommandListener(new ProtocolCommandListener() {

            @Override
            public void protocolCommandSent(ProtocolCommandEvent event) {
                LOG.debug("sent: {}, {} -> {}", event.getCommand(), event.getMessage(), event.getReplyCode());
            }

            @Override
            public void protocolReplyReceived(ProtocolCommandEvent event) {
                LOG.debug("recieved: {}, {} -> {}", event.getCommand(), event.getMessage(), event.getReplyCode());
            }
        });

        // Set time out.
        client.setDataTimeout(5000);
        client.setControlKeepAliveTimeout(config.getKeepAliveControl());

        int reply = client.getReplyCode();
        LOG.debug("Connect reply: {}, {}", reply, client.getReplyString());
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new DPUException("Server refused the connection.");
        }

        // For now support only anonymous.
        if (!client.login("anonymous", "")) {
            client.logout();
            client.disconnect();
            throw new DPUException("Can't login as 'anonymous' with no password.");
        }
        reply = client.getReplyCode();
        LOG.debug("Connect reply: {}, {}", reply, client.getReplyString());
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new DPUException("Server refused the connection.");
        }

        // From documentation:
        //      currently calling any connect method will reset the mode to ACTIVE_LOCAL_DATA_CONNECTION_MODE.
        if (config.isUsePassiveMode()) {
            LOG.debug("Using passive mode.");
            client.enterLocalPassiveMode();
        }

        // From documentatio:
        //  currently calling any connect method will reset the type to FTP.ASCII_FILE_TYPE.
        if (config.isUseBinaryMode()) {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        }

        LOG.debug("Downloading ...");
        try (FileOutputStream output = new FileOutputStream(file)) {
//             ; InputStream input = client.retrieveFileStream(host)) {
//
//            if (input == null) {
//                throw new DPUException("Can't get input file stream!");
//            }
//            // Copy stream and log progress.
//            final byte[] buffer = new byte[1024]; // new byte[16384];
//            int len;
//            long total = 0;
//            LOG.debug("Start reading ..");
//            while ((len = input.read(buffer)) > 0) {
//                total += len;
//                if (total > 158289770) {
//                    LOG.debug("Reading data: {}", len);
//                    LOG.debug(" : {}", buffer);
//                }
//                output.write(buffer, 0, len);
//            }

            client.retrieveFile("/" + filePath, output);

            LOG.debug("Downloading ... flush");
            output.flush();
        }
        LOG.debug("Downloading ... done");

        client.logout();
        client.disconnect();
    }

}
