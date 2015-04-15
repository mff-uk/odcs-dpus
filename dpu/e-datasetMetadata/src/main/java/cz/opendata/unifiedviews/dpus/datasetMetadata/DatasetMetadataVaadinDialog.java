package cz.opendata.unifiedviews.dpus.datasetMetadata;

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

public class DatasetMetadataVaadinDialog extends AbstractDialog<DatasetMetadataConfig_V1> {

    private static final long serialVersionUID = 7003725620084616056L;

    private VerticalLayout mainLayout;

    private TextField tfIdentifier;

    private TextField tfLanguage;

    private TextField tfTitle;

    private TextField tfTitleEn;

    private TextField tfDesc;

    private TextField tfDescEn;

    private TextField tfDatasetUri;

    private TextField tfContactPointName;
    
    private TextField tfContactPoint;

    private TextField tfPublisherName;
    
    private TextField tfPublisherURI;

    private TextField tfSchema;

    private CheckBox chkNow;

    private CheckBox chkNowTemporalEnd;

    private DateField dfModified;

    private DateField dfIssued;

    private DateField dfTemporalStart;

    private DateField dfTemporalEnd;

    private TextField tfSpatial;

    private TextField tfLandingPage;

    private TextField tfPeriodicity;

    private ListSelect lsLicenses;

    private ListSelect lsSources;

    private ListSelect lsAuthors;

//    private ListSelect lsPublishers;

    private ListSelect lsKeywords_orig;

    private ListSelect lsKeywords_en;

    private ListSelect lsThemes;

    private ListSelect lsLanguages;

    private final List<String> licenses = new LinkedList<>();
    
    private final List<String> publishers = new LinkedList<>();
    
    private final List<String> languages = new LinkedList<>();

    public DatasetMetadataVaadinDialog() {
        super(DatasetMetadata.class);
        licenses.add("https://creativecommons.org/licenses/by/4.0/");
        licenses.add("https://creativecommons.org/licenses/by-sa/4.0/");
        licenses.add("http://opendatacommons.org/licenses/pddl/1.0/");
        
        publishers.add("http://opendata.cz");
        
        languages.add("http://id.loc.gov/vocabulary/iso639-1/cs");
        languages.add("http://id.loc.gov/vocabulary/iso639-1/en");
        languages.add("http://id.loc.gov/vocabulary/iso639-1/sk");
        languages.add("http://id.loc.gov/vocabulary/iso639-1/it");
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

        tfDatasetUri = new TextField();
        tfDatasetUri.setCaption("Dataset URI:");
        tfDatasetUri.setInputPrompt("http://data.mydomain.com/resource/dataset/mydataset");
        tfDatasetUri.setWidth("100%");
        mainLayout.addComponent(tfDatasetUri);

        tfLanguage = new TextField();
        tfLanguage.setCaption("Original language (RDF language tag, e.g. cs):");
        tfLanguage.setInputPrompt("cs|en|sk|it");
        tfLanguage.setWidth("100%");
        mainLayout.addComponent(tfLanguage);

        tfTitle = new TextField();
        tfTitle.setCaption("Dataset title original language:");
        tfTitle.setInputPrompt("My dataset");
        tfTitle.setWidth("100%");
        mainLayout.addComponent(tfTitle);

        tfTitleEn = new TextField();
        tfTitleEn.setCaption("Dataset title in English:");
        tfTitleEn.setInputPrompt("My dataset");
        tfTitleEn.setWidth("100%");
        mainLayout.addComponent(tfTitleEn);

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

        dfIssued = new DateField();
        dfIssued.setCaption("Issued:");
        dfIssued.setWidth("100%");
        dfIssued.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfIssued);

        dfModified = new DateField();
        dfModified.setCaption("Modified:");
        dfModified.setWidth("100%");
        dfModified.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfModified);

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

        tfIdentifier = new TextField();
        tfIdentifier.setCaption("Identifier:");
        tfIdentifier.setInputPrompt("CTIA_1");
        tfIdentifier.setWidth("100%");
        mainLayout.addComponent(tfIdentifier);

        lsKeywords_orig = new ListSelect();
        lsKeywords_orig.setWidth("100%");
        lsKeywords_orig.setNewItemsAllowed(true);
        lsKeywords_orig.setCaption("Keywords in original language");
        lsKeywords_orig.setMultiSelect(true);
        lsKeywords_orig.setRows(3);
        mainLayout.addComponent(lsKeywords_orig);

        lsKeywords_en = new ListSelect();
        lsKeywords_en.setWidth("100%");
        lsKeywords_en.setNewItemsAllowed(true);
        lsKeywords_en.setCaption("Keywords in English");
        lsKeywords_en.setMultiSelect(true);
        lsKeywords_en.setRows(3);
        mainLayout.addComponent(lsKeywords_en);

        lsLanguages = new ListSelect();
        lsLanguages.setWidth("100%");
        lsLanguages.setNewItemsAllowed(true);
        lsLanguages.setCaption("Languages");
        lsLanguages.setMultiSelect(true);
        lsLanguages.addItems(languages);
        lsLanguages.setRows(3);
        mainLayout.addComponent(lsLanguages);

        tfContactPointName = new TextField();
        tfContactPointName.setCaption("Contact point name:");
        tfContactPointName.setInputPrompt("My organization");
        tfContactPointName.setWidth("100%");
        mainLayout.addComponent(tfContactPointName);

        tfContactPoint = new TextField();
        tfContactPoint.setCaption("Contact point email:");
        tfContactPoint.setInputPrompt("contact@myorganization.com");
        tfContactPoint.setWidth("100%");
        mainLayout.addComponent(tfContactPoint);

        tfPublisherName = new TextField();
        tfPublisherName.setCaption("Publisher name:");
        tfPublisherName.setInputPrompt("Opendata.cz");
        tfPublisherName.setWidth("100%");
        mainLayout.addComponent(tfPublisherName);

        tfPublisherURI = new TextField();
        tfPublisherURI.setCaption("Publisher URI:");
        tfPublisherURI.setInputPrompt("http://opendata.cz");
        tfPublisherURI.setWidth("100%");
        mainLayout.addComponent(tfPublisherURI);

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

        tfSpatial = new TextField();
        tfSpatial.setCaption("Spatial coverage URI:");
        tfSpatial.setInputPrompt("http://ruian.linked.opendata.cz/resource/adresni-mista/25958810");
        tfSpatial.setWidth("100%");
        mainLayout.addComponent(tfSpatial);

        tfPeriodicity = new TextField();
        tfPeriodicity.setCaption("Periodicity:");
        tfPeriodicity.setInputPrompt("R/P1Y");
        tfPeriodicity.setWidth("100%");
        mainLayout.addComponent(tfPeriodicity);

        tfSchema = new TextField();
        tfSchema.setCaption("Schema URL:");
        tfSchema.setInputPrompt("http://data.example.org/dataset/myschema");
        tfSchema.setWidth("100%");
        mainLayout.addComponent(tfSchema);

        tfLandingPage = new TextField();
        tfLandingPage.setCaption("Landing page URL:");
        tfLandingPage.setInputPrompt("http://data.example.org/dataset/mydataset");
        tfLandingPage.setWidth("100%");
        mainLayout.addComponent(tfLandingPage);

        lsLicenses = new ListSelect();
        lsLicenses.setWidth("100%");
        lsLicenses.setNewItemsAllowed(true);
        lsLicenses.setCaption("Licenses");
        lsLicenses.setMultiSelect(false);
        lsLicenses.setNullSelectionAllowed(false);
        lsLicenses.setRows(3);
        lsLicenses.addItems(licenses);
        mainLayout.addComponent(lsLicenses);

        lsSources = new ListSelect();
        lsSources.setWidth("100%");
        lsSources.setNewItemsAllowed(true);
        lsSources.setCaption("Sources");
        lsSources.setMultiSelect(true);
        lsSources.setRows(2);
        mainLayout.addComponent(lsSources);

        lsThemes = new ListSelect();
        lsThemes.setWidth("100%");
        lsThemes.setNewItemsAllowed(true);
        lsThemes.setCaption("Themes");
        lsThemes.setMultiSelect(true);
        lsThemes.setRows(3);
        mainLayout.addComponent(lsThemes);

        lsAuthors = new ListSelect();
        lsAuthors.setWidth("100%");
        lsAuthors.setNewItemsAllowed(true);
        lsAuthors.setCaption("Selected authors");
        lsAuthors.setMultiSelect(true);
        lsAuthors.setRows(2);
        mainLayout.addComponent(lsAuthors);

/*        lsPublishers = new ListSelect();
        lsPublishers.setWidth("100%");
        lsPublishers.setNewItemsAllowed(true);
        lsPublishers.setCaption("Publishers");
        lsPublishers.setMultiSelect(true);
        lsPublishers.addItems(publishers);
        lsPublishers.setRows(2);
        mainLayout.addComponent(lsPublishers);*/

        Panel p = new Panel();
        p.setSizeFull();
        p.setContent(mainLayout);

        setCompositionRoot(p);

        // Tabs.
        this.addTab(ConfigCopyPaste.create(ctx), "Copy&Paste");
    }

    @Override
    public void setConfiguration(DatasetMetadataConfig_V1 conf) throws DPUConfigException {
        tfDatasetUri.setValue(conf.getDatasetURI());
        tfLanguage.setValue(conf.getLanguage_orig());
        tfTitle.setValue(conf.getTitle_cs());
        tfTitleEn.setValue(conf.getTitle_en());
        tfDesc.setValue(conf.getDesc_cs());
        tfDescEn.setValue(conf.getDesc_en());
        dfIssued.setValue(conf.getIssued());
        dfModified.setValue(conf.getModified());
        chkNow.setValue(conf.isUseNow());
        chkNowTemporalEnd.setValue(conf.isUseNowTemporalEnd());
        tfIdentifier.setValue(conf.getIdentifier());
        
    	for (String s: conf.getKeywords_orig()) lsKeywords_orig.addItem(s);
        lsKeywords_orig.setValue(conf.getKeywords_orig());

    	for (String s: conf.getKeywords_en()) lsKeywords_en.addItem(s);
        lsKeywords_en.setValue(conf.getKeywords_en());

        for (String s: conf.getLanguages()) lsLanguages.addItem(s);
    	lsLanguages.setValue(conf.getLanguages());

        tfContactPoint.setValue(conf.getContactPoint());
        tfContactPointName.setValue(conf.getContactPointName());
        dfTemporalStart.setValue(conf.getTemporalStart());
        dfTemporalEnd.setValue(conf.getTemporalEnd());
        tfSpatial.setValue(conf.getSpatial());
        tfPeriodicity.setValue(conf.getPeriodicity());
        tfLandingPage.setValue(conf.getLandingPage());
        tfSchema.setValue(conf.getSchema());

        for (String s: conf.getSources()) lsSources.addItem(s);
    	lsSources.setValue(conf.getSources());

    	for (String s: conf.getAuthors()) lsAuthors.addItem(s);
    	lsAuthors.setValue(conf.getAuthors());

    	tfPublisherName.setValue(conf.getPublisherName());
    	tfPublisherURI.setValue(conf.getPublisherURI());
    	//for (String s: conf.getPublishers()) lsPublishers.addItem(s);
    	//lsPublishers.setValue(conf.getPublishers());

    	if (!conf.getLicense().isEmpty()) lsLicenses.addItem(conf.getLicense());
    	lsLicenses.setValue(conf.getLicense());
        
    	for (String s: conf.getThemes()) lsThemes.addItem(s);
    	lsThemes.setValue(conf.getThemes());
    	
        
    }

    @SuppressWarnings("unchecked")
	@Override
    public DatasetMetadataConfig_V1 getConfiguration() throws DPUConfigException {
        DatasetMetadataConfig_V1 conf = new DatasetMetadataConfig_V1();

        try {
            conf.setDatasetURI((new URL(tfDatasetUri.getValue())).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid dataset URL.", ex);
        }
        
        conf.setLanguage_orig(tfLanguage.getValue());
        conf.setTitle_cs(tfTitle.getValue());
        conf.setTitle_en(tfTitleEn.getValue());
        conf.setDesc_orig(tfDesc.getValue());
        conf.setDesc_en(tfDescEn.getValue());
        conf.setIssued(dfIssued.getValue());
        conf.setModified(dfModified.getValue());
        conf.setUseNow((boolean) chkNow.getValue());
        conf.setUseNowTemporalEnd((boolean) chkNowTemporalEnd.getValue());
        conf.setIdentifier(tfIdentifier.getValue());
        conf.setKeywords_orig((Collection<String>) lsKeywords_orig.getValue());
        conf.setKeywords_en((Collection<String>) lsKeywords_en.getValue());
        conf.setLanguages((Collection<String>) lsLanguages.getValue());
        conf.setContactPoint(tfContactPoint.getValue());
        conf.setContactPointName(tfContactPointName.getValue());
        conf.setTemporalStart(dfTemporalStart.getValue());
        conf.setTemporalEnd(dfTemporalEnd.getValue());
        conf.setPublisherURI(tfPublisherURI.getValue());
        conf.setPublisherName(tfPublisherName.getValue());
        
        try {
	        conf.setSpatial(new URL(tfSpatial.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid spatial converage URL.", ex);
        }
        
        conf.setPeriodicity(tfPeriodicity.getValue());
        conf.setLandingPage(tfLandingPage.getValue());
        
        try {
            if (tfSchema.getValue().isEmpty()) conf.setSchema("");
            else conf.setSchema(new URL(tfSchema.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid schema URL.", ex);
        }

        try {
            conf.setLicense(new URL((String) lsLicenses.getValue()).toString());
        } catch (MalformedURLException ex) {
            throw new DPUConfigException("Invalid license URL.", ex);
        }

        conf.setAuthors((Collection<String>) lsAuthors.getValue());
        //conf.setPublishers((Collection<String>) lsPublishers.getValue());
        conf.setSources((Collection<String>) lsSources.getValue());
        conf.setThemes((Collection<String>) lsThemes.getValue());

        return conf;
    }

}
