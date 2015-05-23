package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import org.openrdf.model.URI;

import eu.unifiedviews.dpu.DPUException;

/**
 * Special version of root template without a predicate. This is required as if we have a CompositeTemplate
 * as a root, then the predicate filter apply - which we do not want to.
 *
 * @author Škoda Petr
 */
class RootTemplate extends CompositeTemplate {

    public RootTemplate() {
        super(null);
    }

    @Override
    public void render(RenderContext context, URI subject) throws DPUException, CantCreateTemplate {
        // Just pass out subject.
        for (Template template : templates) {
            template.render(context, subject);
        }
    }

}
