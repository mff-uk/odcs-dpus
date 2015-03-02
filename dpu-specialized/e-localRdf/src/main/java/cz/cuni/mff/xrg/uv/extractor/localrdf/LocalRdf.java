package cz.cuni.mff.xrg.uv.extractor.localrdf;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.Update;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

@DPU.AsExtractor
public class LocalRdf extends DpuAdvancedBase<LocalRdfConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRdf.class);

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit output;

    @DataUnit.AsOutput(name = "outputFiles")
    public WritableFilesDataUnit outputFiles;

    public LocalRdf() {
        super(LocalRdfConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void innerExecute() throws DPUException {
        final String sourceUriString = "http://unifiedviews.eu/resource/internal/dataunit/exec/"
                + config.getExecution() + "/dpu/"
                + config.getDpu() + "/du/"
                + config.getDataUnit();
        RepositoryConnection connection = null;
        try {
            connection = output.getConnection();
            // Determine type.
            final String askIsFiles = "ASK FROM <http://unifiedviews.eu/resource/internal/data>\n"
                    + "WHERE {\n"
                    + "<" + sourceUriString + "> <http://unifiedviews.eu/ontology/internal/data/write> ?g\n"
                    + "GRAPH ?g { ?s <http://unifiedviews.eu/DataUnit/MetadataDataUnit/FilesDataUnit/fileURI> ?o }\n"
                    + "}";
            LOG.info("Used detectionquery: {}", askIsFiles);
            final URI targetUri;
            if (connection.prepareBooleanQuery(QueryLanguage.SPARQL, askIsFiles).evaluate()) {
                // Is File.
                SendMessage.sendInfo(context, "Files data unite detected.", "");
                targetUri = outputFiles.getMetadataWriteGraphname();
            } else {
                SendMessage.sendInfo(context, "RDF data unite detected.", "");
                targetUri = output.getMetadataWriteGraphname();
            }

            // Prepare copy query.
            final String copyQuery = "WITH <" + targetUri.stringValue() + ">\n"
                    + "INSERT {?s ?p ?o}\n"
                    + "USING <" + sourceUriString + ">\n"
                    + "WHERE {?s ?p ?o}";
            LOG.info("Copy data {} -> {}", sourceUriString, targetUri.stringValue());
            LOG.info("Used query: {}", copyQuery);

            final Update update = connection.prepareUpdate(QueryLanguage.SPARQL, copyQuery);
            update.execute();
        }  catch (DataUnitException | MalformedQueryException | RepositoryException | 
                UpdateExecutionException | QueryEvaluationException ex) {
            throw new DPUException("Can't copy data.", ex);
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

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new LocalRdfVaadinDialog();
    }

}
