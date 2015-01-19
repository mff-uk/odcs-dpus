package cz.cuni.mff.xrg.uv.loader.graphstoreprotocol;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.openrdf.http.client.HTTPClient;
import org.openrdf.model.Literal;
import org.openrdf.query.Binding;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.FaultToleranceWrap;
import cz.cuni.mff.xrg.uv.boost.dpu.context.ContextUtils;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dpu.DPUContext;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsLoader
public class GraphStoreProtocol extends DpuAdvancedBase<GraphStoreProtocolConfig_V1> {

    private final static String QUERY_SIZE_BINDING = "size";

    private final static String QUERY_SIZE = "SELECT (count(*) as ?" + QUERY_SIZE_BINDING + " ) \nWHERE { GRAPH <%s> { ?s ?p ?o } }";

    private final static String QUERY_ADD_GRAPH = "ADD <%s> TO <%s>";
    
    private final static String QUERY_CLEAR_GRAPH = "CLEAR GRAPH <%s>";

    private final static String UPLOAD_GRAPH_PREFIX = "http://unifiedviews.eu/resource/graphStoreProtocol/uploadGraph/";

    private static final Logger LOG = LoggerFactory.getLogger(GraphStoreProtocol.class);

    static class AuthAwareHTTPClient extends HTTPClient {

        @Override
        protected void setUsernameAndPasswordForUrl(String username, String password, String url) {
            super.setUsernameAndPasswordForUrl(username, password, url);
            // Required by Virtuoso, othervise it fail for 401 (unauthorized).
            httpClient.getParams().setAuthenticationPreemptive(false);
        }

    }

    static class AuthAwareRepository extends SPARQLRepository {

        public AuthAwareRepository(String queryEndpointUrl, String updateEndpointUrl) {
            super(queryEndpointUrl, updateEndpointUrl);
        }

        @Override
        protected HTTPClient createHTTPClient() {
            // Use our own client.
            return new AuthAwareHTTPClient();
        }

    }


    /**
     * Remote repository - select.
     */
    private SPARQLRepository remoteRepository = null;

    @DataUnit.AsInput(name = "input")
    public FilesDataUnit inputFiles;

    public GraphStoreProtocol() {
        super(GraphStoreProtocolConfig_V1.class, AddonInitializer.create(new FaultToleranceWrap()));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final FaultToleranceWrap faultWrap = getAddon(FaultToleranceWrap.class);
        // Prepare remote repositories.
        remoteRepository = new AuthAwareRepository(config.getEndpointSelect(), config.getEndpointUpdate());
        if (config.isUseAuthentification()) {
            remoteRepository.setUsernameAndPassword(config.getUserName(), config.getPassword());
        }
        try {
            remoteRepository.initialize();
        } catch (RepositoryException ex) {
            throw new DPUException("Can't initialized remote repository.", ex);
        }
        // Get all input graphs.
        final List<FilesDataUnit.Entry> entries = new LinkedList<>();
        faultWrap.execute(new FaultToleranceWrap.Action() {

            @Override
            public void action() throws Exception {
                entries.clear();
                final FilesDataUnit.Iteration iter = inputFiles.getIteration();
                while (iter.hasNext()) {
                    entries.add(iter.next());
                }
            }
        });
        // Clear target graph.
        LOG.info("Clearing remote target graph '{}'", config.getTargetGraphURI());
        faultWrap.execute(new FaultToleranceWrap.Action() {

            @Override
            public void action() throws Exception {
                executeRemoteUpdate(String.format(QUERY_CLEAR_GRAPH, config.getTargetGraphURI()));
            }
        });
        // Upload each file ..
        int counter = 0;
        for (FilesDataUnit.Entry entry : entries) {
            LOG.info("Uploading entry {}/{}", counter++, entries.size());
            // Upload into same file.
            uploadFile(entry, config.getTargetGraphURI());
        }
    }

    @Override
    protected void innerCleanUp() {
        super.innerCleanUp();
        if (remoteRepository != null) {
            try {
                remoteRepository.shutDown();
            } catch (RepositoryException ex) {
                LOG.warn("Remote repository shut down failed.", ex);
            }
        }
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new GraphStoreProtocolVaadinDialog();
    }

    /**
     * Upload given file.
     *
     * @param fileEntry
     * @pram graphUri
     */
    private void uploadFile(final FilesDataUnit.Entry fileEntry, final String graphUri) throws DPUException {
        // Get wrap.
        final FaultToleranceWrap faultWrap = getAddon(FaultToleranceWrap.class);
        // Log size of remote graph.
        final long beforeRemoteGraphSize = getRemoteGraphSize(config.getTargetGraphURI());
        LOG.debug("Remote graph size: {}", beforeRemoteGraphSize);        
        final String uploadGraph;
        if (useTempGraph()) {
            // Prepare name for upload graph - some random.
            uploadGraph = UPLOAD_GRAPH_PREFIX + Long.toString((new Date()).getTime());
            LOG.info("Used temp graph: '{}'", uploadGraph);
            // Get size.
            final long beforeRemoteTempGraphSize = getRemoteGraphSize(uploadGraph);
            LOG.debug("Remote temp graph size: {}", beforeRemoteTempGraphSize);
        } else {
            uploadGraph = graphUri;
        }
        // Get file to upload.
        final File file = faultWrap.execute(new FaultToleranceWrap.ActionReturn<File>() {

            @Override
            public File action() throws Exception {
                return new File(java.net.URI.create(fileEntry.getFileURIString()));
            }
        });
        // Prepare target URL.
        final URL targetURL = prepareTargetURL(config.getEndpointCRUD());
        LOG.info("Target URL: '{}'", targetURL);
        // Upload file.
        faultWrap.execute(new FaultToleranceWrap.Action() {

            @Override
            public void action() throws Exception {
                uploadFile(targetURL, file, uploadGraph);
            }
        });
        if (useTempGraph()) {
            // Check upload file size.
            final long afterRemoteTempGraphSize = getRemoteGraphSize(uploadGraph);
            LOG.debug("Remote temp graph size: {}", afterRemoteTempGraphSize);
            // Copy remote files.
            LOG.info("Copying data from temp graph to target graph ...");
            faultWrap.execute(new FaultToleranceWrap.Action() {

                @Override
                public void action() throws Exception {
                    executeRemoteUpdate(String.format(QUERY_ADD_GRAPH, uploadGraph, graphUri));
                }
            });
            // Clear temp graph.
            LOG.info("Clearing remote temp graph ...");
            faultWrap.execute(new FaultToleranceWrap.Action() {

                @Override
                public void action() throws Exception {
                    executeRemoteUpdate(String.format(QUERY_CLEAR_GRAPH, uploadGraph));
                }
            });
        }
        // Get size of remote graph after add.
        final long afterRemoteGraphSize = getRemoteGraphSize(config.getTargetGraphURI());
        LOG.debug("Remote graph size: {}", afterRemoteGraphSize);
        ContextUtils.sendMessage(context, DPUContext.MessageType.INFO,
                "File uploaded '" + file.toString() + "'",
                "Graph size before: %d\nGraph size after: %d", beforeRemoteGraphSize, afterRemoteGraphSize);
    }
    
    /**
     * Execute remote query.
     * 
     * @param endpoitURL
     * @param query
     * @throws DPUException
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws UpdateExecutionException
     */
    private void executeRemoteUpdate(String query) throws DPUException, RepositoryException, QueryEvaluationException, UpdateExecutionException {
        RepositoryConnection connection = null;
        try {
            connection = remoteRepository.getConnection();
            LOG.debug("Executing update query on remote repository: {}", query);
            connection.prepareUpdate(QueryLanguage.SPARQL, query).execute();
        } catch (MalformedQueryException ex) {
            throw new DPUException("Can't execute remote graph size query.", ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException ex) {
                    LOG.warn("Can't close connection.", ex);
                }
            }
        }
    }

    /**
     *
     * @param endpointURL
     * @param graphURI
     * @return Size of given graph on given remote endpoint.
     */
    private long getRemoteGraphSize(final String graphURI) throws DPUException {
        final FaultToleranceWrap faultWrap = getAddon(FaultToleranceWrap.class);
        // Get size of remote graph.
        return faultWrap.execute(new FaultToleranceWrap.ActionReturn<Long>() {

            @Override
            public Long action() throws Exception {
                // Connect to remote repository.
                RepositoryConnection connection = null;
                try {
                    connection = remoteRepository.getConnection();
                    final TupleQueryResult result =
                            connection.prepareTupleQuery(QueryLanguage.SPARQL,
                                    String.format(QUERY_SIZE, graphURI) ).evaluate();
                    if (!result.hasNext()) {
                        // Empty result.
                        throw new DPUException("Remote query for size does not return any value.");
                    }
                    final Binding binding = result.next().getBinding(QUERY_SIZE_BINDING);
                    return ((Literal)binding.getValue()).longValue();
                } catch (MalformedQueryException ex) {
                    throw new DPUException("Can't execute remote graph size query.", ex);
                } finally {
                    if (connection != null) {
                        try {
                            connection.close();
                        } catch (RepositoryException ex) {
                            LOG.warn("Can't close connection.", ex);
                        }
                    }
                }
            }
        });
    }

    private URL prepareTargetURL(String base) throws DPUException {
        final String targetAsString = base;
        final URL targetURL;
        try {
            targetURL = new URL(targetAsString);
        } catch (MalformedURLException ex) {
            throw new DPUException("Malformed server uri.", ex);
        }
        return targetURL;
    }

   /**
     * Upload given file to given address.
     * 
     * @param url
     * @param fileToUpload
     * @throws IOException
     */
    private void uploadFile(URL url, File fileToUpload, String targetGraph) throws IOException, DPUException {
        final HttpClient httpClient = new HttpClient();
        if (config.isUseAuthentification()) {
            httpClient.getState().setCredentials(new AuthScope(url.getHost(), AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(config.getUserName(), config.getPassword()));
        }

        LOG.info("Uploading file to endpoint: {}", url.toString());

        PostMethod method = new PostMethod(url.toString());
        method.setParameter("Content-type", "application/xml");

        final List<Part> parts = new ArrayList<>();

        String fileFormat = "text/turtle";
        switch(config.getRepositoryType()) {
            case Virtuoso:
                // Virtuoso does not support chunked mode
                method.setContentChunked(false);
                // Target graph.
                parts.add(new StringPart("graph", targetGraph));
                break;
            case Fuseki:
                method.setContentChunked(true);
                // Target graph.
                parts.add(new StringPart("graph", targetGraph));
                break;
            case FusekiTrig: // Support for Fuseki 2.+
                method.setContentChunked(true);
                fileFormat = "text/trig";
                break;
        }
        // Add file.
        parts.add(new FilePart("res-file", fileToUpload, fileFormat, "UTF-8"));

        // final Part[] parts = {new FilePart("res-file", fileToUpload, "application/rdf+xml", "UTF-8") };  // Virtuoso - require "res-file"
        // final Part[] parts = {new FilePart(fileToUpload.getName(), fileToUpload, "text/turtle", "UTF-8"), new StringPart("graph", targetGraph) }; // Fuseki

        // FilePath.name = "rest-file" is required by Virtuso. Fuseky ignore this, so we can use it.
        //final Part[] parts = {new FilePart("res-file", fileToUpload, "text/turtle", "UTF-8"), new StringPart("graph", targetGraph) };

        final MultipartRequestEntity entity = new MultipartRequestEntity(
                parts.toArray(new Part[0]),
                method.getParams());
        method.setRequestEntity(entity);

        LOG.info("Response code: {}", httpClient.executeMethod(method));        
        LOG.info("Response: {}", method.getResponseBodyAsString());
    }

    private boolean useTempGraph() {
        return config.getRepositoryType() != GraphStoreProtocolConfig_V1.RepositoryType.FusekiTrig;
    }

}
