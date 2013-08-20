package cz.opendata.linked.ares;

import java.util.Calendar;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

    private GridLayout mainLayout;
    private TextField numDownloads;
    private TextField hoursToCheck;
    
	public ExtractorDialog() {
		super(new ExtractorConfig());
        buildMainLayout();
        setCompositionRoot(mainLayout);        
    }  
	
    private GridLayout buildMainLayout() {
        // common part: create layout
        mainLayout = new GridLayout(1, 2);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        numDownloads = new TextField();
        numDownloads.setValue("4900");
        numDownloads.setCaption("Number of downloads from 18:00:");
        numDownloads.setWidth("100%");
        mainLayout.addComponent(numDownloads);
        
        hoursToCheck = new TextField();
        hoursToCheck.setValue("12");
        hoursToCheck.setCaption("Count files in cache downloaded in the last X hours:");
        hoursToCheck.setWidth("100%");
        mainLayout.addComponent(hoursToCheck);

        return mainLayout;
    }	
     
	/**
	 * 
	 */
	private static final long serialVersionUID = -8158163219102623590L;
	
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
	
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		try { Integer.parseInt(numDownloads.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.PerDay = Integer.parseInt(numDownloads.getValue());
		try { Integer.parseInt(hoursToCheck.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.hoursToCheck = Integer.parseInt(hoursToCheck.getValue());
		return conf;
	}
	
}
