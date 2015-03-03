package eu.unifiedviews.helpers.dataunit.virtualgraphhelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.WritableMetadataDataUnit;
import eu.unifiedviews.helpers.dataunit.internal.metadata.MetadataHelper;
import eu.unifiedviews.helpers.dataunit.internal.metadata.MetadataHelpers;

/**
 * Static helper nutshell for {@link VirtualGraphHelper}
 * <p>
 * The helper can be used in two ways:
 * <ul>
 * <li>static (and ineffective), quick and dirty way {@code VirtualGraphHelpers.getVirtualGraph(dataUnit, "symbolicName")}.
 * This does the job, but every call opens new connection to the underlying storage and then closes the connection adding a little overhead.</li>
 * <li>dynamic way,
 * <p><blockquote><pre>
 * //first create helper over dataunit
 * VirtualGraphHelper helper = VirtualGraphHelpers.create(dataUnit);
 * try {
 *   // use many times (helper holds its connections open)
 *   String virtualGraph = helper.getVirtualGraph("symbolicName");
 *   helper.setVirtualGraph("symbolicName", "http://myNewGraphName");
 * } finally {
 *   helper.close();
 * }
 * </pre></blockquote></p>
 * </ul>
 */
public class VirtualGraphHelpers {
    private static final VirtualGraphHelpers selfie = new VirtualGraphHelpers();

    private VirtualGraphHelpers() {
    }

    /**
     * Create read-only {@link VirtualGraphHelper} using {@link MetadataDataUnit},
     * returned helper instance method {@link VirtualGraphHelper#setVirtualGraph(String, String)} is unsupported (throws {@link DataUnitException}).
     * @param metadataDataUnit data unit to work with
     * @return helper, do not forget to close it after using it
     */
    public static VirtualGraphHelper create(MetadataDataUnit metadataDataUnit) {
        return selfie.new VirtualGraphHelperImpl(metadataDataUnit);
    }

    /**
     * Create read-write {@link VirtualGraphHelper} using {@link WritableMetadataDataUnit}
     * @param writableMetadataDataUnit data unit to work with
     * @return helper, do not forget to close it after using it
     */
    public static VirtualGraphHelper create(WritableMetadataDataUnit writableMetadataDataUnit) {
        return selfie.new VirtualGraphHelperImpl(writableMetadataDataUnit);
    }

    /**
     * Just do the job, get virtualGraph from given symbolicName.
     * Opens and closes connection to storage each time it is called.
     * @param metadataDataUnit data unit to work with
     * @param symbolicName
     * @return virtual graph
     * @throws DataUnitException
     */
    public static String getVirtualGraph(MetadataDataUnit metadataDataUnit, String symbolicName) throws DataUnitException {
        String result = null;
        VirtualGraphHelper helper = null;
        try {
            helper = create(metadataDataUnit);
            result = helper.getVirtualGraph(symbolicName);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
        return result;
    }

    /**
     * Just do the job, set virtualGraph for given symbolicName.
     * Opens and closes connection to storage each time it is called.
     * @param metadataDataUnit data unit to work with
     * @param symbolicName
     * @param virtualGraph virtual graph
     * @throws DataUnitException
     */
    public static void setVirtualGraph(WritableMetadataDataUnit metadataDataUnit, String symbolicName, String virtualGraph) throws DataUnitException {
        VirtualGraphHelper helper = null;
        try {
            helper = create(metadataDataUnit);
            helper.setVirtualGraph(symbolicName, virtualGraph);
        } finally {
            if (helper != null) {
                helper.close();
            }
        }
    }

    private class VirtualGraphHelperImpl implements VirtualGraphHelper {
        private final Logger LOG = LoggerFactory.getLogger(VirtualGraphHelperImpl.class);

        protected MetadataHelper metadataHelper;

        public VirtualGraphHelperImpl(MetadataDataUnit dataUnit) {
            this.metadataHelper = MetadataHelpers.create(dataUnit);
        }

        public VirtualGraphHelperImpl(WritableMetadataDataUnit dataUnit) {
            this.metadataHelper = MetadataHelpers.create(dataUnit);
        }

        @Override
        public String getVirtualGraph(String symbolicName) throws DataUnitException {
            return metadataHelper.get(symbolicName, VirtualGraphHelper.PREDICATE_VIRTUAL_GRAPH);
        }

        @Override
        public void setVirtualGraph(String symbolicName, String virtualGraph) throws DataUnitException {
            metadataHelper.set(symbolicName, VirtualGraphHelper.PREDICATE_VIRTUAL_GRAPH, virtualGraph);
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
