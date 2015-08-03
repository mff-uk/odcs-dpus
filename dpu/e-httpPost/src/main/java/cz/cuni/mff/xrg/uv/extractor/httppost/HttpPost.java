package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.extension.rdf.RdfConfiguration;

// org.apache.commons.httpclient.HttpMethod

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class HttpPost extends AbstractDpu<HttpPostConfig_V2> {

    private static final Logger LOG = LoggerFactory.getLogger(HttpPost.class);

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @RdfConfiguration.ContainsConfiguration
    @DataUnit.AsInput(name = "config", optional = true, description = "DPU's configuration.")
    public RDFDataUnit inRdfToDownload;

    @DataUnit.AsOutput(name = "files", description = "Downloaded content.")
    public WritableFilesDataUnit outFilesFiles;

    @ExtensionInitializer.Init(param = "outFilesFiles")
    public WritableSimpleFiles outFiles;

    @ExtensionInitializer.Init
    public RdfConfiguration _rdfConfiguration;

	public HttpPost() {
		super(HttpPostVaadinDialog.class, 
                ConfigHistory.history(HttpPostConfig_V1.class).addCurrent(HttpPostConfig_V2.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        ContextUtils.sendShortInfo(ctx, "Downloading: {0}", config.getRequest().size());
        int counter = 0;
        for (HttpPostConfig_V2.Request request : config.getRequest()) {
            LOG.info("Executing {}/{}", ++counter, config.getRequest().size());
            // Prepare output file.
            final File targetFile = outFiles.create(request.getFileName());

            // Exectute post.
            HttpClient client = new HttpClient();
            PostMethod method = new PostMethod(request.getUri());
            for (HttpPostConfig_V2.Argument argument : request.getArguments()) {
                method.addParameter(argument.getName(), argument.getValue());
            }

            try {
                int returnCode = client.executeMethod(method);
                if (returnCode != HttpStatus.SC_OK) {
                    throw ContextUtils.dpuException(ctx, "Response code: {0}", returnCode);
                }
                // Copy content.
                FileUtils.copyInputStreamToFile(method.getResponseBodyAsStream(), targetFile);
            } catch (IOException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Can't download data!");
            }
        }
    }	
}
