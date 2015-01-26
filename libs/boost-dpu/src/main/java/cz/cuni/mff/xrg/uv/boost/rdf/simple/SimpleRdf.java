package cz.cuni.mff.xrg.uv.boost.rdf.simple;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;

/**
 * Wrap for {@link RDFDataUnit} aims to provide more user friendly way how to handler RDF functionality and
 * also reduce code duplicity.
 *
 * @author Škoda Petr
 */
public class SimpleRdf {

    private final RDFDataUnit readDataUnit;

    public SimpleRdf(RDFDataUnit dataUnit) {
        this.readDataUnit = dataUnit;
    }

    public RDFDataUnit getReadDataUnit() {
        return readDataUnit;
    }

}
