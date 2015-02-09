package cz.cuni.mff.xrg.uv.extractor.textholder;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.AbstractDpu;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import org.apache.commons.io.FileUtils;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigHistory;
import cz.cuni.mff.xrg.uv.boost.dpu.initialization.AutoInitializer;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.boost.extensions.RdfConfiguration;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class TextHolder extends AbstractDpu<TextHolderConfig_V1> {

    @DataUnit.AsOutput(name = "file")
    public WritableFilesDataUnit outFiles;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    public TextHolder() {
        super(TextHolderVaadinDialog.class, ConfigHistory.noHistory(TextHolderConfig_V1.class),
                TextHolderOntology.class);
    }
		
    @Override
    protected void innerExecute() throws DPUException {
        // Create new file.
        final FilesDataUnit.Entry newFileEntry = faultTolerance.execute(
                new FaultTolerance.ActionReturn<FilesDataUnit.Entry>() {

            @Override
            public FilesDataUnit.Entry action() throws Exception {
                return FilesDataUnitUtils.createFile(outFiles, config.getFileName());
            }
        });
        // Write string.
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                File file = FilesDataUnitUtils.asFile(newFileEntry);
                FileUtils.writeStringToFile(file, config.getText(), "UTF-8");
            }
        });
    }
	
}
