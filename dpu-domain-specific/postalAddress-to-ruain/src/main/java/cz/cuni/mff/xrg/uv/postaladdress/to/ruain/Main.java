package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.odcs.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.odcs.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.ErrorLogger;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.QueryCreator;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.QueryException;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsTransformer
public class Main extends ConfigurableBase<Configuration>
        implements ConfigDialogProvider<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String SELECT_POSTAL_ADDRESS = "SELECT ?s WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://schema.org/PostalAddress>}";

    @InputDataUnit(name = "ruian", description = "RUIAN data set")
    public RDFDataUnit inRdfRuian;

    @InputDataUnit(name = "postalAddress", description = "Triples with type s:PostalAddress and related triples that should be linked to RUAIN")
    public RDFDataUnit inRdfPostalAddress;

    @OutputDataUnit(name = "mapping", description = "Mappping from postalAddress to ruain")
    public WritableRDFDataUnit outRdfMapping;

    private SimpleRdfRead rdfRuain;

    private ValueFactory ruianValueFactory;

    private SimpleRdfRead rdfPostalAddress;

    private SimpleRdfWrite rdfMapping;

    private DPUContext context;

    private int failCounter = 0;

    public Main() {
        super(Configuration.class);
    }

    @Override
    public AbstractConfigDialog<Configuration> getConfigurationDialog() {
        return new Dialog();
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

    private void init() throws OperationFailedException {
        rdfRuain = new SimpleRdfRead(inRdfRuian, context);
        ruianValueFactory = rdfRuain.getValueFactory();
        rdfPostalAddress = new SimpleRdfRead(inRdfPostalAddress, context);
        rdfMapping = new SimpleRdfWrite(outRdfMapping, context);
        failCounter = 0;
    }

    private void execute() {
        
        final ErrorLogger errorLogger = new ErrorLogger();
        final KnowledgeBase knowledgeBase = null; // TODO create knowledge base here
        
        QueryCreator creator = new QueryCreator(rdfPostalAddress, errorLogger, 
                knowledgeBase);
        
        try (ConnectionPair<TupleQueryResult> addresses = rdfPostalAddress.
                executeSelectQuery(SELECT_POSTAL_ADDRESS)) {
            while (addresses.getObject().hasNext()) {
                final BindingSet binding = addresses.getObject().next();
                // map single address
                processPostalAddress(creator, binding.getValue("s"));
            }
        } catch (OperationFailedException | QueryEvaluationException ex) {
            context.sendMessage(MessageType.ERROR, "Dpu failed",
                    "DPU failed for repository related exception", ex);
        }
    }

    @Override
    public void cleanUp() {
        context.sendMessage(MessageType.INFO,
                String.format("Failed to parse %d streetAddresses", failCounter));
        try {
            rdfMapping.flushBuffer();
        } catch (OperationFailedException ex) {
            LOG.error("Failed to flush dataUnit.", ex);
        }
    }

    private void processPostalAddress(QueryCreator creator, Value addr) 
            throws OperationFailedException, QueryEvaluationException {
        String connectQuery = "not-created";        
        // get all triples related to the given address
        try {
            // prepare query into ruian that gives use statement for mapping
            connectQuery = creator.createQuery(addr);
            // we ask query into rdfRuain and use result to mapp
            // the source and add the triple into output

            // TODO
        } catch (QueryException ex) {
            // log the state
            LOG.warn("Failed to map: '{}'", addr.stringValue(), ex);
            LOG.info("Query: {}", connectQuery);
            // and increase fail counter
            failCounter++;
        }
    }

}
