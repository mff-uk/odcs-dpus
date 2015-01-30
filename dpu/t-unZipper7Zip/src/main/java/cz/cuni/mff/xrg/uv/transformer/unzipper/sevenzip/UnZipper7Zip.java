package cz.cuni.mff.xrg.uv.transformer.unzipper.sevenzip;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.Manipulator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class UnZipper7Zip extends DpuAdvancedBase<UnZipper7ZipConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(UnZipper7Zip.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    public UnZipper7Zip() {
        super(UnZipper7ZipConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerExecute() throws DPUException {
        final FilesDataUnit.Iteration filesIteration;
        try {
            filesIteration = inFilesData.getIteration();
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }

        final File baseTargetDirectory;
        try {
            baseTargetDirectory = new File(java.net.URI.create(outFilesData.getBaseFileURIString()));
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }

        boolean symbolicNameUsed = false;

        try {
            byte[] buffer = new byte[16 * 1024];

            while (!context.canceled() && filesIteration.hasNext()) {
                final FilesDataUnit.Entry entry = filesIteration.next();
                // Prepare source/target file/directory
                final File sourceFile = new File(java.net.URI.create(entry.getFileURIString()));

                String zipRelativePath = Manipulator.getFirst(inFilesData, entry,
                        VirtualPathHelper.PREDICATE_VIRTUAL_PATH);

                if (zipRelativePath == null) {
                    // use symbolicv name
                    zipRelativePath = entry.getSymbolicName();
                    if (!symbolicNameUsed) {
                        // first usage
                        LOG.warn("Not all input files use VirtualPath, symbolic name is used instead.");
                    }
                    symbolicNameUsed = true;
                }

                final File targetDirectory = new File(baseTargetDirectory, zipRelativePath);
                // Unzip
                if (!unzip7Zip(sourceFile, targetDirectory, buffer)) {
                    // failure
                    break;
                }

                // TODO we can try to unpack other archives as well

                // Scan for new files and add them
                scanDirectory(targetDirectory);
                // Copy metadata

                // TODO
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
     * Scan directory and add it's content into {@link #outFilesData}.
     *
     * @param directory
     * @throws DataUnitException
     */
    private void scanDirectory(File directory) throws DataUnitException {
        LOG.debug("> scanDirectory");
        final Path directoryPath = directory.getParentFile().toPath();
        final Iterator<File> iter = FileUtils.iterateFiles(directory, null, true);
        while (iter.hasNext()) {
            final File newFile = iter.next();
            final String relativePath = directoryPath.relativize(newFile.toPath()).toString();
            final String newSymbolicName = relativePath;
            // add file
            outFilesData.addExistingFile(newSymbolicName, newFile.toURI().toString());
            //
            // add metadata
            //
            Manipulator.add(outFilesData, newSymbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH,
                    relativePath);
        }
        LOG.debug("< scanDirectory");
    }

    /**
     * Unzip given 7zip file.
     *
     * @param zipFile
     * @param targetDirectory
     * @param buffer
     * @return
     */
    private boolean unzip7Zip(File zipFile, File targetDirectory, byte[] buffer) {
        try {
            final SevenZFile sevenZFile = new SevenZFile(zipFile);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while (entry != null) {

                if (entry.isDirectory()) {
                    // skip directories
                    entry = sevenZFile.getNextEntry();
                    continue;
                }

                LOG.debug("Unpacking: '{}' ... ", entry.getName());
                final File targetFile = new File(targetDirectory, entry.getName());
                targetFile.getParentFile().mkdirs();

                try (FileOutputStream out = new FileOutputStream(targetFile)) {
                    // copy file content
                    while (true) {
                        int readSize = sevenZFile.read(buffer, 0, buffer.length);
                        LOG.trace("\treadSize = {}", readSize);
                        if (readSize == -1) {
                            // end of stream
                            break;
                        }
                        out.write(buffer, 0, readSize);
                    }
                    entry = sevenZFile.getNextEntry();
                }
                LOG.debug("Unpacking: '{}' ... done", entry.getName());
            }
        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Extraction failed.", "", ex);
            return false;
        }
        return true;
        
    }

    /**
     * Unzip given archive into given directory. Archives are determined by ArchiveStreamFactory, except
     * the 7zip that does not support streaming.
     *
     * @param zipFile
     * @return
     */
    private boolean unzip(File zipFile, File targetDirectory) {
        // for othesr then 7zip
        try (InputStream is = new FileInputStream(zipFile);
                ArchiveInputStream in = new ArchiveStreamFactory().createArchiveInputStream(
                        ArchiveStreamFactory.SEVEN_Z, is)) {

            ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();
            while (entry != null) {
                if (entry.isDirectory()) {
                    entry = (ZipArchiveEntry) in.getNextEntry();
                }
                LOG.debug("Unpacking: '{}' ... ", entry.getName());
                final File targetFile = new File(targetDirectory, entry.getName());
                targetFile.getParentFile().mkdirs();

                // copy file
                try (OutputStream out = new FileOutputStream(targetFile)) {
                    IOUtils.copy(in, out);
                }
                LOG.debug("Unpacking: '{}' ... done", entry.getName());
                entry = (ZipArchiveEntry) in.getNextEntry();
            }

        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Extraction failed.", "", ex);
            return false;
        } catch (ArchiveException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "ArchiveException.", "", ex);
            return false;
        }
        return true;
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new UnZipper7ZipVaadinDialog();
    }

}
