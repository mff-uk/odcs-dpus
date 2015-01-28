package cz.cuni.mff.xrg.uv.boost.dpu.config.transformer;

import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigException;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigManager;
import cz.cuni.mff.xrg.uv.boost.dpu.config.ConfigTransformer;

/**
 * Update configuration after add-on migration between versions 1.?.? and 2.0.0.
 * @author Škoda Petr
 */
public class AddonMigration implements ConfigTransformer {

    @Override
    public void configure(ConfigManager configManager) throws ConfigException {
        // No-op.
    }

    @Override
    public String transformString(String configName, String config) throws ConfigException {
        // Some fix transformations.
        return config
                .replaceAll("cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.FaultToleranceWrap_-Configuration__V1",
                        "cz.cuni.mff.xrg.uv.boost.extensions.FaultTolerance_-Configuration__V1")
                .replaceAll("cz.cuni.mff.xrg.uv.boost.dpu.addon.impl.CachedFileDownloader_-Configuration",
                        "cz.cuni.mff.xrg.uv.boost.extensions.CachedFileDownloader_-Configuration__V1");
    }

    @Override
    public <TYPE> void transformObject(String configName, TYPE config) throws ConfigException {
        // No-op.
    }

}
