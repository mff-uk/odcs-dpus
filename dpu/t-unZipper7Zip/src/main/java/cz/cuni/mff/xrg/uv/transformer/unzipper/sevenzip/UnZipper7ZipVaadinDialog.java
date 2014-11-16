package cz.cuni.mff.xrg.uv.transformer.unzipper.sevenzip;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

public class UnZipper7ZipVaadinDialog extends AdvancedVaadinDialogBase<UnZipper7ZipConfig_V1> {

    public UnZipper7ZipVaadinDialog() {
         super(UnZipper7ZipConfig_V1.class, AddonInitializer.noAddons());
    }

    @Override
    protected void setConfiguration(UnZipper7ZipConfig_V1 conf) throws DPUConfigException {
        
    }

    @Override
    protected UnZipper7ZipConfig_V1 getConfiguration() throws DPUConfigException {
        return new UnZipper7ZipConfig_V1();
    }

}
