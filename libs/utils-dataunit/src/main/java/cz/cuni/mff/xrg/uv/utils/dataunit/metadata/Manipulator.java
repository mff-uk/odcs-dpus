package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import org.openrdf.repository.RepositoryConnection;

/**
 * Provides easy way how to set/get metadata (predicate/object) for given symbolic name.
 *
 * This class can be used for a simple one shot actions with metadata, or
 * {@link ManipulatorInstance}/{@link WritableManipulatorInstance} can be created and reused to prevent object
 * creation and deletion.
 *
 * <pre>
 * {@code
 * // set path to given file with respective symbolicName
 * Manipulator.add(WritableFilesDataUnit, symbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH, path);
 * }
 * </pre>
 *
 * @author Škoda Petr
 */
public class Manipulator {

    private Manipulator() {

    }

    /**
     * Close must be called on returned class after usage.
     *
     * @param dataUnit
     * @param symbolicName
     * @return
     * @throws DataUnitException
     */
    public static ManipulatorInstance create(MetadataDataUnit dataUnit, String symbolicName)
            throws DataUnitException {
        return new ManipulatorInstance(dataUnit.getConnection(), dataUnit.getMetadataGraphnames(),
                symbolicName, true);
    }

    /**
     * Does not close given connection.
     *
     * @param dataUnit
     * @param symbolicName
     * @param connection
     * @return
     * @throws DataUnitException
     */
    public static ManipulatorInstance create(MetadataDataUnit dataUnit, String symbolicName,
            RepositoryConnection connection) throws DataUnitException {
        return new ManipulatorInstance(connection, dataUnit.getMetadataGraphnames(), symbolicName, false);
    }

    /**
     * Close must be called on returned class after usage.
     *
     * @param dataUnit
     * @param entry
     * @return
     * @throws DataUnitException
     */
    public static ManipulatorInstance create(MetadataDataUnit dataUnit, MetadataDataUnit.Entry entry)
            throws DataUnitException {
        return create(dataUnit, entry.getSymbolicName());
    }

    /**
     *
     * Does not close given connection.
     *
     * @param dataUnit
     * @param entry
     * @param connection
     * @return
     * @throws DataUnitException
     */
    public static ManipulatorInstance create(MetadataDataUnit dataUnit, MetadataDataUnit.Entry entry,
            RepositoryConnection connection) throws DataUnitException {
        return create(dataUnit, entry.getSymbolicName(), connection);
    }

    /**
     * Close must be called on returned class after usage.
     *
     * @param dataUnit
     * @param symbolicName
     * @return
     * @throws DataUnitException
     */
    public static WritableManipulatorInstance create(WritableMetadataDataUnit dataUnit, String symbolicName)
            throws DataUnitException {
        return new WritableManipulatorInstance(dataUnit.getConnection(),
                dataUnit.getMetadataGraphnames(),
                dataUnit.getMetadataWriteGraphname(), symbolicName, true);
    }

    /**
     *
     * Does not close given connection.
     *
     * @param dataUnit
     * @param symbolicName
     * @param connection
     * @return
     * @throws DataUnitException
     */
    public static WritableManipulatorInstance create(WritableMetadataDataUnit dataUnit, String symbolicName,
            RepositoryConnection connection) throws DataUnitException {
        return new WritableManipulatorInstance(connection,
                dataUnit.getMetadataGraphnames(),
                dataUnit.getMetadataWriteGraphname(), symbolicName, false);
    }

    /**
     * Get a strings stored under given predicate and symbolicName.
     *
     * If more strings are stored under given predicate then one of them is returned.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @return
     * @throws DataUnitException
     */
    public static String getFirst(MetadataDataUnit dataUnit, String symbolicName, String predicate)
            throws DataUnitException {
        try (ManipulatorInstance instance = create(dataUnit, symbolicName)) {
            return instance.getFirst(predicate);
        }
    }

    /**
     * Get a strings stored under given predicate and symbolicName.
     *
     * If more strings are stored under given predicate then one of them is returned.
     *
     * @param dataUnit
     * @param entry
     * @param predicate
     * @return
     * @throws DataUnitException
     */
    public static String getFirst(MetadataDataUnit dataUnit, MetadataDataUnit.Entry entry, String predicate)
            throws DataUnitException {
        return getFirst(dataUnit, entry.getSymbolicName(), predicate);
    }

    /**
     * Set metadata under given predicate If the predicate is already set then is replaced. To add multiple
     * metadata under same predicate use
     * {@link #add(eu.unifiedviews.dataunit.WritableMetadataDataUnit, java.lang.String, java.lang.String, java.lang.String)}.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param value
     * @throws DataUnitException
     */
    public static void set(WritableMetadataDataUnit dataUnit, String symbolicName, String predicate,
            String value) throws DataUnitException {
        try (WritableManipulatorInstance instance = create(dataUnit, symbolicName)) {
            instance.set(predicate, value);
        }
    }

    /**
     * Add metadata for given symbolic name. The old data under same predicate are not deleted. Use to add
     * multiple metadata of same meaning.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param value
     * @throws DataUnitException
     */
    public static void add(WritableMetadataDataUnit dataUnit, String symbolicName, String predicate,
            String value) throws DataUnitException {
        try (WritableManipulatorInstance instance = create(dataUnit, symbolicName)) {
            instance.add(predicate, value);
        }
    }

}
