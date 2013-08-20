package cz.opendata.linked.buyer_profiles;

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
	private static final long serialVersionUID = 1L;
	
	public ExtractorDialog() {
		super(new ExtractorConfig());
        //buildMainLayout();
        //setCompositionRoot(mainLayout);        
    }  
	
	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
	
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		ExtractorConfig conf = new ExtractorConfig();
		return conf;
	}
	
}
