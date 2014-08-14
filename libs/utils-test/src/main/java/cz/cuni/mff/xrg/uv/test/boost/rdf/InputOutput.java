package cz.cuni.mff.xrg.uv.test.boost.rdf;

import cz.cuni.mff.xrg.uv.test.boost.resources.ResourceAccess;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide functions to load/store values from
 * {@link RDFDataUnit}/{@link WritableRDFDataUnit}.
 *
 * To obtain file from resource use {@link ResourceAccess}.
 *
 * @author Škoda Petr
 */
public final class InputOutput {

    private static final Logger LOG = LoggerFactory.getLogger(InputOutput.class);

    private InputOutput() {
    }

    /**
     * Extract triples from given file and add them into repository, into the
     * fixed graph name.
     * 
     * @param source
     * @param format
     * @param target
     */
    public static void extractFromFile(File source, RDFFormat format,
            WritableRDFDataUnit target) {
        RepositoryConnection connection = null;

        try {
            final URI graphUri = target.getConnection().getValueFactory()
                    .createURI(target.getBaseDataGraphURI().stringValue() + 
                            "/fromFile");
            
            connection = target.getConnection();
            connection.begin();
            connection.add(source, "http://default//", format, graphUri);
            connection.commit();

            LOG.info("{} triples have been extracted from {}",
                    connection.size(), source.toString());

        } catch (IOException | RepositoryException | RDFParseException | 
                DataUnitException e) {
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
        
        // get all contexts
        final List<URI> uris = new LinkedList<>();
        try {
            final RDFDataUnit.Iteration iter = source.getIteration();
            while (iter.hasNext()) {
                uris.add(iter.next().getDataGraphURI());
            }        
        } catch (DataUnitException ex) {
            LOG.error("Faield to get graph list.", ex);            
            return;
        }
        
        // load
        final URI[] sourceContexts = uris.toArray(new URI[0]);
        try (FileOutputStream out = new FileOutputStream(target);
                OutputStreamWriter os = new OutputStreamWriter(out, Charset
                        .forName("UTF-8"));) {
            connection = source.getConnection();

            RDFWriter rdfWriter = Rio.createWriter(format, os);
            connection.export(rdfWriter, sourceContexts);
        } catch (DataUnitException | RepositoryException | IOException | RDFHandlerException e) {
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
