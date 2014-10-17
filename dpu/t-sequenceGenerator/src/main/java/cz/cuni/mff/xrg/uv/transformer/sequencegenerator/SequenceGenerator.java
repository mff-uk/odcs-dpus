package cz.cuni.mff.xrg.uv.transformer.sequencegenerator;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.SimpleRdfConfigurator;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUContext;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Škoda Petr
 */
@DPU.AsTransformer
public class SequenceGenerator extends DpuAdvancedBase<SequenceGeneratorConfig_V1> {

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

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "inRdfData")
    public SimpleRdfRead inData;

    @SimpleRdfConfigurator.Configure(dataUnitFieldName = "outRdfSequence")
    public SimpleRdfWrite outSequence;

    public ValueFactory valueFactory;

    public SequenceGenerator() {
        super(SequenceGeneratorConfig_V1.class,
                AddonInitializer.create(new SimpleRdfConfigurator(SequenceGenerator.class)));
    }

    @Override
    protected void innerExecute() throws DPUException {
        try {
            valueFactory = inData.getValueFactory();
        } catch (OperationFailedException ex) {
            SendMessage.sendMessage(context, ex);
            return;
        }

        final URI predicate = valueFactory.createURI(config.getPredicateOutput());
        final String query = String.format(QUERY_TEMPLATE,
                config.getPredicateStart(), config.getPredicateEnd());

        try (ConnectionPair<TupleQueryResult> connection = inData.executeSelectQuery(query)) {
            final TupleQueryResult result = connection.getObject();
            while (result.hasNext()) {
                final BindingSet binding = result.next();
                // get data and parse them
                final String fromStr = binding.getBinding(BINDING_FROM).getValue().stringValue();
                final String toStr =  binding.getBinding(BINDING_TO).getValue().stringValue();
                final URI subject = (URI)binding.getBinding(BINDING_SUBJECT).getValue();

                final Integer from, to;
                try {
                    from = Integer.parseInt(fromStr);
                    to = Integer.parseInt(toStr);
                } catch (NumberFormatException ex) {
                    context.sendMessage(DPUContext.MessageType.ERROR, "Invalid data",
                            "Can't parse object as integer for subject: " + subject.stringValue(), ex);
                    return;
                }

                // create sequence
                generateSequence(from, to, subject, predicate);
            }
        } catch (OperationFailedException ex) {
            SendMessage.sendMessage(context, ex);
        } catch (QueryEvaluationException ex) {
            context.sendMessage(DPUContext.MessageType.ERROR, "Query evaluation failed.", "", ex);
        }

    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new SequenceGeneratorVaadinDialog();
    }

    /**
     * Generate sequence under given subject and store it under given predicate.
     *
     * @param from
     * @param to
     * @param subject
     * @param predicate
     */
    private void generateSequence(Integer from, Integer to, URI subject, URI predicate) throws OperationFailedException {
        LOG.debug("generating sequence {}:{} for {}", from, to, subject.stringValue());
        
       for (Integer index = from; index <= to; ++index) {
           final Literal value = valueFactory.createLiteral(index);
           // add triple
           outSequence.add(subject, predicate, value);
       }
    }

}
