package cz.opendata.linked.geocoder.google;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	private GridLayout mainLayout;
    private TextField interval;
    private TextField limit;
    private TextField hoursToCheck;

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
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);

        limit = new TextField();
        limit.setCaption("Maximum amount of downloads:");
        mainLayout.addComponent(limit);

        hoursToCheck = new TextField();
        hoursToCheck.setCaption("Maximum amount interval:");
        mainLayout.addComponent(hoursToCheck);

        return mainLayout;
    }	
     
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		interval.setValue(Integer.toString(conf.getInterval()));
		hoursToCheck.setValue(Integer.toString(conf.getHoursToCheck()));
		limit.setValue(Integer.toString(conf.getLimit()));

	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setInterval(Integer.parseInt(interval.getValue()));
		conf.setHoursToCheck(Integer.parseInt(hoursToCheck.getValue()));
		conf.setLimit(Integer.parseInt(limit.getValue()));
		return conf;
	}
	
}
