package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.io.Writer;

import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.helpers.dpu.exec.UserExecContext;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 * Holds object used during template rendering.
 * 
 * @author Škoda Petr
 */
public class RenderContext {

    private final UserExecContext context;

    private Writer writer;

    private final RDFDataUnit rdfDataUnit;

    private final RDFDataUnit.Entry graph;

    private final FaultTolerance faultTolerance;

    public RenderContext(UserExecContext context, RDFDataUnit rdfDataUnit, RDFDataUnit.Entry graph,
            FaultTolerance faultTolerance) {
        this.context = context;
        this.writer = null;
        this.rdfDataUnit = rdfDataUnit;
        this.graph = graph;
        this.faultTolerance = faultTolerance;
    }

    public UserExecContext getContext() {
        return context;
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

    public RDFDataUnit.Entry getGraph() {
        return graph;
    }

    public FaultTolerance getFaultTolerance() {
        return faultTolerance;
    }

}
