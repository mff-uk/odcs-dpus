package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
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

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

@DPU.AsTransformer
public class GraphMerge extends DpuAdvancedBase<GraphMergeConfig_V1> {

	private static final Logger LOG = LoggerFactory.getLogger(GraphMerge.class);

    /**
     * %s - from
     * %s - to
     */
    private static final String COPY_QUERY = "ADD <%s> TO <%s>";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;
    
    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

	public GraphMerge() {
		super(GraphMergeConfig_V1.class, AddonInitializer.noAddons());
	}
		
    @Override
    protected void innerExecute() throws DPUException {
        // Get list of input graphs.
        final List<URI> inputGraphs = new LinkedList<>();
        try {
            final RDFDataUnit.Iteration iter = rdfInput.getIteration();
            while (iter.hasNext()) {
                inputGraphs.add(iter.next().getDataGraphURI());
            }
        } catch (DataUnitException ex) {
            throw new DPUException("Can't gent input graphs.", ex);
        }
        // Prepare output graph.
        final URI targetGraph;
        try {
            targetGraph = rdfOutput.addNewDataGraph(generateOutputSymbolicName());
        } catch (DataUnitException ex) {
            throw new DPUException("Can't add output graph.", ex);
        }
        // Copy data from input graphs to output.
        RepositoryConnection connection;
        try {
            connection = rdfOutput.getConnection();
        } catch (DataUnitException ex) {
            throw new DPUException("Can't get output connection.", ex);
        }
        try {
            int counter = 1;
            for (URI sourceGraph : inputGraphs) {
                final String query = String.format(COPY_QUERY,
                        sourceGraph.stringValue(), targetGraph.stringValue());
                // In case of failure quit. No failure tolerance here.
                LOG.info("Merging {}/{}", counter++, inputGraphs.size());
                LOG.debug("Merging {} into {}", sourceGraph.stringValue(), targetGraph.stringValue());
                LOG.debug("Query {}", query);
                try {
                    connection.prepareUpdate(QueryLanguage.SPARQL, query).execute();
                } catch (MalformedQueryException | UpdateExecutionException ex) {
                    throw new DPUException("Problem with query.", ex);
                } catch (RepositoryException ex) {
                    throw new DPUException("Can't execute query due to a problem with repository.", ex);
                }
            }
        } finally {
            try {
                connection.close();
            } catch (RepositoryException ex) {
                LOG.error("Can't close connection.");
            }
        }
        // TODO Add metadata here!
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new GraphMergeVaadinDialog();
    }

    /**
     *
     * @return New and unique output graph name.
     */
    private String generateOutputSymbolicName() {
        return "GraphMerge/output/generated-" + Long.toString((new Date()).getTime());
    }
	
}
