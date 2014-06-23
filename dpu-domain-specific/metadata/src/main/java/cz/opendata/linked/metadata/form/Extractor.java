package cz.opendata.linked.metadata.form;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
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
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.*;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

@AsTransformer
public class Extractor 
	extends ConfigurableBase<ExtractorConfig> 
	implements DPU, ConfigDialogProvider<ExtractorConfig> {

	private static final Logger LOG = LoggerFactory.getLogger(DPU.class);
	
	@InputDataUnit(name = "Statistics", optional = true)
	public RDFDataUnit stats;

	@InputDataUnit(name = "Input_data", optional = true)
	public RDFDataUnit in;

	@OutputDataUnit(name = "Metadata")
	public WritableRDFDataUnit out;
	
	private SimpleRdfRead inWrap;
	
	private SimpleRdfWrite outWrap;
	
	public Extractor() {
		super(ExtractorConfig.class);
	}

	@Override
	public AbstractConfigDialog<ExtractorConfig> getConfigurationDialog() {		
		return new ExtractorDialog();
	}

	@Override
	public void execute(DPUContext ctx) throws DPUException, DataUnitException
	{
		java.util.Date date = new java.util.Date();
		long start = date.getTime();

		//Void dataset and DCAT dataset
		String ns_dcat = "http://www.w3.org/ns/dcat#";
		String ns_foaf = "http://xmlns.com/foaf/0.1/";
		String ns_void = "http://rdfs.org/ns/void#";
		String ns_qb = "http://purl.org/linked-data/cube#";

		outWrap = new SimpleRdfWrite(out, ctx);
		outWrap.setPolicy(AddPolicy.BUFFERED);
		
		final ValueFactory valueFactory = outWrap.getValueFactory();
		
		URI foaf_agent = valueFactory.createURI(ns_foaf + "Agent");
		URI qb_DataSet = valueFactory.createURI(ns_qb + "DataSet");
		URI dcat_keyword = valueFactory.createURI(ns_dcat + "keyword");
		URI dcat_distribution = valueFactory.createURI(ns_dcat + "distribution");
		URI dcat_downloadURL = valueFactory.createURI(ns_dcat + "downloadURL");
		URI dcat_mediaType = valueFactory.createURI(ns_dcat + "mediaType");
		URI dcat_theme = valueFactory.createURI(ns_dcat + "theme");
		URI xsd_date = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#date");
		URI xsd_integer = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#integer");
		URI dcat_distroClass = valueFactory.createURI(ns_dcat + "Distribution");
		URI dcat_datasetClass = valueFactory.createURI(ns_dcat + "Dataset");
		URI void_datasetClass = valueFactory.createURI(ns_void + "Dataset");
		URI void_triples = valueFactory.createURI(ns_void + "triples");
		URI void_entities = valueFactory.createURI(ns_void + "entities");
		URI void_classes = valueFactory.createURI(ns_void + "classes");
		URI void_properties = valueFactory.createURI(ns_void + "properties");
		URI void_dSubjects = valueFactory.createURI(ns_void + "distinctSubjects");
		URI void_dObjects = valueFactory.createURI(ns_void + "distinctObjects");

		URI datasetURI = valueFactory.createURI(config.getDatasetURI().toString());
		URI distroURI = valueFactory.createURI(config.getDistroURI().toString());
		URI exResURI = valueFactory.createURI(ns_void + "exampleResource");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		outWrap.add(datasetURI, RDF.TYPE, void_datasetClass);
		outWrap.add(datasetURI, RDF.TYPE, dcat_datasetClass);
		if (config.isIsQb()) outWrap.add(datasetURI, RDF.TYPE, qb_DataSet);
		if (config.getDesc_cs() != null) outWrap.add(datasetURI, DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_cs(), "cs"));
		if (config.getDesc_en() != null) outWrap.add(datasetURI, DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_en(), "en"));
		if (config.getTitle_cs() != null) outWrap.add(datasetURI, DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_cs(), "cs"));
		if (config.getTitle_en() != null) outWrap.add(datasetURI, DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_en(), "en"));
		if (config.getDataDump() != null) outWrap.add(datasetURI, valueFactory.createURI(ns_void + "dataDump"), valueFactory.createURI(config.getDataDump().toString()));
		if (config.getSparqlEndpoint() != null) outWrap.add(datasetURI, valueFactory.createURI(ns_void + "sparqlEndpoint"), valueFactory.createURI(config.getSparqlEndpoint().toString()));
		
		for (URL u : config.getAuthors()) { outWrap.add(datasetURI, DCTERMS.CREATOR, valueFactory.createURI(u.toString()));	}
		for (URL u : config.getPublishers())	{ 
			URI publisherURI = valueFactory.createURI(u.toString());
			outWrap.add(datasetURI, DCTERMS.PUBLISHER, publisherURI); 
			outWrap.add(publisherURI, RDF.TYPE, foaf_agent);
			//TODO: more publisher data?
		}
		for (URL u : config.getLicenses()) { outWrap.add(datasetURI, DCTERMS.LICENSE, valueFactory.createURI(u.toString())); }
		for (URL u : config.getExampleResources()) { outWrap.add(datasetURI, exResURI, valueFactory.createURI(u.toString())); }
		for (URL u : config.getSources()) { outWrap.add(datasetURI, DCTERMS.SOURCE, valueFactory.createURI(u.toString())); }
		for (String u : config.getKeywords()) { outWrap.add(datasetURI, dcat_keyword, valueFactory.createLiteral(u.toString())); }
		for (URL u : config.getLanguages()) { outWrap.add(datasetURI, DCTERMS.LANGUAGE, valueFactory.createURI(u.toString())); }
		for (URL u : config.getThemes()) { 
			URI themeURI = valueFactory.createURI(u.toString());
			outWrap.add(datasetURI, dcat_theme, themeURI);
			outWrap.add(themeURI, RDF.TYPE, SKOS.CONCEPT);
			outWrap.add(themeURI, SKOS.IN_SCHEME, valueFactory.createURI("http://linked.opendata.cz/resource/catalog/Themes"));
		}

		if (config.isUseNow()) {
			outWrap.add(datasetURI, DCTERMS.MODIFIED, valueFactory.createLiteral(df.format(new Date()), xsd_date));
		}
		else outWrap.add(datasetURI, DCTERMS.MODIFIED, valueFactory.createLiteral(df.format(config.getModified()), xsd_date));

		outWrap.add(datasetURI, dcat_distribution, distroURI);

		//DCAT Distribution
		
		outWrap.add(distroURI, RDF.TYPE, dcat_distroClass);
		if (config.getDesc_cs() != null)	outWrap.add(distroURI, DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_cs(), "cs"));
		if (config.getDesc_en() != null) outWrap.add(distroURI, DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_en(), "en"));
		if (config.getTitle_cs() != null) outWrap.add(distroURI, DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_cs(), "cs"));
		if (config.getTitle_en() != null) outWrap.add(distroURI, DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_en(), "en"));
		if (config.getDataDump() != null) outWrap.add(distroURI, dcat_downloadURL, valueFactory.createURI(config.getDataDump().toString()));
		if (config.getDataDump() != null) outWrap.add(distroURI, dcat_mediaType, valueFactory.createLiteral(config.getMime()));
		for (URL u : config.getLicenses()) { outWrap.add(distroURI, DCTERMS.LICENSE, valueFactory.createURI(u.toString())); }
		
		if (config.isUseNow()) {
			outWrap.add(distroURI, DCTERMS.MODIFIED, valueFactory.createLiteral(df.format(new Date()), xsd_date));
		}
		else {
			outWrap.add(distroURI, DCTERMS.MODIFIED, valueFactory.createLiteral(df.format(config.getModified()), xsd_date));
		}
		
		if (stats != null) {
			ctx.sendMessage(MessageType.INFO, "Found statistics on input - copying");
			out.addAll(stats);
		}
		else if (in != null) {
			inWrap = new SimpleRdfRead(in, ctx);
			//Now compute statistics on input data
			ctx.sendMessage(MessageType.INFO, "Starting statistics computation");
			executeCountQuery("SELECT (COUNT (*) as ?count) WHERE {?s ?p ?o}", void_triples, datasetURI);
			executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s a ?t}", void_entities, datasetURI);
			executeCountQuery("SELECT (COUNT (distinct ?t) as ?count) WHERE {?s a ?t}", void_classes, datasetURI);
			executeCountQuery("SELECT (COUNT (distinct ?p) as ?count) WHERE {?s ?p ?o}", void_properties, datasetURI);
			executeCountQuery("SELECT (COUNT (distinct ?s) as ?count) WHERE {?s ?p ?o}", void_dSubjects, datasetURI);
			executeCountQuery("SELECT (COUNT (distinct ?o) as ?count) WHERE {?s ?p ?o}", void_dObjects, datasetURI);
			ctx.sendMessage(MessageType.INFO, "Statistics computation done");
			//Done computing statistics
		}
		
		outWrap.flushBuffer();
		
		java.util.Date date2 = new java.util.Date();
		long end = date2.getTime();
		ctx.sendMessage(MessageType.INFO, "Done in " + (end-start) + "ms");
	}
	
	void executeCountQuery(String countQuery, URI property, URI datasetURI) throws OperationFailedException
	{
		final ValueFactory valueFactory = inWrap.getValueFactory();		
		URI xsd_integer = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#integer");
		int number;

		try (ConnectionPair<TupleQueryResult> res = inWrap.executeSelectQuery(countQuery)) {
			number = Integer.parseInt(res.getObject().next().getValue("count")
					.stringValue());
			outWrap.add(datasetURI, property, valueFactory.createLiteral(Integer
					.toString(number), xsd_integer));
		} catch (QueryEvaluationException e) {
			LOG.error("Failed to execute query", e);
		}
	}
		
	@Override
	public void cleanUp() {	}

}
