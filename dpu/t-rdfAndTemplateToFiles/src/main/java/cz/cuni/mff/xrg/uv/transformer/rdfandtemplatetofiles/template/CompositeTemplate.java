package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;

/**
 * Template that consists of several other templates.
 *
 * @author Škoda Petr
 */
class CompositeTemplate extends Template {

    private static final Logger LOG = LoggerFactory.getLogger(CompositeTemplate.class);

    List<Template> templates = new LinkedList<>();

    public CompositeTemplate(URI uri) {
        this.predicate = uri;
    }

    @Override
    protected void render(RenderContext context, List<Value> objects) throws DPUException, CantCreateTemplate {
        for (Value value : objects) {
            if (value instanceof URI) {
                // Render child templates.
                for (Template template : templates) {
                    template.render(context, (URI) value);
                }
            } else {
                LOG.warn("Predicate ignored in composite template as it's not URI: {}", value);
            }
        }
    }

}
