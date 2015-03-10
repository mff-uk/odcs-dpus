package cz.cuni.mff.xrg.uv.extractor.sukl;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

public class SuklVaadinDialog extends AbstractDialog<SuklConfig_V1> {

    public SuklVaadinDialog() {
        super(Sukl.class);
    }

    @Override
    public void setConfiguration(SuklConfig_V1 conf) throws DPUConfigException {

    }

    @Override
    public SuklConfig_V1 getConfiguration() throws DPUConfigException {
        return new SuklConfig_V1();
    }

    @Override
    protected void buildDialogLayout() {
    }

}
