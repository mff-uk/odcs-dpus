package cz.cuni.mff.xrg.uv.transformer.filedecoder;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.files.FilesVocabulary;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsTransformer
public class FileDecoder extends AbstractDpu<FileDecoderConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(FileDecoder.class);

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inputFiles;
    
    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outputFiles;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public FileDecoder() {
		super(FileDecoderVaadinDialog.class, ConfigHistory.noHistory(FileDecoderConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        final List<FilesDataUnit.Entry> files = FaultToleranceUtils.getEntries(faultTolerance, inputFiles, 
                FilesDataUnit.Entry.class);
        for (final FilesDataUnit.Entry entry : files) {
            final File inputFile = FaultToleranceUtils.asFile(faultTolerance, entry);
            // Read file content.
            final String fileContent;
            try {
                fileContent = FileUtils.readFileToString(inputFile);
            } catch (IOException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Can't read input file.");
            }
            // Transform.
            final byte[] outputContent = javax.xml.bind.DatatypeConverter.parseBase64Binary(fileContent);
            // Get metadata about input.
            final String symbolicName = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                @Override
                public String action() throws Exception {
                    return entry.getSymbolicName();
                }
            });
            final String virtualPath = FaultToleranceUtils.getVirtualPath(faultTolerance, inputFiles, entry);
            // Create output.
            final File outputFile = faultTolerance.execute(new FaultTolerance.ActionReturn<File>() {

                @Override
                public File action() throws Exception {
                    return new File(java.net.URI.create(outputFiles.addNewFile(symbolicName)));
                }
            });
            faultTolerance.execute(new FaultTolerance.Action() {
                @Override
                public void action() throws Exception {
                    MetadataUtils.set(outputFiles, symbolicName, FilesVocabulary.UV_VIRTUAL_PATH, virtualPath);
                }
            });
            // Set file content.
            try {
                FileUtils.writeByteArrayToFile(outputFile, outputContent);
            } catch (IOException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Can't write data to the output file.");
            }
        }
    }
	
}
