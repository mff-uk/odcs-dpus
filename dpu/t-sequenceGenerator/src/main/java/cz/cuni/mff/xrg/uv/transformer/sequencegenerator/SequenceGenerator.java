package cz.cuni.mff.xrg.uv.transformer.sequencegenerator;

import java.util.Map;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.SimpleRdf;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SequenceGenerator extends AbstractDpu<SequenceGeneratorConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(SequenceGenerator.class);

    private static final String BINDING_SUBJECT = "subject";

    private static final String BINDING_FROM = "from";

    private static final String BINDING_TO = "to";

    private static final String QUERY_TEMPLATE
            = "SELECT ?" + BINDING_SUBJECT + " ?" + BINDING_FROM + " ?" + BINDING_TO + " WHERE { "
            + "?" + BINDING_SUBJECT + " <%s> ?" + BINDING_FROM + "; "
            + " <%s> ?" + BINDING_TO + ". }";

    @DataUnit.AsInput(name = "data")
    public RDFDataUnit inRdfData;

    @DataUnit.AsOutput(name = "sequence", description = "Subjects with generated sequences.")
    public WritableRDFDataUnit outRdfSequence;

    @ExtensionInitializer.Init(param = "inRdfData")
    public SimpleRdf inData;

    @ExtensionInitializer.Init(param = "outRdfSequence")
    public WritableSimpleRdf outSequence;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public ValueFactory valueFactory;

    public SequenceGenerator() {
        super(SequenceGeneratorVaadinDialog.class, ConfigHistory.noHistory(SequenceGeneratorConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        valueFactory = inData.getValueFactory();

        final URI predicate = valueFactory.createURI(config.getPredicateOutput());
        final String query = String.format(QUERY_TEMPLATE, config.getPredicateStart(), config
                .getPredicateEnd());

        final SparqlUtils.SparqlSelectObject select = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlSelectObject>() {

                    @Override
                    public SparqlUtils.SparqlSelectObject action() throws Exception {
                        return SparqlUtils.createSelect(query,
                                FaultToleranceUtils.getEntries(faultTolerance, inRdfData,
                                        RDFDataUnit.Entry.class));

                    }
                });

        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(inRdfData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                result.prepare();
                SparqlUtils.execute(connection, ctx, select, result);
            }

        });

        for (Map<String, Value> item : result.getResults()) {
            final String fromStr = item.get(BINDING_FROM).stringValue();
            final String toStr = item.get(BINDING_TO).stringValue();
            final URI subject = (URI) item.get(BINDING_SUBJECT);

            final Integer from, to;
            try {
                from = Integer.parseInt(fromStr);
                to = Integer.parseInt(toStr);
            } catch (NumberFormatException ex) {
                throw ContextUtils.dpuException(ctx, ex, "Invalid data for subject: " + subject.stringValue());
            }

            // create sequence
            generateSequence(from, to, subject, predicate);
        }
    }

    /**
     * Generate sequence under given subject and store it under given predicate.
     *
     * @param from
     * @param to
     * @param subject
     * @param predicate
     */
    private void generateSequence(Integer from, Integer to, final URI subject, final URI predicate)
            throws DPUException {
        LOG.debug("generating sequence {}:{} for {}", from, to, subject.stringValue());

        for (Integer index = from; index <= to; ++index) {
            final Literal value = valueFactory.createLiteral(index);
            // add triple
            faultTolerance.execute(new FaultTolerance.Action() {

                @Override
                public void action() throws Exception {
                    outSequence.add(subject, predicate, value);
                }
            });
        }
    }

}
