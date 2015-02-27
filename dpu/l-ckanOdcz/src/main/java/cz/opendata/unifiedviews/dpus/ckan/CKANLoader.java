package cz.opendata.unifiedviews.dpus.ckan;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUContext.MessageType;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.cuni.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.cuni.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.cuni.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.cuni.dpu.exec.AutoInitializer;
import eu.unifiedviews.helpers.cuni.extensions.FaultTolerance;
import eu.unifiedviews.helpers.cuni.migration.ConfigurationUpdate;
import eu.unifiedviews.helpers.cuni.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.cuni.rdf.sparql.SparqlUtils;

@DPU.AsLoader
public class CKANLoader extends AbstractDpu<CKANLoaderConfig> 
{

    private static final Logger logger = LoggerFactory.getLogger(CKANLoader.class);
    
    @DataUnit.AsInput(name = "metadata")
    public RDFDataUnit metadata;

    @AutoInitializer.Init
    public FaultTolerance faultTolerance;

    @AutoInitializer.Init(param = "cz.opendata.unifiedviews.dpus.ckan.CKANLoaderConfig")
    public ConfigurationUpdate _ConfigurationUpdate;

    public CKANLoader() {
        super(CKANLoaderDialog.class, ConfigHistory.noHistory(CKANLoaderConfig.class));
    }

    @Override
    protected void innerExecute() throws DPUException
    {
        java.util.Date date = new java.util.Date();
        long start = date.getTime();

        logger.debug("Querying metadata");
        
        String datasetURI = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + CKANLoaderVocabulary.DCAT_DATASET_CLASS + ">}", "d");
        String title = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"cs\"))}", "title");
        String description = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"cs\"))}", "description");
        String periodicity = executeSimpleSelectQuery("SELECT ?periodicity WHERE {<" + datasetURI + "> <"+ DCTERMS.ACCRUAL_PERIODICITY + "> ?periodicity }", "periodicity");
    	String temporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
    	String temporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + CKANLoaderVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
    	String spatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + datasetURI + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
    	String schemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + datasetURI + "> <"+ DCTERMS.REFERENCES + "> ?schema }", "schema");
    	String contactPoint = executeSimpleSelectQuery("SELECT ?contact WHERE {<" + datasetURI + "> <"+ CKANLoaderVocabulary.ADMS_CONTACT_POINT + ">/<" + CKANLoaderVocabulary.VCARD_HAS_EMAIL + "> ?contact }", "contact");
    	String issued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + datasetURI + "> <"+ DCTERMS.ISSUED + "> ?issued }", "issued");
    	String modified = executeSimpleSelectQuery("SELECT ?modified WHERE {<" + datasetURI + "> <"+ DCTERMS.MODIFIED + "> ?modified }", "modified");
    	String license = executeSimpleSelectQuery("SELECT ?license WHERE {<" + datasetURI + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
    	
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

    	logger.debug("Creating JSON");
        try {
            JSONObject root = new JSONObject();
            
            JSONArray tags = new JSONArray();
            //tags.put(keywords);
            for (String keyword : keywords) tags.put(keyword);
            
            JSONArray resources = new JSONArray();
            
            /* Start of Sparql Endpoint resource
            JSONObject sparqlEndpoint = new JSONObject();
            
            sparqlEndpoint.put("format","api/sparql");
            sparqlEndpoint.put("resource_type","api");
            
            resources.put(sparqlEndpoint);
            // End of Sparql Endpoint resource
            */
            
            // Start of VoID resource
            /*JSONObject voidJson = new JSONObject();
            
            voidJson.put("format","meta/void");
            voidJson.put("resource_type","file");
            voidJson.put("description","VoID description generated live");
            voidJson.put("name","VoID");
            voidJson.put("url", sparqlEndpointVoid + "?query=" + URLEncoder.encode("DESCRIBE <" + datasetURI + ">", "UTF-8") + "&output=" + URLEncoder.encode("text/turtle","UTF-8") );
            
            resources.put(voidJson);*/
            // End of VoID resource

            // Start of Dump resource
            /*JSONObject dump = new JSONObject();
            
            dump.put("format","application/x-trig");
            dump.put("resource_type","file");
            dump.put("description","Dump is a zipped TriG file");
            dump.put("name","Dump");
            dump.put("url", datadump );
            
            resources.put(dump);*/
            // End of Dump resource

            /*for (String example: examples)
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
	            
            }*/

            JSONObject extras = new JSONObject();

            if (!config.getDatasetID().isEmpty()) root.put("name", config.getDatasetID());
            root.put("url", datasetURI);
            root.put("title", title);
			root.put("notes", description);
			root.put("maintainer_email", contactPoint);
			root.put("metadata_created", issued);
			root.put("metadata_modified", modified);
			
			//TODO: Matching?
			root.put("license_id", "other-open");
			
			
			root.put("license_link", license);
			root.put("temporalStart", temporalStart);
			root.put("temporalEnd", temporalEnd);
			root.put("accrualPeriodicity", periodicity);
			
			//TODO: Spatial
			root.put("spatialNotation", "stát");
			root.put("spatialType", "1");
			
			String concatThemes = "";
			for (String theme: themes) { concatThemes += theme + " ";}
			root.put("themes", concatThemes);
			
			
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
				
		    	distro.put("name", dtitle);
		    	distro.put("description", ddescription);
		    	distro.put("license_link", dlicense);
		    	distro.put("temporalStart", dtemporalStart);
		    	distro.put("temporalEnd", dtemporalEnd);
		    	distro.put("describedBy", dschemaURL);
		    	distro.put("describedByType", dschemaType);
		    	distro.put("format", dformat);
		    	distro.put("url", dwnld);

		    	distro.put("created", dissued);
		    	distro.put("last_modified", dmodified);

		    	//TODO: Spatial
		    	distro.put("spatialNotation", "stát");
		    	distro.put("spatialType", "1");
				
				resources.put(distro);
			}
			
			root.put("tags", tags);
			root.put("resources", resources);
			root.put("extras", extras);
			
			boolean created = false;
			
			if (config.isCreateFirst()) {
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
	                	created = true;
	                } else if (response.getStatusLine().getStatusCode() == 409) {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Dataset already exists: " + ent);
	                } else {
	                	String ent = EntityUtils.toString(response.getEntity());
	                	logger.error("Response:" + ent);
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
			
			if (!ctx.canceled() && (!config.isCreateFirst() || created)) {
				logger.debug("Posting to CKAN");
				CloseableHttpClient client = HttpClients.createDefault();
	            URIBuilder uriBuilder = new URIBuilder(config.getApiUri() + "/" + config.getDatasetID());
	            HttpPost httpPost = new HttpPost(uriBuilder.build().normalize());
	            httpPost.addHeader(new BasicHeader("Authorization", config.getApiKey()));
	            
	            String json = root.toString();
	            
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
		}
        
        if (!ctx.canceled()) {
	        java.util.Date date2 = new java.util.Date();
	        long end = date2.getTime();
	
//	        ctx.sendMessage(DPUContext.MessageType.INFO, "Loaded in " + (end-start) + "ms");
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
        } else if (result.getResults().size() == 0) {
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
