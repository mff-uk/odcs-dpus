package cz.cuni.mff.xrg.uv.transformer.tabular;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author Škoda Petr
 */
public class Utils {

    private Utils() {
        
    }

    public static String convertStringToURIPart(String part) {
        try {
            return URLEncoder.encode(part, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding", ex);
        }
	}

}
