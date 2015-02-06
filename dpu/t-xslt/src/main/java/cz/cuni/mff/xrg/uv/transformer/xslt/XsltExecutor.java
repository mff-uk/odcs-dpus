package cz.cuni.mff.xrg.uv.transformer.xslt;

import java.io.File;
import java.io.StringReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.advanced.UserExecContext;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Transform given file with given template.
 *
 * @author Škoda Petr
 */
public class XsltExecutor extends Thread {

    /**
     * Task definition.
     */
    public static class Task {

        private final String symbolicName;

        private final File sourceFile;

        private final File targetFile;

        private boolean addToOutput = false;

        private final int order;

        public Task() {
            this.symbolicName = null;
            this.sourceFile = null;
            this.targetFile = null;
            this.order = -1;
        }

        public Task(String symbolicName, File sourceFile, File targetFile, int order) {
            this.symbolicName = symbolicName;
            this.sourceFile = sourceFile;
            this.targetFile = targetFile;
            this.order = order;
        }

        public String getSymbolicName() {
            return symbolicName;
        }

        public File getSourceFile() {
            return sourceFile;
        }

        public File getTargetFile() {
            return targetFile;
        }

        public boolean isAddToOutput() {
            return addToOutput;
        }

    }

    /**
     * Used to kill executor thread.
     */
    public static class DeadPill extends Task {

    }

    private static final Logger LOG = LoggerFactory.getLogger(XsltExecutor.class);

    /**
     * If set to true then all threads will terminate as soon as possible.
     */
    private static boolean terminateThreads = false;

    private final Processor proc;

    private final XsltCompiler compiler;

    private final XsltExecutable executable;

    private final XsltConfig_V2 config;

    private final BlockingQueue<Task> taskQueue;

    private final UserExecContext ctx;

    private final Integer totalFileCounter;

    public XsltExecutor(XsltConfig_V2 config,
            BlockingQueue<Task> taskQueue, FaultTolerance faultTolerance, UserExecContext ctx,
            FilesDataUnit filesInput, WritableFilesDataUnit filesOutput, Integer totalFileCounter)
            throws DPUException {
        this.proc = new Processor(false);
        this.compiler = proc.newXsltCompiler();
        try {
            this.executable = compiler.compile(
                    new StreamSource(new StringReader(config.getXsltTemplate())));
        } catch (SaxonApiException ex) {
            throw new DPUException("Cannot compile XSLT template.", ex);
        }
        this.config = config;
        this.taskQueue = taskQueue;
        this.ctx = ctx;
        this.totalFileCounter = totalFileCounter;
    }

    @Override
    public void run() {
        Task task;
        try {
            while (!((task = (Task) taskQueue.take()) instanceof DeadPill) && !terminateThreads) {
                execute(task);
            }
            // Reinsert DeadPill for other thread.
            while (!taskQueue.offer(task, 1, TimeUnit.SECONDS));
        } catch (InterruptedException ex) {
            LOG.error("InterruptedException executor terminated!", ex);
        }
    }

    private void execute(Task task){
        LOG.info("Processing {}/{}", task.order, totalFileCounter);

        // Transform file - go parallel.
        LOG.debug("Memory used: {}M", String.valueOf((Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        // Prepare classes and parameters.
        final Serializer out = new Serializer(task.targetFile);
        final XsltTransformer transformer = executable.load();

        for (XsltConfig_V2.Parameter parameter : config.getFilesParameters(task.symbolicName)) {
            LOG.debug("\t {} : {}", parameter.getKey(), parameter.getValue());
            transformer.setParameter(new QName(parameter.getKey()), new XdmAtomicValue(parameter.getValue()));
        }
        try {
            transformer.setSource(new StreamSource(task.sourceFile));
            transformer.setDestination(out);
            transformer.transform();
            transformer.getUnderlyingController().clearDocumentPool();
            task.addToOutput = true;
        } catch (SaxonApiException ex) {
            if (config.isFailOnError()) {
                ContextUtils.sendError(ctx, "Error processig file number " + Integer.toString(task.order), ex,
                        "File: %s</br>File location: %s", task.symbolicName, task.sourceFile);
                terminateThreads = true;    // Should kill all threads.
                return;
            } else {
                ContextUtils.sendWarn(ctx, "Error processig file number " + Integer.toString(task.order), ex,
                        "File: %s</br>File location: %s", task.symbolicName, task.sourceFile);
            }
        } finally {
            // In every case close opened resources.
            try {
                out.close();
            } catch (SaxonApiException ex) {
                LOG.warn("Can't close Serializer.", ex);
            }
            try {
                transformer.close();
            } catch (SaxonApiException ex) {
                LOG.warn("Can't close XsltTransformer.", ex);
            }
        }
        LOG.debug("Memory used: {}M", String.valueOf((Runtime.getRuntime().totalMemory()
                - Runtime.getRuntime().freeMemory()) / 1024 / 1024));

    }

}
