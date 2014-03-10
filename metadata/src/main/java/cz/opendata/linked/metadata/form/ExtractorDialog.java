package cz.opendata.linked.metadata.form;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7003725620084616056L;
	private GridLayout mainLayout;
    private TextField tfTitleCs;
    private TextField tfTitleEn;
    private TextField tfDescCs;
    private TextField tfDescEn;
    private TextField tfDatasetUri;
    private TextField tfDataDumpUrl;
    private TextField tfSparqlEndpointUrl;
    private CheckBox chkNow;
    private DateField dfModified;
    private TwinColSelect tcsLicenses;
    private TwinColSelect tcsExamples;
    private TwinColSelect tcsSources;
    private TwinColSelect tcsAuthors;
    private TwinColSelect tcsPublishers;

	public ExtractorDialog() {
		super(ExtractorConfig.class);
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }  
	
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(true);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        tfDatasetUri = new TextField();
        tfDatasetUri.setCaption("Dataset URI:");
        tfDatasetUri.setWidth("100%");
        mainLayout.addComponent(tfDatasetUri);

        tfDataDumpUrl = new TextField();
        tfDataDumpUrl.setCaption("Data dump URL:");
        tfDataDumpUrl.setWidth("100%");
        mainLayout.addComponent(tfDataDumpUrl);

        tfSparqlEndpointUrl = new TextField();
        tfSparqlEndpointUrl.setCaption("Sparql Endpoint URI:");
        tfSparqlEndpointUrl.setWidth("100%");
        mainLayout.addComponent(tfSparqlEndpointUrl);

        tfTitleCs = new TextField();
        tfTitleCs.setCaption("Title (cs):");
        tfTitleCs.setWidth("100%");
        mainLayout.addComponent(tfTitleCs);

        tfTitleEn = new TextField();
        tfTitleEn.setCaption("Title (en):");
        tfTitleEn.setWidth("100%");
        mainLayout.addComponent(tfTitleEn);

        tfDescCs = new TextField();
        tfDescCs.setCaption("Description (cs):");
        tfDescCs.setWidth("100%");
        mainLayout.addComponent(tfDescCs);

        tfDescEn = new TextField();
        tfDescEn.setCaption("Description (en):");
        tfDescEn.setWidth("100%");
        mainLayout.addComponent(tfDescEn);

        dfModified = new DateField();
        dfModified.setCaption("Modified:");
        dfModified.setWidth("100%");
        dfModified.setResolution(Resolution.DAY);
        mainLayout.addComponent(dfModified);

        chkNow = new CheckBox();
        chkNow.setCaption("Always use current date instead");
        chkNow.setWidth("100%");
        mainLayout.addComponent(chkNow);
        
        tcsLicenses = new TwinColSelect();
        tcsLicenses.setWidth("97%");
        tcsLicenses.setNewItemsAllowed(true);
        tcsLicenses.setLeftColumnCaption("Available licenses");
        tcsLicenses.setRightColumnCaption("Selected licenses");
        mainLayout.addComponent(tcsLicenses);

        tcsExamples = new TwinColSelect();
        tcsExamples.setWidth("97%");
        tcsExamples.setNewItemsAllowed(true);
        tcsExamples.setLeftColumnCaption("Available example resources");
        tcsExamples.setRightColumnCaption("Selected example resources");
        mainLayout.addComponent(tcsExamples);

        tcsSources = new TwinColSelect();
        tcsSources.setWidth("97%");
        tcsSources.setNewItemsAllowed(true);
        tcsSources.setLeftColumnCaption("Available sources");
        tcsSources.setRightColumnCaption("Selected sources");
        mainLayout.addComponent(tcsSources);

        tcsAuthors = new TwinColSelect();
        tcsAuthors.setWidth("97%");
        tcsAuthors.setNewItemsAllowed(true);
        tcsAuthors.setLeftColumnCaption("Available authors");
        tcsAuthors.setRightColumnCaption("Selected authors");
        mainLayout.addComponent(tcsAuthors);

        tcsPublishers = new TwinColSelect();
        tcsPublishers.setWidth("97%");
        tcsPublishers.setNewItemsAllowed(true);
        tcsPublishers.setLeftColumnCaption("Available publishers");
        tcsPublishers.setRightColumnCaption("Selected publishers");
        mainLayout.addComponent(tcsPublishers);

        return mainLayout;
    }	
     
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		tfDatasetUri.setValue(conf.datasetURI.toString());
		tfDataDumpUrl.setValue(conf.dataDump.toString());
		tfSparqlEndpointUrl.setValue(conf.sparqlEndpoint.toString());
		tfTitleCs.setValue(conf.title_cs);
		tfTitleEn.setValue(conf.title_en);
		tfDescCs.setValue(conf.desc_cs);
		tfDescEn.setValue(conf.desc_en);
		chkNow.setValue(conf.useNow);
		dfModified.setValue(conf.modified);
		
		setTcsConfig(conf.sources, conf.possibleSources, tcsSources);
		setTcsConfig(conf.authors, conf.possibleAuthors, tcsAuthors);
		setTcsConfig(conf.publishers, conf.possiblePublishers, tcsPublishers);
		setTcsConfig(conf.exampleResources, conf.possibleExampleResources, tcsExamples);
		setTcsConfig(conf.licenses, conf.possibleLicenses, tcsLicenses);
		
	}

	private void setTcsConfig(LinkedList<URL> list, LinkedList<URL> possibleList, TwinColSelect tcs)
	{
		for (URL c : possibleList) tcs.addItem(c.toString());
		tcs.setRows(possibleList.size());
		
        for (URL l : list) {
			if (!tcs.containsId(l.toString())) tcs.addItem(l.toString());
		}
		
        Collection<String> srcs = new LinkedList<String>();
		for (URL l : list)
		{
			srcs.add(l.toString());
		}
		tcs.setValue(srcs);
	}

	
	private void getTcsConfig(LinkedList<URL> list, LinkedList<URL> possibleList, TwinColSelect tcs) throws MalformedURLException
	{
		list.clear();
		for (Object u : (Collection<Object>)tcs.getValue()) {
			if (u instanceof URL) list.add((URL)u);
			else if (u instanceof String) list.add(new URL ((String)u));
		}
	
		possibleList.clear();
		for (Object u : (Collection<Object>)tcs.getItemIds()) {
			if (u instanceof URL) possibleList.add((URL)u);
			else if (u instanceof String) possibleList.add(new URL ((String)u));
		}
	}
	
	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		
		conf.title_cs = tfTitleCs.getValue();
		conf.title_en = tfTitleEn.getValue();
		conf.desc_cs = tfDescCs.getValue();
		conf.desc_en = tfDescEn.getValue();
		conf.licenses = new LinkedList<URL>();
		conf.useNow = chkNow.getValue();
		conf.modified = dfModified.getValue();
		
		try {
			conf.datasetURI = new URL(tfDatasetUri.getValue());
			conf.dataDump = new URL(tfDataDumpUrl.getValue());
			conf.sparqlEndpoint = new URL(tfSparqlEndpointUrl.getValue());

			getTcsConfig(conf.authors, conf.possibleAuthors, tcsAuthors);
			getTcsConfig(conf.publishers, conf.possiblePublishers, tcsPublishers);
			getTcsConfig(conf.licenses, conf.possibleLicenses, tcsLicenses);
			getTcsConfig(conf.exampleResources, conf.possibleExampleResources, tcsExamples);
			getTcsConfig(conf.sources, conf.possibleSources, tcsSources);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return conf;
	}
	
}
