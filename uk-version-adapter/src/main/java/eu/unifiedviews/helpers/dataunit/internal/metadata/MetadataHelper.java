package eu.unifiedviews.helpers.dataunit.internal.metadata;

import eu.unifiedviews.dataunit.DataUnitException;

/**
 *  <strong>This class in not accessible in UV environment, do not use!</strong>
 */
public interface MetadataHelper extends AutoCloseable {
    String get(String symbolicName, String predicate) throws DataUnitException;

    void set(String symbolicName, String predicate, String newValue) throws DataUnitException;

    void add(String symbolicName, String predicate, String newValue) throws DataUnitException;

    @Override
    public void close() throws DataUnitException;
}
