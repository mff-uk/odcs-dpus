package cz.opendata.linked.cz.ruian;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	private static final long serialVersionUID = 7003725620084616056L;

	private GridLayout mainLayout;
	private CheckBox chkRewriteCache;
	private CheckBox chkPassOutput;
	private CheckBox chkGeoData;
    private TextField interval;
    private TextField timeout;
    
	public ExtractorDialog() {
		super(ExtractorConfig.class);
        buildMainLayout();
        setCompositionRoot(mainLayout);        
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

        chkRewriteCache = new CheckBox("Rewrite cache:");
        chkRewriteCache.setDescription("When selected, cache will be ignored.");
        chkRewriteCache.setWidth("100%");
        
        mainLayout.addComponent(chkRewriteCache);

        chkPassOutput = new CheckBox("Pass files to output:");
        chkPassOutput.setDescription("When selected, files will be passed to output. This may crash because some of them are 1GB large.");
        chkPassOutput.setWidth("100%");
        
        mainLayout.addComponent(chkPassOutput);

        chkGeoData = new CheckBox("Downloads include complete geodata:");
        chkGeoData.setDescription("When selected, files with complete geodata will be downloaded.");
        chkGeoData.setWidth("100%");
        
        mainLayout.addComponent(chkGeoData);

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);
        
        timeout = new TextField();
        timeout.setCaption("Timeout for download:");
        
        mainLayout.addComponent(timeout);
        
        return mainLayout;
    }	
     
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		chkPassOutput.setValue(conf.isPassToOutput());
		chkRewriteCache.setValue(conf.isRewriteCache());
		chkGeoData.setValue(conf.isInclGeoData());
		interval.setValue(Integer.toString(conf.getInterval()));
		timeout.setValue(Integer.toString(conf.getTimeout()));
	
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setRewriteCache((boolean) chkRewriteCache.getValue());
		conf.setInclGeoData((boolean) chkGeoData.getValue());
		conf.setPassToOutput((boolean) chkPassOutput.getValue());
		try {
			conf.setInterval(Integer.parseInt(interval.getValue()));
		} catch (InvalidValueException e) {
		}
		try {
			conf.setTimeout(Integer.parseInt(timeout.getValue()));
		} catch (InvalidValueException e) {
		}		
		return conf;
	}
	
}
