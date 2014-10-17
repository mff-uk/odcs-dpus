package cz.cuni.mff.xrg.uv.extractor.textholder;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.CreateFile;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class TextHolder extends DpuAdvancedBase<TextHolderConfig_V1> {

    @DataUnit.AsOutput(name = "file")
    public WritableFilesDataUnit outFiles;

	public TextHolder() {
		super(TextHolderConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // create new file
        final File file;
        try {
            file = CreateFile.createFile(outFiles, config.getFileName());
        } catch (DataUnitException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }
        // write string
        try {
            FileUtils.writeStringToFile(file, config.getText(), "UTF-8");
        } catch (IOException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Can't write data into file.", "", ex);
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new TextHolderVaadinDialog();
    }
	
}
