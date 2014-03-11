package cz.opendata.linked.metadata.form;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
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
import cz.cuni.mff.xrg.odcs.rdf.interfaces.RDFDataUnit;

@AsExtractor
public class Extractor 
extends ConfigurableBase<ExtractorConfig> 
implements DPU, ConfigDialogProvider<ExtractorConfig> {

	/**
	 * DPU's configuration.
	 */

	private Logger logger = LoggerFactory.getLogger(DPU.class);
	
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

		URI datasetURI = out.createURI(config.datasetURI.toString());
		URI exResURI = out.createURI("http://rdfs.org/ns/void#exampleResource");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		out.addTriple(datasetURI, RDF.TYPE, out.createURI("http://rdfs.org/ns/void#Dataset"));
		if (config.desc_cs != null)	out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_cs, "cs"));
		if (config.desc_en != null) out.addTriple(datasetURI, DCTERMS.DESCRIPTION, out.createLiteral(config.desc_en, "en"));
		if (config.title_cs != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.title_cs, "cs"));
		if (config.desc_en != null) out.addTriple(datasetURI, DCTERMS.TITLE, out.createLiteral(config.title_en, "en"));
		if (config.dataDump != null) out.addTriple(datasetURI, out.createURI("http://rdfs.org/ns/void#dataDump"), out.createURI(config.dataDump.toString()));
		if (config.sparqlEndpoint != null) out.addTriple(datasetURI, out.createURI("http://rdfs.org/ns/void#sparqlEndpoint"), out.createURI(config.sparqlEndpoint.toString()));
		
		for (URL u : config.authors) { out.addTriple(datasetURI, DCTERMS.CREATOR, out.createURI(u.toString()));	}
		for (URL u : config.publishers)	{ out.addTriple(datasetURI, DCTERMS.PUBLISHER, out.createURI(u.toString())); }
		for (URL u : config.licenses) { out.addTriple(datasetURI, DCTERMS.LICENSE, out.createURI(u.toString())); }
		for (URL u : config.exampleResources) { out.addTriple(datasetURI, exResURI, out.createURI(u.toString())); }
		for (URL u : config.sources) { out.addTriple(datasetURI, DCTERMS.SOURCE, out.createURI(u.toString())); }

		if (config.useNow) {
			out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(new Date()), out.createURI("http://www.w3.org/2001/XMLSchema#date")));
		}
		else out.addTriple(datasetURI, DCTERMS.MODIFIED, out.createLiteral(df.format(config.modified), out.createURI("http://www.w3.org/2001/XMLSchema#date")));
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();

		ctx.sendMessage(MessageType.INFO, "Done in " + (end-start) + "ms");

	}

	@Override
	public void cleanUp() {	}

}
