package cz.cuni.mff.xrg.uv.loader.deleteDirectory;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class DeleteDirectory extends AbstractDpu<DeleteDirectory_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteDirectory.class);

    public DeleteDirectory() {
        super(DeleteDirectoryVaadinDialog.class, ConfigHistory.noHistory(DeleteDirectory_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        try {
            FileUtils.deleteDirectory(new File(config.getDirectory()));
        } catch (IOException ex) {
            throw new DPUException("Can't delete directory.");
        }
    }

}
