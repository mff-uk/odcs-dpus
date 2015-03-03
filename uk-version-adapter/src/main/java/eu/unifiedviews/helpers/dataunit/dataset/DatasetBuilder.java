package eu.unifiedviews.helpers.dataunit.dataset;

import java.util.LinkedHashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.query.impl.DatasetImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic creational class for creating {@link Dataset} instances in .withX() notation.
 * <p>
 * Example usage:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * Set&lt;URI&gt; someSet = obtainRemoveGraphsSomehow();
 * Query query = connection.prepareQuery();
 * query.setDataset(new DatasetBuilder()
 *         .withInsertGraph(&quot;http://default&quot;)
 *         .withDefaultRemoveGraphs(someSet)
 *         .addDefaultGraph(&quot;http://nondefault&quot;)
 *         .build());
 * query.evaluate();
 * </pre>
 * 
 * </blockquote>
 * </p>
 * <p>
 * Fields are by default empty (empty set or null in case of insert graph).
 */
public class DatasetBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetBuilder.class);

    private Set<URI> defaultRemoveGraphs = new LinkedHashSet<URI>();

    private URI defaultInsertGraph;

    private Set<URI> defaultGraphs = new LinkedHashSet<URI>();;

    private Set<URI> namedGraphs = new LinkedHashSet<URI>();

    public DatasetBuilder() {
    }

    /**
     * Add all provided graphs to an existing set of defaultRemoveGraphs. Does not replace previously added graphs.
     * 
     * @param defaultRemoveGraphs
     *            graph URIs to add to defaultRemoveGraphs property
     * @return this
     */
    public DatasetBuilder withDefaultRemoveGraphs(Set<URI> defaultRemoveGraphs) {
        this.defaultRemoveGraphs.addAll(defaultRemoveGraphs);
        return this;
    }

    /**
     * Add provided graph to an existing set of defaultRemoveGraphs. Does not replace previously added graphs.
     * 
     * @param defaultRemoveGraph
     *            graph URI to add to defaultRemoveGraphs property
     * @return this
     */
    public DatasetBuilder addDefaultRemoveGraph(URI defaultRemoveGraph) {
        this.defaultRemoveGraphs.add(defaultRemoveGraph);
        return this;
    }

    /**
     * Sets defaultInsertGraph property (rewrites previous value)
     * 
     * @param defaultInsertGraph
     *            graph URI to set to defaultInsertGraph property
     * @return this
     */
    public DatasetBuilder withInsertGraph(URI defaultInsertGraph) {
        this.defaultInsertGraph = defaultInsertGraph;
        return this;
    }

    /**
     * Add all provided graphs to an existing set of defaultGraphs. Does not replace previously added graphs.
     * 
     * @param defaultGraphs
     *            graph URIs to add to defaultGraphs property
     * @return this
     */
    public DatasetBuilder withDefaultGraphs(Set<URI> defaultGraphs) {
        this.defaultGraphs.addAll(defaultGraphs);
        return this;
    }

    /**
     * @deprecated Do not use. Wrong name! Will be removed in future versions.
     * @param defaultGraph
     * @return this
     */
    @Deprecated
    public DatasetBuilder addDefaultGraphs(URI defaultGraph) {
        this.defaultGraphs.add(defaultGraph);
        return this;
    }

    /**
     * Add provided graph to an existing set of defaultGraphs. Does not replace previously added graphs.
     * 
     * @param defaultGraph
     *            graph URI to add to defaultGraphs property
     * @return this
     */
    public DatasetBuilder addDefaultGraph(URI defaultGraph) {
        this.defaultGraphs.add(defaultGraph);
        return this;
    }

    /**
     * Add all provided graphs to an existing set of namedGraphs. Does not replace previously added graphs.
     * 
     * @param namedGraphs
     *            graph URIs to add to namedGraphs property
     * @return this
     */
    public DatasetBuilder withNamedGraphs(Set<URI> namedGraphs) {
        this.namedGraphs.addAll(namedGraphs);
        return this;
    }

    /**
     * Add provided graph to an existing set of namedGraphs. Does not replace previously added graphs.
     * 
     * @param namedGraph
     *            graph URI to add to namedGraphs property
     * @return this
     */
    public DatasetBuilder addNamedGraph(URI namedGraph) {
        this.namedGraphs.add(namedGraph);
        return this;
    }

    /**
     * Build the {@link Dataset} instance from current state of builder class.
     * 
     * @return mutable {@link Dataset} implementation which is independent of this class (deep copies values)
     */
    public Dataset build() {
        DatasetImpl dataset = new DatasetImpl();

        LOG.info("DatasetBuilder.build: <{}>", defaultInsertGraph);

        LOG.info("\tremove:");
        for (URI graphURI : defaultRemoveGraphs) {
            dataset.addDefaultRemoveGraph(graphURI);
            LOG.info("\t\t{}", graphURI.toString());
        }

        dataset.setDefaultInsertGraph(defaultInsertGraph);
        LOG.info("\tdefault:");
        for (URI graphURI : defaultGraphs) {
            dataset.addDefaultGraph(graphURI);
            LOG.info("\t\t{}", graphURI.toString());
        }
        LOG.info("\tnamed:");
        for (URI graphURI : namedGraphs) {
            dataset.addNamedGraph(graphURI);
            LOG.info("\t\t{}", graphURI.toString());
        }

        return dataset;
    }
}
