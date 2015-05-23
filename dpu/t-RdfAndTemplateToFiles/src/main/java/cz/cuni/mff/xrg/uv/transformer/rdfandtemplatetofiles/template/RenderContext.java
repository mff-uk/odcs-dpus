package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.io.Writer;

import org.openrdf.model.URI;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * Holds object used during template rendering.
 * 
 * @author Škoda Petr
 */
public class RenderContext {

    private Writer writer;

    private final RDFDataUnit rdfDataUnit;

    private final URI graphUri;

    private final FaultTolerance faultTolerance;

    public RenderContext(RDFDataUnit rdfDataUnit, URI graphUri,
            FaultTolerance faultTolerance) {
        this.writer = null;
        this.rdfDataUnit = rdfDataUnit;
        this.graphUri = graphUri;
        this.faultTolerance = faultTolerance;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Writer getWriter() {
        return writer;
    }

    public RDFDataUnit getRdfDataUnit() {
        return rdfDataUnit;
    }

    public URI getGraphUri() {
        return graphUri;
    }

    public FaultTolerance getFaultTolerance() {
        return faultTolerance;
    }

}
