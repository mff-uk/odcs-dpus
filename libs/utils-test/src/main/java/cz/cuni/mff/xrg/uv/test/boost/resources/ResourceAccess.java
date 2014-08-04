package cz.cuni.mff.xrg.uv.test.boost.resources;

import java.io.File;
import java.net.URL;

/**
 *
 * @author Škoda Petr
 */
public class ResourceAccess {

    private ResourceAccess() {
    }

    /**
     * Provide access to files in resources under their names.
     *
     * @param name
     * @return File representation.
     */
    public static File getFile(String name) {
        final URL url = Thread.currentThread().getContextClassLoader()
				.getResource(name);
        if (url == null) {
            throw new RuntimeException(
                    "Required resourcce '" + name + "' is missing.");
        }
        return new File(url.getPath());
    }


}
