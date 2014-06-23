package cz.cuni.mff.xrg.intlib.rdfUtils;

import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.module.dialog.BaseConfigDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 *
 *
 *
 */
public class RDFDistillerDialog extends BaseConfigDialog<RDFaDistillerConfig> {


	public RDFDistillerDialog() {
		super(RDFaDistillerConfig.class);
	}

	@Override
	public void setConfiguration(RDFaDistillerConfig conf) throws ConfigException {

	}

	@Override
	public RDFaDistillerConfig getConfiguration() throws ConfigException {
		return new RDFaDistillerConfig();
	}

}
