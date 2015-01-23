package cz.cuni.mff.xrg.uv.utils.dataunit.rdf;

import org.openrdf.model.URI;

import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;

/**
 * Helper for {@link RDFDataUnit}.
 *
 * @author Škoda Petr
 */
public class RdfDataUnitUtils {

    /**
     * InMemory representation of RDF entry.
     */
    public static class InMemoryEntry implements RDFDataUnit.Entry {

        private final URI graphUri;
                
        private final String symbolicName;

        InMemoryEntry(URI graphUri, String symbolicName) {
            this.graphUri = graphUri;
            this.symbolicName = symbolicName;
        }

        @Override
        public URI getDataGraphURI() throws DataUnitException {
            return graphUri;
        }

        @Override
        public String getSymbolicName() throws DataUnitException {
            return symbolicName;
        }
        
    }

    private RdfDataUnitUtils() {
        
    }

    /**
     * Add entry with generated graph name.
     *
     * @param dataUnit
     * @param symbolicName
     * @return Wrap of a new entry.
     * @throws DataUnitException
     */
    public static InMemoryEntry addGraph(WritableRDFDataUnit dataUnit, String symbolicName)
            throws DataUnitException {
        final URI uri = dataUnit.addNewDataGraph(symbolicName);
        return new InMemoryEntry(uri, symbolicName);
    }

    /**
     * Add given graph under given symbolic name.
     * 
     * @param dataUnit
     * @param symbolicName
     * @param uri
     * @return Wrap of a new entry.
     * @throws DataUnitException 
     */
    public static InMemoryEntry addGraph(WritableRDFDataUnit dataUnit,String symbolicName, URI uri)
            throws DataUnitException {
        dataUnit.addExistingDataGraph(symbolicName, uri);
        return new InMemoryEntry(uri, symbolicName);
    }

}
