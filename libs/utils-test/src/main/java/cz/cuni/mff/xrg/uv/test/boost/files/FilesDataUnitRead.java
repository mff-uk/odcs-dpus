package cz.cuni.mff.xrg.uv.test.boost.files;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import java.util.*;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * Very simple implementation for {@link FilesDataUnitRead} for passing files to DPU input files data unit.
 *
 * Sample usage:
 * <pre> {@code
 * FilesDataUnitRead mock = new FilesDataUnitRead();
 * mock.addFile("d:/directory/myFile.dat");
 * // Set as input dataUnit to your dpu.
 * myDpu.inFile = mock;
 * }
 * </pre>
 *
 *
 * @author Škoda Petr
 */
public class FilesDataUnitRead implements FilesDataUnit {

    /**
     * List of stored files if format (symbolic name; file URI);
     */
    private final Map<String, String> files = new HashMap<>();

    FilesDataUnitRead() {
    }

    /**
     * Add given file to this data unit. The path is converted into URI.
     * Given file Uri is also used as symbolicName.
     *
     * @param file
     */
    public void addFile(String file) {
        files.put(file, file);
    }

    /**
     * Add given file to this data unit.
     *
     * @param file
     * @param symbolicName
     */
    public void addFile(String file, String symbolicName) {
        files.put(symbolicName, file);
    }

    /**
     * {@inheritDoc
     * @throws eu.unifiedviews.dataunit.DataUnitException}
     */
    @Override
    public FilesDataUnit.Iteration getIteration() throws DataUnitException {
        //return new Iteration(fileEntries.iterator());
        return new FilesDataUnit.Iteration() {

            private final Iterator<String> iterator = files.keySet().iterator();

            @Override
            public Entry next() throws DataUnitException {
                final String key = iterator.next();
                return new FilesDataUnit.Entry() {

                    @Override
                    public String getFileURIString() throws DataUnitException {
                        return files.get(key);
                    }

                    @Override
                    public String getSymbolicName() throws DataUnitException {
                        return key;
                    }

                };
            }

            @Override
            public boolean hasNext() throws DataUnitException {
                return iterator.hasNext();
            }

            @Override
            public void close() throws DataUnitException {
                // Do nothing here.
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepositoryConnection getConnection() throws DataUnitException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<URI> getMetadataGraphnames() throws DataUnitException {
        return null;
    }

}
