package cz.cuni.mff.xrg.uv.extractor.scp;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import uk.co.marcoratto.scp.SCP;
import uk.co.marcoratto.scp.SCPPException;
import uk.co.marcoratto.scp.listeners.SCPListenerPrintStream;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import eu.unifiedviews.helpers.dpu.config.ConfigurableBase;

/**
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class FilesFromScp extends ConfigurableBase<FilesFromScpConfig_V1> implements ConfigDialogProvider<FilesFromScpConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FilesFromScp.class);

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    public FilesFromScp() {
        super(FilesFromScpConfig_V1.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        // Prepare library.
        final SCP scp = new SCP(new SCPListenerPrintStream());
        scp.setPort(config.getPort());
        scp.setPassword(config.getPassword());
        scp.setTrust(true);
        if (context.isDebugging()) {
            scp.setVerbose(true);
        }
        // Non recursion we copy ourselfs.
        scp.setRecursive(true);
        // Pepare destination.
        String sourceBase = config.getUsername() + '@' + config.getHostname() + ':' + config.getSource();
        if (!sourceBase.endsWith("/")) {
            sourceBase += "/";
        }
        final File downloadDir = context.getWorkingDir();
        downloadDir.mkdirs();
        LOG.info("Transferring {} -> {} ... ", sourceBase, downloadDir);
        // Transfer files
        try {
            transfer(scp, sourceBase, downloadDir.toString());
        } catch (SCPPException ex) {
            if (config.isSoftFail()) {
                context.sendMessage(DPUContext.MessageType.WARNING, "Failed to upload file/directory", "", ex);
            } else {
                throw new DPUException("Upload failed.", ex);
            }
        }
        LOG.info("Transferring {} -> {} ... done", sourceBase, downloadDir);
        // Files were downloadded into a directory that has same name as last directory in source path.
        final String downloadSubDir = sourceBase.substring(sourceBase.substring(0, sourceBase.length() - 1).lastIndexOf("/"));
        LOG.debug("Download subdirectory: {}", downloadSubDir);
        final File realDownloadDir = new File(downloadDir, downloadSubDir);
        // Scan for new files.
        LOG.info("Scanning for new files in '{}' ...", realDownloadDir);
        scanDirectory(realDownloadDir);
        LOG.info("Scanning for new files in '{}' ... done", realDownloadDir);
    }

    @Override
    public AbstractConfigDialog<FilesFromScpConfig_V1> getConfigurationDialog() {
        return new FilesFromScpVaadinDialog();
    }

    protected void transfer(SCP scp, String uriFrom, String uriTo) throws SCPPException {
        scp.setFromUri(uriFrom);
        scp.setToUri(uriTo);
        scp.execute();
    }

    /**
     * Scan directory and add it's content into {@link #outFilesData}.
     *
     * @param directory
     * @throws DataUnitException
     */
    private void scanDirectory(File directory) throws DPUException {
        final Iterator<File> iter = FileUtils.iterateFiles(directory, null, true);
        while (iter.hasNext()) {
            final File newFile = iter.next();
            try {
                FilesDataUnitUtils.addFile(outFilesData, directory, newFile);
            } catch (DataUnitException ex) {
                throw new DPUException("Can't add file.", ex);
            }
        }
    }

}
