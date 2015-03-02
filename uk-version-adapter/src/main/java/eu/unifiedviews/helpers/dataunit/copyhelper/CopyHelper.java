package eu.unifiedviews.helpers.dataunit.copyhelper;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 * Helper for copying all metadata related to single symbolicName, that is these triples:
 * <p><blockquote><pre>
 *  &ltsubject&gt &lt;{@value eu.unifiedviews.dataunit.MetadataDataUnit#PREDICATE_SYMBOLIC_NAME}&gt; "name"
 *  &ltsubject&gt ?p ?v
 * </pre></blockquote></pre>
 * In future versions it will be improved to copy whole triple-tree rooted at &lt;subject&gt;
 * <p>
 * User of the helper is obliged to close this helper after he finished work with it (closes underlying connections).
 * <p>
 * For example usage see {@link CopyHelpers}.
 */
public interface CopyHelper extends AutoCloseable {
    /**
     * Copy all metadata related to single symbolicName
     * @param symbolicName key to {@link eu.unifiedviews.dataunit.MetadataDataUnit.Entry} which will be copied
     * @throws DataUnitException
     */
    void copyMetadata(String symbolicName) throws DataUnitException;

    @Override
    public void close();
}
