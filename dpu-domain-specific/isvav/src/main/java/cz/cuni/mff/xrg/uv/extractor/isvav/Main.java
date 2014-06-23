package cz.cuni.mff.xrg.uv.extractor.isvav;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.odcs.dataunit.file.handlers.DirectoryHandler;
import cz.cuni.mff.xrg.uv.extractor.isvav.source.*;
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

@AsExtractor
public class Main extends ConfigurableBase<Configuration>
		implements ConfigDialogProvider<Configuration> {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	
	@OutputDataUnit(name = "output", description = "Contains one or more zip files.")
	public FileDataUnit output;
	
	public Main() {
		super(Configuration.class);
	}

	@Override
	public void execute(DPUContext context)
			throws DPUException, DataUnitException {
		// create sources
		List<AbstractSource> usedSource = createSource();
		// for each source
		int index = 0;
		DirectoryHandler root = output.getRootDir();
		for (AbstractSource source : usedSource) {
			final String filename = String.format("%s-%d.zip", 
					source.getBaseFileName(), index++);
			final File file = root.addNewFile(filename).asFile();
			// download file
			if (!downloadData(context, source, file)) {
				// extraction failed
				return;
			} else {
				context.sendMessage(MessageType.INFO, "File extracted", 
						"Extracted file saved into: " + filename);
			}
		}
	}

	@Override
	public AbstractConfigDialog<Configuration> getConfigurationDialog() {
		return new Dialog();
	}

	/**
	 * Create sources for given data type
	 * 
	 * @return 
	 */
	private List<AbstractSource> createSource() {
		List<AbstractSource> sources = new LinkedList<>();
		switch (config.getSourceType()) {
			case Funder:
				sources.add(new SourceFunder());
				break;
			case Organization:
				sources.add(new SourceOrganization());
				break;
			case Programme:
				// 1991 - 2019
				sources.add(new SourceProgramme("1991", "2019"));
				break;
			case Project:
				sources.add(new SourceProject());
				break;
			case Research:
				sources.add(new SourceResearch());
				break;
			case Result:
				Calendar now = Calendar.getInstance();
				final int from = 1991;
				final int to = now.get(Calendar.YEAR);
				LOG.debug("Extracting from {} to {}", from, to);
				for (Integer year = from; year <= to; ++year) {
					sources.add(new SourceResult(year.toString()));
				}
				break;
			case Tender:
				sources.add(new SourceTender());
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
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(source.getFilterUri());
		
		HttpClientContext httpContext = HttpClientContext.create();
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
			context.sendMessage(MessageType.ERROR, "Extraction failed", "Can't connect to filter page.", ex);
			return false;
		}
		
		URL dataUrl;
		try {
			dataUrl = new URL(source.getDownloadUri(sessionID));
		} catch (MalformedURLException ex) {
			context.sendMessage(MessageType.ERROR, "Extraction failed", "Failed to create download URL.", ex);
			return false;
		}
		
		try {
			FileUtils.copyURLToFile(dataUrl, target);
		} catch (IOException ex) {
			context.sendMessage(MessageType.ERROR, "Extraction failed", "Failed to download file.", ex);
			return false;
		}
		
		return true;
	}
	
}
