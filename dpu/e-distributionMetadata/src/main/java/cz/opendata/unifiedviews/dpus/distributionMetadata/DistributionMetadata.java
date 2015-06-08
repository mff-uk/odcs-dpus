package cz.opendata.unifiedviews.dpus.distributionMetadata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;

import eu.unifiedviews.dataunit.DataUnit;
import eu.unifiedviews.dataunit.rdf.RDFDataUnit;
import eu.unifiedviews.dataunit.rdf.WritableRDFDataUnit;
import eu.unifiedviews.dpu.DPU;
import eu.unifiedviews.dpu.DPUException;
import eu.unifiedviews.helpers.dataunit.DataUnitUtils;
import eu.unifiedviews.helpers.dataunit.rdf.RdfDataUnitUtils;
import eu.unifiedviews.helpers.dpu.config.ConfigHistory;
import eu.unifiedviews.helpers.dpu.context.ContextUtils;
import eu.unifiedviews.helpers.dpu.exec.AbstractDpu;
import eu.unifiedviews.helpers.dpu.extension.ExtensionInitializer;
import eu.unifiedviews.helpers.dpu.extension.faulttolerance.FaultTolerance;
import eu.unifiedviews.helpers.dpu.extension.rdf.simple.WritableSimpleRdf;
import eu.unifiedviews.helpers.dpu.rdf.EntityBuilder;
import eu.unifiedviews.helpers.dpu.rdf.sparql.SparqlUtils;

@DPU.AsExtractor
public class DistributionMetadata extends AbstractDpu<DistributionMetadataConfig_V1> {

    @DataUnit.AsInput(name = "datasetMetadata", optional = true)
    public RDFDataUnit inRdfData;
	
	@DataUnit.AsOutput(name = "metadata")
    public WritableRDFDataUnit outRdfData;

    @ExtensionInitializer.Init(param = "outRdfData")
    public WritableSimpleRdf rdfData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public DistributionMetadata() {
        super(DistributionMetadataVaadinDialog.class, ConfigHistory.noHistory(DistributionMetadataConfig_V1.class));
    }

    @Override
    protected void innerExecute() throws DPUException {
        final Date dateStart = new Date();

        generateMetadata();

        final Date dateEnd = new Date();
        ContextUtils.sendShortInfo(ctx, "Done in {0} ms", (dateEnd.getTime() - dateStart.getTime()));
    }

    private void generateMetadata() throws DPUException {
        final ValueFactory valueFactory = faultTolerance.execute(
                new FaultTolerance.ActionReturn<ValueFactory>() {

                    @Override
                    public ValueFactory action() throws Exception {
                        return rdfData.getValueFactory();
                    }
                });
        // Set output graph.
        final RDFDataUnit.Entry entry = faultTolerance.execute(new FaultTolerance.ActionReturn<RDFDataUnit.Entry>() {

            @Override
            public RDFDataUnit.Entry action() throws Exception {
                return RdfDataUnitUtils.addGraph(outRdfData, DataUnitUtils.generateSymbolicName(DistributionMetadata.class));
            }
        });
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                rdfData.setOutput(entry);
            }
        });
        
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Start business
        String datasetURI;
        if (config.isUseDatasetURIfromInput()) {
        	datasetURI = executeSimpleSelectQuery("SELECT ?d WHERE {?d a <" + DistributionMetadataVocabulary.DCAT_DATASET_CLASS + ">}", "d");        	
        }
        else datasetURI = config.getDatasetURI();
        
        String distributionURI;
        if (config.isGenerateDistroURIFromDataset()) {
        	distributionURI = datasetURI + "/distribution";
        }
        else distributionURI = config.getDistributionURI();
        
        String schemaURL;
        if (config.isSchemaFromDataset()) {
        	schemaURL = executeSimpleSelectQuery("SELECT ?schema WHERE {<" + datasetURI + "> <"+ DCTERMS.REFERENCES + "> ?schema }", "schema");
        }
        else schemaURL = config.getSchema();
        
        String license;
        if (config.isLicenseFromDataset()) {
        	license = executeSimpleSelectQuery("SELECT ?license WHERE {<" + datasetURI + "> <"+ DCTERMS.LICENSE + "> ?license }", "license");
        }
        else license = config.getLicense();
        
        String originalLanguage;
        if (config.isOriginalLanguageFromDataset()) {
        	originalLanguage = executeSimpleSelectQuery("SELECT ?language WHERE {<" + datasetURI + "> <"+ DCTERMS.TITLE + "> ?title FILTER(!LANGMATCHES(LANG(?title), \"en\")) BIND(LANG(?title) as ?language) }", "language");
        }
        else originalLanguage = config.getLanguage_orig();
        
        String title_orig, title_en;
        if (config.isTitleFromDataset()) {
        	title_en = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"en\"))}", "title");
        	title_orig = executeSimpleSelectQuery("SELECT ?title WHERE {<" + datasetURI + "> <"+ DCTERMS.TITLE + "> ?title FILTER(LANGMATCHES(LANG(?title), \"" + originalLanguage + "\"))}", "title");
        }
        else {
        	title_orig = config.getTitle_orig();
        	title_en = config.getTitle_en();
        }

        String description_orig, description_en;
        if (config.isTitleFromDataset()) {
        	description_en = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"en\"))}", "description");
        	description_orig = executeSimpleSelectQuery("SELECT ?description WHERE {<" + datasetURI + "> <"+ DCTERMS.DESCRIPTION + "> ?description FILTER(LANGMATCHES(LANG(?description), \"" + originalLanguage + "\"))}", "description");
        }
        else {
        	description_orig = config.getDesc_orig();
        	description_en = config.getDesc_en();
        }
        
        
        String temporalStart = "", temporalEnd = "";
        if (config.isUseTemporal()) {
	        if (config.isTemporalFromDataset()) {
	        	temporalStart = executeSimpleSelectQuery("SELECT ?temporalStart WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + DistributionMetadataVocabulary.SCHEMA_STARTDATE + "> ?temporalStart }", "temporalStart");
	        	temporalEnd = executeSimpleSelectQuery("SELECT ?temporalEnd WHERE {<" + datasetURI + "> <"+ DCTERMS.TEMPORAL + ">/<" + DistributionMetadataVocabulary.SCHEMA_ENDDATE  + "> ?temporalEnd }", "temporalEnd");
	        }
	        else {
	        	temporalStart = dateFormat.format(config.getTemporalStart());
	        	temporalEnd = dateFormat.format(config.getTemporalEnd());
	        }
        }
        
//        String spatial;
//        if (config.isSpatialFromDataset()) {
//        	spatial = executeSimpleSelectQuery("SELECT ?spatial WHERE {<" + datasetURI + "> <"+ DCTERMS.SPATIAL + "> ?spatial }", "spatial");
//        }
//        else spatial = config.getSpatial();
        
        String issued;
        if (config.isIssuedFromDataset()) {
        	issued = executeSimpleSelectQuery("SELECT ?issued WHERE {<" + datasetURI + "> <"+ DCTERMS.ISSUED + "> ?issued }", "issued");
        }
        else {
        	issued = dateFormat.format(config.getIssued());
        }

        
        // Link dataset to distribution
        final EntityBuilder dataset = new EntityBuilder(valueFactory.createURI(datasetURI),
                valueFactory);
        dataset.property(DistributionMetadataVocabulary.DCAT_DISTRIBUTION, valueFactory.createURI(distributionURI));
        
        rdfData.add(dataset.asStatements());

        // Prepare distribution entity and fill it with data.
        final EntityBuilder distribution = new EntityBuilder(valueFactory.createURI(distributionURI),
                valueFactory);
        distribution.property(RDF.TYPE, DistributionMetadataVocabulary.DCAT_DISTRIBUTION_CLASS);
        distribution.property(RDF.TYPE, DistributionMetadataVocabulary.VOID_DATASET_CLASS);
        
        // Build metadata ...

        // Title.
        if (!StringUtils.isBlank(title_orig)) {
            distribution.property(DCTERMS.TITLE, valueFactory.createLiteral(title_orig,
            		originalLanguage));
        }
        if (!StringUtils.isBlank(title_en)) {
            distribution.property(DCTERMS.TITLE, valueFactory.createLiteral(title_en, "en"));
        }

        // Description.
        if (!StringUtils.isBlank(description_orig)) {
            distribution.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(description_orig,
            		originalLanguage));
        }
        if (!StringUtils.isBlank(description_en)) {
            distribution.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(description_en, "en"));
        }

        // issued
       	distribution.property(DCTERMS.ISSUED, valueFactory.createLiteral(issued,
                DistributionMetadataVocabulary.XSD_DATE));
        
        // modified
        if (config.isUseNow()) {
            distribution.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    DistributionMetadataVocabulary.XSD_DATE));
        } else {
            distribution.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(config
                    .getModified()),
                    DistributionMetadataVocabulary.XSD_DATE));
        }
        
        
        if (config.isUseTemporal()) {
	        if (!(temporalStart.isEmpty() && temporalEnd.isEmpty()))
	        {
		        final EntityBuilder temporal = new EntityBuilder(valueFactory.createURI(distributionURI + "/temporal"),
		                valueFactory);
		        temporal.property(RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
		        temporal.property(DistributionMetadataVocabulary.SCHEMA_STARTDATE, valueFactory.createLiteral(temporalStart, DistributionMetadataVocabulary.XSD_DATE));
		        temporal.property(DistributionMetadataVocabulary.SCHEMA_ENDDATE, valueFactory.createLiteral(temporalEnd, DistributionMetadataVocabulary.XSD_DATE));
		        rdfData.add(temporal.asStatements());
		
		        distribution.property(DCTERMS.TEMPORAL, temporal);
	        }
        }
//        if (!StringUtils.isBlank(spatial)) {
//            distribution.property(DCTERMS.SPATIAL, valueFactory.createURI(spatial));
//        }

        if (!StringUtils.isBlank(schemaURL)) {
            distribution.property(DistributionMetadataVocabulary.WDRS_DESCRIBEDBY, valueFactory.createURI(schemaURL));
        }

        if (!StringUtils.isBlank(config.getSchemaType())) {
            distribution.property(DistributionMetadataVocabulary.POD_DISTRIBUTION_DESCRIBREBYTYPE, valueFactory.createLiteral(config.getSchemaType()));
        }

        if (!StringUtils.isBlank(config.getAccessURL())) {
            distribution.property(DistributionMetadataVocabulary.DCAT_ACCESSURL, valueFactory.createURI(config.getAccessURL()));
        }

        if (!StringUtils.isBlank(config.getDownloadURL())) {
            distribution.property(DistributionMetadataVocabulary.DCAT_DOWNLOADURL, valueFactory.createURI(config.getDownloadURL()));
            distribution.property(DistributionMetadataVocabulary.VOID_DATADUMP, valueFactory.createURI(config.getDownloadURL()));
        }

        if (!StringUtils.isBlank(config.getSparqlEndpointUrl())) {
            distribution.property(DistributionMetadataVocabulary.VOID_SPARQLENDPOINT, valueFactory.createURI(config.getSparqlEndpointUrl()));
        }

        
        if (!StringUtils.isBlank(config.getMediaType())) {
	        final EntityBuilder mediatype = new EntityBuilder(valueFactory.createURI(distributionURI + "/mediatype"), valueFactory);
	        mediatype.property(RDF.TYPE, DCTERMS.MEDIA_TYPE_OR_EXTENT);
	        mediatype.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getMediaType()));
	        rdfData.add(mediatype.asStatements());
	
	        distribution.property(DCTERMS.FORMAT, mediatype);
        }
        
        /*if (!StringUtils.isBlank(config.getMediaType())) {
            distribution.property(DistributionMetadataVocabulary.DCAT_MEDIATYPE, valueFactory.createLiteral(config.getMediaType()));
            distribution.property(DCTERMS.FORMAT, valueFactory.createLiteral(config.getMediaType()));
        }*/
        
        if (!StringUtils.isBlank(license)) {
        	distribution.property(DCTERMS.LICENSE, valueFactory.createURI(license));
        }

        // Lists ...
        for (String example : config.getExampleResources()) {
        	distribution.property(DistributionMetadataVocabulary.VOID_EXAMPLERESOURCE, valueFactory.createURI(example));
        }

        rdfData.add(distribution.asStatements());
        
    }
    
    private String executeSimpleSelectQuery(final String queryAsString, String bindingName) throws DPUException {
        // Prepare SPARQL update query.
        final SparqlUtils.SparqlSelectObject query = faultTolerance.execute(
                new FaultTolerance.ActionReturn<SparqlUtils.SparqlSelectObject>() {

                    @Override
                    public SparqlUtils.SparqlSelectObject action() throws Exception {
                        return SparqlUtils.createSelect(queryAsString,
                                DataUnitUtils.getEntries(inRdfData, RDFDataUnit.Entry.class));
                    }
                });
        final SparqlUtils.QueryResultCollector result = new SparqlUtils.QueryResultCollector();
        faultTolerance.execute(inRdfData, new FaultTolerance.ConnectionAction() {

            @Override
            public void action(RepositoryConnection connection) throws Exception {
                result.prepare();
                SparqlUtils.execute(connection, ctx, query, result);
            }
        });
        if (result.getResults().size() == 1) {
            try {
                return result.getResults().get(0).get(bindingName).stringValue();
            } catch (NumberFormatException ex) {
                throw new DPUException(ex);
            }
        } else if (result.getResults().isEmpty()) {
        	return "";
        } else {
            throw new DPUException("Unexpected number of results: " + result.getResults().size() );
        }
    }

}
