package cz.cuni.mff.xrg.uv.boost.dpu.advanced;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.config.MasterConfigObject;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.MasterConfigurationDialog;
import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.config.AbstractConfigDialog;
import eu.unifiedviews.helpers.dpu.config.ConfigDialogProvider;
import java.util.List;

/**
 *
 * @author Škoda Petr
 * @param <CONFIG>
 */
public abstract class ConfigurableBase<CONFIG> extends NonConfigurableBase
        implements ConfigDialogProvider<MasterConfigObject> {

    /**
     * DPU's configuration.
     */
    protected CONFIG config;

    /**
     * DPU's configuration class.
     */
    private Class<CONFIG> configClazz;

    public ConfigurableBase(Class<CONFIG> configClass,
            List<AddonInitializer.AddonInfo> addons) {
        super(addons);
    }

    @Override
    public void configure(String config) throws DPUConfigException {
        super.configure(config);
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultConfiguration() throws DPUConfigException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractConfigDialog<MasterConfigObject> getConfigurationDialog() {
        return new MasterConfigurationDialog();
    }

}
