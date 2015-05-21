package cz.cuni.mff.xrg.uv.extractor.httppost;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

// org.apache.commons.httpclient.HttpMethod

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsExtractor
public class HttpPost extends AbstractDpu<HttpPostConfig_V1> {

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @DataUnit.AsOutput(name = "files", description = "Downloaded content.")
    public WritableFilesDataUnit outFilesFiles;

    @ExtensionInitializer.Init(param = "outFilesFiles")
    public WritableSimpleFiles outFiles;

	public HttpPost() {
		super(HttpPostVaadinDialog.class, ConfigHistory.noHistory(HttpPostConfig_V1.class));
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Prepare output file.
        File targetFile = outFiles.create("content-file");
        // Exectute post.
        HttpClient client = new HttpClient();
        PostMethod method = new PostMethod(config.getEndpoint());
        for (HttpPostConfig_V1.Argument argument : config.getArguments()) {
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
