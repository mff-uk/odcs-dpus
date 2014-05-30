package cz.opendata.linked.geocoder.nominatim;

import com.vaadin.ui.*;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

import java.util.Collection;
import java.util.LinkedList;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {
	
	private GridLayout mainLayout;
    private TextField interval;
    private TextField limit;
    private TextField limitPeriod;
    private TextField country;
    private CheckBox isStructured;
    private CheckBox stripNumFromLocality;
    private CheckBox generateMapUrl;
    private TwinColSelect tcsProperties;
    private String properties[] = {"s:streetAddress", "s:addressRegion", "s:addressLocality", "s:postalCode", "s:addressCountry"};
    
	public ExtractorDialog() {
		super(ExtractorConfig.class);
        buildMainLayout();
		Panel panel = new Panel();
		panel.setSizeFull();
		panel.setContent(mainLayout);
		setCompositionRoot(panel);
	}  
	
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        isStructured = new CheckBox("Structured query (experimental)");
        isStructured.setDescription("When selected, Nominatim will be queried by structured queries.");
        isStructured.setWidth("100%");
        mainLayout.addComponent(isStructured);

        stripNumFromLocality = new CheckBox("Strip number form Locality");
        stripNumFromLocality.setDescription("In structured query, replace Praha 6 => Praha, etc.");
        stripNumFromLocality.setWidth("100%");
        mainLayout.addComponent(stripNumFromLocality);

        generateMapUrl = new CheckBox("Generate schema:url property with direct link to map");
        generateMapUrl.setDescription("Useful option for debugging results, URL to the service is constructed automatically and can be opened from SPARQL query result");
        generateMapUrl.setWidth("100%");
        mainLayout.addComponent(generateMapUrl);

        country = new TextField();
        country.setCaption("Country");
        country.setDescription("Fallback to schema:addressCountry property");
        mainLayout.addComponent(country);

        interval = new TextField();
        interval.setCaption("Interval between downloads (sec):");
        mainLayout.addComponent(interval);

        limit = new TextField();
        limit.setCaption("Maximum number of downloads:");
        mainLayout.addComponent(limit);

        limitPeriod = new TextField();
        limitPeriod.setCaption("Length of period for which the limit is applied (hrs):");
        mainLayout.addComponent(limitPeriod);

        tcsProperties = new TwinColSelect("Select properties to use");
		tcsProperties.setSizeFull();
        tcsProperties.setLeftColumnCaption("Supported properties");
        tcsProperties.setRightColumnCaption("Selected properties");
        for (int p = 0; p < properties.length; p++) {
			tcsProperties.addItem(properties[p]);
		}
        tcsProperties.setRows(properties.length);
        mainLayout.addComponent(tcsProperties);
        
        return mainLayout;
    }	
     
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		interval.setValue(Integer.toString(conf.getInterval()));
		limitPeriod.setValue(Integer.toString(conf.getLimitPeriod()));
		limit.setValue(Integer.toString(conf.getLimit()));
		country.setValue(conf.getCountry());
		isStructured.setValue(conf.isStructured());
		generateMapUrl.setValue(conf.isGenerateMapUrl());
		stripNumFromLocality.setValue(conf.isStripNumFromLocality());

		LinkedList<String> values = new LinkedList<String>();
		if (conf.isUseStreet()) values.add(properties[0]);
		if (conf.isUseRegion()) values.add(properties[1]);
		if (conf.isUseLocality()) values.add(properties[2]);
		if (conf.isUsePostalCode()) values.add(properties[3]);
		if (conf.isUseCountry()) values.add(properties[4]);
		tcsProperties.setValue(values);
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setInterval(Integer.parseInt(interval.getValue()));
		conf.setLimitPeriod(Integer.parseInt(limitPeriod.getValue()));
		conf.setCountry(country.getValue());
		conf.setLimit(Integer.parseInt(limit.getValue()));
		conf.setStructured(isStructured.getValue());
		conf.setStripNumFromLocality(stripNumFromLocality.getValue());
		conf.setGenerateMapUrl(generateMapUrl.getValue());

		Collection<String> values = (Collection<String>)tcsProperties.getValue();
		conf.setUseStreet(values.contains(properties[0])); 
		conf.setUseRegion(values.contains(properties[1]));
		conf.setUseLocality(values.contains(properties[2]));
		conf.setUsePostalCode(values.contains(properties[3]));
		conf.setUseCountry(values.contains(properties[4]));

		return conf;
	}
	
}
