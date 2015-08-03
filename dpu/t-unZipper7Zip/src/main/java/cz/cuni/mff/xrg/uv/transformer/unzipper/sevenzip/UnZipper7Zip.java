package cz.cuni.mff.xrg.uv.transformer.unzipper.sevenzip;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelper;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class UnZipper7Zip extends AbstractDpu<UnZipper7ZipConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(UnZipper7Zip.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFilesData;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFilesData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;


    public UnZipper7Zip() {
        super(UnZipper7ZipVaadinDialog.class, ConfigHistory.noHistory(UnZipper7ZipConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        // Prepare root output directory.
        final File baseTargetDirectory = faultTolerance.execute(new FaultTolerance.ActionReturn<File>() {

            @Override
            public File action() throws Exception {
                return new File(java.net.URI.create(outFilesData.getBaseFileURIString()));
            }
        }, "unzipper7zip.errors.file.outputdir");
        // Get list of files to unzip.
        final List<FilesDataUnit.Entry> files = FaultToleranceUtils.getEntries(faultTolerance, inFilesData,
            FilesDataUnit.Entry.class);

        LOG.info(">> {}", files.size());

        int counter = 0;
        for (final FilesDataUnit.Entry fileEntry : files) {
            LOG.info("Processing: {}/{}", counter++, files.size());
            if (ctx.canceled()) {
                return;
            }
            final File sourceFile = FaultToleranceUtils.asFile(faultTolerance, fileEntry);
            // Get virtual path.
            final String zipRelativePath = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                @Override
                public String action() throws Exception {
                    return MetadataUtils.getFirst(inFilesData, fileEntry, VirtualPathHelper.PREDICATE_VIRTUAL_PATH);
                }
            }, "unzipper7zip.error.virtualpath.get.failed");
            if (zipRelativePath == null) {
                throw ContextUtils.dpuException(ctx, "unzipper7zip.error.missing.virtual.path", fileEntry.toString());
            }
            // Unzip.
            final File targetDirectory = new File(baseTargetDirectory, zipRelativePath);
            unzip(sourceFile, targetDirectory);
            // Scan for new files.
            scanDirectory(targetDirectory, zipRelativePath);
        }
    }

    /**
     * Scan given directory for files and add then to {@link #outFilesData}.
     *
     * @param directory
     * @throws DPUException
     */
    private void scanDirectory(File directory, String pathPrefix) throws DPUException {
        final Path directoryPath = directory.toPath();
        final Iterator<File> iter = FileUtils.iterateFiles(directory, null, true);
        while (iter.hasNext()) {
            final File newFile = iter.next();
            final String relativePath = directoryPath.relativize(newFile.toPath()).toString();
            final String newFileRelativePath;
            if (config.isNotPrefixed()) {
                newFileRelativePath = relativePath;
            } else {
                newFileRelativePath = pathPrefix + "/" + relativePath;
            }
            // Add file.
            faultTolerance.execute(new FaultTolerance.Action() {

                @Override
                public void action() throws Exception {
                    FilesDataUnitUtils.addFile(outFilesData, newFile, newFileRelativePath);
                }
            }, "unzipper7zip.error.file.add");
        }
    }

    /**
     * Extract given zip file into given directory.
     *
     * @param zipFile
     * @param targetDirectory
     * @throws DPUException
     */
    private boolean unzip(File zipFile, File targetDirectory) throws DPUException {
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
            throw ContextUtils.dpuException(ctx, ex, "unzipper7zip.exception");
        } catch (ArchiveException ex) {
            throw ContextUtils.dpuException(ctx, ex, "unzipper7zip.exception");
        }
        return true;
    }

}
