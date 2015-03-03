package eu.unifiedviews.helpers.dataunit.virtualpathhelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.helpers.dataunit.internal.metadata.MetadataHelper;
import eu.unifiedviews.helpers.dataunit.internal.metadata.MetadataHelpers;

/**
 * Static helper nutshell for {@link VirtualPathHelper}
 * <p>
 * The helper can be used in two ways:
 * <ul>
 * <li>static (and ineffective), quick and dirty way {@code VirtualPathHelpers.getVirtualPath(dataUnit, "symbolicName")}.
 * This does the job, but every call opens new connection to the underlying storage and then closes the connection adding a little overhead.</li>
 * <li>dynamic way,
 * <p><blockquote><pre>
 * //first create helper over dataunit
 * VirtualPathHelper helper = VirtualPathHelpers.create(dataUnit);
 * try {
 *   // use many times (helper holds its connections open)
 *   String virtualPath = helper.getVirtualPath("symbolicName");
 *   helper.setVirtualPath("symbolicName", "new/book/pages.csv");
 * } finally {
 *   helper.close();
 * }
 * </pre></blockquote></p>
 * </ul>
 */
public class VirtualPathHelpers {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualPathHelpers.class);

    private static final VirtualPathHelpers selfie = new VirtualPathHelpers();

    private VirtualPathHelpers() {
    }

    /**
     * Create read-only {@link VirtualPathHelper} using {@link MetadataDataUnit},
     * returned helper instance method {@link VirtualPathHelper#setVirtualPath(String, String)} is unsupported (throws {@link DataUnitException}).
     * @param metadataDataUnit data unit to work with
     * @return helper, do not forget to close it after using it
     */
    public static VirtualPathHelper create(MetadataDataUnit metadataDataUnit) {
        return selfie.new VirtualPathHelperImpl(metadataDataUnit);
    }

    /**
     * Create read-write {@link VirtualPathHelper} using {@link WritableMetadataDataUnit}
     * @param writableMetadataDataUnit data unit to work with
     * @return helper, do not forget to close it after using it
     */
    public static VirtualPathHelper create(WritableMetadataDataUnit writableMetadataDataUnit) {
        return selfie.new VirtualPathHelperImpl(writableMetadataDataUnit);
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
        String result = null;
        VirtualPathHelper helper = null;
        try {
            helper = create(metadataDataUnit);
            result = helper.getVirtualPath(symbolicName);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
        return result;
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
        VirtualPathHelper helper = null;
        try {
            helper = create(metadataDataUnit);
            helper.setVirtualPath(symbolicName, virtualPath);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
    }

    private class VirtualPathHelperImpl implements VirtualPathHelper {
        private final Logger LOG = LoggerFactory.getLogger(VirtualPathHelperImpl.class);

        protected MetadataHelper metadataHelper;

        public VirtualPathHelperImpl(MetadataDataUnit dataUnit) {
            this.metadataHelper = MetadataHelpers.create(dataUnit);
        }

        public VirtualPathHelperImpl(WritableMetadataDataUnit dataUnit) {
            this.metadataHelper = MetadataHelpers.create(dataUnit);
        }

        @Override
        public String getVirtualPath(String symbolicName) throws DataUnitException {
            return metadataHelper.get(symbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH);
        }

        @Override
        public void setVirtualPath(String symbolicName, String virtualPath) throws DataUnitException {
            metadataHelper.set(symbolicName, VirtualPathHelper.PREDICATE_VIRTUAL_PATH, virtualPath);
        }

        @Override
        public void close() {
            if (metadataHelper != null) {
                try {
                    metadataHelper.close();
                } catch (DataUnitException ex) {
                    LOG.warn("Error in close.", ex);
                }
            }
        }
    }
}
