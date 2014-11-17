package cz.opendata.linked.psp_cz.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.css_parser.utils.Cache;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
//import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
//import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
//import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

@DPU.AsExtractor
public class Extractor 
extends DpuAdvancedBase<ExtractorConfig> {

	@DataUnit.AsOutput(name = "output")
	public WritableFilesDataUnit outputFiles;

	private static final Logger LOG = LoggerFactory.getLogger(Extractor.class);

	public Extractor(){
		super(ExtractorConfig.class, AddonInitializer.noAddons());
	}

        @Override
        public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
            return new ExtractorDialog();
        }
        
//	@Override
//	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
//		return new ExtractorDialog();
//	}

         @Override
        protected void innerExecute() throws DPUException, DataUnitException {
             
              LOG.info("DPU is running ...");
             
       		// vytvorime si parser
		Cache.setInterval(config.getInterval());
		Cache.setTimeout(config.getTimeout());
		Cache.setBaseDir(context.getUserDirectory() + "/cache/");
		Cache.rewriteCache = config.isRewriteCache();
		Cache.logger = LOG;
		String tempfilename = context.getWorkingDir() + "/" + config.getOutputFileName();
		Parser s = new Parser();
		s.logger = LOG;
		s.context = context;
		try {
			s.ps = new PrintStream(tempfilename, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			LOG.error("Failed to write stream into file", e);
		}

		s.ps.println(
				"@prefix dcterms:  <http://purl.org/dc/terms/> .\n" +
						//"@prefix rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
						//"@prefix rdfs:     <http://www.w3.org/2000/01/rdf-schema#> .\n" +
						"@prefix xsd:      <http://www.w3.org/2001/XMLSchema#> .\n" +
						"\n" +
						"@prefix frbr:     <http://purl.org/vocab/frbr/core#> .\n" +
						"@prefix odcs:     <http://linked.opendata.cz/ontology/odcs/temp/psp.cz-metadata/> .\n" +
						"@prefix lex:      <http://purl.org/lex#> .\n"

				);
		

		// a spustim na vychozi stranku

		context.sendMessage(MessageType.INFO, "Starting extraction. From year: " + config.getStart_year() + " To: " + config.getEnd_year() + " Output: " + tempfilename);
		
		try {
			for (int i = config.getStart_year(); i <= config.getEnd_year(); i++)
			{   
				if (context.canceled())
				{
					LOG.error("Interrupted");
					break;
				}
				try {
					java.util.Date date = new java.util.Date();
					long start = date.getTime();
					if (!config.isCachedLists())
					{
						Path path = Paths.get(context.getUserDirectory().getAbsolutePath() + "/cache/www.psp.cz/sqw/sbirka.sqw@r=" + i);
						LOG.info("Deleting " + path);
						Files.deleteIfExists(path);
					}
					s.parse(new URL("http://www.psp.cz/sqw/sbirka.sqw?r=" + i), "list");
					java.util.Date date2 = new java.util.Date();
					long end = date2.getTime();
					
					context.sendMessage(MessageType.INFO, "Processed " + i + " in " + (end-start) + " ms");
				}
				catch (IOException e) {
					LOG.error(e.getLocalizedMessage());
				}
			}
                        LOG.info("Parsing done. Putting parsed file to the output");
                        
                        File newFileToBeAdded = new File(tempfilename);
                        outputFiles.addExistingFile(newFileToBeAdded.getName(), newFileToBeAdded.toURI().toASCIIString());
                        
                           	
//			LOG.info("Parsing done. Passing RDF to ODCS");
//			SimpleRdfWrite outputWrap = SimpleRdfFactory.create(outputDataUnit, context);
//                        outputWrap.
//			outputWrap.extract(new File(tempfilename), RDFFormat.TURTLE, null);
		} catch (InterruptedException e) {
			LOG.error("Interrupted");
		}

		s.ps.close();
	}

   
}
