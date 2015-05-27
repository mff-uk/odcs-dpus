package cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.template;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.transformer.rdfandtemplatetofiles.RdfAndTemplateToFilesVocabulary;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 *
 * @author Škoda Petr
 */
public abstract class Template {

    private static final Logger LOG = LoggerFactory.getLogger(Template.class);

    /**
     * %s - subject
     * %s - predicate
     */
    private static final String QUERY = "SELECT DISTINCT ?s ?order WHERE {\n"
            + "<%s> <%s> ?s.\n"
            + "OPTIONAL {?s <" + RdfAndTemplateToFilesVocabulary.INDEX + "> ?index}\n"
            + "BIND(IF(BOUND(?index),?index, -1) AS ?order)\n"
            + "}\n";
    
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
                
                final String query = String.format(QUERY, subject.stringValue(), predicate.stringValue());
                SparqlUtils.QueryResultCollector collector = new SparqlUtils.QueryResultCollector();

                SparqlUtils.execute(connection, context.getContext(),
                        SparqlUtils.createSelect(query, Arrays.asList(context.getGraph())), collector);

                // Get results and sort them.
                collector.getResults().sort(new Comparator<Map<String, Value>>() {

                    @Override
                    public int compare(Map<String, Value> left, Map<String, Value> right) {
                        int leftIndex = 0;
                        int rightIndex = 0;
                        // Get indexes.
                        try {
                            leftIndex = Integer.parseInt(left.get("order").stringValue());
                        } catch (NumberFormatException ex) {
                            LOG.warn("Invalid order format (integer expected): {} for {}", 
                                    left.get("order").stringValue(), left.get("s").stringValue());
                        }
                        try {
                            rightIndex = Integer.parseInt(right.get("order").stringValue());
                        } catch (NumberFormatException ex) {
                            LOG.warn("Invalid order format (integer expected): {} for {}",
                                    right.get("order").stringValue(), right.get("s").stringValue());
                        }

                        // We sort in increasing order.
                        return Integer.compare(leftIndex, rightIndex);
                    }
                });
                // Store subjects.
                for (Map<String,Value> row : collector.getResults()) {
                    objects.add(row.get("s"));
                }
            }
        });
        // Call user method.
        if (objects.isEmpty()) {
            // No data, nothing to render.
        } else {
            render(context, objects);
        }
    }

    protected abstract void render(RenderContext context, List<Value> objects) throws DPUException, CantCreateTemplate;

}
