package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import cz.cuni.mff.xrg.uv.boost.dpu.addon.AddonInitializer;
import cz.cuni.mff.xrg.uv.boost.dpu.gui.AdvancedVaadinDialogBase;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * DPU's configuration dialog.
 */
public class GraphMergeVaadinDialog extends AdvancedVaadinDialogBase<GraphMergeConfig_V1> {

    public GraphMergeVaadinDialog() {
        super(GraphMergeConfig_V1.class, AddonInitializer.noAddons());

        buildLayout();
    }

    @Override
    public void setConfiguration(GraphMergeConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public GraphMergeConfig_V1 getConfiguration() throws DPUConfigException {
        final GraphMergeConfig_V1 c = new GraphMergeConfig_V1();

        return c;
    }

    private void buildLayout() {
		
    }
}
