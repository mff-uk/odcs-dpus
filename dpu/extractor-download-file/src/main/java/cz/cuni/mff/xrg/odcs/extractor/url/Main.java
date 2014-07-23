package cz.cuni.mff.xrg.odcs.extractor.url;

import cz.cuni.mff.xrg.uv.boost.dpu.simple.ConfigurableBase;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DPU.AsExtractor
public class Main extends ConfigurableBase<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @DataUnit.AsOutput(name = "output")
    public WritableFilesDataUnit output;

    public Main() {
        super(Configuration.class);
    }

    @Override
    public void execute(DPUContext context) throws DPUException {
        // prepare source
        final URL sourceUrl = config.getURL();
        if (sourceUrl == null) {
            context.sendMessage(MessageType.ERROR, "Source URL not specified.");
        }
        // prepare target
        final String targetFileStr;
        try {
            targetFileStr = output.createFile(config.getTarget());
        } catch (DataUnitException ex) {
            context.sendMessage(MessageType.ERROR, "Source URL not specified.");
            return;
        }
        final File targetFile = new File(targetFileStr);
        // download
        try {
            FileUtils.copyURLToFile(sourceUrl, targetFile);
            // ok we have downloaded the file .. 
            return;
        } catch (IOException ex) {
            LOG.error("Download failed.", ex);
        }
        // try again ?
        int retryCount = config.getRetryCount();
        while (retryCount != 0) {
            // sleep for a while
            try {
                Thread.sleep(config.getRetryDelay());
            } catch (InterruptedException ex) {
            }
            // try to download
            try {
                FileUtils.copyURLToFile(sourceUrl, targetFile);
                return;
            } catch (IOException ex) {
                LOG.error("Download failed.", ex);
            }
            // update retry counter
            if (retryCount > 0) {
                retryCount--;
            }
            // check for cancelation
            if (context.canceled()) {
                context.sendMessage(MessageType.INFO, "DPU has been canceled.");
                return;
            }
        }
        context.sendMessage(MessageType.ERROR, "Failed to download file.");
    }

    @Override
    public AbstractConfigDialog<Configuration> getConfigurationDialog() {
        return new Dialog();
    }

}
