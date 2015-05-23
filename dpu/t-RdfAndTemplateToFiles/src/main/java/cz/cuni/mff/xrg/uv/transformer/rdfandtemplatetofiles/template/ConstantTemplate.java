package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import eu.unifiedviews.dpu.DPUException;

/**
 *
 * @author Škoda Petr
 */
public class ConstantTemplate extends Template {

    String value;

    public ConstantTemplate(String value) {
        this.value = value;
    }

    @Override
    public void render(RenderContext context, URI subject) throws DPUException {
        try {
            context.getWriter().write(value);
        } catch (IOException ex) {
            throw new DPUException(ex);
        }
    }

    @Override
    protected void render(RenderContext context, List<Value> objects) throws DPUException {
        // This method is not used in this context.
    }

}
