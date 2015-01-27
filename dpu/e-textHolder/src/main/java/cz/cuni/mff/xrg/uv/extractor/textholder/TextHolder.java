package cz.cuni.mff.xrg.uv.extractor.textholder;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.extensions.RdfConfiguration;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class TextHolder extends AbstractDpu<TextHolderConfig_V1> {

    @DataUnit.AsOutput(name = "file")
    public WritableFilesDataUnit outFiles;

    @AutoInitializer.Init
    public RdfConfiguration _rdfConfiguration;

	public TextHolder() {
		super(TextHolderVaadinDialog.class, ConfigHistory.noHistory(TextHolderConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // create new file
        final File file;
        try {
            file = FilesDataUnitUtils.createFile(outFiles, config.getFileName());
        } catch (DataUnitException ex) {
            throw new DPUException("Can't create output file.", ex);
        }
        // write string
        try {
            FileUtils.writeStringToFile(file, config.getText(), "UTF-8");
        } catch (IOException ex) {
            throw new DPUException("Can't write data file.", ex);
        }
    }

	
}
