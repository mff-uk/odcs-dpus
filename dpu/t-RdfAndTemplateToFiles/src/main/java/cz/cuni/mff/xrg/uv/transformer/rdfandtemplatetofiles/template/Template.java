package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;

/**
 *
 * @author Škoda Petr
 */
public abstract class Template {

    private static final Logger LOG = LoggerFactory.getLogger(Template.class);

    /**
     * Predicate of whose value should be interpreted by this template.
     */
    protected URI predicate;

    public void render(final RenderContext context, final URI subject) throws DPUException, CantCreateTemplate {
        // Get objects for our combination of predicate, subject.
        final List<Value> objects = new LinkedList<>();
        context.getFaultTolerance().execute(context.getRdfDataUnit(), new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                objects.clear();
                final RepositoryResult<Statement> result = connection.getStatements(subject,
                        predicate, null, false, context.getGraphUri());
                while (result.hasNext()) {
                    objects.add(result.next().getObject());
                }
            }
        });
        // Call user method.
        render(context, objects);
    }

    protected abstract void render(RenderContext context, List<Value> objects) throws DPUException, CantCreateTemplate;

}
