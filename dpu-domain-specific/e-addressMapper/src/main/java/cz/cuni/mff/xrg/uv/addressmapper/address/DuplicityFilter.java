package cz.cuni.mff.xrg.uv.addressmapper.address;

import cz.cuni.mff.xrg.uv.addressmapper.ruian.RuianEntity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Based on SPARQL query remove duplicity entries.
 *
 * @author Škoda Petr
 */
public class DuplicityFilter {

    private static final Logger LOG = LoggerFactory.getLogger(DuplicityFilter.class);

    private DuplicityFilter() {

    }

    public static List<RuianEntity> filter(List<RuianEntity> entities) {
        List<RuianEntity> output = new ArrayList<>(entities.size());
        for (RuianEntity item : entities) {
            boolean ignoreItem = false;
            for (RuianEntity itemTest : output) {
                if (isSame(item, itemTest)) {
                    ignoreItem = true;
                    break;
                }
            }
            if (!ignoreItem) {
                output.add(item);
            }
        }
        return output;
    }

    static boolean isSame(RuianEntity left, RuianEntity right) {
        return left.asRuianQuery().compareTo(right.asRuianQuery()) == 0;
    }

}
