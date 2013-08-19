package cz.opendata.linked.psp_cz.metadata;

import java.util.Calendar;

import com.vaadin.data.Validator;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Slider;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {
<<<<<<< HEAD

        public ExtractorDialog() {
            super(new ExtractorConfig());
        }  
=======

    private GridLayout mainLayout;
    private Slider minYear;
    private Slider maxYear;
    
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

        minYear = new Slider();
        minYear.setMin((double)1918);
        /*minYear.setMax(Calendar.getInstance().get(Calendar.YEAR));
        minYear.setCaption("Start year:");
        minYear.setWidth("100%");
        minYear.setValue((double)1918);
        minYear.setResolution(1);*/
        /*minYear.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                if (((int) value >= 1918 ) && ((int) value <= Calendar.getInstance().get(Calendar.YEAR))) {
                    return;
                }
                throw new Validator.InvalidValueException("Start year must be between 1918 and "  + Calendar.getInstance().get(Calendar.YEAR));
            }
        });*/
        
        mainLayout.addComponent(minYear);
        
        /*maxYear = new Slider();
        maxYear.setMin(1918);
        maxYear.setMax(Calendar.getInstance().get(Calendar.YEAR));
        maxYear.setCaption("End year:");
        maxYear.setWidth("100%");
        maxYear.setValue((double)Calendar.getInstance().get(Calendar.YEAR));
        maxYear.setResolution(1);*/
        /*maxYear.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
                    if (((int) value >= 1918 ) && ((int) value <= Calendar.getInstance().get(Calendar.YEAR))) {
                        return;
                    }
                throw new Validator.InvalidValueException("End year must be between 1918 and "  + Calendar.getInstance().get(Calendar.YEAR));
            }
        });*/
        //mainLayout.addComponent(maxYear);

        return mainLayout;
    }	
>>>>>>> PSP-CZ: config resolved, trying to get a working dialog - no success yet.
     
	/**
	 * 
	 */
	private static final long serialVersionUID = -8158163219102623590L;
	
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
<<<<<<< HEAD
		config = conf;
=======
		minYear.setValue((double)conf.Start_year);
		maxYear.setValue((double)conf.End_year);
>>>>>>> PSP-CZ: config resolved, trying to get a working dialog - no success yet.
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
<<<<<<< HEAD
		return config;
=======
		ExtractorConfig conf = new ExtractorConfig();
		conf.Start_year = (int) Math.floor(minYear.getValue());
		conf.End_year = (int) Math.floor(maxYear.getValue());
		return conf;
>>>>>>> PSP-CZ: config resolved, trying to get a working dialog - no success yet.
	}
	
}
