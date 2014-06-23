package cz.cuni.mff.xrg.uv.test.boost.rdf;

import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import java.io.*;
import java.nio.charset.Charset;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Škoda Petr
 */
public final class InputOutput {

    private static final Logger LOG = LoggerFactory.getLogger(InputOutput.class);

    private InputOutput() {
    }

    /**
     * Extract triples from given file and add them into repository.
     * 
     * @param source
     * @param format
     * @param target
     */
    public static void extractFromFile(File source, RDFFormat format,
            WritableRDFDataUnit target) {
        RepositoryConnection connection = null;

        try {
            connection = target.getConnection();
            connection.begin();
            connection.add(source, "http://default//", format, target.getWriteContext());
            connection.commit();

            LOG.info("{} triples have been extracted from {}",
                    connection.size(), source.toString());

        } catch (IOException | RDFParseException | RepositoryException e) {
            LOG.error("Extraction failed.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    LOG.warn("Failed to close repository", e);
                }
            }
        }
    }

    /**
     * Load data from given {@link RDFDataUnit} into given file.
     * 
     * @param source
     * @param target
     * @param format
     */
    public static void loadToFile(RDFDataUnit source, File target,
            RDFFormat format) {
        RepositoryConnection connection = null;
        URI[] sourceContexts = source.getContexts().toArray(new URI[0]);
        try (FileOutputStream out = new FileOutputStream(target);
                OutputStreamWriter os = new OutputStreamWriter(out, Charset
                        .forName("UTF-8"));) {
            connection = source.getConnection();

            RDFWriter rdfWriter = Rio.createWriter(format, os);
            connection.export(rdfWriter, sourceContexts);
        } catch (RepositoryException | IOException | RDFHandlerException e) {
            LOG.error("Loading failed.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (RepositoryException e) {
                    LOG.warn("Failed to close repository", e);
                }
            }
        }
    }

}
