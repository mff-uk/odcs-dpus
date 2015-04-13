package cz.cuni.mff.xrg.uv.addressmapper.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities.
 *
 * @author Škoda Petr
 */
public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private Utils() {

    }

    public static String normalizeSpaces(String str) {
        return str.replaceAll(" +", " ").trim();
    }

    public static String join(List<String> list, String separator) {
        if (list.isEmpty()) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        Iterator<String> iter = list.iterator();
        builder.append(iter.next());
        while (iter.hasNext()) {
            builder.append(" ");
            builder.append(iter.next());
        }        
        return builder.toString();
    }

    /**
     * If exact match is presented then remove all other options from address.
     *
     * @param list
     * @param value
     * @return
     */
    public static List<String> filterExactMatch(List<String> list, String value) {
        if (list.contains(value)) {
//            LOG.info("\t {} -> {}", list, value);
            return Arrays.asList(value);
        } else {
//            LOG.info("\t -> {}", list);
            return list;
        }
    }

}
