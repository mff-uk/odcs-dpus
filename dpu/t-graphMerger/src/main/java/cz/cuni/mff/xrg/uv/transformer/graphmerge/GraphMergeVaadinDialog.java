package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import cz.cuni.mff.xrg.uv.boost.dpu.gui.AbstractVaadinDialog;
import eu.unifiedviews.dpu.config.DPUConfigException;

/**
 * 
 * @author Škoda Petr
 */
public class GraphMergeVaadinDialog extends AbstractVaadinDialog<GraphMergeConfig_V1> {

    public GraphMergeVaadinDialog() {
        super(GraphMerge.class);
    }

    @Override
    public void setConfiguration(GraphMergeConfig_V1 c) throws DPUConfigException {

    }

    @Override
    public GraphMergeConfig_V1 getConfiguration() throws DPUConfigException {
        final GraphMergeConfig_V1 c = new GraphMergeConfig_V1();
        return c;
    }

    @Override
    protected void buildDialogLayout() {
        // No-op.
    }

}
