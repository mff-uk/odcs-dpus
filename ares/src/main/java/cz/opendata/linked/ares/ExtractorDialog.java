package cz.opendata.linked.ares;

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

    /**
	 * 
	 */
	private static final long serialVersionUID = 7003725620084616056L;
	private GridLayout mainLayout;
	private CheckBox chkUseCacheOnly;
	private CheckBox chkStdAdr;
	private CheckBox chkActive;
	private CheckBox chkGenerateOutput;
	private CheckBox chkPuvAdr;
    private TextField numDownloads;
    private TextField hoursToCheck;
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
        mainLayout.setHeight("100%");
        mainLayout.setMargin(false);
        //mainLayout.setSpacing(true);

        // top-level component properties
        setWidth("100%");
        setHeight("100%");

        chkGenerateOutput = new CheckBox("Generate output:");
        chkGenerateOutput.setDescription("When selected, the cached or downloaded files will be passed as RDF literals to output.");
        chkGenerateOutput.setWidth("100%");
        
        mainLayout.addComponent(chkGenerateOutput);

        chkUseCacheOnly = new CheckBox("Use only cache:");
        chkUseCacheOnly.setDescription("When selected, DPU only goes through cache, does not download and ignores download limits.");
        chkUseCacheOnly.setWidth("100%");
        
        mainLayout.addComponent(chkUseCacheOnly);
        
        chkStdAdr = new CheckBox("Include OR stdadr:");
        chkStdAdr.setDescription("When selected, downloads will include standardized address.");
        chkStdAdr.setWidth("100%");
        
        mainLayout.addComponent(chkStdAdr);

        chkActive = new CheckBox("BASIC only active:");
        chkActive.setDescription("When selected, downloads from BASIC will include data only for currently registered ICs (not cancelled).");
        chkActive.setWidth("100%");
        
        mainLayout.addComponent(chkActive);

        chkPuvAdr = new CheckBox("BASIC PuvAdr:");
        chkPuvAdr.setDescription("When selected, downloads from BASIC will also include address without modifications.");
        chkPuvAdr.setWidth("100%");
        
        mainLayout.addComponent(chkPuvAdr);

        numDownloads = new TextField();
        numDownloads.setCaption("Number of downloads in the last X hours:");
        mainLayout.addComponent(numDownloads);
        
        hoursToCheck = new TextField();
        hoursToCheck.setCaption("Count files in cache downloaded in the last X hours:");
        mainLayout.addComponent(hoursToCheck);

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
		chkUseCacheOnly.setValue(conf.useCacheOnly);
		chkGenerateOutput.setValue(conf.generateOutput);
		chkPuvAdr.setValue(conf.bas_puvadr);
		chkActive.setValue(conf.bas_active);
		chkStdAdr.setValue(conf.or_stdadr);
		interval.setValue(Integer.toString(conf.interval));
		timeout.setValue(Integer.toString(conf.timeout));
		numDownloads.setValue(Integer.toString(conf.PerDay));
		hoursToCheck.setValue(Integer.toString(conf.hoursToCheck));
	
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.bas_puvadr = chkPuvAdr.getValue();
		conf.or_stdadr = chkStdAdr.getValue();
		conf.bas_active = chkActive.getValue();
		conf.useCacheOnly = chkUseCacheOnly.getValue();
		conf.generateOutput = chkGenerateOutput.getValue();
		try { Integer.parseInt(numDownloads.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.PerDay = Integer.parseInt(numDownloads.getValue());
		try { Integer.parseInt(hoursToCheck.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.hoursToCheck = Integer.parseInt(hoursToCheck.getValue());
		try { Integer.parseInt(interval.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.interval = Integer.parseInt(interval.getValue());
		try { Integer.parseInt(timeout.getValue()); } catch (InvalidValueException e) { return conf;}
		conf.timeout = Integer.parseInt(timeout.getValue());
		return conf;
	}
	
}
