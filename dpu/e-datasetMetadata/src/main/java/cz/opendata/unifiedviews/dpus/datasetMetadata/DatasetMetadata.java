package cz.opendata.unifiedviews.dpus.datasetMetadata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.SKOS;

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

@DPU.AsExtractor
public class DatasetMetadata extends AbstractDpu<DatasetMetadataConfig_V1> {

    @DataUnit.AsOutput(name = "metadata")
    public WritableRDFDataUnit outRdfData;

    @ExtensionInitializer.Init(param = "outRdfData")
    public WritableSimpleRdf rdfData;

    @ExtensionInitializer.Init
    public FaultTolerance faultTolerance;

    public DatasetMetadata() {
        super(DatasetMetadataVaadinDialog.class, ConfigHistory.noHistory(DatasetMetadataConfig_V1.class));
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
                return RdfDataUnitUtils.addGraph(outRdfData, DataUnitUtils.generateSymbolicName(DatasetMetadata.class));
            }
        });
        faultTolerance.execute(new FaultTolerance.Action() {

            @Override
            public void action() throws Exception {
                rdfData.setOutput(entry);
            }
        });
        // Prepare dataset entity and fill it with data.
        final EntityBuilder dataset = new EntityBuilder(valueFactory.createURI(config.getDatasetURI()),
                valueFactory);
        dataset.property(RDF.TYPE, DatasetMetadataVocabulary.DCAT_DATASET_CLASS);

        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // Build metadata ...

        // Title.
        if (!StringUtils.isBlank(config.getTitle_cs())) {
            dataset.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_cs(),
                    config.getLanguage_orig()));
        }
        if (!StringUtils.isBlank(config.getTitle_en())) {
            dataset.property(DCTERMS.TITLE, valueFactory.createLiteral(config.getTitle_en(), "en"));
        }

        // Description.
        if (!StringUtils.isBlank(config.getDesc_cs())) {
            dataset.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_cs(),
                    config.getLanguage_orig()));
        }
        if (!StringUtils.isBlank(config.getDesc_en())) {
            dataset.property(DCTERMS.DESCRIPTION, valueFactory.createLiteral(config.getDesc_en(), "en"));
        }

        // issued
       	dataset.property(DCTERMS.ISSUED, valueFactory.createLiteral(dateFormat.format(config.getIssued()),
                DatasetMetadataVocabulary.XSD_DATE));
        
        // modified
        if (config.isUseNow()) {
            dataset.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(new Date()),
                    DatasetMetadataVocabulary.XSD_DATE));
        } else {
            dataset.property(DCTERMS.MODIFIED, valueFactory.createLiteral(dateFormat.format(config
                    .getModified()),
                    DatasetMetadataVocabulary.XSD_DATE));
        }
        
        if (!StringUtils.isBlank(config.getIdentifier())) {
            dataset.property(DCTERMS.IDENTIFIER, valueFactory.createLiteral(config.getIdentifier()));
        }

        for (String keyword : config.getKeywords_orig()) {
            dataset.property(DatasetMetadataVocabulary.DCAT_KEYWORD, valueFactory.createLiteral(keyword, config.getLanguage_orig()));
        }

        for (String keyword : config.getKeywords_en()) {
            dataset.property(DatasetMetadataVocabulary.DCAT_KEYWORD, valueFactory.createLiteral(keyword, "en"));
        }

        for (String language : config.getLanguages()) {
            dataset.property(DCTERMS.LANGUAGE, valueFactory.createURI(language));
        }

        if (!StringUtils.isBlank(config.getContactPoint())) {
            final EntityBuilder contactPoint = new EntityBuilder(valueFactory.createURI(config.getDatasetURI() + "/contactPoint"),
                    valueFactory);
            contactPoint.property(RDF.TYPE, DatasetMetadataVocabulary.VCARD_VCARD_CLASS);
            contactPoint.property(DatasetMetadataVocabulary.VCARD_HAS_EMAIL, valueFactory.createLiteral(config.getContactPoint()));
            rdfData.add(contactPoint.asStatements());

            dataset.property(DatasetMetadataVocabulary.ADMS_CONTACT_POINT, contactPoint);
        }

        if (!StringUtils.isBlank(config.getPeriodicity())) {
            dataset.property(DCTERMS.ACCRUAL_PERIODICITY, valueFactory.createLiteral(config.getPeriodicity()));
        }

        if (!StringUtils.isBlank(config.getLandingPage())) {
            dataset.property(DatasetMetadataVocabulary.DCAT_LANDING_PAGE, valueFactory.createLiteral(config.getLandingPage()));
        }

        final EntityBuilder temporal = new EntityBuilder(valueFactory.createURI(config.getDatasetURI() + "/temporal"),
                valueFactory);
        temporal.property(RDF.TYPE, DCTERMS.PERIOD_OF_TIME);
        temporal.property(DatasetMetadataVocabulary.SCHEMA_STARTDATE, valueFactory.createLiteral(dateFormat.format(config
                .getTemporalStart()), DatasetMetadataVocabulary.XSD_DATE));
        temporal.property(DatasetMetadataVocabulary.SCHEMA_ENDDATE, valueFactory.createLiteral(dateFormat.format(config
                .getTemporalEnd()), DatasetMetadataVocabulary.XSD_DATE));
        rdfData.add(temporal.asStatements());

        dataset.property(DCTERMS.TEMPORAL, temporal);

        if (!StringUtils.isBlank(config.getSpatial())) {
            dataset.property(DCTERMS.SPATIAL, valueFactory.createURI(config.getSpatial()));
        }

        if (!StringUtils.isBlank(config.getSchema())) {
            dataset.property(DCTERMS.REFERENCES, valueFactory.createURI(config.getSchema()));
        }

        // Lists ...
        for (String author : config.getAuthors()) {
            dataset.property(DCTERMS.CREATOR, valueFactory.createURI(author));
        }
        for (String publisherName : config.getPublishers()) {
            dataset.property(DCTERMS.PUBLISHER, valueFactory.createURI(publisherName));
        }
        if (!StringUtils.isBlank(config.getLicense())) {
        	dataset.property(DCTERMS.LICENSE, valueFactory.createURI(config.getLicense()));
        }
        for (String source : config.getSources()) {
            dataset.property(DCTERMS.SOURCE, valueFactory.createURI(source));
        }
        for (String themeUri : config.getThemes()) {
            final EntityBuilder theme = new EntityBuilder(valueFactory.createURI(themeUri),
                    valueFactory);
            theme.property(RDF.TYPE, SKOS.CONCEPT);
            //theme.property(SKOS.IN_SCHEME,
            //        valueFactory.createURI("http://linked.opendata.cz/resource/catalog/Themes"));
            rdfData.add(theme.asStatements());

            dataset.property(DatasetMetadataVocabulary.DCAT_THEME, theme);
        }
        
        rdfData.add(dataset.asStatements());
        
    }

}
