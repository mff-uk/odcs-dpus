package cz.cuni.mff.xrg.intlib.rdfUtils;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 *
 *
 *
 */
public class RDFDistillerDialog extends AdvancedVaadinDialogBase<RDFaDistillerConfig> {


	public RDFDistillerDialog() {
		super(RDFaDistillerConfig.class, AddonInitializer.noAddons());
	}

	@Override
	public void setConfiguration(RDFaDistillerConfig conf) throws DPUConfigException {

	}

	@Override
	public RDFaDistillerConfig getConfiguration() throws DPUConfigException {
		return new RDFaDistillerConfig();
	}

}
