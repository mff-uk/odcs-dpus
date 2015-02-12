package cz.cuni.mff.xrg.uv.boost.extensions;

import java.io.File;
import java.util.List;

import cz.cuni.mff.xrg.uv.utils.dataunit.DataUnitUtils;
import cz.cuni.mff.xrg.uv.utils.dataunit.files.FilesDataUnitUtils;
import eu.unifiedviews.dataunit.MetadataDataUnit;
import eu.unifiedviews.dataunit.files.FilesDataUnit;
import eu.unifiedviews.dpu.DPUException;

/**
 * Contains code for common operation with {@link FaultTolerance}.
 *
 * @author Škoda Petr
 */
public class FaultToleranceUtils {

    private FaultToleranceUtils() {

    }

    /**
     * Eager load entries into list.
     *
     * @param <T>
     * @param <E>
     * @param faultTolerance
     * @param dataUnit
     * @param resultClass
     * @return
     * @throws DPUException
     */
    public static <T extends MetadataDataUnit, E extends T.Entry> List<E> getEntries(
            FaultTolerance faultTolerance, final T dataUnit, final Class<E> resultClass) throws DPUException {

        return faultTolerance.execute(new FaultTolerance.ActionReturn<List<E>>() {

            @Override
            public List<E> action() throws Exception {
                return DataUnitUtils.getEntries(dataUnit, resultClass);
            }
        });
    }

    /**
     * Convert {@link FilesDataUnit.Entry} to {@link File}
     *
     * @param faultTolerance
     * @param entry
     * @return
     * @throws DPUException
     */
    public static File asFile(FaultTolerance faultTolerance, final FilesDataUnit.Entry entry)
            throws DPUException {
        return faultTolerance.execute(new FaultTolerance.ActionReturn<File>() {

            @Override
            public File action() throws Exception {
                return FilesDataUnitUtils.asFile(entry);
            }
        });
    }

}
