package cz.cuni.mff.xrg.uv.transformer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultToleranceUtils;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

/**
 * Main data processing unit class.
 *
 * @author Petr Škoda
 */
@DPU.AsTransformer
public class ModifyDate extends AbstractDpu<ModifyDateConfig_V1> {

    /**
     * %s - predicate place holder
     */
    private final String INPUT_QUERY = "SELECT ?s ?o WHERE { ?s <%s> ?o }";

    @DataUnit.AsInput(name = "input")
    public RDFDataUnit rdfInput;

    @DataUnit.AsOutput(name = "output")
    public WritableRDFDataUnit rdfOutput;

    @ExtensionInitializer.Init(param = "rdfOutput")
    public WritableSimpleRdf output;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public ModifyDate() {
        super(ModifyDateVaadinDialog.class, ConfigHistory.noHistory(ModifyDateConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final List<RDFDataUnit.Entry> inputGraphs = FaultToleranceUtils.getEntries(faultTolerance, rdfInput,
                RDFDataUnit.Entry.class);
        final SparqlUtils.QueryResultCollector results = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(rdfInput, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                SparqlUtils.SparqlSelectObject select = SparqlUtils.createSelect(
                        String.format(INPUT_QUERY, config.getInputPredicate()), inputGraphs);
                SparqlUtils.execute(connection, ctx, select, results);
            }
        });
        // Update dates and put them to output.
        final Calendar calendar = new GregorianCalendar();
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        final ValueFactory valueFactory = ValueFactoryImpl.getInstance();
        final URI outputPredicate = valueFactory.createURI(config.getOutputPredicate());
        final URI xsdDate = valueFactory.createURI("http://www.w3.org/2001/XMLSchema#date");
        for (Map<String, Value> item : results.getResults()) {
            try {
                calendar.setTime(format.parse(item.get("o").stringValue()));
            } catch (ParseException ex) {
                ContextUtils.sendError(ctx, "Invalid date value detected", ex, "subject: {0}",
                        item.get("s").stringValue());
                continue;
            }
            calendar.add(Calendar.DATE, config.getModifyDay());

            output.add((URI) item.get("s"), outputPredicate,
                    valueFactory.createLiteral(format.format(calendar.getTime()), xsdDate));
        }

    }

}
