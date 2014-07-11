package cz.cuni.mff.xrg.uv.postaladdress.to.ruain;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.ontology.Subject;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitException;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUContext;
import cz.cuni.mff.xrg.odcs.commons.dpu.DPUException;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsExtractor;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.AsTransformer;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.InputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.dpu.annotation.OutputDataUnit;
import cz.cuni.mff.xrg.odcs.commons.message.MessageType;
import cz.cuni.mff.xrg.odcs.commons.module.dpu.ConfigurableBase;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.rdf.RDFDataUnit;
import cz.cuni.mff.xrg.odcs.rdf.WritableRDFDataUnit;
import cz.cuni.mff.xrg.uv.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.external.ExternalServicesFactory;
import cz.cuni.mff.xrg.uv.external.rdf.RemoteRepository;
import cz.cuni.mff.xrg.uv.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.mapping.ErrorLogger;
import cz.cuni.mff.xrg.uv.postaladdress.to.ruain.query.*;
import cz.cuni.mff.xrg.uv.rdf.simple.SimpleRdfFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AsExtractor
public class Main extends ConfigurableBase<Configuration>
        implements ConfigDialogProvider<Configuration> {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    @InputDataUnit(name = "seznamUlic", optional = true, description = "Trojice s s:name názvy ulic.")
    public RDFDataUnit inRdfUlice;

    @InputDataUnit(name = "seznamMestObci", optional = true, description = "Trojice s s:name názvy měst a obcí.")
    public RDFDataUnit inRdfObceMesta;

    @InputDataUnit(name = "seznamKraju", optional = true, description = "Trojice s s:name názvy krajů.")
    public RDFDataUnit inRdfKraje;

    @InputDataUnit(name = "postalAddress", description = "Trojice s s:PostalAddress a související jenž se mají namapovat na ruian.")
    public RDFDataUnit inRdfPostalAddress;

    @OutputDataUnit(name = "mapping", description = "Mapovani z postalAddress na ruain pomoci http://ruian.linked.opendata.cz/ontology/links/.")
    public WritableRDFDataUnit outRdfMapping;

    @OutputDataUnit(name = "log", description = "Popisuje chyby při mapování.")
    public WritableRDFDataUnit outRdfLog;

    private ValueFactory rdfMappingFactory;
    
    private ValueFactory rdfLogFactory;

    private SimpleRdfRead rdfPostalAddress;

    private SimpleRdfWrite rdfMapping;

    private SimpleRdfWrite rdfLog;

    private DPUContext context;

    private final Map<Subject, URI> rdfRelations = new HashMap<>();

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

    protected void init() throws OperationFailedException {
        // inputs
        rdfPostalAddress = SimpleRdfFactory.create(inRdfPostalAddress, context);
        // outputs
        rdfMapping = SimpleRdfFactory.create(outRdfMapping, context);
        rdfMappingFactory = rdfMapping.getValueFactory();
        rdfLog = SimpleRdfFactory.create(outRdfLog, context);
        rdfLogFactory = rdfLog.getValueFactory();
        // prepare predicates for ontology
        rdfRelations.clear();
        final Subject[] possibleValues = Subject.values();
        final String baseUri = config.getBaseUri();
        for (Subject s : possibleValues) {
            rdfRelations.put(s, 
                    rdfMappingFactory.createURI(baseUri + s.getRelation()));
        }
    }

    protected void execute() {
        final ErrorLogger errorLogger = new ErrorLogger(rdfLogFactory);

        final KnowledgeBase knowledgeBase = new KnowledgeBase();
        // load cache if given
        if (inRdfUlice != null) {
            SimpleRdfRead rdfUlice = SimpleRdfFactory.create(inRdfUlice,
                    context);
            try {
                knowledgeBase.loadStreetNames(rdfUlice, true);
            } catch (Exception ex) {
                context.sendMessage(MessageType.ERROR,
                        "Knowledge base problem.",
                        "Failed to 'jména ulice' into knowledge base.", ex);
                return;
            }
        }
        if (inRdfObceMesta != null) {
            SimpleRdfRead rdfObceMesta = SimpleRdfFactory.create(inRdfObceMesta,
                    context);
            try {
                knowledgeBase.loadTownNames(rdfObceMesta);
            } catch (Exception ex) {
                context.sendMessage(MessageType.ERROR,
                        "Knowledge base problem.",
                        "Failed to 'jména měst' into knowledge base.", ex);
                return;
            }
        }
        if (inRdfKraje != null) {
            SimpleRdfRead rdfKraje = SimpleRdfFactory.create(inRdfKraje,
                    context);
            try {
                knowledgeBase.loadRegionNames(rdfKraje);
            } catch (Exception ex) {
                context.sendMessage(MessageType.ERROR,
                        "Knowledge base problem.",
                        "Failed to 'jména krajů' into knowledge base.", ex);
                return;
            }
        }
        // prepare needed classes
        RequirementsCreator creator;
        try {
            creator = new RequirementsCreator(rdfPostalAddress,
                    errorLogger,
                    knowledgeBase,
                    config.getMapperConfig());
        } catch (ConfigException ex) {
            context.sendMessage(MessageType.ERROR, "Wrong configuration", 
                    "Faield to init RequirementsCreator.", ex);
            return;
        }
        RequirementsToQuery reqToQuery = new RequirementsToQuery();

        RemoteRepository ruain;
        try {
            ruain = ExternalServicesFactory.remoteRepository(
                    config.getRuainEndpoint(), context, 
                    config.getRuianFailDelay(), config.getRuianFailRetry());
        } catch (ExternalFailure ex) {
            context.sendMessage(MessageType.ERROR,
                    "External service failed",
                    "Creation of remove ruain repository failed.", ex);
            return;
        }

        int failCounter = 0;        
        int okCounter = 0;
        // and do the real stuff here
        try (ConnectionPair<TupleQueryResult> addresses = rdfPostalAddress.
                executeSelectQuery(config.getAddressQuery())) {
            while (addresses.getObject().hasNext()) {
                final BindingSet binding = addresses.getObject().next();
                // map single address
                if (processPostalAddress(ruain, reqToQuery, creator,
                        binding.getValue("s"))) {
                    // ok continue
                    ++okCounter;
                } else {
                    // mapping failed
                    ++failCounter;
                    logFailure(errorLogger); 
                }
            }
        } catch (QueryException | OperationFailedException | QueryEvaluationException ex) {
            context.sendMessage(MessageType.ERROR, "Repository failure",
                    "DPU failed for repository related exception.", ex);
        } catch (ExternalFailure ex) {
            context.sendMessage(MessageType.ERROR, "External failure",
                    "", ex);
        }
        context.sendMessage(MessageType.INFO,
                String.format("Ok/Failed to parse %d/%d streetAddresses", 
                        okCounter, failCounter));
        
    }

    @Override
    public void cleanUp() {
        try {
            rdfMapping.flushBuffer();
        } catch (OperationFailedException ex) {
            LOG.error("Failed to flush dataUnit.", ex);
        }

        try {
            rdfLog.flushBuffer();
        } catch (OperationFailedException ex) {
            LOG.error("Failed to flush dataUnit.", ex);
        }
    }

    /**
     *
     * @param ruain
     * @param reqToQuery
     * @param creator
     * @param addr
     * @return False if the processing fail as a results of exception.
     */
    private boolean processPostalAddress(RemoteRepository ruain,
            RequirementsToQuery reqToQuery,
            RequirementsCreator creator,
            Value addr) throws ExternalFailure, QueryEvaluationException,
            QueryException, OperationFailedException {
        // prepare requirements
        final List<Requirement> reqList = creator.createRequirements(addr);
        if (reqList.isEmpty()) {
            // no requirements
            return false;
        }        
        // convert them to queries
        final List<Query> variants = reqToQuery.convert(reqList);
        // ask ruian
        for (Query query : variants) {            
            final String queryStr = QueryToString.convert(query, 3);
            
            LOG.debug(queryStr);
            
            if (queryStr == null) {
                continue;
            }
            // ask ruian for mapping
            final List<BindingSet> ruainData = ruain.select(queryStr);
            // check number of results
            if (ruainData.size() == 1) {
                // we got it !!!
                final Subject mainSubject = query.getMainSubject();
                final String bindingName = mainSubject.getText().substring(1);
                final Value ruainValue = ruainData.get(0).
                        getBinding(bindingName).getValue();
                // add mapping
                addMapping(addr, mainSubject, ruainValue);
                
                if (context.isDebugging()) {
                    // log data about used query
                    logMappingInfo(reqList, query);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Add triple that represent the mapping between given postalAddess and 
     * ruian triple.
     * 
     * @param postalAddress
     * @param ruianType
     * @param ruianValue
     * @throws OperationFailedException 
     */
    private void addMapping(Value postalAddress, Subject ruianType,
            Value ruianValue) throws OperationFailedException {
        final URI usedUri = rdfRelations.get(ruianType);
        final Resource resource = rdfMappingFactory.createURI(
                postalAddress.stringValue());
        // add triple        
        LOG.debug("+ {} {} {}", resource.stringValue(), usedUri.stringValue(), ruianValue.stringValue());
        rdfMapping.add(resource, usedUri, ruianValue);
    }

    /**
     * Log failure information into rdf output dataUnit.
     * 
     * @param errorLogger 
     */
    private void logFailure(ErrorLogger errorLogger) throws OperationFailedException {
        errorLogger.report(rdfLog);
    }

    /**
     * Log information about used query.
     * 
     * @param requirements
     * @param usedQuery 
     */
    private void logMappingInfo(List<Requirement> requirements, Query usedQuery) {
        
    }
     
}
