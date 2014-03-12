package cz.opendata.linked.geocoder.nominatim;

import com.vaadin.ui.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;


import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {
	
	private static final long serialVersionUID = 7003725620084616056L;
	
	private GridLayout mainLayout;
    private TextField interval;
    private TextField limit;
    private TextField hoursToCheck;
    private TextField tfCountry;
    private CheckBox chkStructured;
    private CheckBox chkStripNumFromLocality;
    private TwinColSelect tcsProperties;
    private String properties[] = {"s:streetAddress", "s:addressRegion", "s:addressLocality", "s:postalCode"};
    
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

        chkStructured = new CheckBox("Structured query (experimental):");
        chkStructured.setDescription("When selected, Nominatim will be queried by structured queries.");
        chkStructured.setWidth("100%");
        
        mainLayout.addComponent(chkStructured);

        chkStripNumFromLocality = new CheckBox("Strip number form Locality:");
        chkStripNumFromLocality.setDescription("In structured query, replace Praha 6 => Praha, etc.");
        chkStripNumFromLocality.setWidth("100%");
        
        mainLayout.addComponent(chkStripNumFromLocality);

        tfCountry = new TextField();
        tfCountry.setCaption("Country");
        mainLayout.addComponent(tfCountry);

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);

        limit = new TextField();
        limit.setCaption("Maximum amount of downloads:");
        mainLayout.addComponent(limit);

        hoursToCheck = new TextField();
        hoursToCheck.setCaption("Maximum amount interval:");
        mainLayout.addComponent(hoursToCheck);

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
		hoursToCheck.setValue(Integer.toString(conf.getHoursToCheck()));
		limit.setValue(Integer.toString(conf.getLimit()));
		tfCountry.setValue(conf.getCountry());
		chkStructured.setValue(conf.isStructured());
		chkStripNumFromLocality.setValue(conf.isStripNumFromLocality());

		LinkedList<String> values = new LinkedList<String>();
		if (conf.isUseStreet()) values.add(properties[0]);
		if (conf.isUseRegion()) values.add(properties[1]);
		if (conf.isUseLocality()) values.add(properties[2]);
		if (conf.isUsePostalCode()) values.add(properties[3]);
		tcsProperties.setValue(values);
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setInterval(Integer.parseInt(interval.getValue()));
		conf.setHoursToCheck(Integer.parseInt(hoursToCheck.getValue()));
		conf.setCountry(tfCountry.getValue());
		conf.setLimit(Integer.parseInt(limit.getValue()));
		conf.setStructured((boolean) chkStructured.getValue());
		conf.setStripNumFromLocality((boolean) chkStripNumFromLocality.getValue());
		
		Collection<String> values = (Collection<String>)tcsProperties.getValue();
		conf.setUseStreet(values.contains(properties[0])); 
		conf.setUseRegion(values.contains(properties[1]));
		conf.setUseLocality(values.contains(properties[2]));
		conf.setUsePostalCode(values.contains(properties[3]));
		
		return conf;
	}
	
}
