package cz.cuni.mff.xrg.uv.rdf.simple;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import java.util.List;
import org.openrdf.model.*;
import org.openrdf.query.*;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to
 * handler RDF functionality and also reduce code duplicity.
 *
 * @author Škoda Petr
 */
public interface SimpleRdfRead {

    /**
     * In case of multiple calls the dame {@link ValueFactory} will be returned.
     *
     * @return Value factory for wrapped {@link RDFDataUnit}
     * @throws OperationFailedException
     */
    ValueFactory getValueFactory() throws OperationFailedException;

    /**
     * Eagerly load all triples and store them into list. Do not use
     * for larger number of triples.
     *
     * @return List of all triples in the repository.
     * @throws OperationFailedException
     */
    List<Statement> getStatements() throws OperationFailedException;

    /**
     * Execute given select query and return result. See {@link ConnectionPair}
     * for more information about usage.
     *
     * @param query SPARQL select query
     * @return
     * @throws OperationFailedException
     */
    ConnectionPair<TupleQueryResult> executeSelectQuery(String query)
            throws OperationFailedException;

    /**
     * Execute given construct query and return result. See
     * {@link ConnectionPair} for more information about usage.
     *
     * @param query SPARQL construct query
     * @return
     * @throws OperationFailedException
     */
    ConnectionPair<Graph> executeConstructQuery(String query)
            throws OperationFailedException;

}
