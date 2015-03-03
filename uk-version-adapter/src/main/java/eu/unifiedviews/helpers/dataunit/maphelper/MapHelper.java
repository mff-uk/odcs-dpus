package eu.unifiedviews.helpers.dataunit.maphelper;

import java.util.Map;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Helper providing easy way to attach a Map<String, String> (usually called properties map) as a metadata to
 * particular {@link eu.unifiedviews.dataunit.MetadataDataUnit.Entry}.
 * <p>
 * Each entry can have unlimited number of maps attached to it, each map is identified by unique mapName (string) when
 * retrieving and saving.
 * <p>
 * For example usage see {@link MapHelpers}
 * <p>
 * Each instance has to be closed after using it.
 * <p>
 * Internal storage format of the map:
 * <p><blockquote><pre>
 * &lt;subject&gt; {@value eu.unifiedviews.dataunit.MetadataDataUnit#PREDICATE_SYMBOLIC_NAME} "name"
 * &lt;subject&gt; &lt;{@value #PREDICATE_HAS_MAP}&gt; &lt;generatedUniqueUriOrBlankNode1&gt;
 * &lt;generatedUniqueUriOrBlankNode1&gt; &lt;{@value #PREDICATE_MAP_TITLE}&gt; "literal mapName"
 * &lt;generatedUniqueUriOrBlankNode1&gt; &lt;{@value #PREDICATE_MAP_CONTAINS}&gt; &lt;generatedUniqueUriOrBlankNode2&gt;
 * &lt;generatedUniqueUriOrBlankNode2&gt; &lt;{@value #PREDICATE_MAP_ENTRY_KEY}&gt; "key literal"
 * &lt;generatedUniqueUriOrBlankNode2&gt; &lt;{@value #PREDICATE_MAP_ENTRY_VALUE}&gt; "value literal"
 * ...
 * </pre></blockquote></p>
 */
public interface MapHelper extends AutoCloseable {
    /**
     * Value: {@value #PREDICATE_HAS_MAP}, predicate used to attach map to entry, triple doing this is:
     * <p><blockquote><pre>
     * &lt;subject&gt; &lt;{@value #PREDICATE_HAS_MAP}&gt; &lt;generatedUniqueUriOrBlankNode1&gt;
     * </p></blockquote></pre>
     */
    public static final String PREDICATE_HAS_MAP = "http://unifiedviews.eu/MapHelper/hasMap";

    /**
     * Value: {@value #PREDICATE_MAP_TITLE}, predicate used to specify mapName property of map.
     */
    public static final String PREDICATE_MAP_TITLE = "http://unifiedviews.eu/MapHelper/map/title";

    /**
     * Value: {@value #PREDICATE_MAP_CONTAINS}, predicate used to attache key-value pair to map.
     */
    public static final String PREDICATE_MAP_CONTAINS = "http://unifiedviews.eu/MapHelper/map/contains";

    /**
     * Value: {@value #PREDICATE_MAP_ENTRY_KEY}, key predicate
     */
    public static final String PREDICATE_MAP_ENTRY_KEY = "http://unifiedviews.eu/MapHelper/map/entry/key";

    /**
     * Value: {@value #PREDICATE_MAP_ENTRY_VALUE}, value predicate
     */
    public static final String PREDICATE_MAP_ENTRY_VALUE = "http://unifiedviews.eu/MapHelper/map/entry/value";

    /**
     * Obtain map named mapName attached to metadata entry named symbolicName.
     *
     * @param symbolicName entry's symbolic name
     * @param mapName name of map (its id)
     * @return metadata map or empty map if no such map exists
     * @throws DataUnitException
     */
    Map<String, String> getMap(String symbolicName, String mapName) throws DataUnitException;

    /**
     * Attach map 'map', named mapName to metadata entry named symbolicName. Any previously attached map with same mapName will be replaced with new one.
     * @param symbolicName entry's symbolic name
     * @param mapName name of map (its id)
     * @param map the map, can not be null
     * @throws DataUnitException
     */
    void putMap(String symbolicName, String mapName, Map<String, String> map) throws DataUnitException;

    @Override
    public void close();
}
