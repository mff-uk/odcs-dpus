package cz.cuni.mff.xrg.uv.extractor.isvav;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.extractor.isvav.source.*;
import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.Manipulator;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class Isvav extends DpuAdvancedBase<IsvavConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(Isvav.class);
	
	@DataUnit.AsOutput(name = "zipFiles", description = "Contains one or more zip files.")
	public WritableFilesDataUnit outFilesData;
	
	public Isvav() {
		super(IsvavConfig_V1.class, AddonInitializer.noAddons());
	}

    @Override
    protected void innerExecute() throws DPUException, DataUnitException {
        // create sources
		List<AbstractSource> usedSource = createSource();
		// for each source
		int index = 0;
		for (AbstractSource source : usedSource) {
			final String filename = String.format("%s-%d.zip", 
					source.getBaseFileName(), index++);
			final File file = new File( 
                    java.net.URI.create(outFilesData.addNewFile(filename)));
            Manipulator.add(outFilesData, filename, VirtualPathHelper.PREDICATE_VIRTUAL_PATH,
                    filename);;
			// download file
			if (!downloadData(context, source, file)) {
				// extraction failed
				return;
			} else {
				context.sendMessage(DPUContext.MessageType.INFO,
                        "File extracted",
						"Extracted file saved into: " + filename);
			}
		}
	}

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new IsvavVaadinDialog();
    }

	/**
	 * Create sources for given data type
	 * 
	 * @return 
	 */
	private List<AbstractSource> createSource() {
		List<AbstractSource> sources = new LinkedList<>();
        // xls, dbf
        String exportType = "dbf";

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
	 * Download data from given source and save them into given file.
	 * 
	 * @param context
	 * @param source
	 * @param target 
	 * @return False if extraction failed.
	 */
	protected boolean downloadData(DPUContext context, AbstractSource source, File target) {
		String sessionID = null;
		
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpGet httpget = new HttpGet(source.getFilterUri());
		final HttpClientContext httpContext = HttpClientContext.create();
		try (CloseableHttpResponse response = httpclient.execute(httpget, httpContext)) {
			CookieStore cookieStore = httpContext.getCookieStore();
			Iterator<Cookie> iter = cookieStore.getCookies().iterator();
			while(iter.hasNext()) {
				final Cookie cookie = iter.next();
				if (cookie.getName().compareTo("JSESSIONID") == 0) {
					sessionID = cookie.getValue();
				}
			}
		} catch (IOException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Can't connect to filter page.", ex);
			return false;
		}
		
		final URL dataUrl;
		try {
			dataUrl = new URL(source.getDownloadUri(sessionID));
		} catch (MalformedURLException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Failed to create download URL.", ex);
			return false;
		}
		
		try {
			FileUtils.copyURLToFile(dataUrl, target);
		} catch (IOException ex) {
			context.sendMessage(DPUContext.MessageType.ERROR,
                    "Extraction failed", "Failed to download file.", ex);
			return false;
		}
		
		return true;
	}
	
}
