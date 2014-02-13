package cz.opendata.linked.ehealth.ndfrt;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class ExtractorDialog extends BaseConfigDialog<ExtractorConfig> {

	private static final long serialVersionUID = -2354280414724551402L;

	public ExtractorDialog() {
		super(ExtractorConfig.class);
	}

	@Override
	public void setConfiguration(ExtractorConfig conf) throws ConfigException {
		return ;
	}

	@Override
	public ExtractorConfig getConfiguration() throws ConfigException {
		return new ExtractorConfig();
	}

}
