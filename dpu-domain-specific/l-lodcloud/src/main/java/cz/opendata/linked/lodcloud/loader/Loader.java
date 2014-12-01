package cz.opendata.linked.lodcloud.loader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.opendata.linked.lodcloud.loader.LoaderConfig.LinkCount;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;

@DPU.AsLoader
public class Loader 
extends DpuAdvancedBase<LoaderConfig> 
{

    private static final Logger logger = LoggerFactory.getLogger(Loader.class);
    
    @DataUnit.AsInput(name = "metadata")
    public RDFDataUnit metadata;

    public Loader() {
        super(LoaderConfig.class,AddonInitializer.create(new SimpleRdfConfigurator<>(Loader.class)));
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {        
        return new LoaderDialog();
    }

    @Override
    protected void innerExecute() throws DPUException, OperationFailedException
    {
        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        logger.debug("Querying metadata");
        
        final SimpleRdfRead addrValueFacWrap = SimpleRdfFactory.create(metadata, context);
        
        String datadump = "";
        String triplecount = "";
        String datasetUrl = "";
        String sparqlEndpointVoid = "";
        String description = "";
        String title = "";
        
        String queryString = "PREFIX dcterms: <http://purl.org/dc/terms/> PREFIX void: <http://rdfs.org/ns/void#> SELECT ?title ?description ?triples ?dump ?datasetURL ?sparqlEndpoint WHERE {?datasetURL a void:Dataset; dcterms:description ?description; dcterms:title ?title; void:sparqlEndpoint ?sparqlEndpoint; void:dataDump ?dump ; void:triples ?triples . FILTER(lang(?description) = \"en\") FILTER(lang(?title) = \"en\")} ";
        try (ConnectionPair<TupleQueryResult> query = addrValueFacWrap.executeSelectQuery(queryString))        
        {
        	final TupleQueryResult result = query.getObject();
            if (result.hasNext()) {
	        	BindingSet first = result.next();
	            datadump = first.getValue("dump").stringValue();
	            triplecount = first.getValue("triples").stringValue();
	            datasetUrl = first.getValue("datasetURL").stringValue();
	            sparqlEndpointVoid = first.getValue("sparqlEndpoint").stringValue();
	            description = first.getValue("description").stringValue();
	            title = first.getValue("title").stringValue();
            }
            else { throw new DPUException("Required metadata missing from input"); }
        } catch (NumberFormatException | QueryEvaluationException e) {
            logger.error("Failed to query and parse value.", e);
        }
        if (description.isEmpty() || datadump.isEmpty() || triplecount.isEmpty() || datasetUrl.isEmpty() || sparqlEndpointVoid.isEmpty())
        {
        	throw new DPUException("Required metadata missing from input. Datadump: " + datadump + "Title: " + title + " Triplecount: " + triplecount + " URL: " + datasetUrl + " Endpoint: " + sparqlEndpointVoid + " Description: " + description);
        }
        logger.debug("Querying for sample resources");

        List<String> examples = new LinkedList<String>();
        String queryStringExamples = "PREFIX void: <http://rdfs.org/ns/void#> SELECT ?example WHERE {[] a void:Dataset; void:exampleResource ?example .} ";
        try (ConnectionPair<TupleQueryResult> query = addrValueFacWrap.executeSelectQuery(queryStringExamples))        
        {
            final TupleQueryResult result = query.getObject();
            while (result.hasNext()) {
            	examples.add(result.next().getValue("example").stringValue());
            }
        } catch (NumberFormatException | QueryEvaluationException e) {
            logger.error("Failed to query and parse value.", e);
        }

        logger.debug("Creating JSON");
        try {
            JSONObject root = new JSONObject();
            
            JSONArray tags = new JSONArray();
            tags.put("lod");
            tags.put(config.getVocabTag().toString());
            tags.put(config.getVocabMappingTag().toString());
            tags.put(config.getPublishedTag().toString());
            tags.put(config.getProvenanceMetadataTag().toString());
            tags.put(config.getLicenseMetadataTag().toString());
            if (config.isLimitedSparql()) tags.put("limited-sparql-endpoint");
            if (config.isLodcloudNolinks()) tags.put("lodcloud.nolinks");
            if (config.isLodcloudUnconnected()) tags.put("lodcloud.unconnected");
            if (config.isLodcloudNeedsInfo()) tags.put("lodcloud.needsinfo");
            if (config.isLodcloudNeedsFixing()) tags.put("lodcloud.needsfixing");
            for (String prefix : config.getVocabularies()) { tags.put("format-" + prefix); }
            tags.put(config.getTopic());
            
            JSONArray resources = new JSONArray();
            
            // Start of Sparql Endpoint resource
            JSONObject sparqlEndpoint = new JSONObject();
            
            sparqlEndpoint.put("format","api/sparql");
            sparqlEndpoint.put("resource_type","api");
            sparqlEndpoint.put("description", config.getSparqlEndpointDescription());
            sparqlEndpoint.put("name", config.getSparqlEndpointName());
            sparqlEndpoint.put("url", sparqlEndpointVoid);
            
            resources.put(sparqlEndpoint);
            // End of Sparql Endpoint resource
            
            // Start of VoID resource
            JSONObject voidJson = new JSONObject();
            
            voidJson.put("format","meta/void");
            voidJson.put("resource_type","file");
            voidJson.put("description","VoID description generated live");
            voidJson.put("name","VoID");
            voidJson.put("url", sparqlEndpointVoid + "?query=" + URLEncoder.encode("DESCRIBE <" + datasetUrl + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8") );
            
            resources.put(voidJson);
            // End of VoID resource

            // Start of Dump resource
            JSONObject dump = new JSONObject();
            
            dump.put("format","application/x-trig");
            dump.put("resource_type","file");
            dump.put("description","Dump is a zipped TriG file");
            dump.put("name","Dump");
            dump.put("url", datadump );
            
            resources.put(dump);
            // End of Dump resource

            for (String example: examples)
            {
	            // Start of Example resource text/turtle
	            JSONObject exTurtle = new JSONObject();
	            
	            exTurtle.put("format","example/turtle");
	            exTurtle.put("resource_type","file");
	            exTurtle.put("description","Generated by Virtuoso FCT");
	            exTurtle.put("name","Example resource in Turtle");
	            exTurtle.put("url", sparqlEndpointVoid + "?query=" + URLEncoder.encode("DESCRIBE <" + example + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8") );
	            
	            resources.put(exTurtle);
	            // End of text/turtle resource

	            // Start of Example resource html
	            JSONObject exHTML = new JSONObject();
	            
	            exHTML.put("format","HTML");
	            exHTML.put("resource_type","file");
	            exHTML.put("description","Generated by Virtuoso FCT");
	            exHTML.put("name","Example resource in Virtuoso FCT");
	            exHTML.put("url", example );
	            
	            resources.put(exHTML);
	            // End of html resource
            }

            JSONObject extras = new JSONObject();
            extras.put("triples", triplecount);
            if (!config.getShortname().isEmpty()) extras.put("shortname", config.getShortname());
            if (!config.getNamespace().isEmpty()) extras.put("namespace", config.getNamespace());
            if (!config.getCustomLicenseLink().isEmpty()) extras.put("license_link", config.getCustomLicenseLink());
            //if (!config.getSparql_graph_name().isEmpty()) extras.put("sparql_graph_name", config.getSparql_graph_name());
            extras.put("sparql_graph_name", datasetUrl);
            for (LinkCount link: config.getLinks()) {
            	extras.put("links:" + link.getTargetDataset(), link.getLinkCount());            	
            }
            

            if (!config.getDatasetID().isEmpty()) root.put("name", config.getDatasetID());
            root.put("url", datasetUrl);
            root.put("title", title);
			if (!config.getMaintainerName().isEmpty()) root.put("maintainer", config.getMaintainerName());
			if (!config.getMaintainerEmail().isEmpty()) root.put("maintainer_email", config.getMaintainerEmail());
			//root.put("private", String.valueOf(config.isDatasetPrivate()));
			root.put("license_id", config.getLicense_id());
			//if (!config.getDatasetDescription().isEmpty()) root.put("notes", config.getDatasetDescription());
			root.put("notes", description);
			if (!config.getAuthorName().isEmpty()) root.put("author", config.getAuthorName());
			if (!config.getAuthorEmail().isEmpty()) root.put("author_email", config.getAuthorEmail());
			
			if (config.isVersionGenerated()) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date versiondate = new Date();
				String version = dateFormat.format(versiondate);
				root.put("version", version);
			} 
			else if (!config.getVersion().isEmpty()) root.put("version", config.getVersion());
			
			root.put("tags", tags);
			root.put("resources", resources);
			root.put("extras", extras);
			
			if (!context.canceled()) {
				logger.debug("Posting to datahub.io");
				CloseableHttpClient client = HttpClients.createDefault();
	            URIBuilder uriBuilder = new URIBuilder(config.getApiUri() + config.getDatasetID());
	            HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
	            httpPost.addHeader(new BasicHeader("Authorization", config.getApiKey()));
	            
	            String json = root.toString();
	            
	            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));
	            
	            CloseableHttpResponse response = null;
	            
	            try {
	                response = client.execute(httpPost);
	                if (response.getStatusLine().getStatusCode() == 200) {
	                	logger.info("Response:" + EntityUtils.toString(response.getEntity()));
	                } else {
	                	logger.error("Response:" + EntityUtils.toString(response.getEntity()));
	                }
	            } catch (ClientProtocolException e) {
	            	logger.error(e.getLocalizedMessage(), e);
				} catch (IOException e) {
	            	logger.error(e.getLocalizedMessage(), e);
				} finally {
	                if (response != null) {
	                    try {
							response.close();
							client.close();
						} catch (IOException e) {
			            	logger.error(e.getLocalizedMessage(), e);
						}
	                }
	            }
			}
		} catch (JSONException e) {
			logger.error(e.getLocalizedMessage(), e);
		} catch (URISyntaxException e) {
        	logger.error(e.getLocalizedMessage(), e);
		} catch (UnsupportedEncodingException e) {
        	logger.error(e.getLocalizedMessage(), e);
		}
        
        if (!context.canceled()) {
	        java.util.Date date2 = new java.util.Date();
	        long end = date2.getTime();
	
	        context.sendMessage(DPUContext.MessageType.INFO, "Loaded in " + (end-start) + "ms");
        }

    }

}
