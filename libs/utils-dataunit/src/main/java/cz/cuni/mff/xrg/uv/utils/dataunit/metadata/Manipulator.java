package cz.cuni.mff.xrg.uv.utils.dataunit.metadata;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides easy way how to set/get metadata (predicate/object) for given
 * symbolic name.
 *
 * @author Škoda Petr
 */
public class Manipulator {

    private static final Logger LOG = LoggerFactory.getLogger(Manipulator.class);

    private Manipulator() {

    }

    public static ManipulatorInstance create(MetadataDataUnit dataUnit, 
            String symbolicName) throws DataUnitException {
        return new ManipulatorInstance(dataUnit.getConnection(), 
                dataUnit.getMetadataGraphnames(), symbolicName, true);
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
    public static ManipulatorInstance create(MetadataDataUnit dataUnit,
            String symbolicName, RepositoryConnection connection) throws DataUnitException {
        return new ManipulatorInstance(connection,
                dataUnit.getMetadataGraphnames(), symbolicName, false);
    }

    public static ManipulatorInstance create(MetadataDataUnit dataUnit, 
            MetadataDataUnit.Entry entry) throws DataUnitException {
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
    public static ManipulatorInstance create(MetadataDataUnit dataUnit, 
            MetadataDataUnit.Entry entry, RepositoryConnection connection) 
            throws DataUnitException {
        return create(dataUnit, entry.getSymbolicName(), connection);
    }

    public static WritableManipulatorInstance create(WritableMetadataDataUnit dataUnit,
            String symbolicName) throws DataUnitException {
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
    public static WritableManipulatorInstance create(WritableMetadataDataUnit dataUnit,
            String symbolicName, RepositoryConnection connection) throws DataUnitException {
        return new WritableManipulatorInstance(connection,
                 dataUnit.getMetadataGraphnames(),
                dataUnit.getMetadataWriteGraphname(), symbolicName, false);
    }

    /**
     * Get a strings stored under given predicate and symbolicName.
     *
     * If more strings are stored under given predicate then one of them
     * is returned.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @return
     * @throws DataUnitException
     */
    public static String getFirst(MetadataDataUnit dataUnit, String symbolicName,
            String predicate) throws DataUnitException {
        try (ManipulatorInstance instance = create(dataUnit, symbolicName)) {
            return instance.getFirst(predicate);
        }
    }

    /**
     * Get a strings stored under given predicate and symbolicName.
     * 
     * If more strings are stored under given predicate then one of them
     * is returned.
     * 
     * @param dataUnit
     * @param entry
     * @param predicate
     * @return
     * @throws DataUnitException 
     */
    public static String getFirst(MetadataDataUnit dataUnit, 
            MetadataDataUnit.Entry entry,
            String predicate) throws DataUnitException {
        return getFirst(dataUnit, entry.getSymbolicName(), predicate);
    }

    /**
     * Set metadata under given predicate If the predicate is already set then
     * is replaced. To add multiple metadata under same predicate use
     * {@link #add(eu.unifiedviews.dataunit.WritableMetadataDataUnit, java.lang.String, java.lang.String, java.lang.String)}.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param value
     * @throws DataUnitException
     */
    public static void set(WritableMetadataDataUnit dataUnit,
            String symbolicName,
            String predicate, String value) throws DataUnitException {
        try (WritableManipulatorInstance instance = 
                create(dataUnit, symbolicName)) {
            instance.set(predicate, value);
        }
    }

    /**
     * Add metadata for given symbolic name. The old data under same predicate
     * are not deleted. Use to add multiple metadata of same meaning.
     *
     * @param dataUnit
     * @param symbolicName
     * @param predicate
     * @param value
     * @throws DataUnitException
     */
    public static void add(WritableMetadataDataUnit dataUnit,
            String symbolicName,
            String predicate, String value) throws DataUnitException {
         try (WritableManipulatorInstance instance =
                create(dataUnit, symbolicName)) {
            instance.add(predicate, value);
        }
    }
    
    /**
     * Dump content of metadata graphs into logs.
     *
     * @param dataUnit
     * @throws DataUnitException
     */
    public static void dump(MetadataDataUnit dataUnit) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            dump(connection, 
                    dataUnit.getMetadataGraphnames().toArray(new URI[0]));
        } finally {
            if (connection != null) {
                try {
                    connection.close();;
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    /**
     * Dump content of metadata graphs into logs.
     *
     * @param dataUnit
     * @throws DataUnitException
     */
    public static void dump(WritableMetadataDataUnit dataUnit) throws DataUnitException {
        RepositoryConnection connection = null;
        try {
            connection = dataUnit.getConnection();
            dump(connection,
                    new URI[]{ dataUnit.getMetadataWriteGraphname()} );
        } finally {
            if (connection != null) {
                try {
                    connection.close();;
                } catch (RepositoryException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

    /**
     * For debug purpose.
     *
     * @param connection
     * @param uris
     * @throws DataUnitException
     */
    static void dump(RepositoryConnection connection, URI[] uris)
            throws DataUnitException {
        final StringBuilder message = new StringBuilder();
        message.append("\n\tGraphs: ");
        for (URI uri : uris) {
            message.append(uri.stringValue());
            message.append(" ");
        }
        message.append("\n");
        try {
            RepositoryResult r = connection.getStatements(null, null, null,
                    true, uris);
            while (r.hasNext()) {
                Statement s = (Statement) r.next();

                message.append("'");
                message.append(s.getSubject().stringValue());
                message.append("' <");
                message.append(s.getPredicate().stringValue());
                message.append("> '");
                message.append(s.getObject().stringValue());
                message.append("'\n");
            }
            message.append("------------");
            LOG.debug("{}", message.toString());
        } catch (RepositoryException ex) {
            throw new DataUnitException("Dump failed.", ex);
        }
    }

}
