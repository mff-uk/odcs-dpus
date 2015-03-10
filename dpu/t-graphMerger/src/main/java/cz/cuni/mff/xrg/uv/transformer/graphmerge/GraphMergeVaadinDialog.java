package cz.cuni.mff.xrg.uv.transformer.graphmerge;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog.
 */
public class GraphMergeVaadinDialog extends AbstractDialog<GraphMergeConfig_V1> {

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
    }

}
