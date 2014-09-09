package cz.cuni.mff.xrg.uv.test.boost.files;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * Very simple implementation for {@link FilesDataUnitMock} for passing files to dpu input files data unit.
 *
 * Sample usage:
 * <pre> {@code
 * FilesDataUnitMock mock = new FilesDataUnitMock();
 mock.addFile("d:/directory/myFile.dat");
 // set as input dataUnit to your dpu
 myDpu.inFile = mock;
 } </pre>
 * @author Škoda Petr
 */
public class FilesDataUnitMock implements FilesDataUnit {

    public class Entry implements FilesDataUnit.Entry {

        private final String fileUri;
        
        private final String symbolicName;

        public Entry(String fileUri) {
            this.fileUri = new File(fileUri).toURI().toString();
            this.symbolicName = fileUri;
        }
        
        public Entry(String fileUri, String symbolicName) {
            this.fileUri = new File(fileUri).toURI().toString();
            this.symbolicName = symbolicName;
        }
                
        @Override
        public String getFileURIString() throws DataUnitException {
            return fileUri;
        }

        @Override
        public String getSymbolicName() throws DataUnitException {
            return symbolicName;
        }
        
    }

    public class Iteration implements FilesDataUnit.Iteration {

        private final Iterator<Entry> iterator;

        public Iteration(Iterator<Entry> iterator) {
            this.iterator = iterator;
        }

        @Override
        public Entry next() throws DataUnitException {
            return iterator.next();
        }

        @Override
        public boolean hasNext() throws DataUnitException {
            return iterator.hasNext();
        }

        @Override
        public void close() throws DataUnitException {
            // nothing to do here
        }
        
    }

    /**
     * List of stored files.
     */
    private final List<Entry> fileEntries = new LinkedList<>();

    /**
     * Add given file to this data unit. The path is converted into Uri.
     * Given file Uri is also used as symbolicName.
     *
     * @param file
     */
    public void addFile(String file) {
        fileEntries.add(new Entry(file));
    }

    /**
     * Add given file to this data unit.
     *
     * @param file
     * @param symbolicName
     */
    public void addFile(String file, String symbolicName) {
        fileEntries.add(new Entry(file, symbolicName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iteration getIteration() throws DataUnitException {
        return new Iteration(fileEntries.iterator());
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
