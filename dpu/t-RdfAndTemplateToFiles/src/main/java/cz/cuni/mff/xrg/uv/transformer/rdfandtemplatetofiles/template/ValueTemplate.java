package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.io.IOException;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import eu.unifiedviews.dpu.DPUException;

/**
 * Write a single value.
 *
 * @author Škoda Petr
 */
public class ValueTemplate extends Template {

    public ValueTemplate(URI uri) {
        this.predicate = uri;
    }

    @Override
    protected void render(RenderContext context, List<Value> objects) throws DPUException, CantCreateTemplate {
        if (objects.size() != 1) {
            throw new CantCreateTemplate("Invalid number of objects: " + Integer.toString(objects.size()) +
                    " for " + this.predicate.stringValue());
        }
        try {
            context.getWriter().write(objects.get(0).stringValue());
        } catch (IOException ex) {
            throw new CantCreateTemplate(ex);
        }
    }

}
