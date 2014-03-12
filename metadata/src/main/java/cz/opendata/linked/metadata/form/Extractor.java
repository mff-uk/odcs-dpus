package cz.opendata.linked.metadata.form;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsTransformer
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	private Logger logger = LoggerFactory.getLogger(DPU.class);
	
	@InputDataUnit(name = "Input data")
	public RDFDataUnit in;	

	@OutputDataUnit(name = "Metadata")
	public RDFDataUnit out;	
	
	public Extractor() {
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	public void execute(DPUContext ctx) throws DPUException
	{
		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Void dataset and DCAT dataset
		String ns_dcat = "http://www.w3.org/ns/dcat#";
		String ns_foaf = "http://xmlns.com/foaf/0.1/";
		String ns_void = "http://rdfs.org/ns/void#";

		URI foaf_agent = out.createURI(ns_foaf + "Agent");
		URI dcat_keyword = out.createURI(ns_dcat + "keyword");
		URI dcat_distribution = out.createURI(ns_dcat + "distribution");
		URI dcat_downloadURL = out.createURI(ns_dcat + "downloadURL");
		URI dcat_mediaType = out.createURI(ns_dcat + "mediaType");
		URI dcat_theme = out.createURI(ns_dcat + "theme");
		URI xsd_date = out.createURI("http://www.w3.org/2001/XMLSchema#date");
		URI dcat_distroClass = out.createURI(ns_dcat + "Distribution");
		URI dcat_datasetClass = out.createURI(ns_dcat + "Dataset");
		URI void_datasetClass = out.createURI(ns_void + "Dataset");

		URI datasetURI = out.createURI(config.datasetURI.toString());
		URI distroURI = out.createURI(config.distroURI.toString());
		URI exResURI = out.createURI(ns_void + "exampleResource");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		out.addTriple(datasetURI, RDF.TYPE, void_datasetClass);
		out.addTriple(datasetURI, RDF.TYPE, dcat_datasetClass);
		if (config.desc_cs != null)	out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_cs, "cs"));
		if (config.desc_en != null) out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_en, "en"));
		if (config.title_cs != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.title_cs, "cs"));
		if (config.title_en != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.title_en, "en"));
		if (config.dataDump != null) out.addTriple(datasetURI, out.createURI(ns_void + "dataDump"), out.createURI(config.dataDump.toString()));
		if (config.sparqlEndpoint != null) out.addTriple(datasetURI, out.createURI(ns_void + "sparqlEndpoint"), out.createURI(config.sparqlEndpoint.toString()));
		
		for (URL u : config.authors) { out.addTriple(datasetURI, DCTERMS.CREATOR, out.createURI(u.toString()));	}
		for (URL u : config.publishers)	{ 
			URI publisherURI = out.createURI(u.toString());
			out.addTriple(datasetURI, DCTERMS.PUBLISHER, publisherURI); 
			out.addTriple(publisherURI, RDF.TYPE, foaf_agent);
			//TODO: more publisher data?
		}
		for (URL u : config.licenses) { out.addTriple(datasetURI, DCTERMS.LICENSE, out.createURI(u.toString())); }
		for (URL u : config.exampleResources) { out.addTriple(datasetURI, exResURI, out.createURI(u.toString())); }
		for (URL u : config.sources) { out.addTriple(datasetURI, DCTERMS.SOURCE, out.createURI(u.toString())); }
		for (String u : config.keywords) { out.addTriple(datasetURI, dcat_keyword, out.createLiteral(u.toString())); }
		for (URL u : config.languages) { out.addTriple(datasetURI, DCTERMS.LANGUAGE, out.createURI(u.toString())); }
		for (URL u : config.themes) { 
			URI themeURI = out.createURI(u.toString());
			out.addTriple(datasetURI, dcat_theme, themeURI);
			out.addTriple(themeURI, RDF.TYPE, SKOS.CONCEPT);
			out.addTriple(themeURI, SKOS.IN_SCHEME, out.createURI("http://linked.opendata.cz/resource/catalog/Themes"));
		}

		if (config.useNow) {
			out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(new Date()), xsd_date));
		}
		else out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(config.modified), xsd_date));

		out.addTriple(datasetURI, dcat_distribution, distroURI);

		//DCAT Distribution
		
		out.addTriple(distroURI, RDF.TYPE, dcat_distroClass);
		if (config.desc_cs != null)	out.addTriple(distroURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_cs, "cs"));
		if (config.desc_en != null) out.addTriple(distroURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_en, "en"));
		if (config.title_cs != null) out.addTriple(distroURI, DCTERMS.TITLE, out.createLiteral(config.title_cs, "cs"));
		if (config.title_en != null) out.addTriple(distroURI, DCTERMS.TITLE, out.createLiteral(config.title_en, "en"));
		if (config.dataDump != null) out.addTriple(distroURI, dcat_downloadURL, out.createURI(config.dataDump.toString()));
		if (config.dataDump != null) out.addTriple(distroURI, dcat_mediaType, out.createLiteral(config.mime));
		for (URL u : config.licenses) { out.addTriple(distroURI, DCTERMS.LICENSE, out.createURI(u.toString())); }
		
		if (config.useNow) {
			out.addTriple(distroURI, DCTERMS.MODIFIED, out.createLiteral(df.format(new Date()), xsd_date));
		}
		else out.addTriple(distroURI, DCTERMS.MODIFIED, out.createLiteral(df.format(config.modified), xsd_date));
		
		//Now compute statistics on input data
		
		
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();
		ctx.sendMessage(MessageType.INFO, "Done in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
