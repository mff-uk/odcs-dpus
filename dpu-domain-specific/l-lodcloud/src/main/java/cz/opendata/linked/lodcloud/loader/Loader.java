package cz.opendata.linked.lodcloud.loader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.opendata.linked.lodcloud.loader.LoaderConfig.LinkCount;
import cz.opendata.linked.lodcloud.loader.LoaderConfig.MappingFile;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsLoader
public class Loader 
extends AbstractDpu<LoaderConfig> 
{

    private static final Logger logger = LoggerFactory.getLogger(Loader.class);
    
    @DataUnit.AsInput(name = "metadata")
    public RDFDataUnit metadata;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public Loader() {
        super(LoaderDialog.class,ConfigHistory.noHistory(LoaderConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException
    {
        logger.debug("Querying metadata");
        
        String datasetUrl = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + LoaderVocabulary.DCAT_DATASET_CLASS + ">}", "d");

    	List<Map<String, Value>> distributions = executeSelectQuery("SELECT ?distribution WHERE {<" + datasetUrl + "> <"+ LoaderVocabulary.DCAT_DISTRIBUTION + "> ?distribution . ?distribution <" + LoaderVocabulary.VOID_SPARQLENDPOINT + "> [] .  }");
    		
    	if (distributions.size() != 1) {
    		throw new DPUException("Expected 1 distribution with SPARQL endpoint. Found: " + distributions.size());
    	}
    	
    	String distribution = distributions.get(0).get("distribution").stringValue();
        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"en\"))}", "title");
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"en\"))}", "description");
    	String sparqlEndpointVoid = executeSimpleSelectQuery("SELECT ?sparqlEndpoint WHERE {<" + distribution + "> <"+ LoaderVocabulary.VOID_SPARQLENDPOINT + "> ?sparqlEndpoint }", "sparqlEndpoint");
    	String datadump = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <"+ LoaderVocabulary.VOID_DATADUMP + "> ?dwnld }", "dwnld");
    	String triplecount = executeSimpleSelectQuery("SELECT ?triplecount WHERE {<" + distribution + "> <"+ LoaderVocabulary.VOID_TRIPLES + "> ?triplecount }", "triplecount");
    	String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <"+ DCTERMS.FORMAT + "> ?format }", "format");
    	String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");

    	LinkedList<String> examples = new LinkedList<String>();
    	for (Map<String,Value> map: executeSelectQuery("SELECT ?exampleResource WHERE {<" + distribution + "> <"+ LoaderVocabulary.VOID_EXAMPLERESOURCE + "> ?exampleResource }")) {
    		examples.add(map.get("exampleResource").stringValue());
    	}

        logger.debug("Querying for the dataset in CKAN");
		boolean exists = false;
		Map<String, String> resUrlIdMap = new HashMap<String, String>();
		Map<String, String> resFormatIdMap = new HashMap<String, String>();
		
        CloseableHttpClient queryClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet httpGet = new HttpGet(config.getApiUri() + "/" + config.getDatasetID());
        CloseableHttpResponse queryResponse = null;
        try {
            queryResponse = queryClient.execute(httpGet);
            if (queryResponse.getStatusLine().getStatusCode() == 200) {
            	logger.info("Dataset found");
            	exists = true;
            	
            	JSONObject response = new JSONObject(EntityUtils.toString(queryResponse.getEntity()));
            	JSONArray resourcesArray = response.getJSONArray("resources"); 
            	for (int i = 0; i < resourcesArray.length(); i++ )
            	{
					try {
	            		String id = resourcesArray.getJSONObject(i).getString("id");
	            		String url = resourcesArray.getJSONObject(i).getString("url");
	            		resUrlIdMap.put(url, id);
					
	            		if (resourcesArray.getJSONObject(i).has("format")) {
		            		String format = resourcesArray.getJSONObject(i).getString("format");
		            		resFormatIdMap.put(format, id);
	            		}
					
					} catch (JSONException e) {
			        	logger.error(e.getLocalizedMessage(), e);
					}
            	}
            	
            } else {
            	String ent = EntityUtils.toString(queryResponse.getEntity());
            	logger.info("Dataset not found: " + ent);
            }
        } catch (ClientProtocolException e) {
        	logger.error(e.getLocalizedMessage(), e);
		} catch (IOException e) {
        	logger.error(e.getLocalizedMessage(), e);
		} catch (ParseException e) {
        	logger.error(e.getLocalizedMessage(), e);
		} catch (JSONException e) {
        	logger.error(e.getLocalizedMessage(), e);
		} finally {
            if (queryResponse != null) {
                try {
					queryResponse.close();
					queryClient.close();
				} catch (IOException e) {
	            	logger.error(e.getLocalizedMessage(), e);
				}
            }
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
            for (String s : config.getAdditionalTags()) tags.put(s);
            
            JSONArray resources = new JSONArray();
            
            // Start of Sparql Endpoint resource
            JSONObject sparqlEndpoint = new JSONObject();
            
            sparqlEndpoint.put("format","api/sparql");
            sparqlEndpoint.put("resource_type","api");
            sparqlEndpoint.put("description", config.getSparqlEndpointDescription());
            sparqlEndpoint.put("name", config.getSparqlEndpointName());
            sparqlEndpoint.put("url", sparqlEndpointVoid);
            
            if (resFormatIdMap.containsKey("api/sparql")) sparqlEndpoint.put("id", resFormatIdMap.get("api/sparql"));
            
            resources.put(sparqlEndpoint);
            // End of Sparql Endpoint resource
            
            // Start of VoID resource
            JSONObject voidJson = new JSONObject();
            
            voidJson.put("format","meta/void");
            voidJson.put("resource_type","file");
            voidJson.put("description","VoID description generated live");
            voidJson.put("name","VoID");
            String voidUrl = sparqlEndpointVoid + "?query=" + URLEncoder.encode("DESCRIBE <" + distribution + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8"); 
            voidJson.put("url", voidUrl);
            
            if (resFormatIdMap.containsKey("meta/void")) voidJson.put("id", resFormatIdMap.get("meta/void"));

            resources.put(voidJson);
            // End of VoID resource

            if (config.getVocabTag() != LoaderConfig.VocabTags.NoProprietaryVocab && !config.getSchemaUrl().isEmpty()) {
	            // Start of RDFS/OWL schema resource
	            JSONObject schemaResource = new JSONObject();
	            
	            schemaResource.put("format","meta/rdf-schema");
	            schemaResource.put("resource_type","file");
	            schemaResource.put("description","RDFS/OWL Schema with proprietary vocabulary");
	            schemaResource.put("name","RDFS/OWL schema");
	            schemaResource.put("url", config.getSchemaUrl() );
	            
	            if (resFormatIdMap.containsKey("meta/rdf-schema")) schemaResource.put("id", resFormatIdMap.get("meta/rdf-schema"));

	            resources.put(schemaResource);
	            // End of RDFS/OWL schema resource
            }

            // Start of Dump resource
            JSONObject dump = new JSONObject();
            
            dump.put("format", dformat);
            dump.put("resource_type","file");
            //dump.put("description","Dump is a zipped TriG file");
            dump.put("name","Dump");
            dump.put("url", datadump );
            
            if (resUrlIdMap.containsKey(datadump)) dump.put("id", resUrlIdMap.get(datadump));
            
            resources.put(dump);
            // End of Dump resource

            for (String example: examples)
            {
	            // Start of Example resource text/turtle
	            JSONObject exTurtle = new JSONObject();
	            
	            exTurtle.put("format","example/turtle");
	            exTurtle.put("resource_type","file");
	            //exTurtle.put("description","Generated by Virtuoso FCT");
	            exTurtle.put("name","Example resource in Turtle");
	            String exTurtleUrl = sparqlEndpointVoid + "?query=" + URLEncoder.encode("DESCRIBE <" + example + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8"); 
	            exTurtle.put("url", exTurtleUrl );
	            
	            if (resUrlIdMap.containsKey(exTurtleUrl)) exTurtle.put("id", resUrlIdMap.get(exTurtleUrl));
	            
	            resources.put(exTurtle);
	            // End of text/turtle resource

	            // Start of Example resource html
	            JSONObject exHTML = new JSONObject();
	            
	            exHTML.put("format","HTML");
	            exHTML.put("resource_type","file");
	            exHTML.put("description","Generated by Virtuoso FCT");
	            exHTML.put("name","Example resource in Virtuoso FCT");
	            exHTML.put("url", example );
	            
	            if (resUrlIdMap.containsKey(example)) exHTML.put("id", resUrlIdMap.get(example));

	            resources.put(exHTML);
	            // End of html resource
	            
	            // Mapping file resources
	            for (MappingFile mapping: config.getMappingFiles()) {
		            JSONObject exMapping = new JSONObject();
		            
		            String mappingMime = "mapping/" + mapping.getMappingFormat();
		            exMapping.put("format",mappingMime);
		            exMapping.put("resource_type","file");
		            exMapping.put("description","Schema mapping file in " + mapping.getMappingFormat() + " format.");
		            exMapping.put("name","Mapping " + mapping.getMappingFormat());
		            exMapping.put("url", mapping.getMappingFile() );
		            
		            if (resFormatIdMap.containsKey(mappingMime)) exMapping.put("id", resFormatIdMap.get(mappingMime));
		            
		            resources.put(exMapping);
	            }
	            // End of mapping file resources
	            
            }

            JSONObject extras = new JSONObject();
            extras.put("triples", triplecount);
            if (!config.getShortname().isEmpty()) extras.put("shortname", config.getShortname());
            if (!config.getNamespace().isEmpty()) extras.put("namespace", config.getNamespace());
            if (!dlicense.isEmpty()) extras.put("license_link", dlicense);
            extras.put("sparql_graph_name", datasetUrl);
            for (LinkCount link: config.getLinks()) {
            	extras.put("links:" + link.getTargetDataset(), link.getLinkCount());            	
            }

            if (!config.getDatasetID().isEmpty()) root.put("name", config.getDatasetID());
            root.put("url", datasetUrl);
            root.put("title", title);
			if (!config.getMaintainerName().isEmpty()) root.put("maintainer", config.getMaintainerName());
			if (!config.getMaintainerEmail().isEmpty()) root.put("maintainer_email", config.getMaintainerEmail());
			root.put("license_id", config.getLicense_id());
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
	        
	        if (!exists) {
	            JSONObject createRoot = new JSONObject();
	            
	            createRoot.put("name", config.getDatasetID());
	            createRoot.put("title", title);
	            createRoot.put("owner_org", config.getOrgID());
				
	            logger.debug("Creating dataset in CKAN");
	            CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
				HttpPost httpPost = new HttpPost(config.getApiUri());
				httpPost.addHeader(new BasicHeader("Authorization", config.getApiKey()));
	            
	            String json = createRoot.toString();
	            
	            logger.debug("Creating dataset with: " + json);
	            
	            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));
	            
	            CloseableHttpResponse response = null;
	            
	            try {
	                response = client.execute(httpPost);
	                if (response.getStatusLine().getStatusCode() == 201) {
	                	logger.info("Dataset created OK");
	                	logger.info("Response: " + EntityUtils.toString(response.getEntity()));
	                } else if (response.getStatusLine().getStatusCode() == 409) {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Dataset already exists: " + ent);
	                	ContextUtils.sendError(ctx, "Dataset already exists", "Dataset already exists: %s: %s", response.getStatusLine().getStatusCode(), ent);
	                } else {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Response:" + ent);
	                	ContextUtils.sendError(ctx, "Error creating dataset", "Response while creating dataset: %s: %s", response.getStatusLine().getStatusCode(), ent);
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
		                	ContextUtils.sendError(ctx, "Error creating dataset", e.getLocalizedMessage());
						}
	                }
	            }
			}
			
			if (!ctx.canceled()) {
				logger.debug("Posting to CKAN");
				CloseableHttpClient client = HttpClients.createDefault();
	            URIBuilder uriBuilder = new URIBuilder(config.getApiUri() + "/" + config.getDatasetID());
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
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Response:" + ent);
	                	ContextUtils.sendError(ctx, "Error updating dataset", "Response while updating dataset: %s: %s", response.getStatusLine().getStatusCode(), ent);
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
		                	ContextUtils.sendError(ctx, "Error updating dataset", e.getLocalizedMessage());
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

    }
    
    private String executeSimpleSelectQuery(final String queryAsString, String bindingName) throws DPUException {
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlSelectObject query = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlSelectObject>() {

                    @Override
                    public SparqlUtils.SparqlSelectObject action() throws Exception {
                        return SparqlUtils.createSelect(queryAsString,
                                DataUnitUtils.getEntries(metadata, RDFDataUnit.Entry.class));
                    }
                });
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(metadata, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                result.prepare();
                SparqlUtils.execute(connection, ctx, query, result);
            }
        });
        if (result.getResults().size() == 1) {
            try {
                return result.getResults().get(0).get(bindingName).stringValue();
            } catch (NumberFormatException ex) {
                throw new DPUException(ex);
            }
        } else if (result.getResults().isEmpty()) {
        	return "";
        } else {
            throw new DPUException("Unexpected number of results: " + result.getResults().size() );
        }
    }

    private List<Map<String, Value>> executeSelectQuery(final String queryAsString) throws DPUException {
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlSelectObject query = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlSelectObject>() {

                    @Override
                    public SparqlUtils.SparqlSelectObject action() throws Exception {
                        return SparqlUtils.createSelect(queryAsString,
                                DataUnitUtils.getEntries(metadata, RDFDataUnit.Entry.class));
                    }
                });
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(metadata, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                result.prepare();
                SparqlUtils.execute(connection, ctx, query, result);
            }
        });
        
        return result.getResults();
    }

}
