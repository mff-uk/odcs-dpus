package cz.cuni.mff.xrg.uv.extractor.isvav;

import cz.cuni.mff.xrg.uv.extractor.isvav.source.*;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionException;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.CachedFileDownloader;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class Isvav extends AbstractDpu<IsvavConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(Isvav.class);
	
	@DataUnit.AsOutput(name = "zipFiles", description = "Contains one or more zip files.")
	public WritableFilesDataUnit outFilesData;

    @ExtensionInitializer.Init(param = "outFilesData")
    public WritableSimpleFiles filesData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    @ExtensionInitializer.Init
    public CachedFileDownloader downloader;

	public Isvav() {
		super(IsvavVaadinDialog.class, ConfigHistory.noHistory(IsvavConfig_V1.class));
	}

    @Override
    protected void innerExecute() throws DPUException {
        // create sources
		final List<AbstractSource> usedSource = createSource();
        ContextUtils.sendShortInfo(ctx, "Extracting files ...");
		// for each source
        int index = 1;
		for (AbstractSource source : usedSource) {
            // we rely on fixed order of sources and different
            // base file names
			final String fileName = source.getFileName() + ".zip";
			final File file = downloadData(ctx.getExecMasterContext().getDpuContext(), source, fileName);
            if (file != null) {
                // file has been downloaded
                filesData.add(file, fileName);
                ContextUtils.sendShortInfo(ctx, "File extracted {0}/{1} as {3}", index++, usedSource.size(),
                        fileName);
			}
            if (ctx.canceled()) {
                throw ContextUtils.dpuExceptionCancelled(ctx);
            }
		}
	}

	/**
	 * Create sources for given data type
	 * 
	 * @return 
	 */
	private List<AbstractSource> createSource() {
		final List<AbstractSource> sources = new LinkedList<>();
        // xls, dbf
        final String exportType = config.getExportType();

		switch (config.getSourceType()) {
			case Funder:
				sources.add(new SourceFunder(exportType));
				break;
			case Organization:
				sources.add(new SourceOrganization(exportType));
				break;
			case Programme:
				// 1991 - 2019
				sources.add(new SourceProgramme(exportType, "1991", "2019"));
				break;
			case Project:
				sources.add(new SourceProject(exportType));
				break;
			case Research:
				sources.add(new SourceResearch(exportType));
				break;
			case Result:
				Calendar now = Calendar.getInstance();
				final int from = 1991;
				final int to = now.get(Calendar.YEAR);
				LOG.debug("Extracting from {} to {}", from, to);
				for (Integer year = from; year <= to; ++year) {
					sources.add(new SourceResult(exportType, year.toString()));
				}
				break;
			case Tender:
				sources.add(new SourceTender(exportType));
				break;
		}
		return sources;
	}
	
	/**
	 * Download data from given source.
	 * 
	 * @param context
	 * @param source
     * @param fileName
	 * @return Downloaded file.
	 */
	protected File downloadData(DPUContext context, AbstractSource source, String fileName) throws DPUException {
		String sessionID = null;
		
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpGet httpget = new HttpGet(source.getFilterUri());
		final HttpClientContext httpContext = HttpClientContext.create();
		try (CloseableHttpResponse response = httpclient.execute(httpget, httpContext)) {
            final CookieStore cookieStore = httpContext.getCookieStore();
			final Iterator<Cookie> iter = cookieStore.getCookies().iterator();
			while(iter.hasNext()) {
				final Cookie cookie = iter.next();
				if (cookie.getName().compareTo("JSESSIONID") == 0) {
					sessionID = cookie.getValue();
				}
			}
            // get number of records to download
            final String responseString = EntityUtils.toString(response.getEntity());
            final String indexStartStr = "<span class=\"total\">";
            int indexStart = responseString.indexOf(indexStartStr);
            final String toDownload;
            if (indexStart != -1) {
                int indexEnd = responseString.indexOf("</span>", indexStart);
                toDownload = responseString.substring(indexStart + indexStartStr.length(), indexEnd);
            } else {
                toDownload = "0";
            }
            LOG.info("Downloading: {} records", toDownload);

		} catch (IOException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Can't connect to filter page.", ex);
			return null;
		}

		final URL dataUrl;
		try {
			dataUrl = new URL(source.getDownloadUri(sessionID));
		} catch (MalformedURLException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Failed to create download URL.", ex);
			return null;
		}

		try {
            return downloader.get(fileName, dataUrl);
		} catch (IOException | ExtensionException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Failed to download file.", ex);
			return null;
		}
	}
	
}
