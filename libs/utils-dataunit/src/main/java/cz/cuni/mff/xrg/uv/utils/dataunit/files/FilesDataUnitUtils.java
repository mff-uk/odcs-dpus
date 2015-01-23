package cz.cuni.mff.xrg.uv.utils.dataunit.files;

import java.io.File;

import cz.cuni.mff.xrg.uv.utils.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dataunit.files.WritableFilesDataUnit;
import eu.unifiedviews.helpers.dataunit.virtualpathhelper.VirtualPathHelper;

/**
 *
 * @author Škoda Petr
 */
public class FilesDataUnitUtils {

    /**
     * InMemory representation of File entry.
     */
    public static class InMemoryEntry implements FilesDataUnit.Entry {

        private final String fileUri;
                
        private final String symbolicName;

        InMemoryEntry(String graphUri, String symbolicName) {
            this.fileUri = graphUri;
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

    private FilesDataUnitUtils() {
        
    }

    /**
     * Add file to the DataUnit. Relative path (from root to file) is used as path to file.
     * 
     * @param dataUnit
     * @param root Root.
     * @param file File to add, should be under root.
     * @return
     * @throws eu.unifiedviews.dataunit.DataUnitException
     */
    public static FilesDataUnit.Entry addFile(WritableFilesDataUnit dataUnit, File root, File file) throws DataUnitException {
        final String symbolicName = root.toPath().relativize(file.toPath()).toString();
        // Add existing file to DataUnit.
        dataUnit.addExistingFile(symbolicName, file.toURI().toString());
        // Set available metadata.
        MetadataUtils.add(dataUnit, symbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH, symbolicName);
        // Return representing instance.
        return new InMemoryEntry(file.toString(), symbolicName);
    }

    /**
     *
     * @param entry
     * @return File representation of given entry.
     * @throws DataUnitException
     */
    public static File asFile(FilesDataUnit.Entry entry) throws DataUnitException {
        return new File(java.net.URI.create(entry.getFileURIString()));
    }

}
