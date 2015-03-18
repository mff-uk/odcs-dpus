package cz.opendata.unifiedviews.dpus.ckan;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.files.simple.WritableSimpleFiles;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;


@DPU.AsLoader
public class CKANLoader extends AbstractDpu<CKANLoaderConfig> 
{

    private static final Logger logger = LoggerFactory.getLogger(CKANLoader.class);
    
    @DataUnit.AsOutput(name = "JSON")
    public WritableFilesDataUnit outFile;    

    @ExtensionInitializer.Init(param = "outFile")
    public WritableSimpleFiles outFileSimple;

    @DataUnit.AsInput(name = "metadata")
    public RDFDataUnit metadata;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public CKANLoader() {
        super(CKANLoaderDialog.class, ConfigHistory.noHistory(CKANLoaderConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException
    {
        logger.debug("Querying metadata");
     
        String datasetID = config.getDatasetID();
        String orgID = config.getOrgID();
        String apiURI = config.getApiUri();
        
        String datasetURI = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + CKANLoaderVocabulary.DCAT_DATASET_CLASS + ">}", "d");
        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"cs\"))}", "title");
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"cs\"))}", "description");
        String periodicity = executeSimpleSelectQuery("SELECT ?periodicity WHERE {<" + datasetURI + "> <"+ DCTERMS.ACCRUAL_PERIODICITY + "> ?periodicity }", "periodicity");
    	String temporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
    	String temporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
    	String spatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + datasetURI + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
    	String schemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + datasetURI + "> <"+ DCTERMS.REFERENCES + "> ?schema }", "schema");
    	String curatorName = executeSimpleSelectQuery("SELECT ?name WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.ADMS_CONTACT_POINT + ">/<" + CKANLoaderVocabulary.VCARD_NAME + "> ?name }", "name");
    	String contactPoint = executeSimpleSelectQuery("SELECT ?contact WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.ADMS_CONTACT_POINT + ">/<" + CKANLoaderVocabulary.VCARD_HAS_EMAIL + "> ?contact }", "contact");
    	String issued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + datasetURI + "> <"+ DCTERMS.ISSUED + "> ?issued }", "issued");
    	String modified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + datasetURI + "> <"+ DCTERMS.MODIFIED + "> ?modified }", "modified");
    	String license = executeSimpleSelectQuery("SELECT ?license WHERE {<" + datasetURI + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
    	String publisher_uri = executeSimpleSelectQuery("SELECT ?publisher_uri WHERE {<" + datasetURI + "> <"+ DCTERMS.PUBLISHER + "> ?publisher_uri }", "publisher_uri");
    	String publisher_name = executeSimpleSelectQuery("SELECT ?publisher_name WHERE {<" + datasetURI + "> <" + DCTERMS.PUBLISHER + ">/<" + FOAF.NAME +"> ?publisher_name }", "publisher_name");
    	
    	LinkedList<String> themes = new LinkedList<String>();
    	for (Map<String,Value> map: executeSelectQuery("SELECT ?theme WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.DCAT_THEME + "> ?theme }")) {
    		themes.add(map.get("theme").stringValue());
    	}
        
    	LinkedList<String> keywords = new LinkedList<String>();
    	for (Map<String,Value> map: executeSelectQuery("SELECT ?keyword WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.DCAT_KEYWORD + "> ?keyword }")) {
    		keywords.add(map.get("keyword").stringValue());
    	}
    	
    	LinkedList<String> distributions = new LinkedList<String>();
    	for (Map<String,Value> map: executeSelectQuery("SELECT ?distribution WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.DCAT_DISTRIBUTION + "> ?distribution }")) {
    		distributions.add(map.get("distribution").stringValue());
    	}

		boolean exists = false;
		Map<String, String> resUrlIdMap = new HashMap<String, String>();
		Map<String, String> resDistroIdMap = new HashMap<String, String>();
		
        logger.debug("Querying for the dataset in CKAN");
        CloseableHttpClient queryClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		HttpGet httpGet = new HttpGet(apiURI + "/" + datasetID);
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
	            		
	            		if (resourcesArray.getJSONObject(i).has("distro_url")) {
		            		String distro = resourcesArray.getJSONObject(i).getString("distro_url");
		            		resDistroIdMap.put(distro, id);
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
            //tags.put(keywords);
            for (String keyword : keywords) tags.put(keyword);
            
            JSONArray resources = new JSONArray();
            
            //JSONObject extras = new JSONObject();

            if (!datasetID.isEmpty()) root.put("name", datasetID);
            if (!title.isEmpty()) root.put("title", title);
            if (!description.isEmpty()) root.put("notes", description);
            if (!contactPoint.isEmpty()) root.put("maintainer_email", contactPoint);
            if (!curatorName.isEmpty()) root.put("maintainer", curatorName);
            if (!issued.isEmpty()) root.put("metadata_created", issued);
            if (!modified.isEmpty()) root.put("metadata_modified", modified);
            if (!publisher_uri.isEmpty()) root.put("publisher_uri", publisher_uri);
            if (!publisher_name.isEmpty()) root.put("publisher_name", publisher_name);
			
			//TODO: Matching?
			root.put("license_id", "other-open");
			root.put("license_link", license);
			
			if (!temporalStart.isEmpty()) root.put("temporal_start", temporalStart);
			if (!temporalEnd.isEmpty()) root.put("temporal_end", temporalEnd);
			if (!periodicity.isEmpty()) root.put("frequency", periodicity);
			if (!schemaURL.isEmpty()) root.put("schema", schemaURL);
			if (!spatial.isEmpty()) {
				root.put("ruian_type", "ST");
				root.put("ruian_code", 1);
				root.put("spatial_uri", spatial);
			}
			
			String concatThemes = "";
			for (String theme: themes) { concatThemes += theme + " ";}
			if (!concatThemes.isEmpty())  root.put("theme", concatThemes);
			
			
			//Distributions
			for (String distribution: distributions) {
				JSONObject distro = new JSONObject();
				
		        String dtitle = executeSimpleSelectQuery("SELECT ?title WHERE {<" + distribution + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"cs\"))}", "title");
		        String ddescription = executeSimpleSelectQuery("SELECT ?description WHERE {<" + distribution + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"cs\"))}", "description");
		    	String dtemporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + distribution + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
		    	String dtemporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + distribution + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
		    	String dspatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + distribution + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
		    	String dschemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <"+ CKANLoaderVocabulary.WDRS_DESCRIBEDBY + "> ?schema }", "schema");
		    	String dschemaType = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + distribution + "> <"+ CKANLoaderVocabulary.POD_DISTRIBUTION_DESCRIBREBYTYPE + "> ?schema }", "schema");
		    	String dissued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + distribution + "> <"+ DCTERMS.ISSUED + "> ?issued }", "issued");
		    	String dmodified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + distribution + "> <"+ DCTERMS.MODIFIED + "> ?modified }", "modified");
		    	String dlicense = executeSimpleSelectQuery("SELECT ?license WHERE {<" + distribution + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
		    	String dformat = executeSimpleSelectQuery("SELECT ?format WHERE {<" + distribution + "> <"+ DCTERMS.FORMAT + "> ?format }", "format");
		    	String dwnld = executeSimpleSelectQuery("SELECT ?dwnld WHERE {<" + distribution + "> <"+ CKANLoaderVocabulary.DCAT_DOWNLOADURL + "> ?dwnld }", "dwnld");

		    	// RDF SPECIFIC - VOID
		    	String sparqlEndpoint = executeSimpleSelectQuery("SELECT ?sparqlEndpoint WHERE {<" + distribution + "> <"+ CKANLoaderVocabulary.VOID_SPARQLENDPOINT + "> ?sparqlEndpoint }", "sparqlEndpoint");
		    	
		    	LinkedList<String> examples = new LinkedList<String>();
	        	for (Map<String,Value> map: executeSelectQuery("SELECT ?exampleResource WHERE {<" + distribution + "> <"+ CKANLoaderVocabulary.VOID_EXAMPLERESOURCE + "> ?exampleResource }")) {
	        		examples.add(map.get("exampleResource").stringValue());
	        	}

	        	if (!sparqlEndpoint.isEmpty()) {
	            //Start of Sparql Endpoint resource
	            JSONObject sparqlEndpointJSON = new JSONObject();
	            
	            sparqlEndpointJSON.put("name", "SPARQL Endpoint");
	            sparqlEndpointJSON.put("url", sparqlEndpoint);
	            sparqlEndpointJSON.put("format","api/sparql");
	            sparqlEndpointJSON.put("resource_type","api");
	            
	            if (resUrlIdMap.containsKey(sparqlEndpoint)) sparqlEndpointJSON.put("id", resUrlIdMap.get(sparqlEndpoint));
	            
	            resources.put(sparqlEndpointJSON);
	            // End of Sparql Endpoint resource
	            
	        	}
	        	
	            for (String example: examples)
	            {
		            // Start of Example resource text/turtle
		            JSONObject exTurtle = new JSONObject();
		            
		            exTurtle.put("format","example/turtle");
		            exTurtle.put("resource_type","file");
		            //exTurtle.put("description","Generated by Virtuoso FCT");
		            exTurtle.put("name","Example resource in Turtle");
		            
		            String exUrl;
		            
		            try {
						if (sparqlEndpoint.isEmpty()) exUrl = example;
						else exUrl = sparqlEndpoint + "?query=" + URLEncoder.encode("DESCRIBE <" + example + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8");
					} catch (UnsupportedEncodingException e) {
						exUrl = "";
						logger.error(e.getLocalizedMessage(), e);
					}
		            exTurtle.put("url", exUrl);
		            
		            if (resUrlIdMap.containsKey(exUrl)) exTurtle.put("id", resUrlIdMap.get(exUrl));
		            
		            resources.put(exTurtle);
		            // End of text/turtle resource

		            // Start of Example resource html
		            JSONObject exHTML = new JSONObject();
		            
		            exHTML.put("format","HTML");
		            exHTML.put("resource_type","file");
		            //exHTML.put("description","Generated by Virtuoso FCT");
		            exHTML.put("name","Example resource");
		            exHTML.put("url", example );

		            if (resUrlIdMap.containsKey(example)) exHTML.put("id", resUrlIdMap.get(example));

		            resources.put(exHTML);
		            // End of html resource
		            
	            }

	            // END OF RDF VOID SPECIFICS
				
		    	if (!dtitle.isEmpty()) distro.put("name", dtitle);
		    	if (!ddescription.isEmpty()) distro.put("description", ddescription);
		    	if (!dlicense.isEmpty()) distro.put("license_link", dlicense);
		    	if (!dtemporalStart.isEmpty()) distro.put("temporal_start", dtemporalStart);
		    	if (!dtemporalEnd.isEmpty()) distro.put("temporal_end", dtemporalEnd);
		    	if (!dschemaURL.isEmpty()) distro.put("describedBy", dschemaURL);
		    	if (!dschemaType.isEmpty()) distro.put("describedByType", dschemaType);
		    	if (!dformat.isEmpty()) distro.put("format", dformat);
		    	if (!dwnld.isEmpty()) distro.put("url", dwnld);
		    	if (!distribution.isEmpty()) distro.put("distro_url", distribution);
	            
		    	if (resDistroIdMap.containsKey(distribution)) distro.put("id", resDistroIdMap.get(distribution));
	            else if (resUrlIdMap.containsKey(dwnld)) distro.put("id", resUrlIdMap.get(dwnld));

		    	if (!dissued.isEmpty()) distro.put("issued", dissued);
		    	if (!dmodified.isEmpty()) distro.put("modified", dmodified);

				if (!dspatial.isEmpty()) {
					distro.put("ruian_type", "ST");
					distro.put("ruian_code", 1);
					distro.put("spatial_uri", dspatial);
				}
				
				resources.put(distro);
			}
			
			root.put("tags", tags);
			root.put("resources", resources);
			//root.put("extras", extras);
			
            if (!exists && config.isLoadToCKAN()) {
	            JSONObject createRoot = new JSONObject();
	            
	            createRoot.put("name", datasetID);
	            createRoot.put("title", title);
	            createRoot.put("owner_org", orgID);
				
	            logger.debug("Creating dataset in CKAN");
	            CloseableHttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
				HttpPost httpPost = new HttpPost(apiURI);
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
	                	ContextUtils.sendError(ctx, "Dataset already exists", "Dataset already exists: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
	                } else {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Response:" + ent);
	                	ContextUtils.sendError(ctx, "Error creating dataset", "Response while creating dataset: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
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
			
            String json = root.toString();
            
            File outfile = outFileSimple.create(config.getFilename());
            try {
				FileUtils.writeStringToFile(outfile, json, "UTF-8");
			} catch (IOException e) {
            	logger.error(e.getLocalizedMessage(), e);
			}
            
			if (!ctx.canceled() && config.isLoadToCKAN()) {
				logger.debug("Posting to CKAN");
				CloseableHttpClient client = HttpClients.createDefault();
	            URIBuilder uriBuilder = new URIBuilder(apiURI + "/" + datasetID);
	            HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
	            httpPost.addHeader(new BasicHeader("Authorization", config.getApiKey()));
	            
	            logger.trace(json);
	            
	            httpPost.setEntity(new StringEntity(json, Charset.forName("utf-8")));
	            
	            CloseableHttpResponse response = null;
	            
	            try {
	                response = client.execute(httpPost);
	                if (response.getStatusLine().getStatusCode() == 200) {
	                	logger.info("Response:" + EntityUtils.toString(response.getEntity()));
	                } else {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Response:" + ent);
	                	ContextUtils.sendError(ctx, "Error updating dataset", "Response while updating dataset: {0}: {1}", response.getStatusLine().getStatusCode(), ent);
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
