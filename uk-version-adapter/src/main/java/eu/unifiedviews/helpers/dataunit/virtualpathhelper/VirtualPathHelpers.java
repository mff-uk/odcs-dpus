package eu.unifiedviews.helpers.dataunit.virtualpathhelper;

import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.files.FilesVocabulary;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtils;
import eu.unifiedviews.helpers.dataunit.metadata.MetadataUtilsInstance;
import eu.unifiedviews.helpers.dataunit.metadata.WritableMetadataUtilsInstance;


public class VirtualPathHelpers {

    private VirtualPathHelpers() {
    }

    public static VirtualPathHelper create(MetadataDataUnit metadataDataUnit) {
        return new VirtualPathHelperImpl(metadataDataUnit);
    }

    /**
     * Create read-write {@link VirtualPathHelper} using {@link WritableMetadataDataUnit}
     * @param writableMetadataDataUnit data unit to work with
     * @return helper, do not forget to close it after using it
     */
    public static VirtualPathHelper create(WritableMetadataDataUnit writableMetadataDataUnit) {
        return new VirtualPathHelperImpl(writableMetadataDataUnit);
    }

    /**
     * Just do the job, get virtualPath from given symbolicName.
     * Opens and closes connection to storage each time it is called.
     * @param metadataDataUnit data unit to work with
     * @param symbolicName
     * @return virtual path
     * @throws DataUnitException
     */
    public static String getVirtualPath(MetadataDataUnit metadataDataUnit, String symbolicName) throws DataUnitException {
        return eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers.getVirtualPath(metadataDataUnit, symbolicName);
    }

    /**
     * Just do the job, set virtualPath for given symbolicName.
     * Opens and closes connection to storage each time it is called.
     * @param metadataDataUnit data unit to work with
     * @param symbolicName
     * @param virtualPath virtual path
     * @throws DataUnitException
     */
    public static void setVirtualPath(WritableMetadataDataUnit metadataDataUnit, String symbolicName, String virtualPath) throws DataUnitException {
        eu.unifiedviews.helpers.dataunit.virtualpath.VirtualPathHelpers.setVirtualPath(metadataDataUnit,
                symbolicName, virtualPath);
    }

    private static class VirtualPathHelperImpl implements VirtualPathHelper {

        private static final Logger LOG = LoggerFactory.getLogger(VirtualPathHelperImpl.class);

        protected MetadataUtilsInstance metadataUtils = null;

        protected WritableMetadataUtilsInstance writableMetadataUtils = null;

        protected MetadataDataUnit dataUnit;

        protected WritableMetadataDataUnit writableDataUnit;

        public VirtualPathHelperImpl(MetadataDataUnit dataUnit) {
            this.dataUnit = dataUnit;
            this.writableDataUnit = null;
        }

        public VirtualPathHelperImpl(WritableMetadataDataUnit dataUnit) {
            this.dataUnit = null;
            this.writableDataUnit = dataUnit;
        }

        private void init() throws DataUnitException {
            if (metadataUtils == null) {
                if (writableDataUnit == null) {
                    // Read only.
                    this.metadataUtils = MetadataUtils.create(dataUnit);
                } else {
                    this.writableMetadataUtils = MetadataUtils.create(writableDataUnit);
                    this.metadataUtils = this.writableMetadataUtils;
                }
            }
        }

        @Override
        public String getVirtualPath(String symbolicName) throws DataUnitException {
            init();
            metadataUtils.setEntry(symbolicName);
            final Value value;
            try {
                value = metadataUtils.get(FilesVocabulary.UV_VIRTUAL_PATH);
            } catch (DPUException ex) {
                throw new DataUnitException(ex);
            }
            if (value == null) {
                return null;
            } else {
                return value.stringValue();
            }
        }

        @Override
        public void setVirtualPath(String symbolicName, String virtualGraph) throws DataUnitException {
            init();
            writableMetadataUtils.setEntry(symbolicName);
            writableMetadataUtils.set(FilesVocabulary.UV_VIRTUAL_PATH, virtualGraph);
        }

        @Override
        public void close() {
            if (metadataUtils != null) {
                try {
                    // If writableDataUnit != null, then as writableDataUnit == metadataUtils
                    // this also close writableDataUnit.
                    metadataUtils.close();
                } catch (DataUnitException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }

}
