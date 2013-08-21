package cz.opendata.linked.buyer_profiles;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = 839859194445887249L;
	private GridLayout mainLayout;
    private CheckBox chkAccessProfiles;
    private CheckBox chkRewriteCache;
    private TextField interval;
    private TextField timeout;
    
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
        
        chkRewriteCache = new CheckBox("Ignore (Rewrite) document cache");
        chkRewriteCache.setDescription("When selected, documents cache will be ignored and rewritten. Use for fresh downloads.");
        chkRewriteCache.setWidth("100%");
        
        mainLayout.addComponent(chkRewriteCache);

        chkAccessProfiles = new CheckBox("Access buyer profiles");
        chkAccessProfiles.setDescription("When selected, information from the actual buyer profiles will be processed. This includes in particular public contracts published there.");
        chkAccessProfiles.setWidth("100%");
        
        mainLayout.addComponent(chkAccessProfiles);
        
        interval = new TextField();
        //minYear.setValue("1918");
        interval.setCaption("Interval after failed download:");
        //minYear.setWidth("100%");
        /*minYear.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
            	try	{	Integer.parseInt((String)value);}
            	catch ( NumberFormatException e ) { throw new Validator.InvalidValueException("Year must be an integer, i.e. 1918" ); }
                
            	if ((Integer.parseInt((String)value) >= 1918 ) && (Integer.parseInt((String)value) <= Calendar.getInstance().get(Calendar.YEAR))) {
                    return;
                }
                throw new Validator.InvalidValueException("Start year must be between 1918 and "  + Calendar.getInstance().get(Calendar.YEAR));
            }
        });*/
        /*minYear.addTextChangeListener(new TextChangeListener() {

			@Override
			public void textChange(TextChangeEvent event) {
				minYear.validate();
			}
        	
        });*/
        mainLayout.addComponent(interval);
        
        timeout = new TextField();
        //maxYear.setValue(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        timeout.setCaption("Timeout for download:");
        //maxYear.setWidth("100%");
        /*maxYear.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws Validator.InvalidValueException {
            	try	{	Integer.parseInt((String)value);}
            	catch ( NumberFormatException e ) { throw new Validator.InvalidValueException("Year must be an integer, i.e. " + Calendar.getInstance().get(Calendar.YEAR)); }
            	
            	if ((Integer.parseInt((String)value) >= 1918 ) && (Integer.parseInt((String)value) <= Calendar.getInstance().get(Calendar.YEAR))) {
                    return;
                }
                throw new Validator.InvalidValueException("End year must be between 1918 and "  + Calendar.getInstance().get(Calendar.YEAR));
            }
        });*/
        
        mainLayout.addComponent(timeout);
        
        return mainLayout;
    }	
     
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		chkRewriteCache.setValue(conf.rewriteCache);
		chkAccessProfiles.setValue(conf.accessProfiles);
		interval.setValue(Integer.toString(conf.interval));
		timeout.setValue(Integer.toString(conf.timeout));
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.rewriteCache = chkRewriteCache.getValue();
		conf.accessProfiles = chkAccessProfiles.getValue();
		conf.interval = Integer.parseInt(interval.getValue());
		conf.timeout = Integer.parseInt(timeout.getValue());
		return conf;
	}	
}
