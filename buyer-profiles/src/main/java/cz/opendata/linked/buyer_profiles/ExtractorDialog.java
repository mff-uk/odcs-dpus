package cz.opendata.linked.buyer_profiles;

import java.util.Calendar;

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

	private static final long serialVersionUID = 839859194445887249L;

	private GridLayout mainLayout;
    private CheckBox chkAccessProfiles;
    private CheckBox chkRewriteCache;
    private CheckBox chkCurrentYearOnly;
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
        
        chkRewriteCache = new CheckBox("Ignore (Rewrite) document cache:");
        chkRewriteCache.setDescription("When selected, documents cache will be ignored and rewritten. Use for fresh downloads.");
        chkRewriteCache.setWidth("100%");
        
        mainLayout.addComponent(chkRewriteCache);

        chkAccessProfiles = new CheckBox("Access buyer profiles:");
        chkAccessProfiles.setDescription("When selected, information from the actual buyer profiles will be processed. This includes in particular public contracts published there.");
        chkAccessProfiles.setWidth("100%");
        
        mainLayout.addComponent(chkAccessProfiles);
        
        chkCurrentYearOnly = new CheckBox("Buyer profile data from current year only:");
        chkCurrentYearOnly.setDescription("When selected, data from buyer profiles will be requested only from " + Calendar.getInstance().get(Calendar.YEAR) + ":");
        chkCurrentYearOnly.setWidth("100%");
        
        mainLayout.addComponent(chkCurrentYearOnly);

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
		chkRewriteCache.setValue(conf.isRewriteCache());
		chkAccessProfiles.setValue(conf.isAccessProfiles());
		chkCurrentYearOnly.setValue(conf.isCurrentYearOnly());
		interval.setValue(Integer.toString(conf.getInterval()));
		timeout.setValue(Integer.toString(conf.getTimeout()));
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setRewriteCache(chkRewriteCache.getValue());
		conf.setAccessProfiles(chkAccessProfiles.getValue());
		conf.setCurrentYearOnly(chkCurrentYearOnly.getValue());
		conf.setInterval(Integer.parseInt(interval.getValue()));
		conf.setTimeout(Integer.parseInt(timeout.getValue()));
		return conf;
	}	
}
