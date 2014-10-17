package cz.cuni.mff.xrg.uv.extractor.sukl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author Škoda Petr
 */
public class Utils {

    private Utils() {
    }

    /**
     * Spaces are replaced by '-'.
     *
     * @param part
     * @return
     */
    public static String convertStringToURIPart(String part) {
        // fix spaces
        part = part.replaceAll("\\s+", "-");
        // encode the rest
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
    }

}
