package eu.unifiedviews.helpers.dataunit.fileshelper;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;

/**
 * Helper to make various tasks with {@link FilesDataUnit} friendly.
 */
public class FilesHelper {
    /**
     * Exhaust {@link eu.unifiedviews.dataunit.files.FilesDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.files.FilesDataUnit#getIteration()}) into one {@link Map} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.files.FilesDataUnit} contains milions or more entries, storing all of this in single {@link Map} is not a good idea.
     * Only suitable for work with ~100000 of entries (files)
     *
     * @param filesDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Map} containing all entries, keys are symbolic names
     * @throws DataUnitException
     */
    public static Map<String, FilesDataUnit.Entry> getFilesMap(FilesDataUnit filesDataUnit) throws DataUnitException {
        if (filesDataUnit == null) {
            return new LinkedHashMap<>();
        }
        FilesDataUnit.Iteration iteration = filesDataUnit.getIteration();
        Map<String, FilesDataUnit.Entry> resultSet = new LinkedHashMap<>();
        try {
            while (iteration.hasNext()) {
                FilesDataUnit.Entry entry = iteration.next();
                resultSet.put(entry.getSymbolicName(), entry);
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }

    /**
     * Exhaust {@link eu.unifiedviews.dataunit.files.FilesDataUnit.Iteration} (obtained using {@link eu.unifiedviews.dataunit.files.FilesDataUnit#getIteration()}) into one {@link Set} of entries.
     * Beware - if the {@link eu.unifiedviews.dataunit.files.FilesDataUnit} contains milions or more entries, storing all of this in single {@link Set} is not a good idea.
     * Only suitable for work with ~100000 of entries (files)
     *
     * @param filesDataUnit data unit from which the iteration will be obtained and exhausted
     * @return {@link Set} containing all entries
     * @throws DataUnitException
     */
    public static Set<FilesDataUnit.Entry> getFiles(FilesDataUnit filesDataUnit) throws DataUnitException {
        if (filesDataUnit == null) {
            return new LinkedHashSet<>();
        }
        FilesDataUnit.Iteration iteration = filesDataUnit.getIteration();
        Set<FilesDataUnit.Entry> resultSet = new LinkedHashSet<>();
        try {
            while (iteration.hasNext()) {
                FilesDataUnit.Entry entry = iteration.next();
                resultSet.add(entry);
            }
        } finally {
            iteration.close();
        }
        return resultSet;
    }
}
