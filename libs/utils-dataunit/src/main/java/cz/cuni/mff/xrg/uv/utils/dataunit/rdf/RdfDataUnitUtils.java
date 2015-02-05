package cz.cuni.mff.xrg.uv.utils.dataunit.rdf;

import java.util.ArrayList;
import java.util.List;
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

        public InMemoryEntry(URI graphUri, String symbolicName) {
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

    /**
     *
     * @param entry
     * @return URI of graph represented by this entry.
     * @throws DataUnitException
     */
    public static URI asGraph(RDFDataUnit.Entry entry) throws DataUnitException {
        return entry.getDataGraphURI();
    }

    /**
     * Convert RDF graph entries into their respective URIs.
     *
     * @param entries
     * @return
     * @throws DataUnitException
     */
    public static URI[] asGraphs(List<RDFDataUnit.Entry> entries) throws DataUnitException {
        final List<URI> result = new ArrayList<>(entries.size());        
        for (RDFDataUnit.Entry entry : entries) {
            result.add(asGraph(entry));
        }   
        return result.toArray(new URI[0]);
    }

}
