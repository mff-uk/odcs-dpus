package cz.cuni.mff.xrg.uv.transformer.unzipper.gzip;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class UnZipperGZipVaadinDialog extends AdvancedVaadinDialogBase<UnZipperGZipConfig_V1> {

    public UnZipperGZipVaadinDialog() {
        super(UnZipperGZipConfig_V1.class, AddonInitializer.noAddons());

    }

    @Override
    public void setConfiguration(UnZipperGZipConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public UnZipperGZipConfig_V1 getConfiguration() throws DPUConfigException {
        return new UnZipperGZipConfig_V1();
    }

}
