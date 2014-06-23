package cz.opendata.linked.cz.ruian;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.dataunit.file.FileDataUnit;
import cz.cuni.mff.xrg.scraper.css_parser.utils.BannedException;
import cz.cuni.mff.xrg.scraper.css_parser.utils.Cache;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

	@OutputDataUnit(name = "XMLObce")
	public FileDataUnit outObce;	
	
	@OutputDataUnit(name = "XMLZsj")
	public FileDataUnit outZsj;	

	public Extractor(){
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext ctx) throws DPUException
	{
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(ctx.getUserDirectory() + "/cache/");
		Cache.logger = LOG;
		//Cache.rewriteCache = config.rewriteCache;
		Scraper_parser s = new Scraper_parser();
		s.logger = LOG;
		s.ctx = ctx;
		s.obce = outObce;
		s.zsj = outZsj;
		s.outputFiles = config.isPassToOutput();

		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Download

		try {
			URL init, initStat;
			if (config.isInclGeoData())
			{
				init = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=K&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
				initStat = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=K&vf.vu=Z&_vf.vu=on&vf.vu=G&_vf.vu=on&vf.vu=H&_vf.vu=on&_vf.vu=on&search=Vyhledat");
			}
			else
			{
				init = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
				initStat = new URL("http://vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&search=Vyhledat");
			}
			if (config.isRewriteCache())
			{
				Path path, pathStat;
				if (config.isInclGeoData())
				{
					path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=K&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
					pathStat = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku?vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=K&vf.vu=Z&_vf.vu=on&vf.vu=G&_vf.vu=on&vf.vu=H&_vf.vu=on&_vf.vu=on&search=Vyhledat");
				}
				else
				{
					path = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=OB&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&vf.uo=A&search=Vyhledat");
					pathStat = Paths.get(ctx.getUserDirectory().getAbsolutePath() + "/cache/vdp.cuzk.cz/vdp/ruian/vymennyformat/seznamlinku@vf.pu=S&_vf.pu=on&_vf.pu=on&vf.cr=U&vf.up=ST&vf.ds=Z&vf.vu=Z&_vf.vu=on&_vf.vu=on&_vf.vu=on&_vf.vu=on&search=Vyhledat");
				}
				LOG.info("Deleting " + path);
				Files.deleteIfExists(path);
				LOG.info("Deleting " + pathStat);
				Files.deleteIfExists(pathStat);				
			}
			
			try {
				s.parse(init, "init");
				s.parse(initStat, "initStat");
			} catch (BannedException b) {
				LOG.warn("Seems like we are banned for today");
			}
			
        	LOG.info("Download done.");
		
		} catch (IOException e) {
			LOG.error("IOException", e);
		} catch (InterruptedException e) {
			LOG.error("Interrupted");
		}
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Processed in " + (end-start) + "ms");

	}

}
