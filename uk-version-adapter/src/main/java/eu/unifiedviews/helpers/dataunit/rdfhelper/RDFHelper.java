package eu.unifiedviews.helpers.dataunit.rdfhelper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.Dataset;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dataunit.dataset.DatasetBuilder;

/**
 * Helper to make various tasks with {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} friendly.
 */
public class RDFHelper {
    /**
     * Exhaust {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit#getIteration()}) into one {@link Map} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} contains milions or more entries, storing all of this in single {@link Map} is not a good idea.
     * Only suitable for work with ~100000 of entries (graphs)
     *
     * @param rdfDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Map} containing all entries, symbolic names are used as keys
     * @throws DataUnitException
     */
    public static Map<String, RDFDataUnit.Entry> getGraphsMap(RDFDataUnit rdfDataUnit) throws DataUnitException {
        if (rdfDataUnit == null) {
            return new LinkedHashMap<>();
        }
        RDFDataUnit.Iteration iteration = rdfDataUnit.getIteration();
        Map<String, RDFDataUnit.Entry> resultMap = new LinkedHashMap<>();
        try {
            while (iteration.hasNext()) {
                RDFDataUnit.Entry entry = iteration.next();
                resultMap.put(entry.getSymbolicName(), entry);
            }
        } finally {
            iteration.close();
        }
        return resultMap;
    }

    /**
     * Exhaust {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit#getIteration()}) into one {@link Set} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} contains milions or more entries, storing all of this in single {@link Set} is not a good idea.
     * Only suitable for work with ~100000 of entries (graphs)
     *
     * @param rdfDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Set} containing all entries
     * @throws DataUnitException
     */
    public static Set<RDFDataUnit.Entry> getGraphs(RDFDataUnit rdfDataUnit) throws DataUnitException {
        if (rdfDataUnit == null) {
            return new LinkedHashSet<>();
        }
        RDFDataUnit.Iteration iteration = rdfDataUnit.getIteration();
        Set<RDFDataUnit.Entry> resultSet = new LinkedHashSet<>();
        try {
            while (iteration.hasNext()) {
                RDFDataUnit.Entry entry = iteration.next();
                resultSet.add(entry);
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }

    /**
     * Exhaust {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit#getIteration()}) into one {@link Set} of graph URIs (throw away symbolic names).
     * Beware - if the {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} contains milions or more entries, storing all of this in single {@link Set} is not a good idea.
     * Only suitable for work with ~100000 of entries (graphs)
     * <p>
     * Useful for feeding {@link Dataset} class (together with {@link DatasetBuilder}):
     * <p><blockquote><pre>
     * query.setDataset(new DatasetBuilder().withNamedGraphs(RDFHelper.getGraphsURISet(inputDataUnit)).build())
     * </pre></blockquote></p>
     * @param rdfDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Set} containing all graphs from the rdfDataUnit
     * @throws DataUnitException
     */
    public static Set<URI> getGraphsURISet(RDFDataUnit rdfDataUnit) throws DataUnitException {
        if (rdfDataUnit == null) {
            return new LinkedHashSet<>();
        }
        RDFDataUnit.Iteration iteration = rdfDataUnit.getIteration();
        Set<URI> resultSet = new LinkedHashSet<>();
        try {
            while (iteration.hasNext()) {
                RDFDataUnit.Entry entry = iteration.next();
                resultSet.add(entry.getDataGraphURI());
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }

    /**
     * Exhaust {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit#getIteration()}) into one array of graph URIs (throw away symbolic names).
     * Beware - if the {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} contains milions or more entries, storing all of this in single array is not a good idea.
     * Only suitable for work with ~100000 of entries (graphs)
     * <p>
     * Useful for methods from {@link RepositoryConnection} which are varargs. Such as
     * <p><blockquote><pre>
     * connection.add(statement, RDFHelper.getGraphsURIArray(outputDataUnit));
     * </pre></blockquote></p>
     * @param rdfDataUnit data unit from which the iteration will be obtained and exhausted
     * @return array of URIs containing all graphs from the rdfDataUnit
     * @throws DataUnitException
     */
    public static URI[] getGraphsURIArray(RDFDataUnit rdfDataUnit) throws DataUnitException {
        return getGraphsURISet(rdfDataUnit).toArray(new URI[0]);
    }

    /**
     * Most simple way to obtain dataset where all graphs stored in {@link eu.unifiedviews.dataunit.rdf.RDFDataUnit} are set as default graphs.
     * <p>
     * Used to shorten this (more verbose) way of creating equivalent dataset:
     * <p><blockquote><pre>
     * query.setDataset(new DatasetBuilder().withDefaultGraphs(RDFHelper.getGraphsURISet(inputDataUnit)).build())
     * </pre></blockquote></p>
     * into shortened form using this method:
     * <p><blockquote><pre>
     * query.setDataset(RDFHelper.getDatasetWithDefaultGraphs(inputDataUnit)))
     * </pre></blockquote></p>
     * Beware that this method refuses to create dataset with empty defaultGraphs parameter. This is to prevent
     * bugs and errors, as with different storages the empty defaultGraphs may be interpreted in different ways.
     *
     * @param rdfDataUnit data unit from which to obtain all graphs
     * @return {@link Dataset} with defaultGraphs set to all graphs from data unit
     * @throws DataUnitException when rdfDataUnit does contain no graphs or connection errors.
     */
    public static Dataset getDatasetWithDefaultGraphs(RDFDataUnit rdfDataUnit) throws DataUnitException {
        Set<URI> graphsUriSet = RDFHelper.getGraphsURISet(rdfDataUnit);
        if (graphsUriSet.isEmpty()) {
            throw new DataUnitException("Trying to build dataset from dataunit, which contains no data graphs");
        }
        return new DatasetBuilder().withDefaultGraphs(graphsUriSet).build();
    }
}
