package cz.cuni.mff.xrg.uv.existencechecker;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.NonConfigurableBase;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.external.ExternalServicesFactory;
import cz.cuni.mff.xrg.uv.external.rdf.RemoteRepository;
import cz.cuni.mff.xrg.uv.rdf.simple.*;
import java.util.List;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsExtractor
public class Main extends NonConfigurableBase {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @InputDataUnit(name = "input")
    public RDFDataUnit inRdfInput;

    @OutputDataUnit(name = "output")
    public WritableRDFDataUnit outRdfOutput;

    private ValueFactory rdfOutputFactory;

    private SimpleRdfRead rdfInput;

    private SimpleRdfWrite rdfOutput;

    private DPUContext context;

    private URI predicateProperty;
    
    private Value objectNotExists;
    
    public Main() {

    }

    @Override
    public void execute(DPUContext context) throws DPUException, DataUnitException {
        this.context = context;
        boolean initDone = false;
        try {
            init();
            initDone = true;
            execute();
        } catch (Exception e) {
            context.sendMessage(MessageType.ERROR,
                    "DPU failed because of en exception",
                    "An unhandled exception occurred.", e);
        }
        if (initDone) {
            cleanUp();
        }
    }

    protected void init() throws OperationFailedException {
        rdfInput = SimpleRdfFactory.create(inRdfInput, context);
        rdfOutput = SimpleRdfFactory.create(outRdfOutput, context);
        rdfOutputFactory = rdfOutput.getValueFactory();
        //
        objectNotExists = rdfOutputFactory.createURI("http://localhost/ontology/Invalid");
        predicateProperty = rdfOutputFactory.createURI("http://localhost/ontology/property");
    }

    protected void execute() {
        RemoteRepository ruain;
        try {
            ruain = ExternalServicesFactory.remoteRepository(
                    "http://ruian.linked.opendata.cz/sparql", context,
                    10000);
        } catch (ExternalFailure ex) {
            context.sendMessage(MessageType.ERROR,
                    "External service failed",
                    "Creation of remove ruain repository failed.", ex);
            return;
        }

        try (ConnectionPair<TupleQueryResult> addresses = rdfInput.
                executeSelectQuery("SELECT ?o WHERE {?s ?p ?o}")) {
            while (addresses.getObject().hasNext()) {
                final BindingSet binding = addresses.getObject().next();
                String value = binding.getBinding("o").getValue().stringValue();
                
                // prepare query
                StringBuilder queryStr = new StringBuilder();
                queryStr.append("SELECT ?s WHERE { <");
                queryStr.append(value);                
                queryStr.append("> ?p ?o}");
                               
                final List<BindingSet> ruainData = ruain.select(queryStr.toString());
                if (ruainData.isEmpty()) {
                    addReport(value);
                }
            }
        } catch (OperationFailedException | QueryEvaluationException ex) {
            context.sendMessage(MessageType.ERROR, "Repository failure",
                    "DPU failed for repository related exception.", ex);
        } catch (ExternalFailure ex) {
            context.sendMessage(MessageType.ERROR, "External failure",
                    "", ex);
        }
    }

    @Override
    public void cleanUp() {
        try {
            rdfOutput.flushBuffer();
        } catch (OperationFailedException ex) {
            LOG.error("Failed to flush dataUnit.", ex);
        }
    }

    private void addReport(String subject) throws OperationFailedException {
        rdfOutput.add(rdfOutputFactory.createURI(subject), predicateProperty,
                objectNotExists);
    }
    
}
