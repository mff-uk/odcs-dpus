package cz.cuni.mff.xrg.uv.test.boost.rdf;

import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import org.openrdf.repository.RepositoryException;

/**
 * Factory for test RDF data units.
 *
 * @author Škoda Petr
 */
public class RDFDataUnitFactory {

    private RDFDataUnitFactory() {
        
    }

    public static WritableRDFDataUnit createInMemory() throws RepositoryException {
        return new InMemoryRDFDataUnit();
    }

}
