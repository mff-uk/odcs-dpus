package cz.opendata.linked.psp_cz.metadata;

import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.module.dialog.BaseConfigDialog;
import cz.cuni.xrg.intlib.commons.web.AbstractConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU configuration.
 *
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

        public ExtractorDialog() {
            super(new ExtractorConfig());
        }  
     
	/**
	 * 
	 */
	private static final long serialVersionUID = -8158163219102623590L;
	
	private ExtractorConfig config;

	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		config = conf;
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		return config;
	}
	
}
