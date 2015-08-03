package cz.cuni.mff.xrg.uv.transformer.jsontojsonld;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

/**
 * Main data processing unit class.
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class JsonToJsonLd extends AbstractDpu<JsonToJsonLdConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonToJsonLd.class);
		
    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inFiles;

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit outFiles;

    @ExtensionInitializer.Init(param = "outFiles")
    public WritableSimpleFiles output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

	public JsonToJsonLd() {
		super(JsonToJsonLdVaadinDialog.class, ConfigHistory.noHistory(JsonToJsonLdConfig_V1.class));
	}

    @Override
    protected void innerExecute() throws DPUException {
        final List<FilesDataUnit.Entry> files = FaultToleranceUtils.getEntries(faultTolerance, inFiles,
                FilesDataUnit.Entry.class);
        ContextUtils.sendShortInfo(ctx, "Number of files: {0}", files.size());
        int counter = 0;
        for (final FilesDataUnit.Entry entry : files) {
            LOG.info("Processing {}/{}", ++counter, files.size());
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
            final File inputFile  = FaultToleranceUtils.asFile(faultTolerance, entry);
            final String virtualPath  = FaultToleranceUtils.getVirtualPath(faultTolerance, inFiles, entry);
            if (virtualPath == null || virtualPath.isEmpty()) {
                throw ContextUtils.dpuException(ctx, "Missing virtual path for entry: {0}", entry);
            }
            final File outputFile = output.create(virtualPath);

            final String symbolicName = faultTolerance.execute(new FaultTolerance.ActionReturn<String>() {

                @Override
                public String action() throws Exception {
                    return entry.getSymbolicName();
                }
            });

            // Add header and footer to the file.
            try (InputStream stream = new AddContextStream(config.getContext(), config.getEncoding(),
                    FileUtils.openInputStream(inputFile), symbolicName)) {
                FileUtils.copyInputStreamToFile(stream, outputFile);
            } catch (IOException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Conversion failed for: {0}", entry);
            }
        }
    }
	
}
