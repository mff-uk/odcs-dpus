package cz.cuni.mff.xrg.uv.test.boost.dpu;

import eu.unifiedviews.dpu.DPUContext;
import java.io.File;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base mock class for {@link DPUContext}. Can be used for testing of simple 
 * DPUs that use context only for message publication and cancelled state
 * check.
 *
 * @author Škoda Petr
 */
public class DPUContextMockBase implements DPUContext {

    private static final Logger LOG = LoggerFactory.getLogger(
            DPUContextMockBase.class);

    @Override
    public void sendMessage(MessageType type, String shortMessage) {
        LOG.info("sendMessage({}, \"{}\")", type, shortMessage);
    }

    @Override
    public void sendMessage(MessageType type, String shortMessage,
            String fullMessage) {
        LOG.info("sendMessage({}, \"{}\", \"{}\")", type, shortMessage, fullMessage);
    }

    @Override
    public void sendMessage(MessageType type, String shortMessage,
            String fullMessage, Exception exception) {
        LOG.info("sendMessage({}, \"{}\", \"{}\", ...)", type, shortMessage, fullMessage, exception);
    }

    @Override
    public boolean isDebugging() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canceled() {
        return false;
    }

    @Override
    public File getWorkingDir() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getResultDir() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getJarPath() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getLastExecutionTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getGlobalDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getUserDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDpuInstanceDirectory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
