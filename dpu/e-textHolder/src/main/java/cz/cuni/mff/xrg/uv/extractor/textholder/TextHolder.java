package cz.cuni.mff.xrg.uv.extractor.textholder;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class TextHolder extends AbstractDpu<TextHolderConfig_V1> {

    @DataUnit.AsOutput(name = "file")
    public WritableFilesDataUnit filesOutput;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init(param = "filesOutput")
    public WritableSimpleFiles output;

	public TextHolder() {
		super(TextHolderVaadinDialog.class, ConfigHistory.noHistory(TextHolderConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        final File file = output.create(config.getText());
        try {
            FileUtils.writeStringToFile(file, config.getText(), "UTF-8");
        } catch (IOException ex) {
            throw ContextUtils.dpuException(ctx, ex, "Can't write data into file.");
        }
    }
	
}
