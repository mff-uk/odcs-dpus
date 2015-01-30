package cz.cuni.mff.xrg.uv.transformer.unzipper.gzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.Manipulator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class UnZipperGZip extends DpuAdvancedBase<UnZipperGZipConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(UnZipperGZip.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    public UnZipperGZip() {
        super(UnZipperGZipConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Get iteration for input files.
        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesData.getIteration();
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        // Prepare output directory.
        final File baseTargetDirectory;
        try {
            baseTargetDirectory = new File(java.net.URI.create(outFilesData.getBaseFileURIString()));
            baseTargetDirectory.mkdirs();
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }

        byte[] buffer = new byte[16 * 1024];
        boolean symbolicNameUsed = false;
        try {

            // Iterate over files and unpact.
            while (!context.canceled() && filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();
                // Prepare source/target file/directory
                final File sourceFile = FilesDataUnitUtils.asFile(entry);
                String zipRelativePath = Manipulator.getFirst(inFilesData, entry,
                        VirtualPathHelper.PREDICATE_VIRTUAL_PATH);
                if (zipRelativePath == null) {
                    // Use symbolicv name as fall back.
                    zipRelativePath = entry.getSymbolicName();
                    if (!symbolicNameUsed) {
                        // Log in case of first symbolic name usage.
                        LOG.warn("Not all input files use VirtualPath, symbolic name is used instead.");
                    }
                    symbolicNameUsed = true;
                }
                // Remove .gz suffix.
                if (zipRelativePath.endsWith(".gz")) {
                    zipRelativePath = zipRelativePath.substring(0, zipRelativePath.length() - 3);
                }
                final File targetFile = new File(baseTargetDirectory, zipRelativePath);
                if (!unzip(sourceFile, targetFile, buffer)) {
                    // failure
                    break;
                }
                // Scan for new files and add them
                FilesDataUnitUtils.addFile(outFilesData, baseTargetDirectory, targetFile);
                // TODO Petr: Copy metadata
            }
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
        } finally {
            try {
                filesIteration.close();
            } catch (DataUnitException ex) {
                LOG.warn("Error in close.", ex);
            }
        }
    }

    /**
     * Unzip given archive into given directory. Archives are determined by ArchiveStreamFactory, except the
     * 7zip that does not support streaming.
     *
     * @param zipFile
     * @param targetFile
     * @param buffer
     * @return
     */
    private boolean unzip(File zipFile, File targetFile, byte[] buffer) {
        LOG.trace("unzip({}, {}, ...)", zipFile, targetFile);
        try (FileInputStream fis = new FileInputStream(zipFile);
                GZIPInputStream gis = new GZIPInputStream(fis);
                FileOutputStream fos = new FileOutputStream(targetFile)) {
            int len = 0;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Extraction failed.", "", ex);
            return false;
        }
        return true;
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new UnZipperGZipVaadinDialog();
    }

}
