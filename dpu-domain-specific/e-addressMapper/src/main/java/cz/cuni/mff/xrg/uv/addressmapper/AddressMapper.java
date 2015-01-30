package cz.cuni.mff.xrg.uv.addressmapper;

import cz.cuni.mff.xrg.uv.addressmapper.query.RequirementsToQuery;
import cz.cuni.mff.xrg.uv.addressmapper.query.Query;
import cz.cuni.mff.xrg.uv.addressmapper.query.QueryException;
import cz.cuni.mff.xrg.uv.addressmapper.query.QueryToString;
import cz.cuni.mff.xrg.uv.addressmapper.query.Requirement;
import cz.cuni.mff.xrg.uv.addressmapper.query.RequirementsCreator;
import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.advanced.DpuAdvancedBase;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.ConnectionPair;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.OperationFailedException;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfRead;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfWrite;
import cz.cuni.mff.xrg.uv.addressmapper.knowledge.KnowledgeBase;
import cz.cuni.mff.xrg.uv.addressmapper.mapping.ErrorLogger;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Output;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Ruian;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.Subject;
import cz.cuni.mff.xrg.uv.addressmapper.ontology.UriTranslator;
import cz.cuni.mff.xrg.uv.rdf.utils.dataunit.rdf.simple.SimpleRdfFactory;
import cz.cuni.mff.xrg.uv.service.external.ExternalFailure;
import cz.cuni.mff.xrg.uv.service.external.ExternalServicesFactory;
import cz.cuni.mff.xrg.uv.service.external.rdf.RemoteRepository;
import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.DataUnitException;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import java.util.List;
import org.openrdf.model.*;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.cuni.mff.xrg.uv.boost.dpu.utils.SendMessage;

/**
 *
 * @author Škoda Petr
 */
@DPU.AsExtractor
public class AddressMapper extends DpuAdvancedBase<AddressMapperConfig_V1> {

    private static final Logger LOG = LoggerFactory.getLogger(AddressMapper.class);

    @DataUnit.AsInput(name = "ulice", optional = true, description = "Trojice s s:name názvy ulic.")
    public RDFDataUnit inRdfUlice;

    @DataUnit.AsInput(name = "obece", optional = true, description = "Trojice s s:name názvy obcí.")
    public RDFDataUnit inRdfObce;

    @DataUnit.AsInput(name = "casti-obci", optional = true, description = "Trojice s s:name názvy částí obcí.")
    public RDFDataUnit inRdfCastiObci;   
    
    @DataUnit.AsInput(name = "vusc", optional = true, description = "Trojice s s:name názvy krajů.")
    public RDFDataUnit inRdfKraje;

    @DataUnit.AsInput(name = "vstup", description = "Trojice s s:PostalAddress a související jenž se mají namapovat na ruian.")
    public RDFDataUnit inRdfPostalAddress;

    @DataUnit.AsOutput(name = "mapovani", description = "Mapovani z postalAddress na ruain pomoci http://ruian.linked.opendata.cz/ontology/links/.")
    public WritableRDFDataUnit outRdfMapping;

    @DataUnit.AsOutput(name = "log", description = "Popisuje chyby při mapování.")
    public WritableRDFDataUnit outRdfLog;

    private ValueFactory rdfMappingFactory;
    
    private ValueFactory rdfLogFactory;

    private SimpleRdfRead rdfPostalAddress;

    private SimpleRdfWrite rdfMapping;

    private SimpleRdfWrite rdfLog;

    public AddressMapper() {
        super(AddressMapperConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new AddressMapperVaadinDialog();
    }

    @Override
    protected void innerInit() throws DataUnitException, DPUException {
        super.innerInit();
        // Inputs.
        rdfPostalAddress = SimpleRdfFactory.create(inRdfPostalAddress, context);
        // Outputs.
        rdfMapping = SimpleRdfFactory.create(outRdfMapping, context);
        rdfMappingFactory = rdfMapping.getValueFactory();
        rdfLog = SimpleRdfFactory.create(outRdfLog, context);
        rdfLogFactory = rdfLog.getValueFactory();
        // Init UriTranslator.
        UriTranslator.add(rdfMappingFactory, Output.O_ALTERNATIVE);
        UriTranslator.add(rdfMappingFactory, Output.O_CLASS);
        UriTranslator.add(rdfMappingFactory, Output.O_REDUCTION);
        UriTranslator.add(rdfMappingFactory, Output.P_PROPERTY);
        UriTranslator.add(rdfMappingFactory, Output.P_SOURCE);
        UriTranslator.add(rdfMappingFactory, Output.P_TARGET);
        UriTranslator.add(rdfMappingFactory, Output.P_TYPE);
        UriTranslator.add(rdfMappingFactory, Output.P_MAPPING_TYPE);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_ADRESNI_MISTO);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_ULICE);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_OBEC);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_ORP);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_POU);
        UriTranslator.add(rdfMappingFactory, Ruian.P_LINK_VUSC);
    }

    @Override
    protected void innerExecute() throws DPUException, DataUnitException {
        final ErrorLogger errorLogger = new ErrorLogger(rdfLogFactory);
        final KnowledgeBase knowledgeBase = new KnowledgeBase();
        // Load cache if provided.
        if (inRdfUlice != null) {
            final SimpleRdfRead rdfUlice = SimpleRdfFactory.create(inRdfUlice, context);
            try {
                knowledgeBase.loadStreetNames(rdfUlice, true);
            } catch (Exception ex) {
                throw new DPUException("Can't load 'jména ulice' into knowledge base.", ex);
            }
        }
        if (inRdfObce != null) {
            final SimpleRdfRead rdfObce = SimpleRdfFactory.create(inRdfObce, context);
            try {
                knowledgeBase.loadTownNames(rdfObce);
            } catch (Exception ex) {
                throw new DPUException("Can't load 'jména obcí' into knowledge base.", ex);
            }
        }
        if (inRdfCastiObci != null) {
            final SimpleRdfRead rdfCastiObci = SimpleRdfFactory.create(inRdfCastiObci, context);
            try {
                knowledgeBase.loadTownPartNames(rdfCastiObci);
            } catch (Exception ex) {
                throw new DPUException("Can't load 'jména částí obcí' into knowledge base.", ex);
            }
        }        
        if (inRdfKraje != null) {
            final SimpleRdfRead rdfKraje = SimpleRdfFactory.create(inRdfKraje, context);
            try {
                knowledgeBase.loadRegionNames(rdfKraje);
            } catch (Exception ex) {
                throw new DPUException("Can't load 'jména krajů' into knowledge base.", ex);
            }
        }
        // Prepare class that will extract informations from given address object.
        final RequirementsCreator creator;
        try {
            creator = new RequirementsCreator(rdfPostalAddress,
                    errorLogger,
                    knowledgeBase,
                    config.getMapperConfig());
        } catch (DPUConfigException ex) {
            throw new DPUException("Faield to init RequirementsCreator.", ex);
        }
        // Prepare remote repository with RUIAN.
        final RequirementsToQuery reqToQuery = new RequirementsToQuery();
        final RemoteRepository ruain;
        try {
            ruain = ExternalServicesFactory.remoteRepository(
                    config.getRuainEndpoint(), context, 
                    config.getRuianFailDelay(), config.getRuianFailRetry());
        } catch (ExternalFailure ex) {
            throw new DPUException("Creation of remove ruain repository failed.", ex);
        }
        // Go throuh the data and translate.
        int failCounter = 0;        
        int okCounter = 0;
        try (ConnectionPair<TupleQueryResult> addresses =
                rdfPostalAddress.executeSelectQuery(config.getAddressQuery())) {
            while (addresses.getObject().hasNext() && !context.canceled()) {
                LOG.debug("Processing entity number: {}", failCounter + okCounter);
                final BindingSet binding = addresses.getObject().next();
                // Map single address.
                if (processPostalAddress(ruain, reqToQuery, creator, binding.getValue("s"))) {
                    ++okCounter;
                } else {
                    // Failure.
                    ++failCounter;
                    logFailure(errorLogger); 
                }
            }
        } catch (QueryException | OperationFailedException | QueryEvaluationException ex) {
            throw new DPUException("DPU failed for repository related exception.", ex);
        } catch (ExternalFailure ex) {
            throw new DPUException(ex);
        }
        SendMessage.sendInfo(context,
                String.format("Ok/Failed to parse %d/%d streetAddresses", okCounter, failCounter), "");
    }

    @Override
    protected void innerCleanUp() {
        super.innerCleanUp();
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
    private boolean processPostalAddress(RemoteRepository ruain, RequirementsToQuery reqToQuery,
            RequirementsCreator creator, Value addr) throws ExternalFailure, QueryEvaluationException,
            QueryException, OperationFailedException {
        // Prepare requirements.
        final List<Requirement> reqList = creator.createRequirements(addr);
        if (reqList.isEmpty()) {
            // No requirements.
            return false;
        }        
        // Convert requirementes to queries.
        final List<Query> variants = reqToQuery.convert(reqList);
        // Ask ruian.
        for (Query query : variants) {            
            final String queryStr = QueryToString.convert(query, 3);
            if (queryStr == null) {
                continue;
            }
            if (context.canceled()) {
                return false;
            }
            // Get result.
            final List<BindingSet> ruainData = ruain.select(queryStr);
            // Check number of results.
            if (ruainData.size() == 1) {
                // We got it !!!
                final Subject mainSubject = query.getMainSubject();
                final String bindingName = mainSubject.getValueName().substring(1);
                final Value ruainValue = ruainData.get(0).getBinding(bindingName).getValue();
                addMapping(addr, ruainValue, query);
                return true;
            }
        }
        // No variants return one option -> fail.
        return false;
    }

    /**
     * Add triple that represent the mapping between given postalAddess and RUIAN triple.
     * 
     * @param postalAddress
     * @param ruianType
     * @param ruianValue
     * @throws OperationFailedException 
     */
    private void addMapping(Value postalAddress, Value ruianValue, Query usedQuery)
            throws OperationFailedException {
        final String relUriString = usedQuery.getMainSubject().getRelation();        
        final BNode node = rdfMappingFactory.createBNode();        
        final Resource address = rdfMappingFactory.createURI(postalAddress.stringValue());
        // Add mapping.
        rdfMapping.add(node, UriTranslator.toUri(Output.P_TYPE), UriTranslator.toUri(Output.O_CLASS));
        rdfMapping.add(node, UriTranslator.toUri(Output.P_SOURCE), address);
        rdfMapping.add(node, UriTranslator.toUri(Output.P_TARGET), ruianValue);        
        rdfMapping.add(node, UriTranslator.toUri(Output.P_MAPPING_TYPE), UriTranslator.toUri(relUriString));        
        // Add basic metadata.
        if (usedQuery.isAlternative()) {
            rdfMapping.add(node, 
                    UriTranslator.toUri(Output.P_PROPERTY),
                    UriTranslator.toUri(Output.O_ALTERNATIVE));
        }
        if (usedQuery.isReduction()) {
            rdfMapping.add(node,
                    UriTranslator.toUri(Output.P_PROPERTY),
                    UriTranslator.toUri(Output.O_REDUCTION));
        }
    }

    /**
     * Log failure information into rdf output dataUnit.
     * 
     * @param errorLogger 
     */
    private void logFailure(ErrorLogger errorLogger) throws OperationFailedException {
        errorLogger.report(rdfLog);
    }
     
}
