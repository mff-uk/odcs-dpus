package cz.opendata.unifiedviews.dpus.distributionMetadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;
import eu.unifiedviews.helpers.dpu.vaadin.tabs.ConfigCopyPaste;


public class DistributionMetadataVaadinDialog extends AbstractDialog<DistributionMetadataConfig_V1> {

    private static final long serialVersionUID = 7003725620084616056L;

    private VerticalLayout mainLayout;

    private CheckBox chkDatasetURIFromInput;

    private TextField tfDatasetURI;

    private CheckBox chkGenerateDistroURIFromDataset;

    private TextField tfDistributionURI;

    private CheckBox chkLanguageFromInput;

    private TextField tfLanguage;

    private CheckBox chkTitleFromInput;

    private TextField tfTitle;

    private TextField tfTitleEn;

    private CheckBox chkDescriptionFromInput;

    private TextField tfDesc;

    private TextField tfDescEn;

    private CheckBox chkSchemaFromInput;

    private CheckBox chkUseTemporal;

    private CheckBox chkNowTemporalEnd;

    private TextField tfSchema;

    private TextField tfSchemaType;

    private TextField tfSPARQLEndpointURL;

    private TextField tfDownloadURL;

    private TextField tfAccessURL;

    private TextField tfMediaType;

    private CheckBox chkNow;

    private DateField dfModified;

    private CheckBox chkIssuedFromInput;

    private DateField dfIssued;

    private CheckBox chkTemporalFromInput;

    private DateField dfTemporalStart;

    private DateField dfTemporalEnd;

//    private CheckBox chkSpatialFromInput;
//
//    private TextField tfSpatial;

    private CheckBox chkLicensesFromInput;

    private ListSelect lsLicenses;

    private ListSelect lsExampleResources;

    private final List<String> licenses = new LinkedList<>();
    
    public DistributionMetadataVaadinDialog() {
        super(DistributionMetadata.class);
        licenses.add("https://creativecommons.org/licenses/by/4.0/");
        licenses.add("https://creativecommons.org/licenses/by-sa/4.0/");
        licenses.add("http://opendatacommons.org/licenses/pddl/1.0/");
        
    }

    @Override
    protected void buildDialogLayout() {
        // common part: create layout
        mainLayout = new VerticalLayout();
        mainLayout.setImmediate(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight(null);
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        tfDownloadURL = new TextField();
        tfDownloadURL.setCaption("Download URL:");
        tfDownloadURL.setInputPrompt("http://data.mydomain.com/dumps/dataset.ttl");
        tfDownloadURL.setWidth("100%");
        mainLayout.addComponent(tfDownloadURL);

        tfMediaType = new TextField();
        tfMediaType.setCaption("Media (MIME) type:");
        tfMediaType.setInputPrompt("text/turtle|text/csv");
        tfMediaType.setWidth("100%");
        mainLayout.addComponent(tfMediaType);

        tfAccessURL = new TextField();
        tfAccessURL.setCaption("Access URL:");
        tfAccessURL.setInputPrompt("http://data.mydomain.com/dataset/dataset");
        tfAccessURL.setWidth("100%");
        mainLayout.addComponent(tfAccessURL);

        lsExampleResources = new ListSelect();
        lsExampleResources.setWidth("100%");
        lsExampleResources.setNewItemsAllowed(true);
        lsExampleResources.setCaption("Example resources");
        lsExampleResources.setMultiSelect(true);
        lsExampleResources.setRows(3);
        mainLayout.addComponent(lsExampleResources);

        tfSPARQLEndpointURL = new TextField();
        tfSPARQLEndpointURL.setCaption("SPARQL Endpoint URL:");
        tfSPARQLEndpointURL.setInputPrompt("http://linked.opendata.cz/sparql");
        tfSPARQLEndpointURL.setWidth("100%");
        mainLayout.addComponent(tfSPARQLEndpointURL);

        tfSchemaType = new TextField();
        tfSchemaType.setCaption("Schema MIME type:");
        tfSchemaType.setInputPrompt("text/csv");
        tfSchemaType.setWidth("100%");
        mainLayout.addComponent(tfSchemaType);

        chkSchemaFromInput = new CheckBox();
        chkSchemaFromInput.setCaption("Use schema from dataset");
        chkSchemaFromInput.setWidth("100%");
        chkSchemaFromInput.setImmediate(true);
        chkSchemaFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfSchema.setEnabled(!chkSchemaFromInput.getValue());
		}});
        mainLayout.addComponent(chkSchemaFromInput);

        tfSchema = new TextField();
        tfSchema.setCaption("Schema URL:");
        tfSchema.setInputPrompt("http://data.example.org/dataset/myschema");
        tfSchema.setWidth("100%");
        mainLayout.addComponent(tfSchema);

        chkDatasetURIFromInput = new CheckBox();
        chkDatasetURIFromInput.setCaption("Get dataset URI from dataset");
        chkDatasetURIFromInput.setWidth("100%");
        chkDatasetURIFromInput.setImmediate(true);
        chkDatasetURIFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfDatasetURI.setEnabled(!chkDatasetURIFromInput.getValue());
		}});
        mainLayout.addComponent(chkDatasetURIFromInput);

        tfDatasetURI = new TextField();
        tfDatasetURI.setCaption("Dataset URI:");
        tfDatasetURI.setInputPrompt("http://data.mydomain.com/resource/dataset/mydataset");
        tfDatasetURI.setWidth("100%");
        mainLayout.addComponent(tfDatasetURI);

        chkGenerateDistroURIFromDataset = new CheckBox();
        chkGenerateDistroURIFromDataset.setCaption("Generate distribution URI from dataset (+\"/distribution\")");
        chkGenerateDistroURIFromDataset.setWidth("100%");
        chkGenerateDistroURIFromDataset.setImmediate(true);
        chkGenerateDistroURIFromDataset.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfDistributionURI.setEnabled(!chkGenerateDistroURIFromDataset.getValue());
		}});
        mainLayout.addComponent(chkGenerateDistroURIFromDataset);

        tfDistributionURI = new TextField();
        tfDistributionURI.setCaption("Distribution URI:");
        tfDistributionURI.setInputPrompt("http://data.mydomain.com/resource/dataset/mydataset/distribution/rdf");
        tfDistributionURI.setWidth("100%");
        mainLayout.addComponent(tfDistributionURI);

        chkLanguageFromInput = new CheckBox();
        chkLanguageFromInput.setCaption("Get original language from dataset");
        chkLanguageFromInput.setWidth("100%");
        chkLanguageFromInput.setImmediate(true);
        chkLanguageFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfLanguage.setEnabled(!chkLanguageFromInput.getValue());
		}});
        mainLayout.addComponent(chkLanguageFromInput);

        tfLanguage = new TextField();
        tfLanguage.setCaption("Original language (RDF language tag, e.g. cs):");
        tfLanguage.setInputPrompt("cs|en|sk|it");
        tfLanguage.setWidth("100%");
        mainLayout.addComponent(tfLanguage);

        chkTitleFromInput = new CheckBox();
        chkTitleFromInput.setCaption("Get title from dataset");
        chkTitleFromInput.setWidth("100%");
        chkTitleFromInput.setImmediate(true);
        chkTitleFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfTitle.setEnabled(!chkTitleFromInput.getValue());
				tfTitleEn.setEnabled(!chkTitleFromInput.getValue());
		}});
        mainLayout.addComponent(chkTitleFromInput);

        tfTitle = new TextField();
        tfTitle.setCaption("Dataset title in original language:");
        tfTitle.setInputPrompt("My dataset");
        tfTitle.setWidth("100%");
        mainLayout.addComponent(tfTitle);

        tfTitleEn = new TextField();
        tfTitleEn.setCaption("Dataset title in English:");
        tfTitleEn.setInputPrompt("My dataset");
        tfTitleEn.setWidth("100%");
        mainLayout.addComponent(tfTitleEn);

        chkDescriptionFromInput = new CheckBox();
        chkDescriptionFromInput.setCaption("Get description from dataset");
        chkDescriptionFromInput.setWidth("100%");
        chkDescriptionFromInput.setImmediate(true);
        chkDescriptionFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				tfDesc.setEnabled(!chkDescriptionFromInput.getValue());
				tfDescEn.setEnabled(!chkDescriptionFromInput.getValue());
		}});
        mainLayout.addComponent(chkDescriptionFromInput);

        tfDesc = new TextField();
        tfDesc.setCaption("Description in original language:");
        tfDesc.setInputPrompt("Longer description in original language");
        tfDesc.setWidth("100%");
        mainLayout.addComponent(tfDesc);

        tfDescEn = new TextField();
        tfDescEn.setCaption("Description in English:");
        tfDescEn.setInputPrompt("Longer description in English");
        tfDescEn.setWidth("100%");
        mainLayout.addComponent(tfDescEn);

        chkIssuedFromInput = new CheckBox();
        chkIssuedFromInput.setCaption("Use issued date from dataset");
        chkIssuedFromInput.setWidth("100%");
        chkIssuedFromInput.setImmediate(true);
        chkIssuedFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				dfIssued.setEnabled(!chkIssuedFromInput.getValue());
		}});
        mainLayout.addComponent(chkIssuedFromInput);

        dfIssued = new DateField();
        dfIssued.setCaption("Issued:");
        dfIssued.setWidth("100%");
        dfIssued.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfIssued);

        chkNow = new CheckBox();
        chkNow.setCaption("Use current date as modified");
        chkNow.setWidth("100%");
        chkNow.setImmediate(true);
        chkNow.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				dfModified.setEnabled(!chkNow.getValue());
		}});
        mainLayout.addComponent(chkNow);

        dfModified = new DateField();
        dfModified.setCaption("Modified:");
        dfModified.setWidth("100%");
        dfModified.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfModified);

        chkUseTemporal = new CheckBox();
        chkUseTemporal.setCaption("Use temporal coverage");
        chkUseTemporal.setWidth("100%");
        chkUseTemporal.setImmediate(true);
        chkUseTemporal.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				chkTemporalFromInput.setEnabled(chkUseTemporal.getValue());
				dfTemporalStart.setEnabled(chkUseTemporal.getValue() && !chkTemporalFromInput.getValue());
				dfTemporalEnd.setEnabled(chkUseTemporal.getValue() && !chkNowTemporalEnd.getValue() && !chkTemporalFromInput.getValue());
				chkNowTemporalEnd.setEnabled(chkUseTemporal.getValue() && !chkTemporalFromInput.getValue());
		}});
        mainLayout.addComponent(chkUseTemporal);

        chkTemporalFromInput = new CheckBox();
        chkTemporalFromInput.setCaption("Use temporal coverage from dataset");
        chkTemporalFromInput.setWidth("100%");
        chkTemporalFromInput.setImmediate(true);
        chkTemporalFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				dfTemporalStart.setEnabled(!chkTemporalFromInput.getValue());
				dfTemporalEnd.setEnabled(!chkTemporalFromInput.getValue() && !chkNowTemporalEnd.getValue());
				chkNowTemporalEnd.setEnabled(!chkTemporalFromInput.getValue());
		}});
        mainLayout.addComponent(chkTemporalFromInput);

        dfTemporalStart = new DateField();
        dfTemporalStart.setCaption("Temporal coverage start:");
        dfTemporalStart.setWidth("100%");
        dfTemporalStart.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfTemporalStart);

        dfTemporalEnd = new DateField();
        dfTemporalEnd.setCaption("Temporal coverage end:");
        dfTemporalEnd.setWidth("100%");
        dfTemporalEnd.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfTemporalEnd);

        chkNowTemporalEnd = new CheckBox();
        chkNowTemporalEnd.setCaption("Use current date as temporal coverage end");
        chkNowTemporalEnd.setWidth("100%");
        chkNowTemporalEnd.setImmediate(true);
        chkNowTemporalEnd.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				dfTemporalEnd.setEnabled(!chkNowTemporalEnd.getValue());
		}});
        mainLayout.addComponent(chkNowTemporalEnd);

//        chkSpatialFromInput = new CheckBox();
//        chkSpatialFromInput.setCaption("Use spatial coverage from dataset");
//        chkSpatialFromInput.setWidth("100%");
//        chkSpatialFromInput.setImmediate(true);
//        chkSpatialFromInput.addValueChangeListener(new ValueChangeListener() {
//			private static final long serialVersionUID = -6135328311357043784L;
//
//			@Override
//			public void valueChange(ValueChangeEvent event) {
//				tfSpatial.setEnabled(!chkSpatialFromInput.getValue());
//		}});
//        mainLayout.addComponent(chkSpatialFromInput);
//
//        tfSpatial = new TextField();
//        tfSpatial.setCaption("Spatial coverage URI:");
//        tfSpatial.setInputPrompt("http://ruian.linked.opendata.cz/resource/adresni-mista/25958810");
//        tfSpatial.setWidth("100%");
//        mainLayout.addComponent(tfSpatial);

        chkLicensesFromInput = new CheckBox();
        chkLicensesFromInput.setCaption("Use license from dataset");
        chkLicensesFromInput.setWidth("100%");
        chkLicensesFromInput.setImmediate(true);
        chkLicensesFromInput.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6135328311357043784L;

			@Override
			public void valueChange(ValueChangeEvent event) {
				lsLicenses.setEnabled(!chkLicensesFromInput.getValue());
		}});
        mainLayout.addComponent(chkLicensesFromInput);

        lsLicenses = new ListSelect();
        lsLicenses.setWidth("100%");
        lsLicenses.setNewItemsAllowed(true);
        lsLicenses.setCaption("License");
        lsLicenses.setMultiSelect(false);
        lsLicenses.setNullSelectionAllowed(false);
        lsLicenses.setRows(3);
        lsLicenses.addItems(licenses);
        mainLayout.addComponent(lsLicenses);

        Panel p = new Panel();
        p.setSizeFull();
        p.setContent(mainLayout);

        setCompositionRoot(p);

        // Tabs.
        this.addTab(ConfigCopyPaste.create(ctx), "Copy&Paste");
    }

    @Override
    public void setConfiguration(DistributionMetadataConfig_V1 conf) throws DPUConfigException {
        tfDatasetURI.setValue(conf.getDatasetURI());
        chkGenerateDistroURIFromDataset.setValue(conf.isGenerateDistroURIFromDataset());
        tfDistributionURI.setValue(conf.getDistributionURI());
        chkDatasetURIFromInput.setValue(conf.isUseDatasetURIfromInput());
        chkLanguageFromInput.setValue(conf.isOriginalLanguageFromDataset());
        tfLanguage.setValue(conf.getLanguage_orig());
        chkTitleFromInput.setValue(conf.isTitleFromDataset());
        tfTitle.setValue(conf.getTitle_orig());
        tfTitleEn.setValue(conf.getTitle_en());
        chkDescriptionFromInput.setValue(conf.isDescriptionFromDataset());
        tfDesc.setValue(conf.getDesc_orig());
        tfDescEn.setValue(conf.getDesc_en());
        chkIssuedFromInput.setValue(conf.isIssuedFromDataset());
        dfIssued.setValue(conf.getIssued());
        dfModified.setValue(conf.getModified());
        chkNow.setValue(conf.isUseNow());
        chkNowTemporalEnd.setValue(conf.isUseNowTemporalEnd());
        chkUseTemporal.setValue(conf.isUseTemporal());
        tfDownloadURL.setValue(conf.getDownloadURL());
        tfAccessURL.setValue(conf.getAccessURL());
        tfMediaType.setValue(conf.getMediaType());
        chkSchemaFromInput.setValue(conf.isSchemaFromDataset());
        tfSchema.setValue(conf.getSchema());
        tfSchemaType.setValue(conf.getSchemaType());
        tfSPARQLEndpointURL.setValue(conf.getSparqlEndpointUrl());
        
        chkTemporalFromInput.setValue(conf.isTemporalFromDataset());
        dfTemporalStart.setValue(conf.getTemporalStart());
        dfTemporalEnd.setValue(conf.getTemporalEnd());
//        chkSpatialFromInput.setValue(conf.isSpatialFromDataset());
//        tfSpatial.setValue(conf.getSpatial());

    	for (String s: conf.getExampleResources()) lsExampleResources.addItem(s);
    	lsExampleResources.setValue(conf.getExampleResources());

        chkLicensesFromInput.setValue(conf.isLicenseFromDataset());
    	lsLicenses.setValue(conf.getLicense());
    }

	@SuppressWarnings("unchecked")
	@Override
    public DistributionMetadataConfig_V1 getConfiguration() throws DPUConfigException {
        DistributionMetadataConfig_V1 conf = new DistributionMetadataConfig_V1();

        conf.setUseDatasetURIfromInput(chkDatasetURIFromInput.getValue());
        try {
            conf.setDatasetURI((new URL(tfDatasetURI.getValue())).toString());
        } catch (MalformedURLException ex) {
        	if (chkDatasetURIFromInput.getValue()) conf.setDatasetURI(""); 
        	else throw new DPUConfigException("Invalid dataset URI.", ex);
        }

        conf.setGenerateDistroURIFromDataset(chkGenerateDistroURIFromDataset.getValue());
        
        try {
            conf.setDistributionURI((new URL(tfDistributionURI.getValue())).toString());
        } catch (MalformedURLException ex) {
        	if (chkGenerateDistroURIFromDataset.getValue()) conf.setDistributionURI(""); 
        	else throw new DPUConfigException("Invalid distribution URI.", ex);
        }

        conf.setOriginalLanguageFromDataset(chkLanguageFromInput.getValue());
        conf.setLanguage_orig(tfLanguage.getValue());
        conf.setTitleFromDataset(chkTitleFromInput.getValue());
        conf.setTitle_orig(tfTitle.getValue());
        conf.setTitle_en(tfTitleEn.getValue());
        conf.setDescriptionFromDataset(chkDescriptionFromInput.getValue());
        conf.setDesc_orig(tfDesc.getValue());
        conf.setDesc_en(tfDescEn.getValue());
        conf.setIssuedFromDataset(chkIssuedFromInput.getValue());
        conf.setIssued(dfIssued.getValue());
        conf.setModified(dfModified.getValue());
        conf.setUseNow((boolean) chkNow.getValue());
        conf.setUseNowTemporalEnd((boolean) chkNowTemporalEnd.getValue());
        conf.setUseTemporal((boolean) chkUseTemporal.getValue());
        conf.setTemporalFromDataset(chkTemporalFromInput.getValue());
        conf.setTemporalStart(dfTemporalStart.getValue());
        conf.setTemporalEnd(dfTemporalEnd.getValue());

//        conf.setSpatialFromDataset(chkSpatialFromInput.getValue());
//        try {
//	        conf.setSpatial(new URL(tfSpatial.getValue()).toString());
//        } catch (MalformedURLException ex) {
//            if (chkSpatialFromInput.getValue()) conf.setSpatial("");
//            else throw new DPUConfigException("Invalid spatial converage URL.", ex);
//        }
        
        conf.setSchemaFromDataset(chkSchemaFromInput.getValue());
        try {
            conf.setSchema(new URL(tfSchema.getValue()).toString());
        } catch (MalformedURLException ex) {
            if (chkSchemaFromInput.getValue()) conf.setSchema("");
            else throw new DPUConfigException("Invalid schema URL.", ex);
        }

        conf.setSchemaType(tfSchemaType.getValue());
        conf.setMediaType(tfMediaType.getValue());

        conf.setLicenseFromDataset(chkLicensesFromInput.getValue());
        try {
            conf.setLicense(new URL((String)lsLicenses.getValue()).toString());
        } catch (MalformedURLException ex) {
            if (chkLicensesFromInput.getValue()) conf.setLicense("");
            else throw new DPUConfigException("Invalid license URL.", ex);
        }

        try {
            conf.setDownloadURL(new URL(tfDownloadURL.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid download URL.", ex);
        }

        try {
            String val = tfSPARQLEndpointURL.getValue();
            if (val.isEmpty()) conf.setSparqlEndpointUrl("");
            else conf.setSparqlEndpointUrl(new URL(tfSPARQLEndpointURL.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid SPARQL Endpoint URL.", ex);
        }

        try {
            conf.setAccessURL(new URL(tfAccessURL.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid access URL.", ex);
        }

        try {
            conf.setSchema(new URL(tfSchema.getValue()).toString());
        } catch (MalformedURLException ex) {
            if (chkSchemaFromInput.getValue()) conf.setSchema("");
            else throw new DPUConfigException("Invalid schema URL.", ex);
        }

        try {
            for (String resource: (Collection<String>) lsExampleResources.getValue())
        	conf.getExampleResources().add(new URL(resource).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid example resource URL: " + ex.getMessage(), ex);
        }

        return conf;
    }

}
