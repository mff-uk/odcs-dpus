package cz.opendata.linked.psp_cz.metadata;

import java.util.Calendar;

import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

    private GridLayout mainLayout;
    private TextField minYear;
    private TextField maxYear;
    private CheckBox chkCachedLists;
    private CheckBox chkRewriteCache;
    private TextField interval;
    private TextField timeout;
    
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

        minYear = new TextField();
        //minYear.setValue("1918");
        minYear.setCaption("Start year:");
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
        mainLayout.addComponent(minYear);
        
        maxYear = new TextField();
        //maxYear.setValue(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        maxYear.setCaption("End year:");
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
        
        mainLayout.addComponent(maxYear);
        
        chkRewriteCache = new CheckBox("Ignore (Rewrite) document cache:");
        chkRewriteCache.setDescription("When selected, documents cache will be ignored and rewritten. This will refresh in particular passive derogations of documents.");
        chkRewriteCache.setWidth("100%");
        
        mainLayout.addComponent(chkRewriteCache);

        chkCachedLists = new CheckBox("Use cached lists:");
        chkCachedLists.setDescription("When selected, lists of documents in each year will be used from cache. This impacts in particular the actual year where the list will not be refreshed. On the other hand, if RewriteCache is not enabled and cache is complete, this extractor will not access the source.");
        chkCachedLists.setWidth("100%");
        
        mainLayout.addComponent(chkCachedLists);

        interval = new TextField();
        interval.setCaption("Interval between downloads:");
        mainLayout.addComponent(interval);
        
        timeout = new TextField();
        timeout.setCaption("Timeout for download:");
        mainLayout.addComponent(timeout);
        
        return mainLayout;
    }	
     
	/**
	 * 
	 */
	private static final long serialVersionUID = -8158163219102623590L;
	
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		minYear.setValue(Integer.toString(conf.getStart_year()));
		maxYear.setValue(Integer.toString(conf.getEnd_year()));
		chkRewriteCache.setValue(conf.isRewriteCache());
		chkCachedLists.setValue(conf.isCachedLists());
		interval.setValue(Integer.toString(conf.getInterval()));
		timeout.setValue(Integer.toString(conf.getTimeout()));
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		conf.setStart_year(Integer.parseInt(minYear.getValue()));
		conf.setEnd_year(Integer.parseInt(maxYear.getValue()));
		conf.setRewriteCache((boolean) chkRewriteCache.getValue());
		conf.setCachedLists((boolean) chkCachedLists.getValue());
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
